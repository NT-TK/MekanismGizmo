package jp.example.mekanismgizmo.block;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import mekanism.api.providers.IBlockProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Liquid heater (mekanismgizmo:liquid_heater).
 * <p>
 * Uses energy to heat sodium into superheated sodium - the coolant a fission reactor produces, which is normally only
 * available from a reactor and a boiler. One operation converts {@link #SODIUM_PER_OPERATION} mB of sodium into the
 * same amount of superheated sodium, and every tier runs one more operation in parallel (so ultimate converts five
 * times as much per operation).
 * <p>
 * The two chemicals live in separate tanks, so the machine is configured like Mekanism's own chemical machines: sodium
 * is the input of the chemical configuration and superheated sodium is its output. Everything else - side
 * configuration, redstone control, upgrades, security and the energy slot - comes from Mekanism's configurable machine
 * base.
 */
public class LiquidHeaterBlockEntity extends TieredMachineBlockEntity {
    /** Tank size per tier: forty operations worth of sodium, so a full tank keeps the machine busy for a while. */
    public static final int TANK_CAPACITY = 20_000;
    /** Sodium converted per operation (multiplied by the parallel operations of the tier). */
    public static final int SODIUM_PER_OPERATION = 500;
    public static final int BASE_TICKS_REQUIRED = 20;

    // --- screen layout ---
    public static final int SODIUM_GAUGE_X = 6;
    public static final int SUPERHEATED_GAUGE_X = 96;
    public static final int GAUGE_Y = 13;
    public static final int ARROW_X = 54;
    public static final int ARROW_Y = 40;
    public static final int ENERGY_SLOT_X = 143;
    public static final int ENERGY_SLOT_Y = 15;
    public static final int POWER_BAR_X = 164;
    public static final int POWER_BAR_Y = 16;

    private IGasTank sodiumTank;
    private IGasTank superheatedTank;
    private MachineEnergyContainer<LiquidHeaterBlockEntity> energyContainer;
    private EnergyInventorySlot energySlot;

    private int heatTicks;
    private int ticksRequired = BASE_TICKS_REQUIRED;

    public LiquidHeaterBlockEntity(IBlockProvider block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        // Only the energy slot is part of the item configuration; the chemicals are piped in and out
        configComponent.setupItemIOConfig(List.of(), List.of(), energySlot, false);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        // Sodium is the chemical input of this machine and superheated sodium is its chemical output
        configComponent.setupInputConfig(TransmissionType.GAS, sodiumTank);
        configComponent.setupOutputConfig(TransmissionType.GAS, superheatedTank);
        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.GAS);
    }


    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        sodiumTank = ChemicalTankBuilder.GAS.input(tankCapacity(parallel()),
              gas -> gas == MekanismGases.SODIUM.get(), listener);
        superheatedTank = ChemicalTankBuilder.GAS.output(tankCapacity(parallel()), listener);
        builder.addTank(sodiumTank);
        builder.addTank(superheatedTank);
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener,
              ENERGY_SLOT_X, ENERGY_SLOT_Y));
        return builder.build();
    }

    /** Capacity of each of the two chemical tanks for a tier. */
    public static int tankCapacity(int parallel) {
        return TANK_CAPACITY * parallel;
    }

    /** Sodium converted by one operation of this tier. */
    public int operationAmount() {
        return SODIUM_PER_OPERATION * parallel();
    }

    /** Room left in the superheated sodium tank. */
    private long superheatedRoom() {
        return superheatedTank.getCapacity() - superheatedTank.getStored();
    }

    /** True when there is hot coolant in the output tank but the tank cannot take the next operation. */
    public boolean isOutputBlocked() {
        return sodiumTank.getStored() >= operationAmount() && superheatedRoom() < operationAmount();
    }

    private boolean canHeat() {
        return sodiumTank.getStored() >= operationAmount() && superheatedRoom() >= operationAmount();
    }

    public IGasTank getSodiumTank() {
        return sodiumTank;
    }

    public IGasTank getSuperheatedTank() {
        return superheatedTank;
    }

    public MachineEnergyContainer<LiquidHeaterBlockEntity> getEnergyContainer() {
        return energyContainer;
    }

    public double getScaledProgress() {
        if (heatTicks <= 0 || ticksRequired <= 0) {
            return 0;
        }
        return 1D - (double) heatTicks / ticksRequired;
    }

    public int getHeatTicks() {
        return heatTicks;
    }

    public void setHeatTicks(int heatTicks) {
        this.heatTicks = heatTicks;
    }

    public int getTicksRequired() {
        return ticksRequired;
    }

    public void setTicksRequired(int ticksRequired) {
        this.ticksRequired = ticksRequired;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();

        if (MekanismUtils.canFunction(this)) {
            if (heatTicks > 0) {
                // No room for the hot coolant right now: pause and keep the progress (do not throw it away)
                if (canHeat()) {
                    FloatingLong energyPerTick = energyContainer.getEnergyPerTick();
                    if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
                        energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                        if (--heatTicks <= 0) {
                            finishHeating();
                        }
                    }
                }
            } else if (canHeat()) {
                heatTicks = ticksRequired;
            }
        }
        setActive(heatTicks > 0);
    }

    private void finishHeating() {
        heatTicks = 0;
        int amount = operationAmount();
        GasStack superheated = new GasStack(MekanismGases.SUPERHEATED_SODIUM.get(), amount);
        // Check the output before touching the input: if the tank cannot take the whole operation, nothing is lost
        if (!superheatedTank.insert(superheated, Action.SIMULATE, AutomationType.INTERNAL).isEmpty()) {
            return;
        }
        // Take the sodium first so a failure cannot duplicate anything, then fill the output tank
        if (sodiumTank.extract(amount, Action.EXECUTE, AutomationType.INTERNAL).getAmount() < amount) {
            return;
        }
        superheatedTank.insert(superheated, Action.EXECUTE, AutomationType.INTERNAL);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(sodiumTank.getStored(), sodiumTank.getCapacity());
    }


    @Override
    public List<Component> getInfo(Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(this::getHeatTicks, this::setHeatTicks));
        container.track(SyncableInt.create(this::getTicksRequired, this::setTicksRequired));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("HeatTicks", heatTicks);
        tag.putInt("TicksRequired", ticksRequired);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        heatTicks = tag.getInt("HeatTicks");
        ticksRequired = tag.contains("TicksRequired") ? tag.getInt("TicksRequired") : BASE_TICKS_REQUIRED;
    }
}