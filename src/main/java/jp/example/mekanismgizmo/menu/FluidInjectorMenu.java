package jp.example.mekanismgizmo.menu;

import jp.example.mekanismgizmo.block.FluidInjectorBlockEntity;
import jp.example.mekanismgizmo.registry.ModMenus;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;

public class FluidInjectorMenu extends MekanismTileContainer<FluidInjectorBlockEntity> {
    /** Button id of the Fill / Empty / Both mode button. */
    public static final int CONTAINER_MODE_BUTTON = 1000;

    public FluidInjectorMenu(int id, Inventory inv, FluidInjectorBlockEntity tile) {
        super(ModMenus.FLUID_INJECTOR, id, inv, tile);
    }

    @Override
    protected int getInventoryYOffset() {
        // One extra container slot pair (18 pixels) per tier
        return FluidInjectorBlockEntity.inventoryYOffset(getTileEntity().parallel());
    }

    @Override
    public boolean clickMenuButton(net.minecraft.world.entity.player.Player player, int id) {
        if (id == CONTAINER_MODE_BUTTON) {
            getTileEntity().cycleContainerMode();
            return true;
        }
        return super.clickMenuButton(player, id);
    }
}
