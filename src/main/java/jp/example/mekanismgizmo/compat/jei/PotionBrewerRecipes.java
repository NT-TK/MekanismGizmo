package jp.example.mekanismgizmo.compat.jei;

import jp.example.mekanismgizmo.MekanismGizmo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Builds the potion brewer page content from the game's own brewing data, so every vanilla, NeoForge and data pack
 * brewing recipe shows up (the brewer accepts exactly what {@link PotionBrewing} accepts).
 */
public final class PotionBrewerRecipes {

    private PotionBrewerRecipes() {
    }

    /** All brewing recipes of the given level (the client level in game), one per bottle + ingredient pair. */
    public static List<PotionBrewerJeiRecipe> all(@Nullable Level level) {
        if (level == null) {
            return List.of();
        }
        PotionBrewing brewing = level.potionBrewing();
        // Every bottle the game knows: water bottle, awkward potion, and all brewable potions. Splash and lingering
        // potions have to be included as well, otherwise recipes such as splash potion + dragon's breath would be
        // missing from the page.
        List<ItemStack> bottles = new ArrayList<>();
        for (Holder<Potion> potion : BuiltInRegistries.POTION.holders().toList()) {
            for (Item bottleItem : List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION)) {
                ItemStack bottle = new ItemStack(bottleItem);
                bottle.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                bottles.add(bottle);
            }
        }
        List<PotionBrewerJeiRecipe> recipes = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack ingredient = new ItemStack(item);
            if (ingredient.isEmpty() || !brewing.isIngredient(ingredient)) {
                continue;
            }
            for (ItemStack bottle : bottles) {
                if (!brewing.hasMix(bottle, ingredient)) {
                    continue;
                }
                ItemStack output = brewing.mix(ingredient, bottle.copy());
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
        return ResourceLocation.fromNamespaceAndPath(MekanismGizmo.MOD_ID,
              "potion_brewer/" + potionName(recipe.input())
                    + "/" + ingredientName
                    + "/" + potionName(recipe.output()));
    }

    private static String potionName(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return "none";
        }
        // Include the item (potion, splash potion, lingering potion) as well as the potion itself: both can differ
        // between two otherwise identical recipes, and recipe ids have to stay unique.
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemName = itemId.getNamespace() + "_" + itemId.getPath().replace('/', '_');
        // Include the namespace: two mods can register a potion with the same path. The hash also separates custom
        // effects/colours, which have no registry name and would otherwise all share the "custom<count>" name.
        String base = contents.potion()
              .flatMap(holder -> holder.unwrapKey().map(key -> {
                  ResourceLocation location = key.location();
                  return location.getNamespace() + "_" + location.getPath().replace('/', '_');
              }))
              .orElse("custom");
        return itemName + "_" + base + "_" + Integer.toUnsignedString(contents.hashCode(), 16);
    }

    private static String key(ItemStack input, ItemStack ingredient, ItemStack output) {
        return input.getItem() + "|" + input.get(DataComponents.POTION_CONTENTS) + "|" + ingredient.getItem() + "|"
              + output.getItem() + "|" + output.get(DataComponents.POTION_CONTENTS);
    }
}
