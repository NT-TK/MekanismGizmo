package jp.example.mekanismgizmo.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Simple flat panel background for the custom JEI categories (Mekanism style grey machine page).
 * <p>
 * The width of a page is measured from its info lines, so a long translation (Japanese is noticeably wider than
 * English) never spills out of the page.
 */
final class RecipePanel implements IDrawable {

    private final int width;
    private final int height;

    RecipePanel(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /** Page width that fits every given line of text (plus the page padding), but at least {@code minimum}. */
    static int widthFor(int minimum, Component... lines) {
        Minecraft minecraft = Minecraft.getInstance();
        int widest = 0;
        if (minecraft != null && minecraft.font != null) {
            for (Component line : lines) {
                widest = Math.max(widest, minecraft.font.width(line));
            }
        }
        return Math.max(minimum, widest + 16);
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
