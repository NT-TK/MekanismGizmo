package jp.example.mekanismgizmo.item;

import java.util.List;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.merged.GaugeDropperContentsHandler;
import mekanism.common.item.ItemGaugeDropper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Tiered gauge dropper ("pipette"). 1.20.1's gauge dropper stores its contents in item NBT; this item replaces its
 * fixed sixteen-bucket contents handler with one whose capacity and transfer rate follow the tier.
 */
public class PipetteItem extends ItemGaugeDropper {

    private final int capacity;

    public PipetteItem(Item.Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        super.gatherCapabilities(capabilities, stack, nbt);
        capabilities.removeIf(capability -> capability instanceof GaugeDropperContentsHandler);
        capabilities.add(new TieredGaugeDropperContentsHandler(capacity));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.mekanismgizmo.pipette_capacity", capacity)
              .withStyle(ChatFormatting.DARK_GRAY));
    }
}