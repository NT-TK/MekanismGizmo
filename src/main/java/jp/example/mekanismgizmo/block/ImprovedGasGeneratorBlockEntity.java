package jp.example.mekanismgizmo.block;

import java.util.List;
import jp.example.mekanismgizmo.registry.ModSideConfigs;
import jp.example.mekanismgizmo.util.AmountFormat;
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
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Improved gas generator (mekanismgizmo:improved_gas_generator).
 * <p>
 * A gas burning generator with a hundred times the fuel tank and a hundred times the internal energy buffer of
 * Mekanism's gas generator ({@link #GAS_CAPACITY} mB and {@link #ENERGY_CAPACITY} FE instead of 18,000 mB and
 * 200,000 FE), and therefore built to be worth {@link #GENERATOR_PARALLEL} of them at once.
 * <p>
 * Everything the machine does is expressed with Mekanism's own numbers, so the screen can show the same values as a gas
 * generator's screen:
 * <ul>
 *     <li>the energy per mB is exactly {@code ChemicalFuel#energyDensity()} (hydrogen 200 FE/mB, ethene 28,200 FE/mB),
 *     the value Mekanism's own gas generator burns the fuel at;</li>
 *     <li>the maximum burn rate is {@link #GENERATOR_PARALLEL} times Mekanism's burn rate for that fuel, which burns one
 *     mB every {@code ChemicalFuel#burnTicks()} ticks: 100 mB/t for hydrogen (1 tick per mB) and 2.5 mB/t for ethene
 *     (40 ticks per mB), i.e. exactly the fuel consumption of 100 gas generators.</li>
 * </ul>
 * The burn rate follows the fill level of the tank - it is proportional to how much fuel is inside, so a full tank is
 * burned at the full rate and an almost empty tank only simmers along - and the energy per mB never changes. Fuel is
 * only burned while the internal buffer has room, so nothing is wasted.
 * <p>
 * The fuel value is read from Mekanism's own {@code chemicalFuel} data map (with the legacy chemical attribute as the
 * fallback), so every gas Mekanism accepts - and every gas another mod adds to that data map - works here with exactly
 * its own value and its own burn time.
 */
public class ImprovedGasGeneratorBlockEntity extends TileEntityConfigurableMachine {
    /** Hundred times the 18,000 mB tank of Mekanism's gas generator. */
    public static final long GAS_CAPACITY = 1_800_000L;
    /** Hundred times the 200,000 FE buffer of Mekanism's gas generator. */
    public static final long ENERGY_CAPACITY = 20_000_000L;
    /** How many Mekanism gas generators this machine is worth at a full tank (the tank and buffer scale is 100 as well). */
    public static final int GENERATOR_PARALLEL = 100;

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

    /** Fractional part of the burn rate, so a rate like 2.5 mB/t really burns 5 mB every two ticks. */
    private double burnAccumulator;
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

    /**
     * Fuel consumption of {@link #GENERATOR_PARALLEL} Mekanism gas generators burning this chemical: Mekanism burns one
     * mB every {@code burnTicks} ticks, so 100 generators burn {@code 100 / burnTicks} mB per tick - 100 mB/t for
     * hydrogen, 2.5 mB/t for ethene.
     */
    public static double maxBurnRate(ChemicalStack stack) {
        ChemicalFuel fuel = fuelData(stack);
        if (fuel == null) {
            return 0;
        }
        return GENERATOR_PARALLEL / (double) Math.max(1, fuel.burnTicks());
    }

    /** How full the gas tank is, 0 when empty and 1 when full. */
    public double getFillLevel() {
        if (gasTank == null || gasTank.getCapacity() <= 0) {
            return 0;
        }
        return Math.min(1D, (double) gasTank.getStored() / gasTank.getCapacity());
    }

    /** Current burn rate in mB per tick: the full rate scaled by how much fuel is left in the tank. */
    public double getBurnRate() {
        return maxBurnRate(gasTank.getStack()) * getFillLevel();
    }

    /** Energy per mB of the stored gas - Mekanism's own value, independent of the fill level. */
    public long getEnergyPerMb() {
        return fuelValue(gasTank.getStack());
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        fuelSlot.fillTank();
        energySlot.drainContainer();

        long perMb = getEnergyPerMb();
        double rate = perMb > 0 ? getBurnRate() : 0;
        int produced = 0;
        if (canFunction() && perMb > 0 && rate > 0) {
            long needed = energyContainer.getNeeded();
            if (needed > 0) {
                // Fractional rates are carried over, so a rate of 2.5 mB/t burns 5 mB every two ticks
                burnAccumulator += rate;
                int whole = (int) Math.min(Integer.MAX_VALUE, Math.floor(burnAccumulator));
                burnAccumulator -= whole;
                // Never burn more than the buffer can take, so no fuel is wasted on a full buffer
                int burn = (int) Math.min(Math.min(whole, gasTank.getStored()), Math.max(1, (needed + perMb - 1) / perMb));
                if (burn > 0) {
                    if (gasTank.extract(burn, Action.EXECUTE, AutomationType.INTERNAL).getAmount() == burn) {
                        produced = (int) Math.min(needed, burn * perMb);
                        energyContainer.insert(produced, Action.EXECUTE, AutomationType.INTERNAL);
                    }
                }
            }
        }
        if (produced == 0) {
            // Nothing was burned this tick, so do not bank a burst for later
            burnAccumulator = Math.min(burnAccumulator, 1D);
        }
        generationRate = produced;
        burnRateHundredths = (int) Math.round(rate * 100);
        energyPerMb = (int) Math.min(Integer.MAX_VALUE, perMb);
        setActive(produced > 0);
        return sendUpdatePacket;
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

    /** Name of the gas in the tank, as shown on the screen. */
    public Component getStoredGasName() {
        return gasTank.isEmpty() ? Component.translatable("gui.mekanismgizmo.improved_gas_generator.no_fuel")
              : gasTank.getStack().getTextComponent();
    }

    @Override
    public List<Component> getInfo(Upgrade upgrade) {
        return List.of(
              Component.translatable("gui.mekanismgizmo.improved_gas_generator.burn_rate",
                    formatBurnRate(burnRateHundredths)),
              Component.translatable("gui.mekanismgizmo.improved_gas_generator.output",
                    AmountFormat.format(generationRate), AmountFormat.format(energyPerMb)));
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(this::getGenerationRate, value -> generationRate = value));
        container.track(SyncableInt.create(this::getBurnRateHundredths, value -> burnRateHundredths = value));
        container.track(SyncableInt.create(this::getSyncedEnergyPerMb, value -> energyPerMb = value));
    }
}
