package jp.example.mekanismgizmo.client;

import jp.example.mekanismgizmo.block.FluidInjectorBlockEntity;
import jp.example.mekanismgizmo.menu.FluidInjectorMenu;
import jp.example.mekanismgizmo.registry.ModLang;
import jp.example.mekanismgizmo.util.AmountFormat;
import mekanism.api.math.FloatingLong;
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
        // Every tier adds one more container slot pair; the screen only grows once the slots pass the fixed elements
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
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
        // The tank reading fills the space under the mode button instead of leaving it empty
        Component tankInfo = Component.translatable("gui.mekanismgizmo.fluid_injector.tank",
              AmountFormat.format(tile.getFluidTank().getFluidAmount()),
              AmountFormat.format(tile.getFluidTank().getCapacity()));
        drawScaled(guiGraphics, tankInfo, FluidInjectorBlockEntity.MODE_BUTTON_X + 2,
              FluidInjectorBlockEntity.MODE_BUTTON_Y + FluidInjectorBlockEntity.MODE_BUTTON_HEIGHT + 6,
              FluidInjectorBlockEntity.POWER_BAR_X - FluidInjectorBlockEntity.MODE_BUTTON_X - 4);
    }

    /** Draws {@code text} at the given position, scaling it down when it does not fit into {@code maxWidth}. */
    private void drawScaled(GuiGraphics guiGraphics, Component text, int x, int y, int maxWidth) {
        int width = font.width(text);
        if (width <= maxWidth) {
            guiGraphics.drawString(font, text, x, y, 0x404040, false);
            return;
        }
        float scale = (float) maxWidth / width;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1F);
        guiGraphics.drawString(font, text, Math.round(x / scale), Math.round(y / scale), 0x404040, false);
        guiGraphics.pose().popPose();
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(),
              FluidInjectorBlockEntity.POWER_BAR_X, FluidInjectorBlockEntity.POWER_BAR_Y))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> tile.hasContainerInput()
                    && tile.getEnergyContainer().getEnergy().smallerThan(FloatingLong.create(FluidInjectorBlockEntity.ENERGY_PER_OPERATION)));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        // The tank gauge is stretched down to the last container row, so the left column is filled on every tier
        addRenderableWidget(new GuiFluidGauge(() -> tile.getFluidTank(), () -> tile.getFluidTanks(null),
              GaugeType.STANDARD, this, FluidInjectorBlockEntity.GAUGE_X, FluidInjectorBlockEntity.GAUGE_Y,
              FluidInjectorBlockEntity.GAUGE_WIDTH, FluidInjectorBlockEntity.gaugeHeight(tile.parallel())));
        // Fill (tank -> containers) / Empty (containers -> tank) / Both, like Mekanism's fluid tank
        // The button runs from the container slots to the power bar, so the top row is filled edge to edge
        containerModeButton = new MekanismButton(this, FluidInjectorBlockEntity.MODE_BUTTON_X,
              FluidInjectorBlockEntity.MODE_BUTTON_Y, FluidInjectorBlockEntity.MODE_BUTTON_WIDTH,
              FluidInjectorBlockEntity.MODE_BUTTON_HEIGHT, tile.getContainerMode().getTextComponent(),
              this::sendContainerMode, getOnHover(ModLang.TOOLTIP_CONTAINER_MODE));
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