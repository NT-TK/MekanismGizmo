package jp.example.mekanismgizmo.block;

import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import net.minecraft.world.level.material.MapColor;

public class WindFusionBlock extends BlockTile.BlockTileModel<WindFusionBlockEntity, Machine<WindFusionBlockEntity>> {

    public WindFusionBlock(Machine<WindFusionBlockEntity> type) {
        super(type, properties -> properties.mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops());
    }
}
