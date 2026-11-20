package jp.example.mekanismgizmo.menu;

import jp.example.mekanismgizmo.block.MachineResetterBlockEntity;
import jp.example.mekanismgizmo.registry.ModMenus;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;

public class MachineResetterMenu extends MekanismTileContainer<MachineResetterBlockEntity> {

    public MachineResetterMenu(int id, Inventory inv, MachineResetterBlockEntity tile) {
        super(ModMenus.MACHINE_RESETTER, id, inv, tile);
    }
}
