package jp.example.mekanismgizmo.item;

import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.energy.AttachedEnergy;
import mekanism.common.attachments.containers.energy.ComponentBackedEnergyContainer;
import mekanism.common.attachments.containers.energy.EnergyContainersBuilder;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.ItemPortableTeleporter;
import net.minecraft.world.item.Item;

/**
 * Mekanism's portable teleporter with a hundred times the internal buffer.
 * <p>
 * Everything else is Mekanism's own: the item is a straight subclass, so it uses Mekanism's teleporter screen, its
 * frequency network (the same frequencies the teleporter blocks use), the teleport packet with Mekanism's own distance
 * based energy cost, the owner/security handling and the energy charge from any Mekanism energy item or cube. Only the
 * energy container that Mekanism attaches to the item is replaced, with a capacity and a charge rate of
 * {@code portableTeleporterMaxEnergy} and {@code portableTeleporterChargeRate} multiplied by
 * {@link #STORAGE_MULTIPLIER}.
 */
public class ImprovedPortableTeleporterItem extends ItemPortableTeleporter {

    /** Mekanism's portable teleporter holds 1,000,000 FE; this one holds a hundred times as much. */
    public static final long STORAGE_MULTIPLIER = 100L;
    /** Ceiling for a single teleport: however far the destination is, one jump costs at most this much energy. */
    public static final long MAX_TELEPORT_COST = 10_000_000L;

    public ImprovedPortableTeleporterItem(Item.Properties properties) {
        super(properties);
    }

    /** Buffer size, in FE (Mekanism's joules are 1:1 with FE for items). */
    public static long maxEnergy() {
        return MekanismConfig.gear.portableTeleporterMaxEnergy.get() * STORAGE_MULTIPLIER;
    }

    /** How much energy the item accepts per tick, scaled the same way so it fills as quickly as Mekanism's. */
    public static long chargeRate() {
        return MekanismConfig.gear.portableTeleporterChargeRate.get() * STORAGE_MULTIPLIER;
    }

    /**
     * The energy container Mekanism attaches to the item. It is built like Mekanism's own portable teleporter one
     * ({@code addBasic(chargeRate, maxEnergy)}: manual extraction only, insertion from anywhere) with the scaled values,
     * but uses {@link CappedTeleportEnergyContainer} so that a single teleport can never cost more than
     * {@link #MAX_TELEPORT_COST}.
     */
    public static BaseContainerCreator<AttachedEnergy, ComponentBackedEnergyContainer> energyContainers() {
        return EnergyContainersBuilder.builder()
              .addContainer((type, stack, index) -> new CappedTeleportEnergyContainer(stack, index,
                    BasicEnergyContainer.manualOnly, ConstantPredicates.alwaysTrue(),
                    ImprovedPortableTeleporterItem::chargeRate, ImprovedPortableTeleporterItem::maxEnergy,
                    MAX_TELEPORT_COST))
              .build();
    }
}
