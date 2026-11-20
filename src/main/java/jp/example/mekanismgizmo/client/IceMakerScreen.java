package jp.example.mekanismgizmo.client;

import jp.example.mekanismgizmo.block.IceMakerBlockEntity;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class IceMakerScreen extends GuiConfigurableTile<IceMakerBlockEntity, MekanismTileContainer<IceMakerBlockEntity>> {
    /** Same place Mekanism puts the arrow of its electric machines. */
    public static final int ARROW_X = 54;
    public static final int ARROW_Y = 40;

    public IceMakerScreen(MekanismTileContainer<IceMakerBlockEntity> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        // Every tier adds one ice output slot to the right
        imageWidth = IceMakerBlockEntity.guiWidth(tile.parallel());
    }

    @Override
    protected void init() {
        // The screen width depends on the tier, so it has to be set before the labels are positioned
        imageWidth = IceMakerBlockEntity.guiWidth(tile.parallel());
        super.init();
        titleLabelY = 6;
        inventoryLabelX = IceMakerBlockEntity.inventoryXOffset(tile.parallel());
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
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164 + (tile.parallel() - 1) * 18, 16))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> tile.getFreezeTicks() > 0
                    && tile.getEnergyContainer().getEnergy() < tile.getEnergyContainer().getEnergyPerTick());
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        addRenderableWidget(new GuiFluidGauge(() -> tile.getWaterTank(), () -> tile.getFluidTanks(null),
              GaugeType.STANDARD, this, 6, 13))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile::isOutputBlocked);
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.RIGHT, this, ARROW_X, ARROW_Y))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile::isOutputBlocked);
    }
}
