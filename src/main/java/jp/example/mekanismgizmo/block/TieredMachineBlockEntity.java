package jp.example.mekanismgizmo.block;

import jp.example.mekanismgizmo.util.MachineTier;
import jp.example.mekanismgizmo.util.NbtUpgradeData;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.upgrade.IUpgradeData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base class of the tiered machines. The tier is read from the block that created the tile, so every machine needs
 * one block entity type per tier (like Mekanism's factories), and the tier installer can replace the block and move
 * the contents across.
 */
public abstract class TieredMachineBlockEntity extends GizmoMachineBlockEntity {

    protected TieredMachineBlockEntity(IBlockProvider block, BlockPos pos, BlockState state) {
        super(block, pos, state);
    }

    /** The tier of this machine. Safe to call while the tile is being constructed (used for the slot layout). */
    public MachineTier getTier() {
        return getBlockType() instanceof TieredMachine tiered ? tiered.getTier() : MachineTier.UNTIERED;
    }

    /** Number of items this tier can process in parallel (1 for the untiered machine, 5 for ultimate). */
    public int parallel() {
        return getTier().getParallel();
    }

    @Override
    public IUpgradeData getUpgradeData() {
        // Mekanism's tier installer calls this, replaces the block, then hands the data to parseUpgradeData of the
        // new tier. Saving and reloading the whole tile keeps everything (contents, config, owner, upgrades).
        return canBeUpgraded() ? new NbtUpgradeData(saveWithoutMetadata()) : null;
    }

    @Override
    public void parseUpgradeData(IUpgradeData data) {
        if (data instanceof NbtUpgradeData nbt) {
            load(nbt.tag());
            setChanged();
            sendUpdatePacket();
        } else {
            super.parseUpgradeData(data);
        }
    }
}