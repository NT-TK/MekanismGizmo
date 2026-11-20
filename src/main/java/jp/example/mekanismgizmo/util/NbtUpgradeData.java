package jp.example.mekanismgizmo.util;

import mekanism.common.upgrade.IUpgradeData;
import net.minecraft.nbt.CompoundTag;

/**
 * Upgrade payload carried by Mekanism's tier installer. The machine is saved without metadata and loaded again on
 * the next tier, which keeps the inventory, energy buffer, fluid/chemical tanks, side configuration, owner and the
 * installed upgrades (only the slot count of the new tier differs).
 */
public record NbtUpgradeData(CompoundTag tag) implements IUpgradeData {
}
