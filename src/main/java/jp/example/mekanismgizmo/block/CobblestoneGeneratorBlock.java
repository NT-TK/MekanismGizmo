package jp.example.mekanismgizmo.block;

import jp.example.mekanismgizmo.util.MachineTier;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import net.minecraft.world.level.material.MapColor;

public class CobblestoneGeneratorBlock extends BlockTile.BlockTileModel<CobblestoneGeneratorBlockEntity, Machine<CobblestoneGeneratorBlockEntity>> implements TieredMachine {
    private final MachineTier tier;

    public CobblestoneGeneratorBlock(Machine<CobblestoneGeneratorBlockEntity> type, MachineTier tier) {
        super(type, properties -> properties.mapColor(MapColor.STONE).strength(3.5F).requiresCorrectToolForDrops());
        this.tier = tier;
    }

    @Override
    public MachineTier getTier() {
        return tier;
    }
}
