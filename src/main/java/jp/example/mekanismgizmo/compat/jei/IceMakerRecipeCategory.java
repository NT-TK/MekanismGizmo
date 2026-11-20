package jp.example.mekanismgizmo.compat.jei;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.block.IceMakerBlockEntity;
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
import net.minecraft.world.level.material.Fluids;

/**
 * JEI page of the ice maker, laid out like Mekanism's own machine pages: input on the left, an arrow, the
 * output on the right, plus the machine's energy usage and processing time.
 */
public class IceMakerRecipeCategory implements IRecipeCategory<IceMakerJeiRecipe> {

    public static final RecipeType<IceMakerJeiRecipe> TYPE = new RecipeType<>(
          ResourceLocation.fromNamespaceAndPath(MekanismGizmo.MOD_ID, "ice_maker"), IceMakerJeiRecipe.class);
    private static final int WIDTH = 116;
    private static final int HEIGHT = 58;

    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable arrow;

    public IceMakerRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ICE_MAKER.asItem()));
        background = new RecipePanel(WIDTH, HEIGHT);
        arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<IceMakerJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mekanismgizmo.ice_maker");
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
    public void setRecipe(IRecipeLayoutBuilder builder, IceMakerJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(10, 16)
              .setStandardSlotBackground()
              .setFluidRenderer(IceMakerBlockEntity.WATER_CAPACITY, false, 16, 16)
              .addFluidStack(Fluids.WATER, recipe.waterAmount());
        builder.addOutputSlot(68, 16)
              .setOutputSlotBackground()
              .addItemStack(recipe.output());
    }

    @Override
    public void draw(IceMakerJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Drawn before the slots so it is behind them (anything added through IRecipeExtrasBuilder is rendered on top)
        background.draw(guiGraphics, 0, 0);
        arrow.draw(guiGraphics, 34, 17);
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.drawString(minecraft.font, Component.translatable("jei.mekanismgizmo.info.energy", 20),
              10, 38, ChatFormatting.DARK_GRAY.getColor(), false);
        guiGraphics.drawString(minecraft.font, Component.translatable("jei.mekanismgizmo.info.time", IceMakerBlockEntity.BASE_TICKS_REQUIRED),
              10, 47, ChatFormatting.DARK_GRAY.getColor(), false);
    }
}
