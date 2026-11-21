package jp.example.mekanismgizmo.client;

import java.util.List;
import jp.example.mekanismgizmo.block.HeatFusionBlockEntity;
import jp.example.mekanismgizmo.util.AmountFormat;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen of the heat generator fusion machine: a 3x9 grid of heat generator slots, the lava tank, the energy bar,
 * Mekanism's energy tab and a line telling how many generators are fused and how much power they produce right now.
 */
public class HeatFusionScreen extends GuiConfigurableTile<HeatFusionBlockEntity, MekanismTileContainer<HeatFusionBlockEntity>> {

    public static final int INFO_X = 8;
    public static final int INFO_WIDTH = HeatFusionBlockEntity.MEASURE_WIDTH;

    public HeatFusionScreen(MekanismTileContainer<HeatFusionBlockEntity> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageWidth = HeatFusionBlockEntity.GUI_WIDTH;
        imageHeight = HeatFusionBlockEntity.GUI_HEIGHT;
    }

    @Override
    protected void init() {
        imageWidth = HeatFusionBlockEntity.GUI_WIDTH;
        imageHeight = HeatFusionBlockEntity.GUI_HEIGHT;
        super.init();
        titleLabelY = 6;
        inventoryLabelX = HeatFusionBlockEntity.INVENTORY_X;
        inventoryLabelY = HeatFusionBlockEntity.INVENTORY_Y - 12;
    }

    @Override
    protected void drawForegroundText(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
        Component info = Component.translatable("gui.mekanismgizmo.heat_fusion.info",
              AmountFormat.format(tile.getActiveGenerators()), AmountFormat.format(tile.getMaxGenerators()),
              AmountFormat.format(tile.getGenerationRate()));
        int width = font.width(info);
        if (width > INFO_WIDTH) {
            float scale = (float) INFO_WIDTH / width;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, 1F);
            guiGraphics.drawString(font, info, Math.round(INFO_X / scale), Math.round(HeatFusionBlockEntity.INFO_Y / scale),
                  0x404040, false);
            guiGraphics.pose().popPose();
        } else {
            guiGraphics.drawString(font, info, INFO_X, HeatFusionBlockEntity.INFO_Y, 0x404040, false);
        }
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(),
              HeatFusionBlockEntity.POWER_BAR_X, HeatFusionBlockEntity.POWER_BAR_Y));
        addRenderableWidget(new GuiEnergyTab(this, this::getEnergyInfo));
        addRenderableWidget(new GuiFluidGauge(() -> tile.getLavaTank(), () -> tile.getFluidTanks(null),
              GaugeType.STANDARD, this, HeatFusionBlockEntity.LAVA_GAUGE_X, HeatFusionBlockEntity.LAVA_GAUGE_Y));
    }

    private List<Component> getEnergyInfo() {
        return List.of(
              Component.translatable("gui.mekanismgizmo.heat_fusion.energy",
                    AmountFormat.format(tile.getEnergyContainer().getEnergy()), AmountFormat.format(HeatFusionBlockEntity.ENERGY_CAPACITY)),
              Component.translatable("gui.mekanismgizmo.heat_fusion.output", AmountFormat.format(tile.getGenerationRate())),
              Component.translatable("gui.mekanismgizmo.heat_fusion.temperature",
                    String.format(java.util.Locale.ROOT, "%.1f", tile.getTemperatureTenths() / 10D)));
    }
}
