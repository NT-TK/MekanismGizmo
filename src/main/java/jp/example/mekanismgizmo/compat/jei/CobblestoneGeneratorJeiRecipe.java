package jp.example.mekanismgizmo.compat.jei;

import net.minecraft.world.item.ItemStack;

/**
 * A single "recipe" of the cobblestone generator: only energy goes in, one cobblestone comes out.
 */
public record CobblestoneGeneratorJeiRecipe(ItemStack output) {
}
