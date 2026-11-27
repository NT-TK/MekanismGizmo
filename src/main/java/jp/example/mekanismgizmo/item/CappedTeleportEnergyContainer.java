package jp.example.mekanismgizmo.item;

import java.util.function.LongSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.common.attachments.containers.energy.ComponentBackedEnergyContainer;
import net.minecraft.world.item.ItemStack;

/**
 * Energy container of the improved portable teleporter, with a ceiling on how much a single teleport may cost.
 * <p>
 * Mekanism charges a teleport by asking the item's energy container to extract the full distance based cost, twice:
 * once as a simulation (both for the "ready" status of the screen and for the teleport packet's own check) and once for
 * real when the teleport is committed. Both of those calls use {@link AutomationType#MANUAL} - nothing else can pull
 * energy out of gear - so this container reacts to a single manual request that is larger than the ceiling:
 * <ul>
 *     <li>the <b>simulation</b> reports the whole request as available as long as the ceiling can be paid, which is what
 *     makes the screen say "ready" and lets the packet through</li>
 *     <li>the <b>execution</b> takes the ceiling, but still reports the whole request as paid</li>
 * </ul>
 * The result is that a teleport never costs more than {@link #maxTeleportCost}, however far away the destination is,
 * while every other use of the item's buffer (charging, the energy bar, the tooltip) is untouched. The container is
 * attached to the improved portable teleporter only, so Mekanism's own portable teleporter keeps its uncapped cost.
 */
public class CappedTeleportEnergyContainer extends ComponentBackedEnergyContainer {

    private final long maxTeleportCost;

    public CappedTeleportEnergyContainer(ItemStack stack, int index, java.util.function.Predicate<AutomationType> canExtract,
          java.util.function.Predicate<AutomationType> canInsert, LongSupplier rate, LongSupplier capacity, long maxTeleportCost) {
        super(stack, index, canExtract, canInsert, rate, capacity);
        this.maxTeleportCost = maxTeleportCost;
    }

    @Override
    public long extract(long amount, Action action, AutomationType automationType) {
        if (automationType != AutomationType.MANUAL || amount <= maxTeleportCost) {
            return super.extract(amount, action, automationType);
        }
        // A teleport that costs more than the ceiling
        if (getEnergy() < maxTeleportCost) {
            // Not even the ceiling is affordable, so report what is really there and let Mekanism fail the teleport
            return super.extract(amount, action, automationType);
        }
        if (action.simulate()) {
            return amount;
        }
        long extracted = super.extract(maxTeleportCost, Action.EXECUTE, automationType);
        return extracted >= maxTeleportCost ? amount : extracted;
    }
}
