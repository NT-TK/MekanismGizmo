package jp.example.mekanismgizmo.block;

import java.util.List;
import jp.example.mekanismgizmo.registry.ModSideConfigs;
import jp.example.mekanismgizmo.util.MachineSideDefaults;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismAPITags;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wind generator fusion machine (mekanismgizmo:wind_fusion).
 * <p>
 * The block fuses up to {@link #SLOT_COUNT} Mekanism wind generators into a single machine: every inserted wind
 * generator produces exactly what the same wind generator would produce at this position, so one generator gives one
 * generator worth of power, two give twice that, and a full machine gives twenty-seven times that. Like Mekanism's own
 * wind generator the output depends on the height of the machine (and therefore on the sky, the weather and the
 * dimension), and the produced energy is stored in an internal buffer and pushed to cables on every side.
 * <p>
 * Everything else - the side configuration, redstone control, security, the energy item slot and the data synced to the
 * screen - comes from Mekanism's configurable machine base.
 */
public class WindFusionBlockEntity extends TileEntityConfigurableMachine {
    /** Number of wind generators one block can fuse: three rows of nine, one generator per slot. */
    public static final int SLOT_COUNT = 27;
    public static final int COLUMNS = 9;
    public static final int ROWS = SLOT_COUNT / COLUMNS;
    /** Energy the machine can buffer: Mekanism's own wind generator storage, once per fused generator. */
    public static final long BUFFER_PER_GENERATOR = 200_000L;
    public static final long ENERGY_CAPACITY = BUFFER_PER_GENERATOR * SLOT_COUNT;
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
    private int activeGenerators;
    /** Power one fused generator produces here, synced to the screen. */
    private int generationPerGenerator;
    private boolean blacklistDimension;

    public WindFusionBlockEntity(Holder<Block> block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        configComponent.setupItemIOConfig(List.of(generatorSlots), List.of(), energySlot, false);
        // The machine is a generator: its energy leaves through every side that is configured as "Energy"
        configComponent.setupOutputConfig(TransmissionType.ENERGY, energyContainer);
        // The machine has no item or fluid outputs, but the configurable machine base expects the component
        ejectorComponent = new TileComponentEjector(this);
        MachineSideDefaults.applyIfUnconfigured(configComponent, ModSideConfigs.WIND_FUSION);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        // Items dropped by an older version (or copied from an unconfigured machine) can carry an all-"None" side
        // configuration; repair it so the machine still exports its energy.
        MachineSideDefaults.applyIfUnconfigured(getConfig(), ModSideConfigs.WIND_FUSION);
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        generatorSlots = new WindGeneratorSlot[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            builder.addSlot(generatorSlots[i] = new WindGeneratorSlot(listener, slotX(i), slotY(i)));
        }
        builder.addSlot(energySlot = EnergyInventorySlot.drain(energyContainer, listener, ENERGY_SLOT_X, ENERGY_SLOT_Y));
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = BasicEnergyContainer.output(ENERGY_CAPACITY, listener));
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
        // Dimensions tagged as windless (Mekanism's own tag) never produce power
        blacklistDimension = level != null && level.dimensionTypeRegistration().is(MekanismAPITags.DimensionTypes.NO_WIND);
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.drainContainer();

        activeGenerators = countGenerators();
        if (ticker % REFRESH_INTERVAL == 0) {
            // The output only changes when the height, the sky or the weather changes, so Mekanism recalculates it
            // once a second as well
            multiplier = getMultiplier();
            generationPerGenerator = getGenerationPerGenerator();
        }
        boolean generating = canFunction() && !blacklistDimension && activeGenerators > 0 && generationPerGenerator > 0;
        setActive(generating);
        if (generating && energyContainer.getNeeded() > 0) {
            // One generator worth per fused generator: a full machine produces 27 times a single wind generator
            energyContainer.insert((long) activeGenerators * generationPerGenerator, Action.EXECUTE, AutomationType.INTERNAL);
        }
        return sendUpdatePacket;
    }

    /** Number of wind generators inside the machine. */
    public int countGenerators() {
        if (generatorSlots == null) {
            return 0;
        }
        int count = 0;
        for (WindGeneratorSlot slot : generatorSlots) {
            count += slot.getCount();
        }
        return count;
    }

    /** Power a single fused generator produces at this position, mirroring Mekanism's wind generator. */
    public int getGenerationPerGenerator() {
        return (int) MathUtils.clampToLong(MekanismGeneratorsConfig.generators.windGenerationMin.get() * multiplier);
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
        int minY = level.getMinBuildHeight();
        int maxY = Math.min(level.getMaxBuildHeight(), minY + level.dimensionType().logicalHeight()) - 1;
        int generationMinY = Math.max(MekanismGeneratorsConfig.generators.windGenerationMinY.get(), minY);
        int generationMaxY = Math.min(MekanismGeneratorsConfig.generators.windGenerationMaxY.get(), maxY);
        if (generationMaxY <= generationMinY) {
            // A configuration without a height range behaves like a single wind generator
            return 1;
        }
        int height = Math.min(generationMaxY, Math.max(generationMinY, above.getY()));
        long minGeneration = MekanismGeneratorsConfig.generators.windGenerationMin.get();
        long maxGeneration = MekanismGeneratorsConfig.generators.windGenerationMax.get();
        double generationPerBlock = (maxGeneration - minGeneration) / (double) (generationMaxY - generationMinY);
        double generation = minGeneration + generationPerBlock * (height - generationMinY);
        return generation / (double) minGeneration;
    }

    public BasicEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    /** Number of fused generators the screen shows. */
    public int getActiveGenerators() {
        return activeGenerators;
    }

    /** Power one fused generator produces here, as shown on the screen. */
    public int getGenerationPerGeneratorSynced() {
        return generationPerGenerator;
    }

    public WindGeneratorSlot[] getGeneratorSlots() {
        return generatorSlots;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> activeGenerators, value -> activeGenerators = value));
        container.track(SyncableInt.create(() -> generationPerGenerator, value -> generationPerGenerator = value));
        container.track(SyncableBoolean.create(() -> blacklistDimension, value -> blacklistDimension = value));
    }
}
