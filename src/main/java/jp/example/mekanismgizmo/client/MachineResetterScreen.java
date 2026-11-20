package jp.example.mekanismgizmo.client;

import jp.example.mekanismgizmo.block.MachineResetterBlockEntity;
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

/** Screen of the machine resetter: machine item in, clean machine item out, with the usual energy bar and arrow. */
public class MachineResetterScreen extends GuiConfigurableTile<MachineResetterBlockEntity, MekanismTileContainer<MachineResetterBlockEntity>> {

    public MachineResetterScreen(MekanismTileContainer<MachineResetterBlockEntity> container, Inventory inv, Component title) {
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
              MachineResetterBlockEntity.POWER_BAR_X, MachineResetterBlockEntity.POWER_BAR_Y))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> tile.getProgress() > 0
                    && tile.getEnergyContainer().getEnergy() < tile.getEnergyContainer().getEnergyPerTick());
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.RIGHT, this,
              MachineResetterBlockEntity.ARROW_X, MachineResetterBlockEntity.ARROW_Y))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, tile::isOutputBlocked);
    }
}
