package jp.example.mekanismgizmo.menu;

import jp.example.mekanismgizmo.block.ImprovedGasGeneratorBlockEntity;
import jp.example.mekanismgizmo.registry.ModMenus;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;

public class ImprovedGasGeneratorMenu extends MekanismTileContainer<ImprovedGasGeneratorBlockEntity> {
    public ImprovedGasGeneratorMenu(int id, Inventory inv, ImprovedGasGeneratorBlockEntity tile) {
        super(ModMenus.IMPROVED_GAS_GENERATOR, id, inv, tile);
    }
}
