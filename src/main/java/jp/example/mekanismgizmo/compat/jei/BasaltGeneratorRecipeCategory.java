package jp.example.mekanismgizmo.compat.jei;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.block.BasaltGeneratorBlockEntity;
import jp.example.mekanismgizmo.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI page of the basalt generator. It has no input at all (only energy), so the page just shows the
 * produced basalt together with the energy usage and the time needed.
 */
public class BasaltGeneratorRecipeCategory implements IRecipeCategory<BasaltGeneratorJeiRecipe> {

    public static final RecipeType<BasaltGeneratorJeiRecipe> TYPE = new RecipeType<>(
          ResourceLocation.fromNamespaceAndPath(MekanismGizmo.MOD_ID, "basalt_generator"), BasaltGeneratorJeiRecipe.class);
    private static final Component NO_INPUT_LINE = Component.translatable("jei.mekanismgizmo.info.no_input");
    private static final Component ENERGY_LINE = Component.translatable("jei.mekanismgizmo.info.energy", 10);
    private static final Component TIME_LINE = Component.translatable("jei.mekanismgizmo.info.time",
          BasaltGeneratorBlockEntity.BASE_TICKS_REQUIRED);
    private static final int HEIGHT = 64;

    /** Page width: wide enough for the widest info line (and for the slots), so nothing spills out. */
    private final int width;
    private final IDrawable icon;
    private final IDrawable background;

    public BasaltGeneratorRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.BASALT_GENERATOR.asItem()));
        width = RecipePanel.widthFor(116, NO_INPUT_LINE, ENERGY_LINE, TIME_LINE);
        background = new RecipePanel(width, HEIGHT);
    }

    @Override
    public RecipeType<BasaltGeneratorJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mekanismgizmo.basalt_generator");
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BasaltGeneratorJeiRecipe recipe, IFocusGroup focuses) {
        builder.addOutputSlot(49, 14)
              .setOutputSlotBackground()
              .addItemStack(recipe.output());
    }

    @Override
    public void draw(BasaltGeneratorJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Drawn before the slots so it is behind them (anything added through IRecipeExtrasBuilder is rendered on top)
        background.draw(guiGraphics, 0, 0);
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.drawString(minecraft.font, NO_INPUT_LINE, 6, 34, ChatFormatting.DARK_GRAY.getColor(), false);
        guiGraphics.drawString(minecraft.font, ENERGY_LINE, 6, 43, ChatFormatting.DARK_GRAY.getColor(), false);
        guiGraphics.drawString(minecraft.font, TIME_LINE, 6, 52, ChatFormatting.DARK_GRAY.getColor(), false);
    }
}
