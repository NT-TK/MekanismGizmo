package jp.example.mekanismgizmo.compat.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * One JEI entry of the fluid injector: a container either gives its fluid to the tank or receives fluid from it.
 * Exactly one of {@link #fluidInput()} / {@link #fluidOutput()} is non-empty.
 */
public record FluidInjectorJeiRecipe(ItemStack input, FluidStack fluidInput, ItemStack output, FluidStack fluidOutput) {
}
