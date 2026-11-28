package jp.example.mekanismgizmo.block;

import java.util.List;
import jp.example.mekanismgizmo.util.WindGeneratorUnits;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.IContentsListener;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.core.BlockPos;
import mekanism.api.providers.IBlockProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wind generator fusion machine (mekanismgizmo:wind_fusion).
 * <p>
 * The block fuses up to {@link #SLOT_COUNT} Mekanism wind generators into a single machine: every inserted wind
 * generator produces exactly what the same wind generator would produce at this position, so one generator gives one
 * generator worth of power, two give twice that, and a full machine gives twenty-seven times that. The compressed wind
 * generators of Compact Mekanism Machines are accepted as well and count as the number of generators they replace (an
 * {@code x532480} block counts as 532480 generators), because that is exactly what they produce on their own. Like
 * Mekanism's own wind generator the output depends on the height of the machine (and therefore on the sky and the
 * dimension), and the produced energy is stored in an internal buffer and pushed to cables on every side.
 * <p>
 * Everything else - the side configuration, redstone control, security, the energy item slot and the data synced to the
 * screen - comes from Mekanism's configurable machine base.
 */
public class WindFusionBlockEntity extends GizmoMachineBlockEntity {
    /** Number of wind generator slots: three rows of nine. */
    public static final int SLOT_COUNT = 27;
    public static final int COLUMNS = 9;
    public static final int ROWS = SLOT_COUNT / COLUMNS;
    /** Every slot holds a full stack of wind generators, so a full machine fuses 27 x 64 of them. */
    public static final int MAX_STACK = 64;
    /**
     * Largest number of generators the machine can hold: 27 stacks of the most compressed wind generator there is
     * ({@code x532480}).
     */
    public static final long MAX_UNITS = (long) SLOT_COUNT * MAX_STACK * WindGeneratorUnits.LARGEST_COMPRESSION;
    /** Energy the machine can buffer: Mekanism's own wind generator storage, once per fusable generator. */
    public static final long BUFFER_PER_GENERATOR = 200_000L;
    public static final long ENERGY_CAPACITY = BUFFER_PER_GENERATOR * MAX_UNITS;
    /** How often the position dependent multiplier is recalculated (Mekanism uses the same interval). */
    public static final int REFRESH_INTERVAL = 20;
    /** Height above the machine that has to see the sky for it to catch wind, like Mekanism's wind generator. */
    public static final int WIND_HEIGHT = 4;

    // --- screen layout ---
    public static final int SLOT_X = 8;
    public static final int SLOT_Y = 18;
    public static final int ENERGY_SLOT_X = 176;
    public static final int ENERGY_SLOT_Y = 18;
    public static final int POWER_BAR_X = 196;
    public static final int POWER_BAR_Y = 16;
    public static final int INFO_Y = 76;
    public static final int GUI_WIDTH = 208;
    public static final int GUI_HEIGHT = 180;
    public static final int INVENTORY_X = 23;
    public static final int INVENTORY_Y = 98;

    private WindGeneratorSlot[] generatorSlots;
    private EnergyInventorySlot energySlot;
    private BasicEnergyContainer energyContainer;

    /** Wind strength multiplier at this position, 0 when the machine cannot catch any wind. */
    private double multiplier;
    /** Number of fused generators, synced to the screen. */
    private int activeUnits;
    /** Number of generators the machine could hold with the items it contains, synced to the screen. */
    private int maxUnits = SLOT_COUNT * MAX_STACK;
    /** Power one fused generator produces here, synced to the screen. */
    private long generationPerUnit;
    private boolean blacklistDimension;

    public WindFusionBlockEntity(IBlockProvider block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        // The generator slots are internal storage: they are not part of the item configuration, so pipes can only
        // reach the energy slot
        configComponent.setupItemIOConfig(List.of(), List.of(), energySlot, false);
        // The machine is a generator: its energy leaves through every side that is configured as "Energy"
        configComponent.setupOutputConfig(TransmissionType.ENERGY, energyContainer);
        // The machine has no item or fluid outputs, but the configurable machine base expects the component
        ejectorComponent = new TileComponentEjector(this);
        applySideDefaults();
    }


    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        generatorSlots = new WindGeneratorSlot[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            builder.addSlot(generatorSlots[i] = new WindGeneratorSlot(listener, slotX(i), slotY(i)));
        }
        builder.addSlot(energySlot = EnergyInventorySlot.drain(energyContainer, listener, ENERGY_SLOT_X, ENERGY_SLOT_Y));
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = BasicEnergyContainer.output(FloatingLong.create(ENERGY_CAPACITY), listener));
        return builder.build();
    }

    /** x position of the {@code index}-th wind generator slot (nine per row). */
    public static int slotX(int index) {
        return SLOT_X + (index % COLUMNS) * 18;
    }

    /** y position of the {@code index}-th wind generator slot (three rows). */
    public static int slotY(int index) {
        return SLOT_Y + (index / COLUMNS) * 18;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        // Match Mekanism's 1.20.1 wind generator: dimensions in its config blacklist never produce power
        blacklistDimension = level != null && MekanismGeneratorsConfig.generators.windGenerationDimBlacklist.get()
              .contains(level.dimension().location());
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.drainContainer();

        countUnits();
        if (ticker % REFRESH_INTERVAL == 0) {
            // The output only changes when the height, the sky or the weather changes, so Mekanism recalculates it
            // once a second as well
            multiplier = getMultiplier();
            generationPerUnit = getGenerationPerUnit();
        }
        boolean generating = MekanismUtils.canFunction(this) && !blacklistDimension && activeUnits > 0 && generationPerUnit > 0;
        setActive(generating);
        if (generating && !energyContainer.getNeeded().isZero()) {
            // One generator worth per fused generator: a full machine produces 27 times a single wind generator
            energyContainer.insert(FloatingLong.create(MathUtils.clampToLong((double) activeUnits * generationPerUnit)), Action.EXECUTE, AutomationType.INTERNAL);
        }
        if (MekanismUtils.canFunction(this)) {
            emitEnergy(energyContainer, FloatingLong.create(getMaxOutput()));
        }
    }

    /** Upper bound for one tick of output: twice the configured maximum of the installed generators. */
    private long getMaxOutput() {
        long perUnit = Math.max(1L, MekanismGeneratorsConfig.generators.windGenerationMax.get().longValue());
        return MathUtils.clampToLong(2D * Math.max(1, activeUnits) * perUnit);
    }

    /**
     * Recounts the wind generators inside the machine. Compressed wind generators count as the number of generators
     * they replace, and the displayed maximum follows the most compressed item inside, so a machine full of
     * compressed generators is not shown as "37 / 1,728".
     */
    private void countUnits() {
        if (generatorSlots == null) {
            activeUnits = 0;
            maxUnits = SLOT_COUNT * MAX_STACK;
            return;
        }
        long units = 0;
        int largest = 1;
        for (WindGeneratorSlot slot : generatorSlots) {
            if (slot.isEmpty()) {
                continue;
            }
            int perItem = WindGeneratorUnits.of(slot.getStack());
            largest = Math.max(largest, perItem);
            units += (long) perItem * slot.getCount();
        }
        activeUnits = (int) Math.min(units, Integer.MAX_VALUE);
        maxUnits = (int) Math.min((long) SLOT_COUNT * MAX_STACK * largest, Integer.MAX_VALUE);
    }

    /** Number of wind generators (compressed ones counted as what they replace) inside the machine. */
    public int countGenerators() {
        countUnits();
        return activeUnits;
    }

    /** Power a single fused generator produces at this position, mirroring Mekanism's wind generator. */
    public long getGenerationPerUnit() {
        double generation = MekanismGeneratorsConfig.generators.windGenerationMin.get().doubleValue() * multiplier;
        // windGenerationMin = 0 makes the multiplier infinite (or 0 * infinite NaN); don't let that turn into
        // a negative int or a fake positive output.
        if (!Double.isFinite(generation) || generation <= 0) {
            return 0;
        }
        return MathUtils.clampToLong(generation);
    }

    /**
     * Wind strength at this position as a multiple of Mekanism's {@code windGenerationMin}, copied from Mekanism's
     * {@code TileEntityWindGenerator}: the machine needs free sky {@link #WIND_HEIGHT} blocks above it, and the output
     * scales linearly with its height between the configured minimum and maximum heights.
     */
    private double getMultiplier() {
        if (level == null) {
            return 0;
        }
        BlockPos above = worldPosition.above(WIND_HEIGHT);
        if (!level.getFluidState(above).isEmpty() || !level.canSeeSky(above)) {
            return 0;
        }
        // Same clamping and interpolation as Mekanism's TileEntityWindGenerator in 1.20.1
        int minY = Math.max(MekanismGeneratorsConfig.generators.windGenerationMinY.get(), level.getMinBuildHeight());
        int maxY = Math.min(MekanismGeneratorsConfig.generators.windGenerationMaxY.get(), level.dimensionType().logicalHeight());
        if (maxY <= minY) {
            // A configuration without a height range behaves like a single wind generator
            return 1;
        }
        int height = Math.min(maxY, Math.max(minY, above.getY()));
        long minGeneration = MekanismGeneratorsConfig.generators.windGenerationMin.get().longValue();
        long maxGeneration = MekanismGeneratorsConfig.generators.windGenerationMax.get().longValue();
        double generationPerBlock = (maxGeneration - minGeneration) / (double) (maxY - minY);
        double generation = minGeneration + generationPerBlock * (height - minY);
        return generation / (double) minGeneration;
    }

    public BasicEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    /** Number of fused generators the screen shows. */
    public int getActiveGenerators() {
        return activeUnits;
    }

    /** Number of generators the machine could hold with the items it contains, as shown on the screen. */
    public int getMaxGenerators() {
        return maxUnits;
    }

    /** Power one fused generator produces here, as shown on the screen. */
    public long getGenerationPerGeneratorSynced() {
        return generationPerUnit;
    }

    /** Total output to display, zero in dimensions where the machine cannot generate wind. */
    public long getDisplayedGeneration() {
        return blacklistDimension ? 0L : MathUtils.clampToLong((double) activeUnits * generationPerUnit);
    }

    public WindGeneratorSlot[] getGeneratorSlots() {
        return generatorSlots;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> activeUnits, value -> activeUnits = value));
        container.track(SyncableInt.create(() -> maxUnits, value -> maxUnits = value));
        container.track(SyncableLong.create(() -> generationPerUnit, value -> generationPerUnit = value));
        container.track(SyncableBoolean.create(() -> blacklistDimension, value -> blacklistDimension = value));
    }
}