package jp.example.mekanismgizmo.block;

import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.world.item.ItemStack;

/**
 * One holder of the wind generator fusion machine. A slot holds exactly one wind generator: the machine fuses at most
 * {@link WindFusionBlockEntity#SLOT_COUNT} of them, and every one of them produces on its own. Like Mekanism's machine
 * input slots, items can be put in by hand and by pipes, but pipes cannot pull the generators back out.
 */
public class WindGeneratorSlot extends BasicInventorySlot {

    public WindGeneratorSlot(IContentsListener listener, int x, int y) {
        super(1, ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(),
              WindGeneratorSlot::isWindGenerator, listener, x, y);
    }

    public static boolean isWindGenerator(ItemStack stack) {
        return stack.is(GeneratorsBlocks.WIND_GENERATOR.asItem());
    }
}
