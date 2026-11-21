package jp.example.mekanismgizmo.menu;

import jp.example.mekanismgizmo.block.HeatFusionBlockEntity;
import jp.example.mekanismgizmo.registry.ModMenus;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;

public class HeatFusionMenu extends MekanismTileContainer<HeatFusionBlockEntity> {

    public HeatFusionMenu(int id, Inventory inv, HeatFusionBlockEntity tile) {
        super(ModMenus.HEAT_FUSION, id, inv, tile);
    }

    @Override
    protected int getInventoryXOffset() {
        return HeatFusionBlockEntity.INVENTORY_X;
    }

    @Override
    protected int getInventoryYOffset() {
        return HeatFusionBlockEntity.INVENTORY_Y;
    }
}
