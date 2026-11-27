package jp.example.mekanismgizmo.block;

import java.util.List;
import jp.example.mekanismgizmo.registry.ModSideConfigs;
import jp.example.mekanismgizmo.util.MachineSideDefaults;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.attachments.containers.ContainerType;
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
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
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

    private IChemicalTank sodiumTank;
    private IChemicalTank superheatedTank;
    private MachineEnergyContainer<LiquidHeaterBlockEntity> energyContainer;
    private EnergyInventorySlot energySlot;

    private int heatTicks;
    private int ticksRequired = BASE_TICKS_REQUIRED;

    public LiquidHeaterBlockEntity(Holder<Block> block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        // Only the energy slot is part of the item configuration; the chemicals are piped in and out
        configComponent.setupItemIOConfig(List.of(), List.of(), energySlot, false);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        // Sodium is the chemical input of this machine and superheated sodium is its chemical output
        configComponent.setupInputConfig(TransmissionType.CHEMICAL, sodiumTank);
        configComponent.setupOutputConfig(TransmissionType.CHEMICAL, superheatedTank);
        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.CHEMICAL);
        MachineSideDefaults.applyIfUnconfigured(configComponent, ModSideConfigs.LIQUID_HEATER);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        // Items dropped by an older version (or copied from an unconfigured machine) can carry an all-"None" side
        // configuration; repair it so the machine still accepts sodium, power and pipes out the hot coolant.
        MachineSideDefaults.applyIfUnconfigured(getConfig(), ModSideConfigs.LIQUID_HEATER);
    }

    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        sodiumTank = BasicChemicalTank.inputModern(tankCapacity(parallel()),
              stack -> stack.is(MekanismChemicals.SODIUM), listener);
        superheatedTank = BasicChemicalTank.output(tankCapacity(parallel()), listener);
        builder.addTank(sodiumTank);
        builder.addTank(superheatedTank);
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
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

    public IChemicalTank getSodiumTank() {
        return sodiumTank;
    }

    public IChemicalTank getSuperheatedTank() {
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
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();

        if (canFunction()) {
            if (heatTicks > 0) {
                // No room for the hot coolant right now: pause and keep the progress (do not throw it away)
                if (canHeat()) {
                    long energyPerTick = energyContainer.getEnergyPerTick();
                    if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL) == energyPerTick) {
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
        return sendUpdatePacket;
    }

    private void finishHeating() {
        heatTicks = 0;
        int amount = operationAmount();
        var superheated = MekanismChemicals.SUPERHEATED_SODIUM.asStack(amount);
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
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.CHEMICAL || type == ContainerType.ITEM;
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
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("HeatTicks", heatTicks);
        tag.putInt("TicksRequired", ticksRequired);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        MachineSideDefaults.applyIfUnconfigured(getConfig(), ModSideConfigs.LIQUID_HEATER);
        heatTicks = tag.getInt("HeatTicks");
        ticksRequired = tag.contains("TicksRequired") ? tag.getInt("TicksRequired") : BASE_TICKS_REQUIRED;
    }
}
