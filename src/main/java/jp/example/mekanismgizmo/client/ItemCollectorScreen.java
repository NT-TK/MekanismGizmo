package jp.example.mekanismgizmo.client;

import java.util.Arrays;
import jp.example.mekanismgizmo.block.ItemCollectorBlockEntity;
import jp.example.mekanismgizmo.menu.ItemCollectorMenu;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiSlider;
import mekanism.client.gui.element.button.ToggleButton;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen of the item collector. It uses Mekanism's own configurable machine screen (side config, upgrades,
 * security, redstone, energy tab) and adds, for every setting, a {@link GuiSlider} and a Mekanism digital
 * {@link GuiTextField} so the value can either be dragged or typed in directly.
 */
public class ItemCollectorScreen extends GuiConfigurableTile<ItemCollectorBlockEntity, MekanismTileContainer<ItemCollectorBlockEntity>> {

    private static final String[] LABEL_KEYS = {
          "gui.mekanismgizmo.item_collector.range_x",
          "gui.mekanismgizmo.item_collector.range_z",
          "gui.mekanismgizmo.item_collector.range_y",
          "gui.mekanismgizmo.item_collector.offset_x",
          "gui.mekanismgizmo.item_collector.offset_z",
          "gui.mekanismgizmo.item_collector.offset_y"
    };
    private static final int[] MIN_VALUES = {ItemCollectorBlockEntity.MIN_RANGE, ItemCollectorBlockEntity.MIN_RANGE, ItemCollectorBlockEntity.MIN_RANGE,
          ItemCollectorBlockEntity.MIN_OFFSET, ItemCollectorBlockEntity.MIN_OFFSET, ItemCollectorBlockEntity.MIN_OFFSET};
    private static final int[] MAX_VALUES = {ItemCollectorBlockEntity.MAX_RANGE, ItemCollectorBlockEntity.MAX_RANGE, ItemCollectorBlockEntity.MAX_RANGE,
          ItemCollectorBlockEntity.MAX_OFFSET, ItemCollectorBlockEntity.MAX_OFFSET, ItemCollectorBlockEntity.MAX_OFFSET};

    private static final int SLIDER_X = 84;
    private static final int SLIDER_WIDTH = 108;
    private static final int FIELD_X = 44;
    private static final int FIELD_WIDTH = 36;
    private static final int FIELD_HEIGHT = 11;
    private static final int ROW_Y = 100;
    private static final int ROW_SPACING = 15;
    private static final int LABEL_X = 8;

    private final GuiSlider[] sliders = new GuiSlider[ItemCollectorBlockEntity.SETTING_COUNT];
    private final GuiTextField[] fields = new GuiTextField[ItemCollectorBlockEntity.SETTING_COUNT];
    private final int[] pendingValues = new int[ItemCollectorBlockEntity.SETTING_COUNT];
    private final boolean[] pending = new boolean[ItemCollectorBlockEntity.SETTING_COUNT];

    public ItemCollectorScreen(MekanismTileContainer<ItemCollectorBlockEntity> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        // Wider (to fit the numeric text fields) and taller (settings area above the player inventory)
        imageWidth = 200;
        imageHeight = 280;
        Arrays.fill(pendingValues, Integer.MIN_VALUE);
    }

    @Override
    protected void init() {
        // The screen is larger than vanilla's default, so the labels have to be positioned after the resize
        imageWidth = 200;
        imageHeight = 280;
        super.init();
        titleLabelY = 6;
        inventoryLabelX = LABEL_X;
        inventoryLabelY = 200 - 12;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        // Same switch style as Mekanism's own machine toggles: shows the collection box in the world
        addRenderableWidget(new ToggleButton(this, 8, 78, tile::isRenderArea, (element, mouseX, mouseY) -> {
            sendPreviewToggle();
            return true;
        }));
        for (int i = 0; i < ItemCollectorBlockEntity.SETTING_COUNT; i++) {
            final int index = i;
            int y = ROW_Y + i * ROW_SPACING;
            GuiTextField field = new GuiTextField(this, FIELD_X, y - 1, FIELD_WIDTH, FIELD_HEIGHT);
            field.setMaxLength(3);
            field.setInputValidator(c -> c >= '0' && c <= '9' || c == '-');
            field.setText(Integer.toString(tile.getSetting(i)));
            field.configureDigitalBorderInput(() -> commitField(index));
            fields[i] = field;
            addRenderableWidget(field);

            GuiSlider slider = new GuiSlider(this, SLIDER_X, y, SLIDER_WIDTH, value -> onSliderChanged(index, value));
            slider.setValue(toRatio(i, tile.getSetting(i)));
            sliders[i] = slider;
            addRenderableWidget(slider);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        for (int i = 0; i < sliders.length; i++) {
            GuiSlider slider = sliders[i];
            if (slider == null) {
                continue;
            }
            if (pending[i] && tile.getSetting(i) == pendingValues[i]) {
                // The server accepted our change
                pending[i] = false;
            }
            if (!pending[i] && !slider.isDragging()) {
                slider.setValue(toRatio(i, tile.getSetting(i)));
            }
            GuiTextField field = fields[i];
            if (field != null && !field.isTextFieldFocused()) {
                String text = Integer.toString(tile.getSetting(i));
                if (!text.equals(field.getText())) {
                    field.setText(text);
                }
            }
        }
    }

    @Override
    public void removed() {
        // Commit text that was typed but not confirmed with enter/checkmark
        if (fields[0] != null) {
            for (int i = 0; i < fields.length; i++) {
                commitField(i);
            }
        }
        super.removed();
    }

    private void commitField(int index) {
        GuiTextField field = fields[index];
        if (field == null) {
            return;
        }
        String text = field.getText().trim();
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            // Not a number: restore the current value
            field.setText(Integer.toString(tile.getSetting(index)));
            return;
        }
        int clamped = Mth.clamp(value, MIN_VALUES[index], MAX_VALUES[index]);
        field.setText(Integer.toString(clamped));
        sendSetting(index, clamped);
    }

    private void sendPreviewToggle() {
        if (minecraft != null && minecraft.getConnection() != null) {
            minecraft.getConnection().send(new ServerboundContainerButtonClickPacket(menu.containerId,
                  ItemCollectorMenu.PREVIEW_BUTTON));
        }
    }

    private void onSliderChanged(int index, double ratio) {
        sendSetting(index, fromRatio(index, ratio));
    }

    private void sendSetting(int index, int value) {
        if (!pending[index]) {
            if (value == tile.getSetting(index)) {
                return;
            }
        } else if (value == pendingValues[index]) {
            return;
        }
        if (minecraft != null && minecraft.getConnection() != null) {
            minecraft.getConnection().send(new ServerboundContainerButtonClickPacket(menu.containerId,
                  ItemCollectorMenu.encodeButton(index, value)));
            pendingValues[index] = value;
            pending[index] = true;
        }
    }

    private static double toRatio(int index, int value) {
        int min = MIN_VALUES[index];
        int max = MAX_VALUES[index];
        return max == min ? 0 : (double) (Mth.clamp(value, min, max) - min) / (max - min);
    }

    private static int fromRatio(int index, double ratio) {
        int min = MIN_VALUES[index];
        int max = MAX_VALUES[index];
        return min + (int) Math.round(Mth.clamp(ratio, 0, 1) * (max - min));
    }

    @Override
    protected void drawForegroundText(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(font, Component.translatable("gui.mekanismgizmo.item_collector.preview"),
              28, 82, 0x404040, false);
        // Smaller text so the labels fit between the slots and the text fields
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.75F, 0.75F, 1.0F);
        for (int i = 0; i < sliders.length; i++) {
            guiGraphics.drawString(font, Component.translatable(LABEL_KEYS[i], tile.getSetting(i)),
                  (int) (LABEL_X / 0.75F), (int) ((ROW_Y + i * ROW_SPACING + 2) / 0.75F), 0x404040, false);
        }
        guiGraphics.pose().popPose();
    }
}
