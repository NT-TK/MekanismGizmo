package jp.example.mekanismgizmo.block;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes.Fuel;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Improved gas generator (mekanismgizmo:improved_gas_generator), the 1.20.1 gas-typed edition.
 */
public class ImprovedGasGeneratorBlockEntity extends GizmoMachineBlockEntity {
    /** Hundred times the 18,000 mB tank of Mekanism's gas generator. */
    public static final long GAS_CAPACITY = 1_800_000L;
    /** Hundred times the 200,000 FE buffer of Mekanism's gas generator. */
    public static final long ENERGY_CAPACITY = 20_000_000L;
    public static final int BURN_TICKS_PER_TICK = 2_560;

    public static final int GAUGE_X = 6;
    public static final int GAUGE_Y = 13;
    public static final int FUEL_SLOT_X = 26;
    public static final int FUEL_SLOT_Y = 36;
    public static final int ENERGY_SLOT_X = 143;
    public static final int ENERGY_SLOT_Y = 15;
    public static final int POWER_BAR_X = 164;
    public static final int POWER_BAR_Y = 16;
    public static final int INFO_Y = 58;

    private IGasTank gasTank;
    private BasicEnergyContainer energyContainer;
    private GasInventorySlot fuelSlot;
    private EnergyInventorySlot energySlot;

    private long burnTicksRemaining;
    @Nullable
    private Gas lastFuelType;
    private int burnRateHundredths;
    private int energyPerMb;
    private int generationRate;

    public ImprovedGasGeneratorBlockEntity(IBlockProvider block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        configComponent.setupItemIOConfig(List.of(fuelSlot), List.of(), energySlot, false);
        configComponent.setupOutputConfig(TransmissionType.ENERGY, energyContainer);
        configComponent.setupInputConfig(TransmissionType.GAS, gasTank);
        ejectorComponent = new TileComponentEjector(this);
        applySideDefaults();
    }

    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        gasTank = ChemicalTankBuilder.GAS.input(GAS_CAPACITY, gas -> gas.has(Fuel.class), listener);
        builder.addTank(gasTank);
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = BasicEnergyContainer.output(FloatingLong.create(ENERGY_CAPACITY), listener));
        return builder.build();
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(fuelSlot = GasInventorySlot.fill(gasTank, listener, FUEL_SLOT_X, FUEL_SLOT_Y));
        builder.addSlot(energySlot = EnergyInventorySlot.drain(energyContainer, listener, ENERGY_SLOT_X, ENERGY_SLOT_Y));
        return builder.build();
    }

    public static boolean isFuel(GasStack stack) {
        return fuelValue(stack) > 0;
    }

    @Nullable
    public static Fuel fuelData(GasStack stack) {
        return stack.isEmpty() ? null : stack.getType().get(Fuel.class);
    }

    public static long fuelValue(GasStack stack) {
        Fuel fuel = fuelData(stack);
        return fuel == null ? 0 : fuel.getEnergyPerTick().longValue();
    }

    public double getFillLevel() {
        if (gasTank == null || gasTank.getCapacity() <= 0) {
            return 0;
        }
        return Math.min(1D, (double) gasTank.getStored() / gasTank.getCapacity());
    }

    public double getSpeedMultiplier() {
        return supportsUpgrades() ? 1_000D / MekanismUtils.getTicks(this, 1_000) : 1;
    }

    public double getBurnTickLimit() {
        return BURN_TICKS_PER_TICK * getSpeedMultiplier();
    }

    @Nullable
    private Fuel activeFuelData() {
        Fuel fuel = fuelData(gasTank.getStack());
        if (fuel == null && gasTank.isEmpty() && burnTicksRemaining > 0 && hasLastFuel()) {
            return lastFuelType.get(Fuel.class);
        }
        return fuel;
    }

    private boolean hasLastFuel() {
        return lastFuelType != null && !lastFuelType.isEmptyType();
    }

    @Nullable
    private static Gas fuelTypeOf(GasStack stack) {
        return stack.isEmpty() || stack.getType().isEmptyType() ? null : stack.getType();
    }

    public double getMaxBurnRate() {
        Fuel fuel = activeFuelData();
        return fuel == null ? 0 : getBurnTickLimit() / Math.max(1, fuel.getBurnTicks());
    }

    public long getEnergyPerMb() {
        Fuel fuel = activeFuelData();
        if (fuel == null) {
            return 0;
        }
        long energyPerTick = Math.max(1, fuel.getEnergyPerTick().longValue());
        long burnTicks = Math.max(1, fuel.getBurnTicks());
        return energyPerTick > Long.MAX_VALUE / burnTicks ? Long.MAX_VALUE : energyPerTick * burnTicks;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        fuelSlot.fillTank();
        energySlot.drainContainer();

        GasStack stored = gasTank.getStack();
        Fuel fuel = fuelData(stored);
        if (!stored.isEmpty()) {
            if (fuel == null) {
                burnTicksRemaining = 0;
                lastFuelType = null;
            } else if (fuelTypeOf(stored) != lastFuelType) {
                burnTicksRemaining = 0;
                lastFuelType = fuelTypeOf(stored);
            }
        } else if (burnTicksRemaining <= 0) {
            lastFuelType = null;
        }
        if (fuel == null && stored.isEmpty() && burnTicksRemaining > 0 && hasLastFuel()) {
            fuel = lastFuelType.get(Fuel.class);
            if (fuel == null) {
                burnTicksRemaining = 0;
                lastFuelType = null;
            }
        }
        long produced = 0;
        double used = 0;
        if (MekanismUtils.canFunction(this) && fuel != null && (!stored.isEmpty() || burnTicksRemaining > 0)) {
            int maxBurnTicks = Math.max(1, fuel.getBurnTicks());
            long energyPerTick = Math.max(1, fuel.getEnergyPerTick().longValue());
            FloatingLong needed = energyContainer.getNeeded();
            if (!needed.isZero()) {
                double fill = stored.isEmpty() ? 1D : getFillLevel();
                long toUse = (long) Math.ceil(getBurnTickLimit() * fill);
                // Widen before multiplying: a full tank times a long burn time can exceed the int range
                long available = burnTicksRemaining + (long) gasTank.getStored() * maxBurnTicks;
                toUse = Math.min(available, toUse);
                toUse = Math.min(toUse, MathUtils.clampToLong(needed.divide(energyPerTick).doubleValue()));
                if (toUse > 0) {
                    long wanted = MathUtils.clampToLong((double) energyPerTick * toUse);
                    FloatingLong remainder = energyContainer.insert(FloatingLong.create(wanted), Action.EXECUTE, AutomationType.INTERNAL);
                    produced = wanted - remainder.longValue();
                    long burnAndFuel = available - toUse;
                    if (burnAndFuel > 0) {
                        gasTank.setStack(new GasStack(gasTank.getStack(), burnAndFuel / maxBurnTicks));
                    } else {
                        gasTank.setEmpty();
                    }
                    burnTicksRemaining = burnAndFuel % maxBurnTicks;
                    used = (double) toUse / maxBurnTicks;
                }
            }
        }
        generationRate = (int) Math.min(Integer.MAX_VALUE, produced);
        burnRateHundredths = (int) Math.round(used * 100);
        energyPerMb = (int) Math.min(Integer.MAX_VALUE, getEnergyPerMb());
        setActive(produced > 0);
        if (MekanismUtils.canFunction(this)) {
            emitEnergy(energyContainer, FloatingLong.create(getMaxOutput()));
        }
    }

    private long getMaxOutput() {
        Fuel fuel = activeFuelData();
        long energyPerTick = fuel == null ? 200L : Math.max(1L, fuel.getEnergyPerTick().longValue());
        return MathUtils.clampToLong(2D * getBurnTickLimit() * energyPerTick);
    }

    public IGasTank getGasTank() {
        return gasTank;
    }

    public BasicEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public int getBurnRateHundredths() {
        return burnRateHundredths;
    }

    public static String formatBurnRate(int hundredths) {
        double rate = hundredths / 100D;
        return String.format(java.util.Locale.ROOT, rate >= 10 ? "%.1f" : "%.2f", rate);
    }

    public int getSyncedEnergyPerMb() {
        return energyPerMb;
    }

    public int getGenerationRate() {
        return generationRate;
    }

    public int getSpeedMultiplierHundredths() {
        return (int) Math.round(getSpeedMultiplier() * 100);
    }

    public Component getStoredGasName() {
        if (!gasTank.isEmpty()) {
            return gasTank.getStack().getTextComponent();
        }
        if (burnTicksRemaining > 0 && hasLastFuel()) {
            return new GasStack(lastFuelType, 1).getTextComponent();
        }
        return Component.translatable("gui.mekanismgizmo.improved_gas_generator.no_fuel");
    }

    @Override
    public List<Component> getInfo(Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("BurnTicks", burnTicksRemaining);
        if (hasLastFuel()) {
            tag.put("LastFuel", new GasStack(lastFuelType, 1).write(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        burnTicksRemaining = Math.max(0, tag.getLong("BurnTicks"));
        if (tag.contains("LastFuel", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            GasStack saved = GasStack.readFromNBT(tag.getCompound("LastFuel"));
            lastFuelType = saved.isEmpty() ? null : saved.getType();
        } else {
            lastFuelType = fuelTypeOf(gasTank.getStack());
        }
        if (lastFuelType == null && burnTicksRemaining > 0 && !gasTank.isEmpty()) {
            lastFuelType = fuelTypeOf(gasTank.getStack());
        }
        if (gasTank.isEmpty() && burnTicksRemaining > 0 && lastFuelType == null) {
            burnTicksRemaining = 0;
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(this::getGenerationRate, value -> generationRate = value));
        container.track(SyncableInt.create(this::getBurnRateHundredths, value -> burnRateHundredths = value));
        container.track(SyncableInt.create(this::getSyncedEnergyPerMb, value -> energyPerMb = value));
    }
}
