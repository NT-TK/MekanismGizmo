package jp.example.mekanismgizmo.compat.jei;

import net.minecraft.world.item.ItemStack;

/**
 * A single "recipe" of the ice maker: some water is turned into one ice block.
 */
public record IceMakerJeiRecipe(int waterAmount, ItemStack output) {
}
