package jp.example.mekanismgizmo.client;

import jp.example.mekanismgizmo.block.PotionBrewerBlockEntity;
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
 * Mekanism's configurable machine screen. Side configuration, the upgrade window, redstone control,
 * security, the energy tab and the FE/J readout are all Mekanism's own GUI elements; this class only adds
 * the machine specific bars and the progress arrows.
 * <p>
 * One brewing row (three bottles in, three potions out) is one parallel unit, so every row gets its own arrow.
 */
public class PotionBrewerScreen extends GuiConfigurableTile<PotionBrewerBlockEntity, MekanismTileContainer<PotionBrewerBlockEntity>> {
    public PotionBrewerScreen(MekanismTileContainer<PotionBrewerBlockEntity> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageWidth = PotionBrewerBlockEntity.guiWidth(tile.bottleCount());
        imageHeight = PotionBrewerBlockEntity.guiHeight(tile.bottleCount());
    }

    @Override
    protected void init() {
        // Higher tiers add another row of three bottle slots, so the screen grows downwards
        int bottles = tile.bottleCount();
        imageWidth = PotionBrewerBlockEntity.guiWidth(bottles);
        imageHeight = PotionBrewerBlockEntity.guiHeight(bottles);
        super.init();
        titleLabelY = 6;
        inventoryLabelX = PotionBrewerBlockEntity.inventoryXOffset(bottles);
        inventoryLabelY = PotionBrewerBlockEntity.inventoryYOffset(bottles) - 12;
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
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), PotionBrewerBlockEntity.GUI_WIDTH - 12, 16))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> tile.getBrewTicks() > 0
                    && tile.getEnergyContainer().getEnergy() < tile.getEnergyContainer().getEnergyPerTick());
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.blazePowderTank, () -> tile.getChemicalTanks(null),
              GaugeType.STANDARD, this, 6, 13));
        for (int row = 0; row < PotionBrewerBlockEntity.rows(tile.bottleCount()); row++) {
            addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.RIGHT, this,
                  PotionBrewerBlockEntity.ARROW_X,
                  PotionBrewerBlockEntity.FIRST_ROW_Y + row * 18 + 4))
                  .warning(WarningType.NO_SPACE_IN_OUTPUT, tile::isOutputBlocked);
        }
    }
}
