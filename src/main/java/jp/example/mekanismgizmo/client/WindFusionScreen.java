package jp.example.mekanismgizmo.client;

import jp.example.mekanismgizmo.block.WindFusionBlockEntity;
import java.util.List;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen of the wind generator fusion machine: a 3x9 grid of wind generator slots, the energy bar, Mekanism's energy
 * tab and a line telling how many generators are fused and how much power that is right now.
 */
public class WindFusionScreen extends GuiConfigurableTile<WindFusionBlockEntity, MekanismTileContainer<WindFusionBlockEntity>> {

    public WindFusionScreen(MekanismTileContainer<WindFusionBlockEntity> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageWidth = WindFusionBlockEntity.GUI_WIDTH;
        imageHeight = WindFusionBlockEntity.GUI_HEIGHT;
    }

    @Override
    protected void init() {
        imageWidth = WindFusionBlockEntity.GUI_WIDTH;
        imageHeight = WindFusionBlockEntity.GUI_HEIGHT;
        super.init();
        titleLabelY = 6;
        inventoryLabelX = WindFusionBlockEntity.INVENTORY_X;
        inventoryLabelY = WindFusionBlockEntity.INVENTORY_Y - 12;
    }

    @Override
    protected void drawForegroundText(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(font, Component.translatable("gui.mekanismgizmo.wind_fusion.info",
              tile.getActiveGenerators(), WindFusionBlockEntity.SLOT_COUNT,
              (long) tile.getActiveGenerators() * tile.getGenerationPerGeneratorSynced()),
              WindFusionBlockEntity.SLOT_X, WindFusionBlockEntity.INFO_Y, 0x404040, false);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(),
              WindFusionBlockEntity.POWER_BAR_X, WindFusionBlockEntity.POWER_BAR_Y));
        // Mekanism's energy tab works with a machine energy container, so this machine describes its own buffer
        addRenderableWidget(new GuiEnergyTab(this, this::getEnergyInfo));
    }

    private List<Component> getEnergyInfo() {
        return List.of(
              Component.translatable("gui.mekanismgizmo.wind_fusion.energy",
                    tile.getEnergyContainer().getEnergy(), WindFusionBlockEntity.ENERGY_CAPACITY),
              Component.translatable("gui.mekanismgizmo.wind_fusion.output",
                    (long) tile.getActiveGenerators() * tile.getGenerationPerGeneratorSynced()));
    }
}
