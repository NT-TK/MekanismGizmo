package jp.example.mekanismgizmo.client;

import jp.example.mekanismgizmo.block.FluidInjectorBlockEntity;
import jp.example.mekanismgizmo.menu.FluidInjectorMenu;
import jp.example.mekanismgizmo.registry.ModLang;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.world.entity.player.Inventory;

public class FluidInjectorScreen extends GuiConfigurableTile<FluidInjectorBlockEntity, MekanismTileContainer<FluidInjectorBlockEntity>> {
    private MekanismButton containerModeButton;

    public FluidInjectorScreen(MekanismTileContainer<FluidInjectorBlockEntity> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        // Every tier adds one more container slot pair, so the screen grows downwards
        imageHeight = FluidInjectorBlockEntity.guiHeight(tile.parallel());
    }

    @Override
    protected void init() {
        int parallel = tile.parallel();
        imageHeight = FluidInjectorBlockEntity.guiHeight(parallel);
        super.init();
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = FluidInjectorBlockEntity.inventoryYOffset(parallel) - 12;
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
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 16))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> tile.hasContainerInput()
                    && tile.getEnergyContainer().getEnergy() < FluidInjectorBlockEntity.ENERGY_PER_OPERATION);
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        addRenderableWidget(new GuiFluidGauge(() -> tile.getFluidTank(), () -> tile.getFluidTanks(null),
              GaugeType.STANDARD, this, 6, 13));
        // Fill (tank -> containers) / Empty (containers -> tank) / Both, like Mekanism's fluid tank
        containerModeButton = new MekanismButton(this, 78, 20, 60, 14, tile.getContainerMode().getTextComponent(),
              (element, mouseX, mouseY) -> {
                  sendContainerMode();
                  return true;
              });
        containerModeButton.setTooltip(ModLang.TOOLTIP_CONTAINER_MODE);
        addRenderableWidget(containerModeButton);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (containerModeButton != null) {
            containerModeButton.setMessage(tile.getContainerMode().getTextComponent());
        }
    }

    private void sendContainerMode() {
        if (minecraft != null && minecraft.getConnection() != null) {
            minecraft.getConnection().send(new ServerboundContainerButtonClickPacket(menu.containerId,
                  FluidInjectorMenu.CONTAINER_MODE_BUTTON));
        }
    }
}
