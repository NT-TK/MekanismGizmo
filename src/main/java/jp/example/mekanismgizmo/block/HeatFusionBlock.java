package jp.example.mekanismgizmo.block;

import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import net.minecraft.world.level.material.MapColor;

public class HeatFusionBlock extends BlockTile.BlockTileModel<HeatFusionBlockEntity, Machine<HeatFusionBlockEntity>> {

    public HeatFusionBlock(Machine<HeatFusionBlockEntity> type) {
        super(type, properties -> properties.mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops());
    }
}
