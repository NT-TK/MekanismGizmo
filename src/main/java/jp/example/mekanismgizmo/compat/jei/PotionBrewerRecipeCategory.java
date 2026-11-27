package jp.example.mekanismgizmo.compat.jei;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.block.PotionBrewerBlockEntity;
import jp.example.mekanismgizmo.registry.ModBlocks;
import jp.example.mekanismgizmo.registry.ModChemicals;
import mekanism.client.jei.MekanismJEI;
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
 * JEI page of the potion brewer, laid out like Mekanism's own machine pages: the bottle and the ingredient on the
 * left, an arrow, the brewed potion on the right, plus the gas and the time one batch of three bottles needs.
 */
public class PotionBrewerRecipeCategory implements IRecipeCategory<PotionBrewerJeiRecipe> {

    public static final RecipeType<PotionBrewerJeiRecipe> TYPE = new RecipeType<>(
          ResourceLocation.tryBuild(MekanismGizmo.MOD_ID, "potion_brewer"), PotionBrewerJeiRecipe.class);
    private static final Component GAS_LINE = Component.translatable("jei.mekanismgizmo.info.brewer_gas",
          PotionBrewerBlockEntity.CHEMICAL_PER_BREW);
    private static final Component TIME_LINE = Component.translatable("jei.mekanismgizmo.info.time",
          PotionBrewerBlockEntity.BASE_TICKS_REQUIRED);
    private static final int HEIGHT = 78;
    /** Left hand gas slot: the blaze powder gas one brew of three bottles uses. */
    private static final int GAS_X = 6;
    private static final int GAS_Y = 20;

    /** Page width: wide enough for the widest info line (and for the slots), so nothing spills out. */
    private final int width;
    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable arrow;

    public PotionBrewerRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.POTION_BREWER.asItem()));
        width = RecipePanel.widthFor(132, GAS_LINE, TIME_LINE);
        background = new RecipePanel(width, HEIGHT);
        arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<PotionBrewerJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mekanismgizmo.potion_brewer");
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
    public ResourceLocation getRegistryName(PotionBrewerJeiRecipe recipe) {
        // Only [a-z0-9_/] are allowed in a resource location, so the potion names are taken from the registry key
        return PotionBrewerRecipes.id(recipe);
    }

    /** True when the page shows the blaze powder gas slot (Mekanism's JEI plugin provides the chemical type). */
    public static boolean hasGasSlot() {
        return true;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PotionBrewerJeiRecipe recipe, IFocusGroup focuses) {
        // Every brew of three bottles also needs blaze powder gas, so the gas is an input of the page as well.
        // (Mekanism's JEI plugin provides the chemical ingredient type; without it the page simply has no gas slot.)
        if (hasGasSlot()) {
            builder.addInputSlot(GAS_X, GAS_Y)
                  .setStandardSlotBackground()
                  .addIngredient(MekanismJEI.TYPE_GAS,
                        new mekanism.api.chemical.gas.GasStack(ModChemicals.BLAZE_POWDER.get(), PotionBrewerBlockEntity.CHEMICAL_PER_BREW));
        }
        // Same order as a brewing stand: the bottle on top, the ingredient underneath, the result to the right
        builder.addInputSlot(30, 12)
              .setStandardSlotBackground()
              .addItemStack(recipe.input());
        builder.addInputSlot(30, 34)
              .setStandardSlotBackground()
              .addItemStack(recipe.ingredient());
        builder.addOutputSlot(80, 12)
              .setOutputSlotBackground()
              .addItemStack(recipe.output());
    }

    @Override
    public void draw(PotionBrewerJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX,
          double mouseY) {
        // Drawn before the slots so it stays behind them
        background.draw(guiGraphics, 0, 0);
        arrow.draw(guiGraphics, 54, 17);
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.drawString(minecraft.font, GAS_LINE, 6, 58, ChatFormatting.DARK_GRAY.getColor(), false);
        guiGraphics.drawString(minecraft.font, TIME_LINE, 6, 68, ChatFormatting.DARK_GRAY.getColor(), false);
    }
}
