package jp.example.mekanismgizmo.item;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.capabilities.energy.item.ItemStackEnergyHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Replaces Mekanism's item energy handler with one whose manual extraction is capped per teleport.
 */
public class CappedTeleportEnergyHandler extends ItemStackEnergyHandler {

    private final IEnergyContainer container;

    public CappedTeleportEnergyHandler(FloatingLongSupplier capacity, FloatingLongSupplier chargeRate,
          Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert,
          FloatingLong maxTeleportCost) {
        this.container = new CappedTeleportEnergyContainer(capacity, chargeRate, canExtract, canInsert, this, maxTeleportCost);
    }

    @Override
    protected List<IEnergyContainer> getInitialContainers() {
        return Collections.singletonList(container);
    }
}
