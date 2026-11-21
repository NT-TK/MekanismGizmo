package jp.example.mekanismgizmo.client;

import jp.example.mekanismgizmo.block.LiquidHeaterBlockEntity;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen of the liquid heater: the sodium tank on the left, the superheated sodium tank on the right and the progress
 * arrow of the conversion in between.
 */
public class LiquidHeaterScreen extends GuiConfigurableTile<LiquidHeaterBlockEntity, MekanismTileContainer<LiquidHeaterBlockEntity>> {

    public LiquidHeaterScreen(MekanismTileContainer<LiquidHeaterBlockEntity> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 84 - 12;
    }

    @Override
    protected void drawForegroundText(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(),
              LiquidHeaterBlockEntity.POWER_BAR_X, LiquidHeaterBlockEntity.POWER_BAR_Y))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> tile.getHeatTicks() > 0
                    && tile.getEnergyContainer().getEnergy() < tile.getEnergyContainer().getEnergyPerTick());
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.getSodiumTank(), () -> tile.getChemicalTanks(null),
              GaugeType.STANDARD, this, LiquidHeaterBlockEntity.SODIUM_GAUGE_X, LiquidHeaterBlockEntity.GAUGE_Y));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.getSuperheatedTank(), () -> tile.getChemicalTanks(null),
              GaugeType.STANDARD, this, LiquidHeaterBlockEntity.SUPERHEATED_GAUGE_X, LiquidHeaterBlockEntity.GAUGE_Y))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile::isOutputBlocked);
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.RIGHT, this,
              LiquidHeaterBlockEntity.ARROW_X, LiquidHeaterBlockEntity.ARROW_Y))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile::isOutputBlocked);
    }
}
