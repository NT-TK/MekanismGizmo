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
        // Every bottle the game knows: water bottle, awkward potion, and all brewable potions
        List<ItemStack> bottles = new ArrayList<>();
        for (Holder<Potion> potion : BuiltInRegistries.POTION.holders().toList()) {
            ItemStack bottle = new ItemStack(Items.POTION);
            bottle.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
            bottles.add(bottle);
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
        return ResourceLocation.fromNamespaceAndPath(MekanismGizmo.MOD_ID,
              "potion_brewer/" + potionName(recipe.input())
                    + "/" + BuiltInRegistries.ITEM.getKey(recipe.ingredient().getItem()).getPath()
                    + "/" + potionName(recipe.output()));
    }

    private static String potionName(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return "none";
        }
        return contents.potion()
              .flatMap(holder -> holder.unwrapKey().map(key -> key.location().getPath()))
              .map(path -> path.replace('/', '_'))
              .orElse("custom" + contents.customEffects().size());
    }

    private static String key(ItemStack input, ItemStack ingredient, ItemStack output) {
        return input.getItem() + "|" + input.get(DataComponents.POTION_CONTENTS) + "|" + ingredient.getItem() + "|"
              + output.get(DataComponents.POTION_CONTENTS);
    }
}
