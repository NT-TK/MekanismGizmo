package jp.example.mekanismgizmo.client;

import java.util.List;
import jp.example.mekanismgizmo.block.ImprovedGasGeneratorBlockEntity;
import jp.example.mekanismgizmo.util.AmountFormat;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen of the improved gas generator: the gas tank, the player's energy items and two lines with real numbers - the
 * FE per mB Mekanism's own gas generator gives the stored fuel and what the machine currently gets out of it (the
 * combustion efficiency scales with the fill level of the tank).
 */
public class ImprovedGasGeneratorScreen extends GuiConfigurableTile<ImprovedGasGeneratorBlockEntity,
      MekanismTileContainer<ImprovedGasGeneratorBlockEntity>> {

    /** Left edge and width of the two info lines; the power bar starts right of them. */
    public static final int INFO_X = 46;
    public static final int INFO_WIDTH = ImprovedGasGeneratorBlockEntity.POWER_BAR_X - INFO_X - 4;

    public ImprovedGasGeneratorScreen(MekanismTileContainer<ImprovedGasGeneratorBlockEntity> container, Inventory inv, Component title) {
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
        // Both lines are real numbers, the same way a gas generator's own screen reports them: the burn rate in mB/t
        // (100 Mekanism gas generators' worth at a full tank) and the energy that produces
        drawScaled(guiGraphics, Component.translatable("gui.mekanismgizmo.improved_gas_generator.burn_rate",
              ImprovedGasGeneratorBlockEntity.formatBurnRate(tile.getBurnRateHundredths())),
              ImprovedGasGeneratorBlockEntity.INFO_Y);
        drawScaled(guiGraphics, Component.translatable("gui.mekanismgizmo.improved_gas_generator.output",
              AmountFormat.format(tile.getGenerationRate()), AmountFormat.format(tile.getSyncedEnergyPerMb())),
              ImprovedGasGeneratorBlockEntity.INFO_Y + 12);
    }

    /** Draws a line at {@link #INFO_X}, scaled down when a translation or a big number would run into the power bar. */
    private void drawScaled(GuiGraphics guiGraphics, Component text, int y) {
        int width = font.width(text);
        if (width > INFO_WIDTH) {
            float scale = (float) INFO_WIDTH / width;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, 1F);
            guiGraphics.drawString(font, text, Math.round(INFO_X / scale), Math.round(y / scale), 0x404040, false);
            guiGraphics.pose().popPose();
        } else {
            guiGraphics.drawString(font, text, INFO_X, y, 0x404040, false);
        }
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(),
              ImprovedGasGeneratorBlockEntity.POWER_BAR_X, ImprovedGasGeneratorBlockEntity.POWER_BAR_Y));
        addRenderableWidget(new GuiEnergyTab(this, this::getEnergyInfo));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.getGasTank(), () -> tile.getChemicalTanks(null),
              GaugeType.STANDARD, this, ImprovedGasGeneratorBlockEntity.GAUGE_X, ImprovedGasGeneratorBlockEntity.GAUGE_Y));
    }

    private List<Component> getEnergyInfo() {
        return List.of(
              Component.translatable("gui.mekanismgizmo.improved_gas_generator.energy",
                    AmountFormat.format(tile.getEnergyContainer().getEnergy()),
                    AmountFormat.format(ImprovedGasGeneratorBlockEntity.ENERGY_CAPACITY)),
              Component.translatable("gui.mekanismgizmo.improved_gas_generator.output",
                    AmountFormat.format(tile.getGenerationRate()), AmountFormat.format(tile.getSyncedEnergyPerMb())));
    }
}
