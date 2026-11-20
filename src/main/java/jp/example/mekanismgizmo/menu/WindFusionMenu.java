package jp.example.mekanismgizmo.menu;

import jp.example.mekanismgizmo.block.WindFusionBlockEntity;
import jp.example.mekanismgizmo.registry.ModMenus;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;

public class WindFusionMenu extends MekanismTileContainer<WindFusionBlockEntity> {

    public WindFusionMenu(int id, Inventory inv, WindFusionBlockEntity tile) {
        super(ModMenus.WIND_FUSION, id, inv, tile);
    }

    @Override
    protected int getInventoryXOffset() {
        // The screen is 208 pixels wide, so the player inventory is centred instead of starting at 8
        return WindFusionBlockEntity.INVENTORY_X;
    }

    @Override
    protected int getInventoryYOffset() {
        return WindFusionBlockEntity.INVENTORY_Y;
    }
}
