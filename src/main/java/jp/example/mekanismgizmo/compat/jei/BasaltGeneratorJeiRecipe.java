package jp.example.mekanismgizmo.compat.jei;

import net.minecraft.world.item.ItemStack;

/**
 * A single "recipe" of the basalt generator: only energy goes in, one basalt comes out.
 */
public record BasaltGeneratorJeiRecipe(ItemStack output) {
}
