package jp.example.mekanismgizmo.block;

import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.world.item.ItemStack;

/**
 * One holder of the wind generator fusion machine. A slot holds a full stack of wind generators, so a full machine fuses
 * {@link WindFusionBlockEntity#MAX_GENERATORS} of them and every single one produces on its own. The slots are a plain
 * internal storage rather than an input: the player can put generators in and take them out, but pipes can neither
 * insert nor extract them.
 */
public class WindGeneratorSlot extends BasicInventorySlot {

    public WindGeneratorSlot(IContentsListener listener, int x, int y) {
        super(WindFusionBlockEntity.MAX_STACK, ConstantPredicates.notExternal(), ConstantPredicates.notExternal(),
              WindGeneratorSlot::isWindGenerator, listener, x, y);
    }

    public static boolean isWindGenerator(ItemStack stack) {
        return stack.is(GeneratorsBlocks.WIND_GENERATOR.asItem());
    }
}
