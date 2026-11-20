package jp.example.mekanismgizmo.item;

import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.chemical.AttachedChemicals;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.fluid.AttachedFluids;
import mekanism.common.attachments.containers.fluid.ComponentBackedFluidTank;
import mekanism.common.attachments.containers.fluid.FluidTanksBuilder;
import mekanism.common.item.ItemGaugeDropper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Tiered gauge dropper ("pipette").
 * <p>
 * It behaves like Mekanism's gauge dropper - pick it up in a machine screen and click a tank gauge to fill or empty the
 * machine, and sneak + right click to void its contents - but every tier holds more and moves more per click. The
 * contents are component backed, so they stay on the item when it is dropped, stored in a chest or previewed in JEI.
 * Because the liquid tank and the gas tank are separate attachments, a pipette can carry both at the same time.
 */
public class PipetteItem extends ItemGaugeDropper {

    private final int capacity;

    public PipetteItem(Item.Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    /** How much liquid and how much gas this pipette holds, in mB. One click moves up to this amount. */
    public int getCapacity() {
        return capacity;
    }

    /** Liquid tank attachment of one pipette tier: the tier fixes both the capacity and the per-operation amount. */
    public static BaseContainerCreator<AttachedFluids, ComponentBackedFluidTank> fluidTanks(int capacity) {
        return FluidTanksBuilder.builder()
              .addTank((type, stack, index) -> new ComponentBackedFluidTank(stack, index,
                    ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
                    () -> capacity, () -> capacity))
              .build();
    }

    /** Gas tank attachment of one pipette tier. */
    public static BaseContainerCreator<AttachedChemicals, ComponentBackedChemicalTank> chemicalTanks(int capacity) {
        return ChemicalTanksBuilder.builder()
              .addTank((type, stack, index) -> new ComponentBackedChemicalTank(stack, index,
                    ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
                    () -> (long) capacity, () -> (long) capacity, null))
              .build();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.mekanismgizmo.pipette_capacity", capacity)
              .withStyle(ChatFormatting.DARK_GRAY));
    }
}
