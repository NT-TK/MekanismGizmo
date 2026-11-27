package jp.example.mekanismgizmo.client;

import jp.example.mekanismgizmo.block.CobblestoneGeneratorBlockEntity;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CobblestoneGeneratorScreen extends GuiConfigurableTile<CobblestoneGeneratorBlockEntity, MekanismTileContainer<CobblestoneGeneratorBlockEntity>> {
    public static final int ARROW_X = 54;
    public static final int ARROW_Y = 40;

    public CobblestoneGeneratorScreen(MekanismTileContainer<CobblestoneGeneratorBlockEntity> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        // Every tier adds one cobblestone output slot to the right
        imageWidth = CobblestoneGeneratorBlockEntity.guiWidth(tile.parallel());
    }

    @Override
    protected void init() {
        imageWidth = CobblestoneGeneratorBlockEntity.guiWidth(tile.parallel());
        super.init();
        titleLabelY = 6;
        inventoryLabelX = CobblestoneGeneratorBlockEntity.inventoryXOffset(tile.parallel());
        inventoryLabelY = 84 - 12;
    }

    @Override
    protected void drawForegroundText(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164 + (tile.parallel() - 1) * 18, 16))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> tile.getProgress() > 0
                    && tile.getEnergyContainer().getEnergy().smallerThan(tile.getEnergyContainer().getEnergyPerTick()));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.RIGHT, this, ARROW_X, ARROW_Y))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile::isOutputBlocked);
    }
}
