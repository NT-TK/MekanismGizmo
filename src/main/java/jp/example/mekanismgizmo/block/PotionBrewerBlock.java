package jp.example.mekanismgizmo.block;

import jp.example.mekanismgizmo.util.MachineTier;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import net.minecraft.world.level.material.MapColor;

public class PotionBrewerBlock extends BlockTile.BlockTileModel<PotionBrewerBlockEntity, Machine<PotionBrewerBlockEntity>> implements TieredMachine {
    private final MachineTier tier;

    public PotionBrewerBlock(Machine<PotionBrewerBlockEntity> type, MachineTier tier) {
        super(type, properties -> properties.mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops());
        this.tier = tier;
    }

    @Override
    public MachineTier getTier() {
        return tier;
    }
}
