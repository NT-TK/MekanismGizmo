package jp.example.mekanismgizmo.item;

import java.util.List;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.ItemStackEnergyHandler;
import mekanism.common.item.ItemPortableTeleporter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Mekanism's portable teleporter with a hundred times the internal buffer and charge rate.
 */
public class ImprovedPortableTeleporterItem extends ItemPortableTeleporter {

    public static final long STORAGE_MULTIPLIER = 100L;
    /** Ceiling for a single teleport: however far the destination is, one jump costs at most this much energy. */
    public static final long MAX_TELEPORT_COST = 10_000_000L;

    public ImprovedPortableTeleporterItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    protected FloatingLong getMaxEnergy(ItemStack stack) {
        return super.getMaxEnergy(stack).multiply(STORAGE_MULTIPLIER);
    }

    @Override
    protected FloatingLong getChargeRate(ItemStack stack) {
        return super.getChargeRate(stack).multiply(STORAGE_MULTIPLIER);
    }

    @Override
    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        super.gatherCapabilities(capabilities, stack, nbt);
        //Replace Mekanism's rate-limited energy handler with one that also caps a single manual teleport payment.
        capabilities.removeIf(capability -> capability instanceof ItemStackEnergyHandler);
        capabilities.add(new CappedTeleportEnergyHandler(
              () -> getMaxEnergy(stack), () -> getChargeRate(stack),
              BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(),
              FloatingLong.create(MAX_TELEPORT_COST)));
    }
}
