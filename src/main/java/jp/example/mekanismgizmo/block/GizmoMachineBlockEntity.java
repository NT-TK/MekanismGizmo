package jp.example.mekanismgizmo.block;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.integration.energy.BlockEnergyCapabilityCache;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.CableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Common base of every machine of this addon.
 * <p>
 * Besides being a Mekanism configurable machine, it makes Mekanism's own configuration card usable across machines:
 * Mekanism only lets a card move between blocks of the same type, so a card taken from a basic tier could not be
 * applied to the elite tier of the same machine - and a card taken from any Mekanism machine could not be applied to a
 * machine of this addon at all. Every machine here accepts a card recorded from any Mekanism-style machine block; the
 * recorded side configuration is keyed by transmission type, so a machine only takes the parts it actually has.
 */
public abstract class GizmoMachineBlockEntity extends TileEntityConfigurableMachine {

    /** Neighbour caches for the energy output, rebuilt when the facing or the configured output sides change. */
    private List<BlockEnergyCapabilityCache> energyOutputCaches;
    private Direction energyOutputDirection;
    private int energyOutputMask = -1;

    protected GizmoMachineBlockEntity(Holder<Block> block, BlockPos pos, BlockState state) {
        super(block, pos, state);
    }

    /**
     * Pushes buffered energy into every side that is configured as an energy output. Mekanism's energy network only
     * distributes energy that was inserted into it; it never pulls from an adjacent generator, so generators have to
     * use the same push path as Mekanism's own {@code TileEntityGenerator}.
     */
    protected final void emitEnergy(IEnergyContainer container, long maxOutput) {
        if (maxOutput <= 0 || container.isEmpty()) {
            return;
        }
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        ConfigInfo energyConfig = getConfig().getConfig(TransmissionType.ENERGY);
        if (energyConfig == null) {
            return;
        }
        Direction facing = getDirection();
        if (facing == null) {
            return;
        }
        int mask = 0;
        for (RelativeSide side : RelativeSide.values()) {
            DataType type = energyConfig.getDataType(side);
            if (type != null && type.canOutput() && energyConfig.isSideEnabled(side)) {
                mask |= 1 << side.ordinal();
            }
        }
        if (mask == 0) {
            return;
        }
        if (energyOutputCaches == null || energyOutputDirection != facing || energyOutputMask != mask) {
            List<BlockEnergyCapabilityCache> caches = new ArrayList<>(Integer.bitCount(mask));
            for (RelativeSide side : RelativeSide.values()) {
                if ((mask & (1 << side.ordinal())) == 0) {
                    continue;
                }
                Direction direction = side.getDirection(facing);
                caches.add(BlockEnergyCapabilityCache.create(serverLevel,
                      getBlockPos().relative(direction), direction.getOpposite()));
            }
            energyOutputCaches = caches;
            energyOutputDirection = facing;
            energyOutputMask = mask;
        }
        CableUtils.emit(energyOutputCaches, container, maxOutput);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        energyOutputCaches = null;
        energyOutputMask = -1;
    }

    @Override
    protected void invalidateDirectionCaches(Direction direction) {
        super.invalidateDirectionCaches(direction);
        energyOutputCaches = null;
    }

    @Override
    public boolean isConfigurationDataCompatible(Block block) {
        return getConfigurationDataType() == block || block instanceof BlockTile<?, ?>;
    }
}
