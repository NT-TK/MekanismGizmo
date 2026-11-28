package jp.example.mekanismgizmo.block;

import java.util.EnumSet;
import java.util.Set;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.CableUtils;
import jp.example.mekanismgizmo.util.MachineSideDefaults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Common base of every machine of this addon.
 */
public abstract class GizmoMachineBlockEntity extends TileEntityConfigurableMachine {

    protected GizmoMachineBlockEntity(IBlockProvider block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        configComponent = new TileComponentConfig(this, TransmissionType.values());
        ejectorComponent = new TileComponentEjector(this);
    }

    /**
     * Applies this machine's default side layout. Subclasses call this at the very end of their constructor, after
     * their own {@code setup...Config} calls, because those calls reset the side layout to Mekanism's generic defaults.
     * A layout that was saved in the world is read from NBT after construction and replaces these defaults.
     */
    protected final void applySideDefaults() {
        MachineSideDefaults.apply(configComponent, ForgeRegistries.BLOCKS.getKey(getBlockType()));
    }

    /**
     * Pushes buffered energy into every side that is configured as an energy output. Mekanism's energy network only
     * distributes energy that was inserted into it; it never pulls from an adjacent generator, so generators have to
     * use the same push path as Mekanism's own {@code TileEntityGenerator}.
     */
    protected final void emitEnergy(IEnergyContainer container, FloatingLong maxOutput) {
        if (maxOutput.isZero() || container.isEmpty() || !(getLevel() instanceof ServerLevel)) {
            return;
        }
        ConfigInfo energyConfig = getConfig() == null ? null : getConfig().getConfig(TransmissionType.ENERGY);
        Direction facing = getDirection();
        if (energyConfig == null || facing == null) {
            return;
        }
        Set<Direction> outputSides = EnumSet.noneOf(Direction.class);
        for (RelativeSide side : RelativeSide.values()) {
            DataType type = energyConfig.getDataType(side);
            if (type != null && type.canOutput() && energyConfig.isSideEnabled(side)) {
                outputSides.add(side.getDirection(facing));
            }
        }
        if (!outputSides.isEmpty()) {
            CableUtils.emit(outputSides, container, this, maxOutput);
        }
    }
}