package jp.example.mekanismgizmo.compat.jei;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.block.FluidInjectorBlockEntity;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI page of the fluid injector. A page shows either "container -&gt; tank" (fluid on the right, empty container on
 * the left output) or "tank -&gt; container" (fluid on the left, filled container on the right), matching the machine's
 * Fill / Empty modes.
 */
public class FluidInjectorRecipeCategory implements IRecipeCategory<FluidInjectorJeiRecipe> {

    public static final RecipeType<FluidInjectorJeiRecipe> TYPE = new RecipeType<>(
          ResourceLocation.fromNamespaceAndPath(MekanismGizmo.MOD_ID, "fluid_injector"), FluidInjectorJeiRecipe.class);
    private static final int WIDTH = 120;
    private static final int HEIGHT = 62;

    private final IDrawable icon;
    private final IDrawable background;
    private final IDrawable arrow;

    public FluidInjectorRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.FLUID_INJECTOR.asItem()));
        background = new RecipePanel(WIDTH, HEIGHT);
        arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<FluidInjectorJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mekanismgizmo.fluid_injector");
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
    public ResourceLocation getRegistryName(FluidInjectorJeiRecipe recipe) {
        String direction = recipe.fluidInput().isEmpty() ? "empty" : "fill";
        ResourceLocation input = BuiltInRegistries.ITEM.getKey(recipe.input().getItem());
        return ResourceLocation.fromNamespaceAndPath(MekanismGizmo.MOD_ID, "fluid_injector/" + direction + "/" + input.getPath());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FluidInjectorJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(8, 16)
              .setStandardSlotBackground()
              .addItemStack(recipe.input());
        if (!recipe.fluidInput().isEmpty()) {
            builder.addInputSlot(32, 16)
                  .setStandardSlotBackground()
                  .setFluidRenderer(FluidInjectorBlockEntity.TANK_CAPACITY, false, 16, 16)
                  .addFluidStack(recipe.fluidInput().getFluid(), recipe.fluidInput().getAmount());
        }
        builder.addOutputSlot(78, 16)
              .setOutputSlotBackground()
              .addItemStack(recipe.output());
        if (!recipe.fluidOutput().isEmpty()) {
            builder.addOutputSlot(100, 16)
                  .setOutputSlotBackground()
                  .setFluidRenderer(FluidInjectorBlockEntity.TANK_CAPACITY, false, 16, 16)
                  .addFluidStack(recipe.fluidOutput().getFluid(), recipe.fluidOutput().getAmount());
        }
    }

    @Override
    public void draw(FluidInjectorJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Drawn before the slots so it stays behind them
        background.draw(guiGraphics, 0, 0);
        arrow.draw(guiGraphics, 52, 17);
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.drawString(minecraft.font,
              Component.translatable("jei.mekanismgizmo.info.energy_per_container", FluidInjectorBlockEntity.ENERGY_PER_OPERATION),
              8, 40, ChatFormatting.DARK_GRAY.getColor(), false);
        guiGraphics.drawString(minecraft.font,
              Component.translatable("jei.mekanismgizmo.info.bottle", FluidInjectorBlockEntity.BOTTLE_AMOUNT),
              8, 50, ChatFormatting.DARK_GRAY.getColor(), false);
    }
}
