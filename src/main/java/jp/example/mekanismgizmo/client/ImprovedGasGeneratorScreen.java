package jp.example.mekanismgizmo.client;

import jp.example.mekanismgizmo.block.ImprovedGasGeneratorBlockEntity;
import jp.example.mekanismgizmo.util.AmountFormat;
import java.util.List;
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
 * Screen of the improved gas generator: the gas tank, the player's energy items and two lines telling how good the fuel
 * is used right now (the combustion efficiency grows with the fill level of the tank).
 */
public class ImprovedGasGeneratorScreen extends GuiConfigurableTile<ImprovedGasGeneratorBlockEntity,
      MekanismTileContainer<ImprovedGasGeneratorBlockEntity>> {

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
        Component efficiency = Component.translatable("gui.mekanismgizmo.improved_gas_generator.efficiency",
              tile.getEfficiencyPercent());
        Component fuel = Component.translatable("gui.mekanismgizmo.improved_gas_generator.fuel",
              AmountFormat.format(tile.getSyncedEnergyPerMb()), AmountFormat.format(tile.getGenerationRate()));
        guiGraphics.drawString(font, efficiency, 46, ImprovedGasGeneratorBlockEntity.INFO_Y, 0x404040, false);
        guiGraphics.drawString(font, fuel, 46, ImprovedGasGeneratorBlockEntity.INFO_Y + 12, 0x404040, false);
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
                    AmountFormat.format(tile.getGenerationRate())));
    }
}
