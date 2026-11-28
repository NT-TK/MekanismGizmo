package jp.example.mekanismgizmo.block;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import mekanism.api.providers.IBlockProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Machine resetter: takes the item form of a machine and hands back a clean copy of it.
 * <p>
 * In Mekanism 10.4 for Minecraft 1.20.1 a machine item keeps everything the machine contains: the owner, the security
 * mode, item, fluid and gas contents, the stored energy, the installed upgrades, the side configuration and the
 * redstone setting. Breaking a machine therefore keeps all of that, and a machine that was picked up in a world you no
 * longer use cannot be cleaned by hand. This machine does that: insert the machine item, pay a little energy, and the
 * result is the same item with its defaults only - exactly the item you would get from crafting it.
 */
public class MachineResetterBlockEntity extends GizmoMachineBlockEntity {
    /** Ticks one machine item needs at base speed. */
    public static final int BASE_TICKS_REQUIRED = 20;

    public static final int INPUT_X = 26;
    public static final int OUTPUT_X = 96;
    public static final int IO_Y = 36;
    /** Arrow between the input and the output slot. */
    public static final int ARROW_X = 54;
    public static final int ARROW_Y = 40;
    public static final int ENERGY_SLOT_X = 143;
    public static final int ENERGY_SLOT_Y = 14;
    public static final int POWER_BAR_X = 164;
    public static final int POWER_BAR_Y = 16;

    private MachineEnergyContainer<MachineResetterBlockEntity> energyContainer;
    private InputInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    private int progress;
    private int ticksRequired = BASE_TICKS_REQUIRED;

    public MachineResetterBlockEntity(IBlockProvider block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        configComponent.setupItemIOConfig(List.of(inputSlot), List.of(outputSlot), energySlot, false);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
        applySideDefaults();
    }


    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(inputSlot = InputInventorySlot.at(MachineResetterBlockEntity::isMachineItem, listener, INPUT_X, IO_Y));
        builder.addSlot(outputSlot = OutputInventorySlot.at(listener, OUTPUT_X, IO_Y));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener,
              ENERGY_SLOT_X, ENERGY_SLOT_Y));
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    /**
     * True for the item form of a Mekanism machine (or any Mekanism block that stores contents): a block item that
     * carries machine data - a side configuration, an ejector setting, stored items, fluids, gas or energy, a redstone
     * setting, upgrades or an owner. Plain blocks such as a steel casing and ordinary items are rejected. Packed
     * cardboard boxes are also rejected: they are block items with data, but resetting one would delete the block
     * stored inside it instead of cleaning a machine.
     */
    public static boolean isMachineItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem
              && !(blockItem.getBlock() instanceof BlockCardboardBox) && hasMachineData(stack);
    }

    private static boolean hasMachineData(ItemStack stack) {
        var data = ItemDataUtils.getDataMapIfPresent(stack);
        return data != null && !data.isEmpty();
    }

    /**
     * The clean copy the machine produces for the current input (same item, no stored data). One machine item is
     * processed per cycle: the energy cost of a cycle is fixed, so a stack of sixty-four machines has to run sixty-four
     * cycles instead of being reset for the price of one.
     */
    public ItemStack getResult() {
        ItemStack input = inputSlot.getStack();
        if (!isMachineItem(input)) {
            return ItemStack.EMPTY;
        }
        return input.getItem().getDefaultInstance();
    }

    private boolean canAcceptResult(ItemStack result) {
        ItemStack output = outputSlot.getStack();
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(output, result)
              && output.getCount() + result.getCount() <= Math.min(result.getMaxStackSize(), outputSlot.getLimit(result));
    }

    private boolean hasWork() {
        ItemStack result = getResult();
        return !result.isEmpty() && canAcceptResult(result);
    }

    public double getScaledProgress() {
        if (progress <= 0 || ticksRequired <= 0) {
            return 0;
        }
        return 1D - (double) progress / ticksRequired;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getTicksRequired() {
        return ticksRequired;
    }

    public void setTicksRequired(int ticksRequired) {
        this.ticksRequired = ticksRequired;
    }

    public MachineEnergyContainer<MachineResetterBlockEntity> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();

        if (MekanismUtils.canFunction(this)) {
            if (progress > 0) {
                // The input was taken out (or the output filled up): pause instead of finishing with nothing to do
                if (hasWork()) {
                    FloatingLong energyPerTick = energyContainer.getEnergyPerTick();
                    if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
                        energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                        if (--progress <= 0) {
                            finishReset();
                        }
                    }
                }
            } else if (hasWork()) {
                progress = ticksRequired;
            }
        }
        setActive(progress > 0);
    }

    private void finishReset() {
        progress = 0;
        ItemStack result = getResult();
        if (result.isEmpty() || !canAcceptResult(result)) {
            return;
        }
        // Consume the input before inserting the clean item: if extraction ever failed, inserting first would duplicate
        ItemStack extracted = inputSlot.extractItem(1, Action.EXECUTE, AutomationType.INTERNAL);
        if (extracted.isEmpty()) {
            return;
        }
        ItemStack output = outputSlot.getStack();
        if (output.isEmpty()) {
            outputSlot.setStack(result);
        } else if (ItemStack.isSameItemSameTags(output, result)) {
            output.setCount(output.getCount() + result.getCount());
            outputSlot.setStack(output);
        } else {
            // The output changed while the reset was running; put the machine item back instead of voiding it
            inputSlot.insertItem(extracted, Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    /** True while the machine is running but the output slot has no room (Mekanism warning hook). */
    public boolean isOutputBlocked() {
        return progress > 0 && !hasWork();
    }

    public InputInventorySlot getInputSlot() {
        return inputSlot;
    }

    public OutputInventorySlot getOutputSlot() {
        return outputSlot;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(List.of(outputSlot));
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
        container.track(SyncableInt.create(this::getProgress, this::setProgress));
        container.track(SyncableInt.create(this::getTicksRequired, this::setTicksRequired));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Progress", progress);
        tag.putInt("TicksRequired", ticksRequired);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("Progress");
        ticksRequired = tag.contains("TicksRequired") ? tag.getInt("TicksRequired") : BASE_TICKS_REQUIRED;
    }
}