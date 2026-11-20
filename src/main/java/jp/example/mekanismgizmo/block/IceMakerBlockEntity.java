package jp.example.mekanismgizmo.block;

import java.util.List;
import jp.example.mekanismgizmo.registry.ModSideConfigs;
import jp.example.mekanismgizmo.util.MachineSideDefaults;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Freezes water into ice. Built on Mekanism's machine base classes, so the side configuration, redstone
 * control, upgrades, fluid capability and item/energy handling all come from Mekanism itself.
 * <p>
 * Every tier adds one more ice output slot (one parallel operation): 1 for the untiered machine, 2 for basic, ...
 * and 5 for ultimate.
 */
public class IceMakerBlockEntity extends TieredMachineBlockEntity {
    public static final int WATER_CAPACITY = 4_000;
    public static final int WATER_PER_ICE = 1_000;
    public static final int BASE_TICKS_REQUIRED = 100;

    /** Top left corner of the ice output row and the vertical position shared by all tiers. */
    public static final int OUTPUT_X = 96;
    public static final int OUTPUT_Y = 36;
    /** y of the water container slot (same row as the ice outputs) and of the energy slot. */
    public static final int INPUT_Y = 36;
    public static final int ENERGY_Y = 14;

    public IExtendedFluidTank waterTank;
    private MachineEnergyContainer<IceMakerBlockEntity> energyContainer;
    private FluidInventorySlot waterSlot;
    private OutputInventorySlot[] iceSlots;
    private EnergyInventorySlot energySlot;

    private int freezeTicks;
    private int ticksRequired = BASE_TICKS_REQUIRED;

    public IceMakerBlockEntity(Holder<Block> block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        configComponent.setupItemIOConfig(List.of(waterSlot), List.of(iceSlots), energySlot, false);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        configComponent.setupInputConfig(TransmissionType.FLUID, waterTank);
        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
        MachineSideDefaults.applyIfUnconfigured(configComponent, ModSideConfigs.ICE_MAKER);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        // Items dropped by an older version (or copied from an unconfigured machine) can carry an all-"None" side
        // configuration; repair it so the machine still accepts power and piped items.
        MachineSideDefaults.applyIfUnconfigured(getConfig(), ModSideConfigs.ICE_MAKER);
    }

    @Override
    public IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this);
        waterTank = BasicFluidTank.input(waterCapacity(parallel()), fluid -> fluid.is(Fluids.WATER), listener);
        builder.addTank(waterTank);
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
        int parallel = parallel();
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        builder.addSlot(waterSlot = FluidInventorySlot.input(waterTank, listener, 26, INPUT_Y));
        iceSlots = new OutputInventorySlot[parallel];
        for (int i = 0; i < parallel; i++) {
            builder.addSlot(iceSlots[i] = OutputInventorySlot.at(listener, outputSlotX(i), OUTPUT_Y));
        }
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, energySlotX(parallel), ENERGY_Y));
        return builder.build();
    }

    /** x position of the {@code index}-th ice output slot. */
    public static int outputSlotX(int index) {
        return OUTPUT_X + index * 18;
    }

    /** x position of the energy slot, right of the last ice output slot. */
    public static int energySlotX(int parallel) {
        return 143 + (parallel - 1) * 18;
    }

    /** Total width of the machine screen for the given tier. */
    public static int guiWidth(int parallel) {
        return 176 + (parallel - 1) * 18;
    }

    /**
     * Water capacity of a tier. A full parallel operation needs {@code WATER_PER_ICE} mB per ice, so the tank has to
     * grow with the tier: 4,000 mB (4 ice) for the untiered machine up to 20,000 mB (20 ice) for ultimate.
     */
    public static int waterCapacity(int parallel) {
        return WATER_CAPACITY * parallel;
    }

    /** x offset of the player inventory inside the machine screen (the screen is wider for higher tiers). */
    public static int inventoryXOffset(int parallel) {
        return 8 + (parallel - 1) * 9;
    }

    public IExtendedFluidTank getWaterTank() {
        return waterTank;
    }

    public MachineEnergyContainer<IceMakerBlockEntity> getEnergyContainer() {
        return energyContainer;
    }

    public double getScaledProgress() {
        if (freezeTicks <= 0 || ticksRequired <= 0) {
            return 0;
        }
        return 1D - (double) freezeTicks / ticksRequired;
    }

    public int getFreezeTicks() {
        return freezeTicks;
    }

    public void setFreezeTicks(int freezeTicks) {
        this.freezeTicks = freezeTicks;
    }

    public int getTicksRequired() {
        return ticksRequired;
    }

    public void setTicksRequired(int ticksRequired) {
        this.ticksRequired = ticksRequired;
    }

    private boolean canAcceptIce(int index) {
        ItemStack output = iceSlots[index].getStack();
        return output.isEmpty() || (output.is(Items.ICE) && output.getCount() < output.getMaxStackSize());
    }

    /** How many ice blocks this operation would produce (limited by free outputs and stored water). */
    private int producibleIce() {
        int free = 0;
        for (int i = 0; i < iceSlots.length && free < waterTank.getFluidAmount() / WATER_PER_ICE; i++) {
            if (canAcceptIce(i)) {
                free++;
            }
        }
        return free;
    }

    /** True when enough water is stored but no ice output slot has room (Mekanism warning hook). */
    public boolean isOutputBlocked() {
        return waterTank.getFluidAmount() >= WATER_PER_ICE && producibleIce() == 0;
    }

    private boolean canMakeIce() {
        return producibleIce() > 0;
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        waterSlot.fillTank();

        if (canFunction()) {
            if (freezeTicks > 0) {
                // No room for the ice right now: pause and keep the progress (do not throw it away)
                if (canMakeIce()) {
                    long energyPerTick = energyContainer.getEnergyPerTick();
                    if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL) == energyPerTick) {
                        energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                        if (--freezeTicks <= 0) {
                            finishIce();
                        }
                    }
                }
            } else if (canMakeIce()) {
                freezeTicks = ticksRequired;
            }
        }
        setActive(freezeTicks > 0);
        return sendUpdatePacket;
    }

    private void finishIce() {
        freezeTicks = 0;
        // Water is consumed when the ice block is produced, so only the slots that can really take a block count.
        // (Slot 0 being full must not eat the water of the ice that would have gone into slot 1.)
        int water = waterTank.getFluidAmount() / WATER_PER_ICE;
        for (int i = 0; i < iceSlots.length && water > 0; i++) {
            if (!canAcceptIce(i)) {
                continue;
            }
            if (waterTank.extract(WATER_PER_ICE, Action.EXECUTE, AutomationType.INTERNAL).getAmount() < WATER_PER_ICE) {
                return;
            }
            water--;
            // Let Mekanism's inventory slot handle merging/limits.
            iceSlots[i].insertItem(new ItemStack(Items.ICE), Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(waterTank.getFluidAmount(), waterTank.getCapacity());
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.FLUID || type == ContainerType.ITEM;
    }

    @Override
    public List<net.minecraft.network.chat.Component> getInfo(Upgrade upgrade) {
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
        container.track(SyncableInt.create(this::getFreezeTicks, this::setFreezeTicks));
        container.track(SyncableInt.create(this::getTicksRequired, this::setTicksRequired));
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("FreezeTicks", freezeTicks);
        tag.putInt("TicksRequired", ticksRequired);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        MachineSideDefaults.applyIfUnconfigured(getConfig(), ModSideConfigs.ICE_MAKER);
        freezeTicks = tag.getInt("FreezeTicks");
        ticksRequired = tag.contains("TicksRequired") ? tag.getInt("TicksRequired") : BASE_TICKS_REQUIRED;
    }
}
