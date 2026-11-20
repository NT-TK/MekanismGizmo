package jp.example.mekanismgizmo.compat.emi;

import java.util.List;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.block.BasaltGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.CobblestoneGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.FluidInjectorBlockEntity;
import jp.example.mekanismgizmo.block.IceMakerBlockEntity;
import jp.example.mekanismgizmo.block.PotionBrewerBlockEntity;
import jp.example.mekanismgizmo.compat.jei.PotionBrewerJeiRecipe;
import jp.example.mekanismgizmo.compat.jei.PotionBrewerRecipes;
import jp.example.mekanismgizmo.registry.ModBlocks;
import jp.example.mekanismgizmo.registry.ModChemicals;
import mekanism.client.recipe_viewer.emi.ChemicalEmiStack;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Optional EMI integration (EMI is only a compile time dependency and is never bundled).
 * <p>
 * EMI can render JEI plugins through its JEI compatibility layer, but that layer has no Mekanism chemical ingredient,
 * so the blaze powder gas could not be shown. This plugin therefore adds the four machines natively: the pages are the
 * same as the JEI ones, and the potion brewer page lists the 5 mB of blaze powder gas one brew of three bottles needs.
 */
@EmiEntrypoint
public class MekanismGizmoEmiPlugin implements EmiPlugin {

    public static final EmiRecipeCategory POTION_BREWER = category("potion_brewer", ModBlocks.POTION_BREWER);
    public static final EmiRecipeCategory ICE_MAKER = category("ice_maker", ModBlocks.ICE_MAKER);
    public static final EmiRecipeCategory COBBLESTONE_GENERATOR = category("cobblestone_generator", ModBlocks.COBBLESTONE_GENERATOR);
    public static final EmiRecipeCategory BASALT_GENERATOR = category("basalt_generator", ModBlocks.BASALT_GENERATOR);
    public static final EmiRecipeCategory FLUID_INJECTOR = category("fluid_injector", ModBlocks.FLUID_INJECTOR);

    private static final Logger LOGGER = LoggerFactory.getLogger("mekanismgizmo-emi");
    private static final Component BREWER_GAS = Component.translatable("jei.mekanismgizmo.info.brewer_gas",
          PotionBrewerBlockEntity.CHEMICAL_PER_BREW);
    private static final Component BREWER_TIME = Component.translatable("jei.mekanismgizmo.info.time",
          PotionBrewerBlockEntity.BASE_TICKS_REQUIRED);

    private static EmiRecipeCategory category(String name, BlockRegistryObject<?, ?> block) {
        return new EmiRecipeCategory(ResourceLocation.fromNamespaceAndPath(MekanismGizmo.MOD_ID, name),
              EmiStack.of(block.asItem()));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MekanismGizmo.MOD_ID, path);
    }

    private static void addWorkstations(EmiRegistry registry, EmiRecipeCategory category, BlockRegistryObject<?, ?>... blocks) {
        for (BlockRegistryObject<?, ?> block : blocks) {
            registry.addWorkstation(category, EmiStack.of(block.asItem()));
        }
    }

    @Override
    public void register(EmiRegistry registry) {
        addBrewer(registry);
        addIceMaker(registry);
        addCobblestoneGenerator(registry);
        addBasaltGenerator(registry);
        addFluidInjector(registry);
    }

    private void addBrewer(EmiRegistry registry) {
        registry.addCategory(POTION_BREWER);
        addWorkstations(registry, POTION_BREWER, ModBlocks.POTION_BREWER, ModBlocks.POTION_BREWER_BASIC,
              ModBlocks.POTION_BREWER_ADVANCED, ModBlocks.POTION_BREWER_ELITE, ModBlocks.POTION_BREWER_ULTIMATE);
        // The gas one brew of three bottles uses, as a Mekanism chemical ingredient
        EmiStack gas = new ChemicalEmiStack(ModChemicals.BLAZE_POWDER.asStack(PotionBrewerBlockEntity.CHEMICAL_PER_BREW));
        // The brewing data comes from the level, so the pages are handed over when EMI builds its index
        registry.addDeferredRecipes(add -> {
            int pages = 0;
            for (PotionBrewerJeiRecipe recipe : PotionBrewerRecipes.all(Minecraft.getInstance().level)) {
                pages++;
                add.accept(new GizmoEmiRecipe(POTION_BREWER, PotionBrewerRecipes.id(recipe), 124, 76,
                      List.of(new GizmoEmiRecipe.Slot(gas, 4, 20),
                            new GizmoEmiRecipe.Slot(EmiStack.of(recipe.input()), 28, 12),
                            new GizmoEmiRecipe.Slot(EmiStack.of(recipe.ingredient()), 28, 34)),
                      List.of(new GizmoEmiRecipe.Slot(EmiStack.of(recipe.output()), 78, 12)),
                      List.of(BREWER_GAS, BREWER_TIME), 54, 17));
            }
            LOGGER.info("Added {} potion brewer pages to EMI", pages);
        });
    }

    private void addIceMaker(EmiRegistry registry) {
        registry.addCategory(ICE_MAKER);
        addWorkstations(registry, ICE_MAKER, ModBlocks.ICE_MAKER, ModBlocks.ICE_MAKER_BASIC, ModBlocks.ICE_MAKER_ADVANCED,
              ModBlocks.ICE_MAKER_ELITE, ModBlocks.ICE_MAKER_ULTIMATE);
        registry.addRecipe(new GizmoEmiRecipe(ICE_MAKER, id("ice_maker"), 100, 58,
              List.of(new GizmoEmiRecipe.Slot(EmiStack.of(Fluids.WATER, IceMakerBlockEntity.WATER_PER_ICE), 8, 16)),
              List.of(new GizmoEmiRecipe.Slot(EmiStack.of(Items.ICE), 68, 16)),
              List.of(Component.translatable("jei.mekanismgizmo.info.energy", 20),
                    Component.translatable("jei.mekanismgizmo.info.time", IceMakerBlockEntity.BASE_TICKS_REQUIRED)),
              34, 17));
    }

    private void addCobblestoneGenerator(EmiRegistry registry) {
        registry.addCategory(COBBLESTONE_GENERATOR);
        addWorkstations(registry, COBBLESTONE_GENERATOR, ModBlocks.COBBLESTONE_GENERATOR,
              ModBlocks.COBBLESTONE_GENERATOR_BASIC, ModBlocks.COBBLESTONE_GENERATOR_ADVANCED,
              ModBlocks.COBBLESTONE_GENERATOR_ELITE, ModBlocks.COBBLESTONE_GENERATOR_ULTIMATE);
        registry.addRecipe(new GizmoEmiRecipe(COBBLESTONE_GENERATOR, id("cobblestone_generator"), 100, 62, List.of(),
              List.of(new GizmoEmiRecipe.Slot(EmiStack.of(Items.COBBLESTONE), 42, 12)),
              List.of(Component.translatable("jei.mekanismgizmo.info.no_input"),
                    Component.translatable("jei.mekanismgizmo.info.energy", 10),
                    Component.translatable("jei.mekanismgizmo.info.time", CobblestoneGeneratorBlockEntity.BASE_TICKS_REQUIRED)),
              -1, -1));
    }

    private void addBasaltGenerator(EmiRegistry registry) {
        registry.addCategory(BASALT_GENERATOR);
        addWorkstations(registry, BASALT_GENERATOR, ModBlocks.BASALT_GENERATOR,
              ModBlocks.BASALT_GENERATOR_BASIC, ModBlocks.BASALT_GENERATOR_ADVANCED,
              ModBlocks.BASALT_GENERATOR_ELITE, ModBlocks.BASALT_GENERATOR_ULTIMATE);
        registry.addRecipe(new GizmoEmiRecipe(BASALT_GENERATOR, id("basalt_generator"), 100, 62, List.of(),
              List.of(new GizmoEmiRecipe.Slot(EmiStack.of(Items.BASALT), 42, 12)),
              List.of(Component.translatable("jei.mekanismgizmo.info.no_input"),
                    Component.translatable("jei.mekanismgizmo.info.energy", 10),
                    Component.translatable("jei.mekanismgizmo.info.time", BasaltGeneratorBlockEntity.BASE_TICKS_REQUIRED)),
              -1, -1));
    }

    private void addFluidInjector(EmiRegistry registry) {
        registry.addCategory(FLUID_INJECTOR);
        addWorkstations(registry, FLUID_INJECTOR, ModBlocks.FLUID_INJECTOR, ModBlocks.FLUID_INJECTOR_BASIC,
              ModBlocks.FLUID_INJECTOR_ADVANCED, ModBlocks.FLUID_INJECTOR_ELITE, ModBlocks.FLUID_INJECTOR_ULTIMATE);
        Component energyLine = Component.translatable("jei.mekanismgizmo.info.energy_per_container",
              FluidInjectorBlockEntity.ENERGY_PER_OPERATION);
        Component bottleLine = Component.translatable("jei.mekanismgizmo.info.bottle", FluidInjectorBlockEntity.BOTTLE_AMOUNT);
        ItemStack bucket = new ItemStack(Items.BUCKET);
        ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
        ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
        ItemStack waterBottle = new ItemStack(Items.POTION);
        waterBottle.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
              new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.WATER));
        EmiStack tankWater = EmiStack.of(Fluids.WATER, 1_000);
        EmiStack bottleWater = EmiStack.of(Fluids.WATER, FluidInjectorBlockEntity.BOTTLE_AMOUNT);

        addInjectorRecipe(registry, "empty/bucket", waterBucket, null, bucket, tankWater, energyLine, bottleLine);
        addInjectorRecipe(registry, "empty/bottle", waterBottle, null, glassBottle, bottleWater, energyLine, bottleLine);
        addInjectorRecipe(registry, "fill/bucket", bucket, tankWater, waterBucket, null, energyLine, bottleLine);
        addInjectorRecipe(registry, "fill/bottle", glassBottle, bottleWater, waterBottle, null, energyLine, bottleLine);
    }

    private void addInjectorRecipe(EmiRegistry registry, String path, ItemStack input, EmiStack fluidIn, ItemStack output,
          EmiStack fluidOut, Component energyLine, Component bottleLine) {
        List<GizmoEmiRecipe.Slot> inputs = new java.util.ArrayList<>(2);
        List<GizmoEmiRecipe.Slot> outputs = new java.util.ArrayList<>(2);
        inputs.add(new GizmoEmiRecipe.Slot(EmiStack.of(input), 8, 16));
        if (fluidIn != null) {
            inputs.add(new GizmoEmiRecipe.Slot(fluidIn, 32, 16));
        }
        outputs.add(new GizmoEmiRecipe.Slot(EmiStack.of(output), 78, 16));
        if (fluidOut != null) {
            outputs.add(new GizmoEmiRecipe.Slot(fluidOut, 100, 16));
        }
        registry.addRecipe(new GizmoEmiRecipe(FLUID_INJECTOR, id("fluid_injector/" + path), 124, 62, inputs, outputs,
              List.of(energyLine, bottleLine), 54, 17));
    }
}
