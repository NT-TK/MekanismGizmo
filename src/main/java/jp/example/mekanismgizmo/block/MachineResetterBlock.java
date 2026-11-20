package jp.example.mekanismgizmo.block;

import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import net.minecraft.world.level.material.MapColor;

public class MachineResetterBlock extends BlockTile.BlockTileModel<MachineResetterBlockEntity, Machine<MachineResetterBlockEntity>> {

    public MachineResetterBlock(Machine<MachineResetterBlockEntity> type) {
        super(type, properties -> properties.mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops());
    }
}
