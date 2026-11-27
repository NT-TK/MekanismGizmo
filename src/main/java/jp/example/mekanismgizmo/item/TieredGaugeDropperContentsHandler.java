package jp.example.mekanismgizmo.item;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.DynamicHandler.InteractPredicate;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicGasHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicInfusionHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicPigmentHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicSlurryHandler;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitGasTank;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitInfusionTank;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitPigmentTank;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitSlurryTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler.RateLimitFluidTank;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.capabilities.merged.MergedTankContentsHandler;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Gauge dropper contents with a per-tier fluid and gas capacity.
 * <p>
 * Mekanism 1.20.1's own gauge dropper hard-codes both tanks to sixteen buckets. This handler is the same merged
 * fluid/gas tank setup, but its capacity and transfer rate come from the pipette tier instead. The NBT keys are the
 * ones Mekanism uses, so a pipette can still be filled and emptied by Mekanism's own GUI handling.
 */
public class TieredGaugeDropperContentsHandler extends MergedTankContentsHandler<MergedTank>
      implements IMekanismFluidHandler, IFluidHandlerItem {

    private final List<IExtendedFluidTank> fluidTanks;

    public TieredGaugeDropperContentsHandler(int capacity) {
        int transferRate = Math.max(256, capacity / 8);
        mergedTank = MergedTank.create(
              new RateLimitFluidTank(() -> transferRate, () -> capacity, this),
              new RateLimitGasTank(() -> transferRate, () -> capacity, ChemicalTankBuilder.GAS.alwaysTrueBi,
                    ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue, null,
                    gasHandler = new DynamicGasHandler(side -> gasTanks, InteractPredicate.ALWAYS_TRUE,
                          InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.GAS_TANKS, gasTanks))),
              new RateLimitInfusionTank(() -> transferRate, () -> capacity, ChemicalTankBuilder.INFUSION.alwaysTrueBi,
                    ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrue,
                    infusionHandler = new DynamicInfusionHandler(side -> infusionTanks, InteractPredicate.ALWAYS_TRUE,
                          InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.INFUSION_TANKS, infusionTanks))),
              new RateLimitPigmentTank(() -> transferRate, () -> capacity, ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
                    ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrue,
                    pigmentHandler = new DynamicPigmentHandler(side -> pigmentTanks, InteractPredicate.ALWAYS_TRUE,
                          InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.PIGMENT_TANKS, pigmentTanks))),
              new RateLimitSlurryTank(() -> transferRate, () -> capacity, ChemicalTankBuilder.SLURRY.alwaysTrueBi,
                    ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrue,
                    slurryHandler = new DynamicSlurryHandler(side -> slurryTanks, InteractPredicate.ALWAYS_TRUE,
                          InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.SLURRY_TANKS, slurryTanks)))
        );
        this.fluidTanks = Collections.singletonList(mergedTank.getFluidTank());
    }

    @Override
    protected void load() {
        super.load();
        ItemDataUtils.readContainers(getStack(), NBTConstants.FLUID_TANKS, getFluidTanks(null));
    }

    @NotNull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Override
    public void onContentsChanged() {
        onContentsChanged(NBTConstants.FLUID_TANKS, fluidTanks);
    }

    @NotNull
    @Override
    public ItemStack getContainer() {
        return getStack();
    }

    @Override
    protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
        super.gatherCapabilityResolvers(consumer);
        consumer.accept(BasicCapabilityResolver.constant(ForgeCapabilities.FLUID_HANDLER_ITEM, this));
    }
}
