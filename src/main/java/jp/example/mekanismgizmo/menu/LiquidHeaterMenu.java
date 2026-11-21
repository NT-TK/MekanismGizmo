package jp.example.mekanismgizmo.menu;

import jp.example.mekanismgizmo.block.LiquidHeaterBlockEntity;
import jp.example.mekanismgizmo.registry.ModMenus;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;

public class LiquidHeaterMenu extends MekanismTileContainer<LiquidHeaterBlockEntity> {
    public LiquidHeaterMenu(int id, Inventory inv, LiquidHeaterBlockEntity tile) {
        super(ModMenus.LIQUID_HEATER, id, inv, tile);
    }
}
