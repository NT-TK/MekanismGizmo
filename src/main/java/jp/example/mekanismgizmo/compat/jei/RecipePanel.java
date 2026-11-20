package jp.example.mekanismgizmo.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Simple flat panel background for the custom JEI categories (Mekanism style grey machine page).
 */
final class RecipePanel implements IDrawable {

    private final int width;
    private final int height;

    RecipePanel(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        guiGraphics.fill(xOffset, yOffset, xOffset + width, yOffset + height, 0xFF373737);
        guiGraphics.fill(xOffset + 1, yOffset + 1, xOffset + width - 1, yOffset + height - 1, 0xFFC6C6C6);
    }
}
