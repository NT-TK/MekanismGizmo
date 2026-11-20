package jp.example.mekanismgizmo.block;

import jp.example.mekanismgizmo.registry.ModBlockTypes;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import net.minecraft.world.level.material.MapColor;

public class ItemCollectorBlock extends BlockTile.BlockTileModel<ItemCollectorBlockEntity, Machine<ItemCollectorBlockEntity>> {
    public ItemCollectorBlock(Machine<ItemCollectorBlockEntity> type) {
        super(type, properties -> properties.mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops());
    }
}
