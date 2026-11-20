package jp.example.mekanismgizmo.menu;

import jp.example.mekanismgizmo.block.ItemCollectorBlockEntity;
import jp.example.mekanismgizmo.registry.ModMenus;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * Container of the item collector. The owner of the settings is the block entity; the GUI sends the slider
 * changes as vanilla container button clicks (no custom packet needed), and Mekanism's container trackers send
 * the resulting values back to the client.
 */
public class ItemCollectorMenu extends MekanismTileContainer<ItemCollectorBlockEntity> {
    private static final int BUTTON_OFFSET = 32;
    private static final int BUTTONS_PER_SETTING = 64;
    /** Button id of the "show collection area" toggle (the slider ids are below this). */
    public static final int PREVIEW_BUTTON = 1000;

    public ItemCollectorMenu(int id, Inventory inv, ItemCollectorBlockEntity tile) {
        super(ModMenus.ITEM_COLLECTOR, id, inv, tile);
    }

    @Override
    protected int getInventoryYOffset() {
        // The machine screen is 280 pixels tall (3x9 slot grid and settings area above the player inventory)
        return 200;
    }

    @Override
    protected int getInventoryXOffset() {
        // The machine screen is 200 pixels wide; center the 162 pixel player inventory
        return 19;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == PREVIEW_BUTTON) {
            getTileEntity().toggleRenderArea();
            return true;
        }
        int index = id / BUTTONS_PER_SETTING;
        int value = id % BUTTONS_PER_SETTING - BUTTON_OFFSET;
        if (index >= 0 && index < ItemCollectorBlockEntity.SETTING_COUNT) {
            getTileEntity().setSetting(index, value);
            return true;
        }
        return false;
    }

    /** Encodes a setting change for {@link net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket}. */
    public static int encodeButton(int index, int value) {
        return index * BUTTONS_PER_SETTING + value + BUTTON_OFFSET;
    }
}
