package jp.example.mekanismgizmo.compat.jei;

import net.minecraft.world.item.ItemStack;

/**
 * One page of the potion brewer: a potion (or water bottle) and one ingredient brew into one potion.
 */
public record PotionBrewerJeiRecipe(ItemStack input, ItemStack ingredient, ItemStack output) {
}
