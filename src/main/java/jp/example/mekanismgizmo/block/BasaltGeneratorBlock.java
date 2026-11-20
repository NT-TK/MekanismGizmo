package jp.example.mekanismgizmo.block;

import jp.example.mekanismgizmo.util.MachineTier;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import net.minecraft.world.level.material.MapColor;

public class BasaltGeneratorBlock extends BlockTile.BlockTileModel<BasaltGeneratorBlockEntity, Machine<BasaltGeneratorBlockEntity>> implements TieredMachine {
    private final MachineTier tier;

    public BasaltGeneratorBlock(Machine<BasaltGeneratorBlockEntity> type, MachineTier tier) {
        super(type, properties -> properties.mapColor(MapColor.COLOR_BLACK).strength(3.5F).requiresCorrectToolForDrops());
        this.tier = tier;
    }

    @Override
    public MachineTier getTier() {
        return tier;
    }
}
