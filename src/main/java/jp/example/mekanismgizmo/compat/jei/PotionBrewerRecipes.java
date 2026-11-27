package jp.example.mekanismgizmo.compat.jei;

import jp.example.mekanismgizmo.MekanismGizmo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import org.jetbrains.annotations.Nullable;

/**
 * Builds the potion brewer page content from Forge's brewing registry, so every vanilla and modded brewing recipe
 * shows up (the brewer accepts exactly what {@link BrewingRecipeRegistry} accepts).
 */
public final class PotionBrewerRecipes {

    private PotionBrewerRecipes() {
    }

    /** All brewing recipes of the given level (the client level in game), one per bottle + ingredient pair. */
    public static List<PotionBrewerJeiRecipe> all(@Nullable Level level) {
        if (level == null) {
            return List.of();
        }
        // Every bottle the game knows: water bottle, awkward potion, and all brewable potions. Splash and lingering
        // potions have to be included as well, otherwise recipes such as splash potion + dragon's breath would be
        // missing from the page.
        List<ItemStack> bottles = new ArrayList<>();
        for (Potion potion : BuiltInRegistries.POTION) {
            for (Item bottleItem : List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION)) {
                bottles.add(PotionUtils.setPotion(new ItemStack(bottleItem), potion));
            }
        }
        List<PotionBrewerJeiRecipe> recipes = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack ingredient = new ItemStack(item);
            if (ingredient.isEmpty() || !BrewingRecipeRegistry.isValidIngredient(ingredient)) {
                continue;
            }
            for (ItemStack bottle : bottles) {
                if (!BrewingRecipeRegistry.hasOutput(bottle, ingredient)) {
                    continue;
                }
                ItemStack output = BrewingRecipeRegistry.getOutput(bottle, ingredient);
                if (output.isEmpty()) {
                    continue;
                }
                String key = key(bottle, ingredient, output);
                if (seen.add(key)) {
                    recipes.add(new PotionBrewerJeiRecipe(bottle.copy(), ingredient, output));
                }
            }
        }
        return recipes;
    }

    /** Unique, resource location safe id of a brewing page (shared by the JEI and the EMI pages). */
    public static ResourceLocation id(PotionBrewerJeiRecipe recipe) {
        ResourceLocation ingredientId = BuiltInRegistries.ITEM.getKey(recipe.ingredient().getItem());
        String ingredientName = ingredientId.getNamespace() + "_" + ingredientId.getPath().replace('/', '_');
        return ResourceLocation.tryBuild(MekanismGizmo.MOD_ID,
              "potion_brewer/" + potionName(recipe.input())
                    + "/" + ingredientName
                    + "/" + potionName(recipe.output()));
    }

    private static String potionName(ItemStack stack) {
        // Include the item (potion, splash potion, lingering potion) as well as the potion itself: both can differ
        // between two otherwise identical recipes, and recipe ids have to stay unique.
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemName = itemId.getNamespace() + "_" + itemId.getPath().replace('/', '_');
        ResourceLocation potionId = BuiltInRegistries.POTION.getKey(PotionUtils.getPotion(stack));
        String base = potionId.getNamespace() + "_" + potionId.getPath().replace('/', '_');
        return itemName + "_" + base;
    }

    private static String key(ItemStack input, ItemStack ingredient, ItemStack output) {
        return input.getItem() + "|" + input.getTag() + "|" + ingredient.getItem() + "|"
              + output.getItem() + "|" + output.getTag();
    }
}
