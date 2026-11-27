package jp.example.mekanismgizmo.block;

import java.util.List;
import jp.example.mekanismgizmo.registry.ModSideConfigs;
import jp.example.mekanismgizmo.util.MachineSideDefaults;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Electric item collector: while it has energy it regularly sucks up {@link ItemEntity item entities} lying in a
 * configurable box around the machine. The box is {@code n x n x n} blocks and can be shifted relative to the
 * machine (all three axes, -5..5 blocks).
 * <p>
 * Like the other machines it is built on Mekanism's configurable machine base, so energy, side configuration, auto
 * eject, redstone control, upgrades (speed = shorter scan interval), security and the configuration card support all
 * come from Mekanism itself.
 */
public class ItemCollectorBlockEntity extends GizmoMachineBlockEntity {
    /** Number of internal output slots (3x9 display, same as the Digital Miner). */
    public static final int SLOT_COUNT = 27;
    /** Edge length limits of the collection box, per axis. */
    public static final int MIN_RANGE = 1;
    public static final int MAX_RANGE = 9;
    /** Center offset limits (blocks), per axis. */
    public static final int MIN_OFFSET = -5;
    public static final int MAX_OFFSET = 5;
    /** Ticks between two scans without any speed upgrades. */
    public static final int BASE_SCAN_INTERVAL = 10;

    /** GUI order of the settings: range X, range Z, range Y, offset X, offset Z, offset Y. */
    public static final int SETTING_COUNT = 6;
    public static final int RANGE_SETTINGS = 3;
    private static final int[] DEFAULT_SETTINGS = {3, 3, 3, 0, 0, 0};

    private final int[] settings = DEFAULT_SETTINGS.clone();
    private OutputInventorySlot[] outputSlots;

    private MachineEnergyContainer<ItemCollectorBlockEntity> energyContainer;
    private EnergyInventorySlot energySlot;

    /** Ticks since the last scan. Not to be confused with {@code TileEntityMekanism.ticker}, which counts machine ticks. */
    private int scanTicker;
    private boolean hadItems;
    /** Whether the collection area preview is rendered in the world (client side value, synced from the server). */
    private boolean renderArea;

    public ItemCollectorBlockEntity(Holder<Block> block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        configComponent.setupItemIOConfig(List.of(), List.<IInventorySlot>of(outputSlots), energySlot, false);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
        MachineSideDefaults.applyIfUnconfigured(configComponent, ModSideConfigs.ITEM_COLLECTOR);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        MachineSideDefaults.applyIfUnconfigured(getConfig(), ModSideConfigs.ITEM_COLLECTOR);
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        // Note: instance field initializers run after super(), and super() calls this method, so the array has to be
        // created here instead of in the field declaration.
        outputSlots = new OutputInventorySlot[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            // 3 rows of 9 slots at the top of the machine screen (same layout as the Digital Miner)
            OutputInventorySlot slot = OutputInventorySlot.at(listener, 8 + (i % 9) * 18, 20 + (i / 9) * 18);
            outputSlots[i] = slot;
            builder.addSlot(slot);
        }
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 172, 20));
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    public MachineEnergyContainer<ItemCollectorBlockEntity> getEnergyContainer() {
        return energyContainer;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Settings (range and center offset)
    // ---------------------------------------------------------------------------------------------------------------

    public int getSetting(int index) {
        return index >= 0 && index < SETTING_COUNT ? settings[index] : 0;
    }

    public void setSetting(int index, int value) {
        if (index < 0 || index >= SETTING_COUNT) {
            return;
        }
        int clamped = index < RANGE_SETTINGS ? Mth.clamp(value, MIN_RANGE, MAX_RANGE) : Mth.clamp(value, MIN_OFFSET, MAX_OFFSET);
        if (settings[index] != clamped) {
            settings[index] = clamped;
            if (level != null && !level.isClientSide) {
                setChanged();
                // Keep the in-world preview of every nearby player in sync, not only the one with the GUI open
                sendUpdatePacket();
            }
        }
    }

    public int getRangeX() {
        return settings[0];
    }

    public int getRangeZ() {
        return settings[1];
    }

    public int getRangeY() {
        return settings[2];
    }

    public int getOffsetX() {
        return settings[3];
    }

    public int getOffsetZ() {
        return settings[4];
    }

    public int getOffsetY() {
        return settings[5];
    }

    /** Whether the collection box should be shown in the world. */
    public boolean isRenderArea() {
        return renderArea;
    }

    public void setRenderArea(boolean renderArea) {
        if (this.renderArea != renderArea) {
            this.renderArea = renderArea;
            if (level != null && !level.isClientSide) {
                setChanged();
                sendUpdatePacket();
            }
        }
    }

    public void toggleRenderArea() {
        setRenderArea(!renderArea);
    }

    /** The box (in world space) that is scanned for dropped items. */
    public AABB getCollectionArea() {
        double centerX = worldPosition.getX() + 0.5 + getOffsetX();
        double centerY = worldPosition.getY() + 0.5 + getOffsetY();
        double centerZ = worldPosition.getZ() + 0.5 + getOffsetZ();
        double halfX = getRangeX() / 2D;
        double halfY = getRangeY() / 2D;
        double halfZ = getRangeZ() / 2D;
        return new AABB(centerX - halfX, centerY - halfY, centerZ - halfZ, centerX + halfX, centerY + halfY, centerZ + halfZ);
    }

    private int getScanInterval() {
        return Math.max(1, MekanismUtils.getTicks(this, BASE_SCAN_INTERVAL));
    }

    private long getScanCost() {
        return energyContainer.getEnergyPerTick() * getScanInterval();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Tick
    // ---------------------------------------------------------------------------------------------------------------

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        boolean active = hadItems;
        if (canFunction() && ++scanTicker >= getScanInterval()) {
            scanTicker = 0;
            long cost = getScanCost();
            if (energyContainer.extract(cost, Action.SIMULATE, AutomationType.INTERNAL) == cost) {
                int found = collectItems();
                if (found > 0) {
                    // Energy is only used when there actually is something to collect
                    energyContainer.extract(cost, Action.EXECUTE, AutomationType.INTERNAL);
                }
                hadItems = found > 0;
                active = hadItems;
            } else {
                hadItems = false;
                active = false;
            }
        }
        setActive(active);
        return sendUpdatePacket;
    }

    /** Collects all dropped items inside the configured box, returns how many item entities were found. */
    private int collectItems() {
        if (level == null || level.isClientSide) {
            return 0;
        }
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, getCollectionArea(),
              entity -> entity.isAlive() && !entity.getItem().isEmpty());
        int collected = 0;
        boolean moved = false;
        for (ItemEntity itemEntity : items) {
            int before = itemEntity.getItem().getCount();
            ItemStack remainder = itemEntity.getItem();
            for (OutputInventorySlot slot : outputSlots) {
                remainder = slot.insertItem(remainder, Action.EXECUTE, AutomationType.INTERNAL);
                if (remainder.isEmpty()) {
                    break;
                }
            }
            if (remainder.getCount() < before) {
                // A partial transfer still uses a scan; otherwise items could be moved for free when a slot is nearly full
                moved = true;
            }
            if (remainder.isEmpty()) {
                itemEntity.discard();
                collected++;
                level.playSound(null, itemEntity.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, 1.4F);
            } else {
                itemEntity.setItem(remainder);
            }
        }
        return moved ? Math.max(1, collected) : 0;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(List.<IInventorySlot>of(outputSlots));
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.ITEM;
    }

    @Override
    public List<Component> getInfo(Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        for (int i = 0; i < SETTING_COUNT; i++) {
            final int index = i;
            container.track(SyncableInt.create(() -> getSetting(index), value -> setSetting(index, value)));
        }
        container.track(SyncableBoolean.create(this::isRenderArea, this::setRenderArea));
    }

    @Override
    public CompoundTag getReducedUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getReducedUpdateTag(registries);
        // Save data is not part of the reduced update tag, so send the preview state and the box settings explicitly;
        // otherwise newly joining clients would render (and display) the default box until a GUI is opened.
        tag.putBoolean("RenderArea", renderArea);
        tag.putIntArray("CollectorSettings", settings);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        renderArea = tag.getBoolean("RenderArea");
        if (tag.contains("CollectorSettings")) {
            int[] loaded = tag.getIntArray("CollectorSettings");
            for (int i = 0; i < SETTING_COUNT; i++) {
                setSetting(i, i < loaded.length ? loaded[i] : DEFAULT_SETTINGS[i]);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putIntArray("CollectorSettings", settings);
        tag.putBoolean("RenderArea", renderArea);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        MachineSideDefaults.applyIfUnconfigured(getConfig(), ModSideConfigs.ITEM_COLLECTOR);
        renderArea = tag.getBoolean("RenderArea");
        if (tag.contains("CollectorSettings")) {
            int[] loaded = tag.getIntArray("CollectorSettings");
            for (int i = 0; i < SETTING_COUNT; i++) {
                settings[i] = i < loaded.length ? loaded[i] : DEFAULT_SETTINGS[i];
                setSetting(i, settings[i]);
            }
        }
    }
}
