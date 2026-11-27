package jp.example.mekanismgizmo.block;

import java.util.List;
import jp.example.mekanismgizmo.registry.ModSideConfigs;
import jp.example.mekanismgizmo.util.HeatGeneratorUnits;
import jp.example.mekanismgizmo.util.MachineSideDefaults;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Heat generator fusion machine (mekanismgizmo:heat_fusion).
 * <p>
 * The block fuses Mekanism heat generators into a single machine, the same way the wind generator fusion does it for
 * wind generators: every inserted heat generator produces exactly what a heat generator would produce at this
 * position, so one generator gives one generator's worth of power and a full machine gives 1,728 times that.
 * Compressed heat generators of Compact Mekanism Machines count as the number of generators they replace. Like
 * Mekanism's own heat generator the machine is heated by
 * <ul>
 *     <li>lava next to it (Mekanism's {@code heatGenerationLava} per adjacent lava block, plus
 *     {@code heatGenerationNether} in an ultra warm dimension) and</li>
 *     <li>lava piped into its internal tank, which is burned at {@code heatGenerationFluidRate} mB per tick for
 *     {@code heatGeneration} heat.</li>
 * </ul>
 * The heat simulation is Mekanism's own: one heat capacitor with the heat generator's capacity (10) and insulation,
 * the same {@code 0.5 * (temperature - ambient)} conversion at the same thermal efficiency, and the same production
 * cap ({@code heatGenerationLava * 7 + heatGenerationNether + heatGeneration}). Because the capacitor, the heat input
 * and the losses are all one generator's worth, the temperature behaves exactly like a single heat generator's and the
 * produced energy is simply multiplied by the number of fused generators.
 * <p>
 * Everything else - the side configuration, redstone control, security, the energy item slot and the data synced to the
 * screen - comes from Mekanism's configurable machine base.
 */
public class HeatFusionBlockEntity extends GizmoMachineBlockEntity {
    /** Number of heat generator slots: three rows of nine. */
    public static final int SLOT_COUNT = 27;
    public static final int COLUMNS = 9;
    public static final int ROWS = SLOT_COUNT / COLUMNS;
    /** Every slot holds a full stack of heat generators, so a full machine fuses 27 x 64 of them. */
    public static final int MAX_STACK = 64;
    /** Largest number of generators the machine can hold: 27 stacks of the most compressed heat generator there is. */
    public static final long MAX_UNITS = (long) SLOT_COUNT * MAX_STACK * HeatGeneratorUnits.LARGEST_COMPRESSION;

    // --- Mekanism's heat generator values ---
    /** Heat capacity of a single heat generator. */
    public static final double HEAT_CAPACITY = 10D;
    public static final double INVERSE_CONDUCTION_COEFFICIENT = 5D;
    public static final double INVERSE_INSULATION_COEFFICIENT = 100D;
    /** Energy stored per fused generator (Mekanism's heat generator holds 160,000 FE). */
    public static final long BUFFER_PER_GENERATOR = 160_000L;
    public static final long ENERGY_CAPACITY = BUFFER_PER_GENERATOR * MAX_UNITS;
    /** Lava tank: one slot's worth of Mekanism heat generator tanks (24,000 mB each, 64 in a stack). */
    public static final int LAVA_CAPACITY = 24_000 * MAX_STACK;

    // --- screen layout ---
    public static final int SLOT_X = 8;
    public static final int SLOT_Y = 18;
    /** The lava gauge sits a little further right so the slot grid keeps a visible margin. */
    public static final int LAVA_GAUGE_X = 182;
    public static final int LAVA_GAUGE_Y = 13;
    public static final int POWER_BAR_X = 202;
    public static final int POWER_BAR_Y = 16;
    public static final int ENERGY_SLOT_X = 214;
    public static final int ENERGY_SLOT_Y = 18;
    public static final int INFO_Y = 76;
    /** Width available for the info line (from the left border to the lava gauge). */
    public static final int MEASURE_WIDTH = 168;
    public static final int GUI_WIDTH = 242;
    public static final int GUI_HEIGHT = 190;
    /** Player inventory, centred under the 27 slots and pushed down so the info line has room above it. */
    public static final int INVENTORY_X = 40;
    public static final int INVENTORY_Y = 108;

    private HeatGeneratorSlot[] generatorSlots;
    private EnergyInventorySlot energySlot;
    private BasicEnergyContainer energyContainer;
    private BasicFluidTank lavaTank;
    private BasicHeatCapacitor heatCapacitor;

    /** Number of fused generators, synced to the screen. */
    private int activeUnits;
    /** Number of generators the machine could hold with the items it contains, synced to the screen. */
    private int maxUnits = SLOT_COUNT * MAX_STACK;
    /** Energy produced in the last tick, synced to the screen. */
    private int generationRate;
    /** Temperature in tenths of a Kelvin, synced to the screen. */
    private int temperatureTenths;

    public HeatFusionBlockEntity(Holder<Block> block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        // The generator slots are internal storage: they are not part of the item configuration, so pipes can only
        // reach the energy slot
        configComponent.setupItemIOConfig(List.of(), List.of(), energySlot, false);
        // The machine is a generator: its energy leaves through every side that is configured as "Energy"
        configComponent.setupOutputConfig(TransmissionType.ENERGY, energyContainer);
        configComponent.setupInputConfig(TransmissionType.FLUID, lavaTank);
        ejectorComponent = new TileComponentEjector(this);
        MachineSideDefaults.applyIfUnconfigured(configComponent, ModSideConfigs.HEAT_FUSION);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        MachineSideDefaults.applyIfUnconfigured(getConfig(), ModSideConfigs.HEAT_FUSION);
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        generatorSlots = new HeatGeneratorSlot[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            builder.addSlot(generatorSlots[i] = new HeatGeneratorSlot(listener, slotX(i), slotY(i)));
        }
        builder.addSlot(energySlot = EnergyInventorySlot.drain(energyContainer, listener, ENERGY_SLOT_X, ENERGY_SLOT_Y));
        return builder.build();
    }

    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this);
        lavaTank = BasicFluidTank.input(LAVA_CAPACITY, fluid -> fluid.is(Fluids.LAVA), listener);
        builder.addTank(lavaTank);
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = BasicEnergyContainer.output(ENERGY_CAPACITY, listener));
        return builder.build();
    }

    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(this::getDirection);
        heatCapacitor = BasicHeatCapacitor.create(HEAT_CAPACITY, INVERSE_CONDUCTION_COEFFICIENT,
              INVERSE_INSULATION_COEFFICIENT, ambientTemperature, listener);
        builder.addCapacitor(heatCapacitor);
        return builder.build();
    }

    /**
     * Mekanism's heat generator fully insulates its bottom face (that is where the lava usually sits), so its heat does
     * not leak downwards. Our machine does the same, otherwise its temperature - and with it the production - would be
     * slightly lower than a real heat generator's.
     */
    @Override
    public double getInverseInsulation(int capacitor, Direction direction) {
        return direction == Direction.DOWN ? 0 : super.getInverseInsulation(capacitor, direction);
    }

    @Override
    public double getTotalInverseInsulation(Direction direction) {
        return direction == Direction.DOWN ? 0 : super.getTotalInverseInsulation(direction);
    }

    /** x position of the {@code index}-th heat generator slot (nine per row). */
    public static int slotX(int index) {
        return SLOT_X + (index % COLUMNS) * 18;
    }

    /** y position of the {@code index}-th heat generator slot (three rows). */
    public static int slotY(int index) {
        return SLOT_Y + (index / COLUMNS) * 18;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
    }

    /**
     * Heat Mekanism's heat generator gains per tick from its surroundings: {@code heatGenerationLava} for every
     * adjacent lava block (including the block the generator itself stands in), plus {@code heatGenerationNether} in an
     * ultra warm dimension.
     */
    private double getBoost() {
        if (level == null) {
            return 0;
        }
        long perLava = MekanismGeneratorsConfig.generators.heatGenerationLava.get();
        long boost = 0;
        if (perLava != 0) {
            int lava = 0;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (Direction direction : EnumUtils.DIRECTIONS) {
                pos.setWithOffset(worldPosition, direction);
                if (WorldUtils.getFluidState(level, pos).filter(state -> state.is(FluidTags.LAVA)).isPresent()) {
                    lava++;
                }
            }
            if (getBlockState().getFluidState().is(FluidTags.LAVA)) {
                lava++;
            }
            boost = perLava * lava;
        }
        if (level.dimensionType().ultraWarm()) {
            boost += MekanismGeneratorsConfig.generators.heatGenerationNether.get();
        }
        return boost;
    }

    /** Highest production of a single heat generator, exactly like Mekanism's own {@code MAX_PRODUCTION}. */
    private static long maxProductionPerGenerator() {
        return MekanismGeneratorsConfig.generators.heatGenerationLava.get() * (EnumUtils.DIRECTIONS.length + 1)
              + MekanismGeneratorsConfig.generators.heatGenerationNether.get()
              + MekanismGeneratorsConfig.generators.heatGeneration.get();
    }

    /** Number of heat generators (compressed ones counted as what they replace) inside the machine. */
    public int countGenerators() {
        if (generatorSlots == null) {
            activeUnits = 0;
            maxUnits = SLOT_COUNT * MAX_STACK;
            return 0;
        }
        long units = 0;
        int largest = 1;
        for (HeatGeneratorSlot slot : generatorSlots) {
            if (slot.isEmpty()) {
                continue;
            }
            int perItem = HeatGeneratorUnits.of(slot.getStack());
            largest = Math.max(largest, perItem);
            units += (long) perItem * slot.getCount();
        }
        activeUnits = (int) Math.min(units, Integer.MAX_VALUE);
        maxUnits = (int) Math.min((long) SLOT_COUNT * MAX_STACK * largest, Integer.MAX_VALUE);
        return activeUnits;
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.drainContainer();
        countGenerators();

        // Heat input: lava around the machine (x units) and lava burned in the tank (x units)
        int units = activeUnits;
        int produced = 0;
        double perGeneratorHeat = getBoost();
        if (units > 0 && canFunction() && energyContainer.getNeeded() > 0) {
            int rate = MekanismGeneratorsConfig.generators.heatGenerationFluidRate.get();
            long needed = (long) rate * units;
            if (needed > 0) {
                // A full machine of compressed generators needs more lava than the tank can hold. Burn what is
                // there and heat the same fraction of the generators instead of refusing to burn anything.
                int request = (int) Math.min(needed, lavaTank.getFluidAmount());
                if (request > 0 && lavaTank.extract(request, Action.SIMULATE, AutomationType.INTERNAL).getAmount() == request) {
                    lavaTank.extract(request, Action.EXECUTE, AutomationType.INTERNAL);
                    perGeneratorHeat += MekanismGeneratorsConfig.generators.heatGeneration.get()
                          * (request / (double) needed);
                }
            }
        }
        if (units > 0) {
            // The capacitor is one generator's worth, so the temperature is exactly a single heat generator's
            heatCapacitor.handleHeat(perGeneratorHeat);
            double ambient = ambientTemperature.getAsDouble();
            double temperature = getTotalTemperature();
            double efficiency = 1D - Math.min(ambient, temperature) / Math.max(ambient, temperature);
            double transfer = 0.5D * (temperature - ambient);
            heatCapacitor.handleHeat(-transfer);
            long perGenerator = Math.min(maxProductionPerGenerator(), MathUtils.clampToLong(Math.abs(transfer) * efficiency));
            if (canFunction() && perGenerator > 0) {
                // Mekanism's insert returns the amount that did not fit, so the production is the difference
                long wanted = MathUtils.multiplyClamped(perGenerator, units);
                long accepted = wanted - energyContainer.insert(wanted, Action.EXECUTE, AutomationType.INTERNAL);
                produced = (int) Math.min(Integer.MAX_VALUE, accepted);
            }
            // Mekanism's own losses, so the steady state temperature matches a real heat generator
            simulateEnvironment();
            simulateAdjacent();
        } else {
            // With no generators there is nothing to heat: discard the old capacitor charge instead of freezing it.
            // Otherwise removing every generator and reinserting them later would release the stored heat multiplied
            // by the new generator count.
            double ambientHeat = ambientTemperature.getAsDouble() * heatCapacitor.getHeatCapacity();
            if (heatCapacitor.getHeat() > ambientHeat) {
                heatCapacitor.setHeat(ambientHeat);
            }
        }

        generationRate = produced;
        temperatureTenths = (int) Math.round(getTotalTemperature() * 10D);
        // Lava around the block also produces power, so "active" must follow the real output, not the tank burner
        setActive(produced > 0);
        if (canFunction()) {
            emitEnergy(energyContainer, getMaxOutput());
        }
        return sendUpdatePacket;
    }

    /** Upper bound for one tick of output: twice the maximum of the installed generators. */
    private long getMaxOutput() {
        return MathUtils.multiplyClamped(2L,
              MathUtils.multiplyClamped(Math.max(1, activeUnits), Math.max(1L, maxProductionPerGenerator())));
    }

    public BasicEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public BasicFluidTank getLavaTank() {
        return lavaTank;
    }

    public IHeatCapacitor getHeatCapacitor() {
        return heatCapacitor;
    }

    public HeatGeneratorSlot[] getGeneratorSlots() {
        return generatorSlots;
    }

    /** Number of fused generators, as shown on the screen. */
    public int getActiveGenerators() {
        return activeUnits;
    }

    /** Number of generators the machine could hold with the items it contains, as shown on the screen. */
    public int getMaxGenerators() {
        return maxUnits;
    }

    /** Energy produced in the last tick, as shown on the screen. */
    public int getGenerationRate() {
        return generationRate;
    }

    /** Temperature in tenths of a Kelvin, as shown on the screen. */
    public int getTemperatureTenths() {
        return temperatureTenths;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> activeUnits, value -> activeUnits = value));
        container.track(SyncableInt.create(() -> maxUnits, value -> maxUnits = value));
        container.track(SyncableInt.create(this::getGenerationRate, value -> generationRate = value));
        container.track(SyncableInt.create(this::getTemperatureTenths, value -> temperatureTenths = value));
    }
}
