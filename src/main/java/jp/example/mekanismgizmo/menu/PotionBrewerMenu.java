package jp.example.mekanismgizmo.menu;

import jp.example.mekanismgizmo.block.PotionBrewerBlockEntity;
import jp.example.mekanismgizmo.registry.ModMenus;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;

public class PotionBrewerMenu extends MekanismTileContainer<PotionBrewerBlockEntity> {
    public PotionBrewerMenu(int id, Inventory inv, PotionBrewerBlockEntity tile) {
        super(ModMenus.POTION_BREWER, id, inv, tile);
    }

    @Override
    protected int getInventoryXOffset() {
        // The screen is 208 pixels wide, so the player inventory is centred instead of starting at 8
        return PotionBrewerBlockEntity.inventoryXOffset(getTileEntity().bottleCount());
    }

    @Override
    protected int getInventoryYOffset() {
        // Tiers above basic add another group of three bottle slots, so the screen grows downwards
        return PotionBrewerBlockEntity.inventoryYOffset(getTileEntity().bottleCount());
    }
}
