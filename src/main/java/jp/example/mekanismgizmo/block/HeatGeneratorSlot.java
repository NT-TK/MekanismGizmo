package jp.example.mekanismgizmo.block;

import jp.example.mekanismgizmo.util.HeatGeneratorUnits;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.world.item.ItemStack;

/**
 * One holder of the heat generator fusion machine. A slot holds a full stack of heat generators, so a full machine fuses
 * {@link HeatFusionBlockEntity#MAX_UNITS} of them and every single one produces on its own. Compressed heat generators
 * (Compact Mekanism Machines) are accepted as well and count as the number of generators they replace. The slots are a
 * plain internal storage rather than an input: the player can put generators in and take them out, but pipes can
 * neither insert nor extract them.
 */
public class HeatGeneratorSlot extends BasicInventorySlot {

    public HeatGeneratorSlot(IContentsListener listener, int x, int y) {
        super(HeatFusionBlockEntity.MAX_STACK, ConstantPredicates.notExternal(), ConstantPredicates.notExternal(),
              HeatGeneratorSlot::isHeatGenerator, listener, x, y);
    }

    public static boolean isHeatGenerator(ItemStack stack) {
        return HeatGeneratorUnits.of(stack) > 0;
    }
}
