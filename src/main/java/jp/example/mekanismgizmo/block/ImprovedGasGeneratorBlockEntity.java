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
 * 200,000 FE). On top of that it burns its fuel the way the reactor of Compact Mekanism Machines works: the fuller the
 * tank, the better the fuel is used. A full tank burns a fuel at exactly the value Mekanism's own gas generator gives
 * it (200 FE per mB for hydrogen), while an almost empty tank only gets {@link #MIN_EFFICIENCY} of that.
 * <p>
 * The fuel value is read from Mekanism's own {@code chemicalFuel} data map (with the legacy chemical attribute as the
 * fallback), so every gas Mekanism accepts - and every gas another mod adds to that data map - works here with exactly
 * its own value.
 */
public class ImprovedGasGeneratorBlockEntity extends TileEntityConfigurableMachine {
    /** Hundred times the 18,000 mB tank of Mekanism's gas generator. */
    public static final long GAS_CAPACITY = 1_800_000L;
    /** Hundred times the 200,000 FE buffer of Mekanism's gas generator. */
    public static final long ENERGY_CAPACITY = 20_000_000L;
    /** Fuel burned per tick while there is room in the buffer. */
    public static final int BURN_RATE = 20;
    /** Efficiency of a nearly empty tank; a full tank reaches 100%, which is Mekanism's own fuel value. */
    public static final double MIN_EFFICIENCY = 0.2D;

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

    /** Energy produced in the last tick, synced to the screen. */
    private int generationRate;
    /** Combustion efficiency in percent, synced to the screen. */
    private int efficiency;

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
     * Energy Mekanism's gas generator produces from one mB of this chemical, or 0 when it is not a fuel. The value
     * comes from Mekanism's {@code chemicalFuel} data map, with the legacy chemical attribute as the fallback - the
     * same two places Mekanism's own gas generator reads.
     */
    public static long fuelValue(ChemicalStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        ChemicalFuel fuel = stack.getData(IMekanismDataMapTypes.INSTANCE.chemicalFuel());
        if (fuel != null) {
            return fuel.energyDensity();
        }
        ChemicalAttributes.Fuel legacy = stack.getLegacy(ChemicalAttributes.Fuel.class);
        return legacy == null ? 0 : legacy.asModern().energyDensity();
    }

    /**
     * Combustion efficiency between {@link #MIN_EFFICIENCY} and 1: an almost empty tank only gets a fifth of the fuel
     * value, and a full tank gets all of it.
     */
    public double getEfficiency() {
        if (gasTank == null || gasTank.getCapacity() <= 0) {
            return MIN_EFFICIENCY;
        }
        double fill = (double) gasTank.getStored() / gasTank.getCapacity();
        return MIN_EFFICIENCY + (1D - MIN_EFFICIENCY) * Math.min(1D, Math.max(0D, fill));
    }

    /** Energy one mB of the stored gas produces right now (fuel value times the combustion efficiency). */
    public long getEnergyPerMb() {
        return Math.round(fuelValue(gasTank.getStack()) * getEfficiency());
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        fuelSlot.fillTank();
        energySlot.drainContainer();

        long perMb = getEnergyPerMb();
        int produced = 0;
        if (canFunction() && perMb > 0) {
            long needed = energyContainer.getNeeded();
            if (needed > 0) {
                // Never burn more than the buffer can take, so no fuel is wasted on a full buffer
                int burn = (int) Math.min(Math.min(BURN_RATE, gasTank.getStored()), (needed + perMb - 1) / perMb);
                if (burn > 0) {
                    if (gasTank.extract(burn, Action.EXECUTE, AutomationType.INTERNAL).getAmount() == burn) {
                        produced = (int) Math.min(needed, burn * perMb);
                        energyContainer.insert(produced, Action.EXECUTE, AutomationType.INTERNAL);
                    }
                }
            }
        }
        generationRate = produced;
        efficiency = (int) Math.round(getEfficiency() * 100);
        setActive(produced > 0);
        return sendUpdatePacket;
    }

    public IChemicalTank getGasTank() {
        return gasTank;
    }

    public BasicEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    /** Energy produced in the last tick, as shown on the screen. */
    public int getGenerationRate() {
        return generationRate;
    }

    /** Combustion efficiency in percent, as shown on the screen. */
    public int getEfficiencyPercent() {
        return efficiency;
    }

    /** Value of the stored fuel at the current efficiency, as shown on the screen. */
    public long getSyncedEnergyPerMb() {
        return getEnergyPerMb();
    }

    @Override
    public List<Component> getInfo(Upgrade upgrade) {
        return List.of(
              Component.translatable("gui.mekanismgizmo.improved_gas_generator.efficiency", efficiency),
              Component.translatable("gui.mekanismgizmo.improved_gas_generator.fuel", AmountFormat.format(getSyncedEnergyPerMb())));
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(this::getGenerationRate, value -> generationRate = value));
        container.track(SyncableInt.create(this::getEfficiencyPercent, value -> efficiency = value));
    }
}
