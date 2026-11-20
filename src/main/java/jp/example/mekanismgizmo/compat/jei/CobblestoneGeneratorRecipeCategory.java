package jp.example.mekanismgizmo.compat.jei;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.block.CobblestoneGeneratorBlockEntity;
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
 * JEI page of the cobblestone generator. It has no input at all (only energy), so the page just shows the
 * produced cobblestone together with the energy usage and the time needed.
 */
public class CobblestoneGeneratorRecipeCategory implements IRecipeCategory<CobblestoneGeneratorJeiRecipe> {

    public static final RecipeType<CobblestoneGeneratorJeiRecipe> TYPE = new RecipeType<>(
          ResourceLocation.fromNamespaceAndPath(MekanismGizmo.MOD_ID, "cobblestone_generator"), CobblestoneGeneratorJeiRecipe.class);
    private static final int WIDTH = 116;
    private static final int HEIGHT = 64;

    private final IDrawable icon;
    private final IDrawable background;

    public CobblestoneGeneratorRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.COBBLESTONE_GENERATOR.asItem()));
        background = new RecipePanel(WIDTH, HEIGHT);
    }

    @Override
    public RecipeType<CobblestoneGeneratorJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mekanismgizmo.cobblestone_generator");
    }

    @Override
    public int getWidth() {
        return WIDTH;
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
    public void setRecipe(IRecipeLayoutBuilder builder, CobblestoneGeneratorJeiRecipe recipe, IFocusGroup focuses) {
        builder.addOutputSlot(49, 14)
              .setOutputSlotBackground()
              .addItemStack(recipe.output());
    }

    @Override
    public void draw(CobblestoneGeneratorJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Drawn before the slots so it is behind them (anything added through IRecipeExtrasBuilder is rendered on top)
        background.draw(guiGraphics, 0, 0);
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.drawString(minecraft.font, Component.translatable("jei.mekanismgizmo.info.no_input"),
              6, 34, ChatFormatting.DARK_GRAY.getColor(), false);
        guiGraphics.drawString(minecraft.font, Component.translatable("jei.mekanismgizmo.info.energy", 10),
              6, 43, ChatFormatting.DARK_GRAY.getColor(), false);
        guiGraphics.drawString(minecraft.font,
              Component.translatable("jei.mekanismgizmo.info.time", CobblestoneGeneratorBlockEntity.BASE_TICKS_REQUIRED),
              6, 52, ChatFormatting.DARK_GRAY.getColor(), false);
    }
}
