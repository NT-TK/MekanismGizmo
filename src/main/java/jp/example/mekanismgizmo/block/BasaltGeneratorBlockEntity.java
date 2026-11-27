package jp.example.mekanismgizmo.block;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import mekanism.api.providers.IBlockProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Electric basalt generator: while it has energy (and room for the output) it keeps producing basalt, without
 * consuming anything else. Built on Mekanism's configurable machine base, so the side configuration, auto eject,
 * redstone control, upgrades, energy handling and the configuration card support all come from Mekanism itself.
 */
public class BasaltGeneratorBlockEntity extends TieredMachineBlockEntity {
    public static final int BASE_TICKS_REQUIRED = 20;

    /** Top left corner of the basalt output row; the row grows to the right with every tier. */
    public static final int OUTPUT_X = 96;
    public static final int OUTPUT_Y = 36;
    public static final int ENERGY_Y = 14;

    private MachineEnergyContainer<BasaltGeneratorBlockEntity> energyContainer;
    private OutputInventorySlot[] outputSlots;
    private EnergyInventorySlot energySlot;

    private int progress;
    private int ticksRequired = BASE_TICKS_REQUIRED;

    public BasaltGeneratorBlockEntity(IBlockProvider block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        configComponent.setupItemIOConfig(List.of(), List.of(outputSlots), energySlot, false);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
    }


    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        int parallel = parallel();
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        outputSlots = new OutputInventorySlot[parallel];
        for (int i = 0; i < parallel; i++) {
            builder.addSlot(outputSlots[i] = OutputInventorySlot.at(listener, outputSlotX(i), OUTPUT_Y));
        }
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, energySlotX(parallel), ENERGY_Y));
        return builder.build();
    }

    /** x position of the {@code index}-th basalt output slot. */
    public static int outputSlotX(int index) {
        return OUTPUT_X + index * 18;
    }

    /** x position of the energy slot, right of the last output slot. */
    public static int energySlotX(int parallel) {
        return 143 + (parallel - 1) * 18;
    }

    /** Total width of the machine screen for the given tier. */
    public static int guiWidth(int parallel) {
        return 176 + (parallel - 1) * 18;
    }

    /** x offset of the player inventory inside the machine screen (the screen is wider for higher tiers). */
    public static int inventoryXOffset(int parallel) {
        return 8 + (parallel - 1) * 9;
    }

    public OutputInventorySlot[] getOutputSlots() {
        return outputSlots;
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    public MachineEnergyContainer<BasaltGeneratorBlockEntity> getEnergyContainer() {
        return energyContainer;
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

    private boolean canAcceptOutput(int index) {
        ItemStack output = outputSlots[index].getStack();
        return output.isEmpty() || (output.is(Items.BASALT) && output.getCount() < output.getMaxStackSize());
    }

    /** How many basalt this operation could produce (limited by the free output slots). */
    private int producibleBasalt() {
        int free = 0;
        for (int i = 0; i < outputSlots.length; i++) {
            if (canAcceptOutput(i)) {
                free++;
            }
        }
        return free;
    }

    /** True when the generator is running but no output slot has room (Mekanism warning hook). */
    public boolean isOutputBlocked() {
        return progress > 0 && producibleBasalt() == 0;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();

        if (MekanismUtils.canFunction(this)) {
            if (progress > 0) {
                // No room for the basalt right now: pause and keep the progress (do not throw it away)
                if (producibleBasalt() > 0) {
                    FloatingLong energyPerTick = energyContainer.getEnergyPerTick();
                    if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
                        energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                        if (--progress <= 0) {
                            finish();
                        }
                    }
                }
            } else if (producibleBasalt() > 0) {
                progress = ticksRequired;
            }
        }
        setActive(progress > 0);
    }

    private void finish() {
        progress = 0;
        // Energy is the only cost, so the basalt is simply produced here. Use every slot that can take a block,
        // not just the first N slots (a full slot 0 must not swallow the output of a free slot 1).
        for (int i = 0; i < outputSlots.length; i++) {
            if (canAcceptOutput(i)) {
                outputSlots[i].insertItem(new ItemStack(Items.BASALT), Action.EXECUTE, AutomationType.INTERNAL);
            }
        }
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(List.of(outputSlots));
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