package jp.example.mekanismgizmo.block;

import java.util.List;
import jp.example.mekanismgizmo.registry.ModSideConfigs;
import jp.example.mekanismgizmo.util.MachineSideDefaults;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.math.MathUtils;
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
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Improved gas generator (mekanismgizmo:improved_gas_generator).
 * <p>
 * A gas burning generator with a hundred times the fuel tank and a hundred times the internal energy buffer of
 * Mekanism's gas generator ({@link #GAS_CAPACITY} mB and {@link #ENERGY_CAPACITY} FE instead of 18,000 mB and
 * 200,000 FE).
 * <p>
 * The burning is <b>Mekanism's own formula</b> - {@code TileEntityGasGenerator#getToUse()} - only with a higher ceiling:
 * <pre>
 * burnTicksPerTick = ceil(BURN_TICKS_PER_TICK * fill)                    // Mekanism uses 256, so a full tank burns fast
 * toUse            = min(fuel inside, burnTicksPerTick)                   // limited by what is in the tank
 * toUse            = min(toUse, bufferRoom / energyPerTick)               // limited by the energy buffer
 * energy           = energyPerTick * toUse                                // one "burn tick" is one tick of generation
 * burnRate         = toUse / burnTicks                                    // the mB/t the screen shows
 * </pre>
 * Because a mB of fuel is worth {@code burnTicks} burn ticks of {@code energyPerTick}, the energy per mB is always
 * Mekanism's own value (hydrogen 200 FE/mB, ethene 28,200 FE/mB), and because the burn limit scales with the fill level
 * the machine burns the more fuel the fuller it is - exactly like a gas generator, and exactly like the reactor of
 * Compact Mekanism Machines.
 * <p>
 * Mekanism's ceiling of 256 burn ticks per tick gives 6.4 mB/t for ethene and 256 mB/t for hydrogen;
 * {@link #BURN_TICKS_PER_TICK} raises it tenfold, so the machine tops out at 64 mB/t for ethene - a full ethene tank
 * burns 64 mB/t = 1,804,800 FE/t and a full hydrogen tank 2,560 mB/t = 512,000 FE/t.
 */
public class ImprovedGasGeneratorBlockEntity extends GizmoMachineBlockEntity {
    /** Hundred times the 18,000 mB tank of Mekanism's gas generator. */
    public static final long GAS_CAPACITY = 1_800_000L;
    /** Hundred times the 200,000 FE buffer of Mekanism's gas generator. */
    public static final long ENERGY_CAPACITY = 20_000_000L;
    /**
     * Burn ticks of fuel the machine may use per tick at a full tank. Mekanism's gas generator uses 256, which is
     * 256 mB/t for hydrogen (1 tick per mB) and 6.4 mB/t for ethene (40 ticks per mB); 2,560 is ten times that, so the
     * machine burns 2,560 mB/t of hydrogen and a maximum of 64 mB/t of ethene at a full tank.
     */
    public static final int BURN_TICKS_PER_TICK = 2_560;

    // --- screen layout ---
    public static final int GAUGE_X = 6;
    public static final int GAUGE_Y = 13;
    public static final int FUEL_SLOT_X = 26;
    public static final int FUEL_SLOT_Y = 36;
    public static final int ENERGY_SLOT_X = 143;
    public static final int ENERGY_SLOT_Y = 15;
    public static final int POWER_BAR_X = 164;
    public static final int POWER_BAR_Y = 16;
    public static final int INFO_Y = 58;

    private IChemicalTank gasTank;
    private BasicEnergyContainer energyContainer;
    private ChemicalInventorySlot fuelSlot;
    private EnergyInventorySlot energySlot;

    /** Fuel already "started" but not burned off yet, in burn ticks (Mekanism keeps the same remainder). */
    private long burnTicksRemaining;
    /** Chemical the remainder belongs to; when the tank holds a different fuel the remainder is dropped. */
    @Nullable
    private mekanism.api.chemical.Chemical lastFuelType;
    /** Burn rate in hundredths of a mB per tick, synced to the screen. */
    private int burnRateHundredths;
    /** Mekanism's value of the stored gas in FE per mB, synced to the screen. */
    private int energyPerMb;
    /** Energy produced in the last tick, synced to the screen. */
    private int generationRate;

    public ImprovedGasGeneratorBlockEntity(Holder<Block> block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        configComponent.setupItemIOConfig(List.of(fuelSlot), List.of(), energySlot, false);
        // The machine is a generator: its energy leaves through every side that is configured as "Energy"
        configComponent.setupOutputConfig(TransmissionType.ENERGY, energyContainer);
        configComponent.setupInputConfig(TransmissionType.CHEMICAL, gasTank);
        ejectorComponent = new TileComponentEjector(this);
        MachineSideDefaults.applyIfUnconfigured(configComponent, ModSideConfigs.IMPROVED_GAS_GENERATOR);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        MachineSideDefaults.applyIfUnconfigured(getConfig(), ModSideConfigs.IMPROVED_GAS_GENERATOR);
    }

    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        gasTank = BasicChemicalTank.inputModern(GAS_CAPACITY, ImprovedGasGeneratorBlockEntity::isFuel, listener);
        builder.addTank(gasTank);
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = BasicEnergyContainer.output(ENERGY_CAPACITY, listener));
        return builder.build();
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        builder.addSlot(fuelSlot = ChemicalInventorySlot.fill(gasTank, listener, FUEL_SLOT_X, FUEL_SLOT_Y));
        builder.addSlot(energySlot = EnergyInventorySlot.drain(energyContainer, listener, ENERGY_SLOT_X, ENERGY_SLOT_Y));
        return builder.build();
    }

    /** True when Mekanism would burn this chemical in its own gas generator. */
    public static boolean isFuel(ChemicalStack stack) {
        return fuelValue(stack) > 0;
    }

    /**
     * The fuel data Mekanism's own gas generator uses for this chemical, or null when it is not a fuel. The value comes
     * from Mekanism's {@code chemicalFuel} data map, with the legacy chemical attribute as the fallback - the same two
     * places Mekanism's gas generator reads.
     */
    public static ChemicalFuel fuelData(ChemicalStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ChemicalFuel fuel = stack.getData(IMekanismDataMapTypes.INSTANCE.chemicalFuel());
        if (fuel != null) {
            return fuel;
        }
        ChemicalAttributes.Fuel legacy = stack.getLegacy(ChemicalAttributes.Fuel.class);
        return legacy == null ? null : legacy.asModern();
    }

    /**
     * Energy Mekanism's gas generator produces from one mB of this chemical (200 for hydrogen, 28,200 for ethene), or 0
     * when it is not a fuel.
     */
    public static long fuelValue(ChemicalStack stack) {
        ChemicalFuel fuel = fuelData(stack);
        return fuel == null ? 0 : fuel.energyDensity();
    }

    /** How full the gas tank is, 0 when empty and 1 when full. */
    public double getFillLevel() {
        if (gasTank == null || gasTank.getCapacity() <= 0) {
            return 0;
        }
        return Math.min(1D, (double) gasTank.getStored() / gasTank.getCapacity());
    }

    /**
     * How much the SPEED upgrades multiply the burn rate, using Mekanism's own speed formula
     * ({@code maxUpgradeMultiplier} to the power of the installed fraction of the speed upgrades), so eight speed
     * upgrades make the machine ten times as fast with the default configuration.
     */
    public double getSpeedMultiplier() {
        if (!supportsUpgrades()) {
            return 1;
        }
        return 1_000D / MekanismUtils.getTicksD(this, 1_000);
    }

    /** Burn ticks this machine may use per tick at a full tank, including the speed upgrades. */
    public double getBurnTickLimit() {
        return BURN_TICKS_PER_TICK * getSpeedMultiplier();
    }

    /**
     * Fuel data for the gas that is currently being burned. When the tank holds gas this is that gas; when the last
     * mB was already moved into {@link #burnTicksRemaining} the tank is empty, but the remainder still belongs to the
     * fuel recorded in {@link #lastFuelType} and must keep burning with that fuel's values.
     */
    @Nullable
    private ChemicalFuel activeFuelData() {
        ChemicalFuel fuel = fuelData(gasTank.getStack());
        if (fuel == null && gasTank.isEmpty() && burnTicksRemaining > 0 && hasLastFuel()) {
            return fuelData(new ChemicalStack(lastFuelType, 1));
        }
        return fuel;
    }

    /** True when a real fuel type (not the empty placeholder) is recorded for the remaining burn ticks. */
    private boolean hasLastFuel() {
        return lastFuelType != null && !lastFuelType.isEmptyType();
    }

    @Nullable
    private static mekanism.api.chemical.Chemical fuelTypeOf(ChemicalStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        mekanism.api.chemical.Chemical chemical = stack.getChemical();
        return chemical.isEmptyType() ? null : chemical;
    }

    /** Highest burn rate this machine reaches with a full tank of the stored gas, in mB per tick. */
    public double getMaxBurnRate() {
        ChemicalFuel fuel = activeFuelData();
        if (fuel == null) {
            return 0;
        }
        return getBurnTickLimit() / Math.max(1, fuel.burnTicks());
    }

    /** Energy per mB of the stored gas - Mekanism's own value. */
    public long getEnergyPerMb() {
        ChemicalFuel fuel = activeFuelData();
        return fuel == null ? 0 : fuel.energyDensity();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        fuelSlot.fillTank();
        energySlot.drainContainer();

        ChemicalStack stored = gasTank.getStack();
        ChemicalFuel fuel = fuelData(stored);
        if (!stored.isEmpty()) {
            if (fuel == null) {
                // A non-fuel chemical (for example from a corrupted save) must not inherit the old remainder
                burnTicksRemaining = 0;
                lastFuelType = null;
            } else if (fuelTypeOf(stored) != lastFuelType) {
                // A different fuel was put in: the partly burned mB of the old one must not be applied to the new one
                burnTicksRemaining = 0;
                lastFuelType = fuelTypeOf(stored);
            }
        } else if (burnTicksRemaining <= 0) {
            lastFuelType = null;
        }
        if (fuel == null && stored.isEmpty() && burnTicksRemaining > 0 && hasLastFuel()) {
            // The tank is empty because its last mB only partly fitted into the energy buffer. Keep burning that mB
            // from the recorded fuel type instead of stranding the remaining burn ticks.
            fuel = fuelData(new ChemicalStack(lastFuelType, 1));
            if (fuel == null) {
                burnTicksRemaining = 0;
                lastFuelType = null;
            }
        }
        long produced = 0;
        double used = 0;
        if (canFunction() && fuel != null && (!stored.isEmpty() || burnTicksRemaining > 0)) {
            int maxBurnTicks = Math.max(1, fuel.burnTicks());
            long energyPerTick = Math.max(1, fuel.energyPerTick());
            long needed = energyContainer.getNeeded();
            if (needed > 0) {
                // Mekanism's own calculation: the fuel limit grows with the fill level of the tank. With an empty tank
                // the remainder is the only fuel left, so let it use the full limit rather than zero.
                double fill = stored.isEmpty() ? 1D : getFillLevel();
                long toUse = (long) Math.ceil(getBurnTickLimit() * fill);
                long available = burnTicksRemaining + gasTank.getStored() * maxBurnTicks;
                toUse = Math.min(available, toUse);
                // Only burn what still fits into the buffer
                toUse = Math.min(toUse, MathUtils.clampToLong(needed / (double) energyPerTick));
                if (toUse > 0) {
                    // Mekanism's insert returns what did not fit, so the production is the difference
                    long wanted = MathUtils.multiplyClamped(energyPerTick, toUse);
                    produced = wanted - energyContainer.insert(wanted, Action.EXECUTE, AutomationType.INTERNAL);
                    long burnAndFuel = available - toUse;
                    if (burnAndFuel > 0) {
                        gasTank.setStack(gasTank.getStack().copyWithAmount(burnAndFuel / maxBurnTicks));
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
        if (canFunction()) {
            emitEnergy(energyContainer, getMaxOutput());
        }
        return sendUpdatePacket;
    }

    /** Upper bound for one tick of output: twice the maximum burn rate, like Mekanism's generators. */
    private long getMaxOutput() {
        ChemicalFuel fuel = activeFuelData();
        long energyPerTick = fuel == null ? 200L : Math.max(1L, fuel.energyPerTick());
        return MathUtils.multiplyClamped(2L, MathUtils.clampToLong(getBurnTickLimit() * (double) energyPerTick));
    }

    public IChemicalTank getGasTank() {
        return gasTank;
    }

    public BasicEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    /** Burn rate in hundredths of a mB per tick, as shown on the screen. */
    public int getBurnRateHundredths() {
        return burnRateHundredths;
    }

    /** Burn rate formatted the way Mekanism formats it: one decimal from 10 mB/t, two below that. */
    public static String formatBurnRate(int hundredths) {
        double rate = hundredths / 100D;
        return String.format(java.util.Locale.ROOT, rate >= 10 ? "%.1f" : "%.2f", rate);
    }

    /** Energy per mB of the stored gas, as shown on the screen. */
    public int getSyncedEnergyPerMb() {
        return energyPerMb;
    }

    /** Energy produced in the last tick, as shown on the screen. */
    public int getGenerationRate() {
        return generationRate;
    }

    /** Speed multiplier in hundredths (100 = no speed upgrades). */
    public int getSpeedMultiplierHundredths() {
        return (int) Math.round(getSpeedMultiplier() * 100);
    }

    /** Name of the gas in the tank, as shown on the screen. */
    public Component getStoredGasName() {
        if (!gasTank.isEmpty()) {
            return gasTank.getStack().getTextComponent();
        }
        if (burnTicksRemaining > 0 && hasLastFuel()) {
            return new ChemicalStack(lastFuelType, 1).getTextComponent();
        }
        return Component.translatable("gui.mekanismgizmo.improved_gas_generator.no_fuel");
    }

    @Override
    public List<Component> getInfo(Upgrade upgrade) {
        // The burn rate and the output are always drawn by the screen; this tab must only show the upgrade's effect
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("BurnTicks", burnTicksRemaining);
        if (hasLastFuel()) {
            // The type is needed even when the tank is empty and only a partial mB is left
            tag.put("LastFuel", new ChemicalStack(lastFuelType, 1).saveOptional(registries));
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        burnTicksRemaining = Math.max(0, tag.getLong("BurnTicks"));
        Tag savedFuel = tag.get("LastFuel");
        if (savedFuel != null) {
            lastFuelType = ChemicalStack.parse(registries, savedFuel)
                  .filter(stack -> !stack.isEmpty())
                  .map(ImprovedGasGeneratorBlockEntity::fuelTypeOf)
                  .orElse(null);
        } else {
            // Older saves did not record the type: use the tank if it still has gas. Do not treat that fuel as a
            // fuel change on the first tick, as that would discard the saved partial burn.
            lastFuelType = fuelTypeOf(gasTank.getStack());
        }
        if (lastFuelType == null && burnTicksRemaining > 0 && !gasTank.isEmpty()) {
            lastFuelType = fuelTypeOf(gasTank.getStack());
        }
        if (gasTank.isEmpty() && burnTicksRemaining > 0 && lastFuelType == null) {
            // No way to know which fuel the old remainder belonged to; dropping it is safer than applying it to
            // whatever is inserted next.
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
