package jp.example.mekanismgizmo.menu;

import jp.example.mekanismgizmo.block.CobblestoneGeneratorBlockEntity;
import jp.example.mekanismgizmo.registry.ModMenus;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;

public class CobblestoneGeneratorMenu extends MekanismTileContainer<CobblestoneGeneratorBlockEntity> {
    public CobblestoneGeneratorMenu(int id, Inventory inv, CobblestoneGeneratorBlockEntity tile) {
        super(ModMenus.COBBLESTONE_GENERATOR, id, inv, tile);
    }

    @Override
    protected int getInventoryXOffset() {
        // The screen grows by one output slot (18 pixels) per tier; keep the player inventory centred
        return 8 + (getTileEntity().parallel() - 1) * 9;
    }
}
