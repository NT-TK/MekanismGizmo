package jp.example.mekanismgizmo.block;

import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import net.minecraft.world.level.material.MapColor;

public class ImprovedGasGeneratorBlock extends BlockTile.BlockTileModel<ImprovedGasGeneratorBlockEntity, Machine<ImprovedGasGeneratorBlockEntity>> {

    public ImprovedGasGeneratorBlock(Machine<ImprovedGasGeneratorBlockEntity> type) {
        super(type, properties -> properties.mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops());
    }
}
