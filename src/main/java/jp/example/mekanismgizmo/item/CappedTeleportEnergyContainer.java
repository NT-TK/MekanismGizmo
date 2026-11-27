package jp.example.mekanismgizmo.item;

import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Energy container of the Improved Portable Teleporter, with a ceiling on how much a single teleport may cost.
 * <p>
 * Mekanism asks the item's energy container to extract the full distance based cost twice: once as a simulation for
 * the screen and the packet check, and once for real when the teleport is committed. Both calls use
 * {@link AutomationType#MANUAL}. A request above the ceiling is reported as fully available when the ceiling is
 * affordable, but only the ceiling is actually removed. Every other interaction (charging, the energy bar, the
 * tooltip) behaves like Mekanism's own container.
 */
public class CappedTeleportEnergyContainer extends VariableCapacityEnergyContainer {

    private final FloatingLongSupplier chargeRate;
    private final FloatingLong maxTeleportCost;

    public CappedTeleportEnergyContainer(FloatingLongSupplier capacity, FloatingLongSupplier chargeRate,
          Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert,
          @Nullable IContentsListener listener, FloatingLong maxTeleportCost) {
        super(capacity, canExtract, canInsert, listener);
        this.chargeRate = chargeRate;
        this.maxTeleportCost = maxTeleportCost;
    }

    @Override
    protected FloatingLong getRate(@Nullable AutomationType automationType) {
        //Manual interaction may bypass the item's normal charge rate, exactly like Mekanism's RateLimitEnergyHandler.
        return automationType == null || automationType == AutomationType.MANUAL ? super.getRate(automationType) : chargeRate.get();
    }

    @Override
    public FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
        if (automationType != AutomationType.MANUAL || amount.smallerOrEqual(maxTeleportCost)) {
            return super.extract(amount, action, automationType);
        }
        if (getEnergy().smallerThan(maxTeleportCost)) {
            //Not even the ceiling is affordable, so report what is really there and let Mekanism fail the teleport.
            return super.extract(amount, action, automationType);
        }
        if (action.simulate()) {
            return amount;
        }
        FloatingLong extracted = super.extract(maxTeleportCost, Action.EXECUTE, automationType);
        return extracted.greaterOrEqual(maxTeleportCost) ? amount : extracted;
    }
}
