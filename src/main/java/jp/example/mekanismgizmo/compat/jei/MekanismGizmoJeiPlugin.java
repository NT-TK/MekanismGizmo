package jp.example.mekanismgizmo.compat.jei;

import java.util.List;
import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.block.CobblestoneGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.FluidInjectorBlockEntity;
import jp.example.mekanismgizmo.block.IceMakerBlockEntity;
import jp.example.mekanismgizmo.client.CobblestoneGeneratorScreen;
import jp.example.mekanismgizmo.client.IceMakerScreen;
import jp.example.mekanismgizmo.client.PotionBrewerScreen;
import jp.example.mekanismgizmo.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Optional JEI integration. JEI is only a compile time dependency, so this class is never loaded when JEI is not
 * installed (JEI discovers it through the {@link JeiPlugin} annotation).
 */
@JeiPlugin
public class MekanismGizmoJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MekanismGizmo.MOD_ID, "jei_plugin");
    /** EMI draws our pages natively (see the emi package), so nothing is registered for JEI in that case. */
    private static final boolean EMI_LOADED = ModList.get().isLoaded("emi");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        if (EMI_LOADED) {
            return;
        }
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new PotionBrewerRecipeCategory(guiHelper), new IceMakerRecipeCategory(guiHelper),
              new CobblestoneGeneratorRecipeCategory(guiHelper), new FluidInjectorRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (EMI_LOADED) {
            return;
        }
        // The brewer gets its own page: every recipe the game's brewing system accepts, one page per bottle+ingredient
        registration.addRecipes(PotionBrewerRecipeCategory.TYPE,
              PotionBrewerRecipes.all(Minecraft.getInstance().level));
        registration.addRecipes(IceMakerRecipeCategory.TYPE, List.of(
              new IceMakerJeiRecipe(IceMakerBlockEntity.WATER_PER_ICE, new ItemStack(Items.ICE))));
        registration.addRecipes(CobblestoneGeneratorRecipeCategory.TYPE, List.of(
              new CobblestoneGeneratorJeiRecipe(new ItemStack(Items.COBBLESTONE))));
        registration.addRecipes(FluidInjectorRecipeCategory.TYPE, injectorRecipes());
        registration.addIngredientInfo(new ItemStack(ModBlocks.FLUID_INJECTOR.asItem()), VanillaTypes.ITEM_STACK,
              Component.translatable("jei.mekanismgizmo.info.fluid_injector"));
        registration.addIngredientInfo(new ItemStack(ModBlocks.ITEM_COLLECTOR.asItem()), VanillaTypes.ITEM_STACK,
              Component.translatable("jei.mekanismgizmo.info.item_collector"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (EMI_LOADED) {
            return;
        }
        // The potion brewer shows up on its own brewing page (one entry per bottle + ingredient)
        registration.addRecipeCatalyst(ModBlocks.POTION_BREWER.asItem(), PotionBrewerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.POTION_BREWER_BASIC.asItem(), PotionBrewerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.POTION_BREWER_ADVANCED.asItem(), PotionBrewerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.POTION_BREWER_ELITE.asItem(), PotionBrewerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.POTION_BREWER_ULTIMATE.asItem(), PotionBrewerRecipeCategory.TYPE);
        // And the two custom machines for their own processing pages
        registration.addRecipeCatalyst(ModBlocks.ICE_MAKER.asItem(), IceMakerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.ICE_MAKER_BASIC.asItem(), IceMakerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.ICE_MAKER_ADVANCED.asItem(), IceMakerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.ICE_MAKER_ELITE.asItem(), IceMakerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.ICE_MAKER_ULTIMATE.asItem(), IceMakerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.COBBLESTONE_GENERATOR.asItem(), CobblestoneGeneratorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.COBBLESTONE_GENERATOR_BASIC.asItem(), CobblestoneGeneratorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.COBBLESTONE_GENERATOR_ADVANCED.asItem(), CobblestoneGeneratorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.COBBLESTONE_GENERATOR_ELITE.asItem(), CobblestoneGeneratorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.COBBLESTONE_GENERATOR_ULTIMATE.asItem(), CobblestoneGeneratorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.FLUID_INJECTOR.asItem(), FluidInjectorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.FLUID_INJECTOR_BASIC.asItem(), FluidInjectorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.FLUID_INJECTOR_ADVANCED.asItem(), FluidInjectorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.FLUID_INJECTOR_ELITE.asItem(), FluidInjectorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.FLUID_INJECTOR_ULTIMATE.asItem(), FluidInjectorRecipeCategory.TYPE);
    }

    private static List<FluidInjectorJeiRecipe> injectorRecipes() {
        ItemStack bucket = new ItemStack(Items.BUCKET);
        ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
        ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
        ItemStack waterBottle = new ItemStack(Items.POTION);
        waterBottle.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
        FluidStack bucketOfWater = new FluidStack(Fluids.WATER, 1_000);
        FluidStack bottleOfWater = new FluidStack(Fluids.WATER, FluidInjectorBlockEntity.BOTTLE_AMOUNT);
        return List.of(
              // Emptying a container into the tank
              new FluidInjectorJeiRecipe(waterBucket, FluidStack.EMPTY, bucket, bucketOfWater),
              new FluidInjectorJeiRecipe(waterBottle, FluidStack.EMPTY, glassBottle, bottleOfWater),
              // Filling a container from the tank
              new FluidInjectorJeiRecipe(bucket, bucketOfWater, waterBucket, FluidStack.EMPTY),
              new FluidInjectorJeiRecipe(glassBottle, bottleOfWater, waterBottle, FluidStack.EMPTY));
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        if (EMI_LOADED) {
            return;
        }
        // Lets the "+" button of the brewing category fill the potion brewer (three bottles + ingredient)
        registration.addRecipeTransferHandler(new PotionBrewerRecipeTransferInfo());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        if (EMI_LOADED) {
            return;
        }
        // Same behaviour as Mekanism machines: clicking the progress arrow shows the recipes the machine can run
        registration.addRecipeClickArea(PotionBrewerScreen.class, 102, 37, 32, 8, PotionBrewerRecipeCategory.TYPE);
        registration.addRecipeClickArea(IceMakerScreen.class, 54, 40, 32, 8, IceMakerRecipeCategory.TYPE);
        registration.addRecipeClickArea(CobblestoneGeneratorScreen.class, 54, 40, 32, 8, CobblestoneGeneratorRecipeCategory.TYPE);
    }
}
