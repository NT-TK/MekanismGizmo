package jp.example.mekanismgizmo.block;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import mekanism.api.providers.IBlockProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

/**
 * Fluid injector: moves fluid between its internal tank and the containers put into its input slot.
 * <p>
 * It behaves like Mekanism's fluid tank (the mode button cycles Fill / Empty / Both), but it is a proper configurable
 * machine: side configuration, security, redstone control, the ejector and container attachments all come from
 * Mekanism.
 * <p>
 * One slot pair handles every container: buckets, Mekanism fluid tanks and any other item exposing a fluid handler are
 * converted through their fluid handler, while vanilla bottles (which have no usable water fluid handler) are converted
 * by {@link #handleBottle()} with {@link #BOTTLE_AMOUNT} mB of water each.
 */
public class FluidInjectorBlockEntity extends TieredMachineBlockEntity {
    /** Internal tank capacity (mB). */
    public static final int TANK_CAPACITY = 16_000;
    /** Amount of water moved per vanilla bottle. */
    public static final int BOTTLE_AMOUNT = 250;
    /** Energy used for one converted container. */
    public static final long ENERGY_PER_OPERATION = 25L;

    /** x/y layout of the container slot pairs (one pair per tier). */
    public static final int INPUT_X = 26;
    public static final int OUTPUT_X = 48;
    public static final int FIRST_ROW_Y = 20;

    public IExtendedFluidTank fluidTank;
    private MachineEnergyContainer<FluidInjectorBlockEntity> energyContainer;
    /** Container inputs: buckets, Mekanism fluid tanks, bottles, ... */
    private InputInventorySlot[] containerSlots;
    /** Converted containers produced by the matching input slot. */
    private OutputInventorySlot[] containerOutputSlots;
    private EnergyInventorySlot energySlot;

    private ContainerEditMode containerMode = ContainerEditMode.BOTH;

    public FluidInjectorBlockEntity(IBlockProvider block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        configComponent.setupItemIOConfig(List.of(containerSlots), List.of(containerOutputSlots), energySlot, false);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        configComponent.setupIOConfig(TransmissionType.FLUID, fluidTank, RelativeSide.BACK);
        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
        applySideDefaults();
    }


    @Override
    public IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
        fluidTank = BasicFluidTank.create(tankCapacity(parallel()), listener);
        builder.addTank(fluidTank);
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        int parallel = parallel();
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        containerSlots = new InputInventorySlot[parallel];
        containerOutputSlots = new OutputInventorySlot[parallel];
        // One input for every container type; the converted container always ends up in the slot to the right.
        for (int i = 0; i < parallel; i++) {
            builder.addSlot(containerSlots[i] = InputInventorySlot.at(this::isContainerInput, listener, INPUT_X, rowY(i)));
            builder.addSlot(containerOutputSlots[i] = OutputInventorySlot.at(listener, OUTPUT_X, rowY(i)));
        }
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener,
              ENERGY_SLOT_X, ENERGY_SLOT_Y));
        return builder.build();
    }

    /** Tank capacity of a tier: every parallel unit adds another 16,000 mB. */
    public static int tankCapacity(int parallel) {
        return TANK_CAPACITY * parallel;
    }

    /** y position of the {@code index}-th container slot pair. */
    public static int rowY(int index) {
        return FIRST_ROW_Y + index * 18;
    }

    // --- screen layout ---
    /** The tank gauge and the power bar are 52 pixels tall, so the player inventory never starts above this. */
    private static final int MIN_INVENTORY_Y = 84;
    /** Room between the last container row and the player inventory. */
    private static final int INVENTORY_MARGIN = 12;
    /** Height of the player inventory block inside the screen (label + 3 rows + gap + hotbar). */
    private static final int INVENTORY_HEIGHT = 166 - 84;
    public static final int GAUGE_X = 6;
    public static final int GAUGE_Y = 13;
    /** Mekanism's standard gauge is 16 x 58; it is stretched downwards per tier so it covers the whole slot block. */
    public static final int GAUGE_WIDTH = 16;
    public static final int GAUGE_HEIGHT = 58;
    public static final int POWER_BAR_X = 164;
    public static final int POWER_BAR_Y = 16;
    public static final int ENERGY_SLOT_X = 143;
    public static final int ENERGY_SLOT_Y = 15;
    /** The mode button runs from the container slots to just before the energy slot. */
    public static final int MODE_BUTTON_X = OUTPUT_X + 24;
    public static final int MODE_BUTTON_Y = 20;
    public static final int MODE_BUTTON_WIDTH = ENERGY_SLOT_X - MODE_BUTTON_X - 4;
    public static final int MODE_BUTTON_HEIGHT = 14;

    /** Height of the tank gauge for the given tier: it reaches down to the last container row. */
    public static int gaugeHeight(int parallel) {
        return Math.max(GAUGE_HEIGHT, rowY(parallel - 1) + 16 - GAUGE_Y);
    }

    /** Total height of the machine screen for the given tier: only as tall as the content needs. */
    public static int guiHeight(int parallel) {
        return inventoryYOffset(parallel) + INVENTORY_HEIGHT;
    }

    /**
     * y offset of the player inventory inside the machine screen. Every tier adds one container slot pair, but the first
     * three tiers still fit under the fixed gauge and power bar, so the inventory only moves down once the slots reach
     * past them - otherwise the screen would carry a wide empty band above the inventory.
     */
    public static int inventoryYOffset(int parallel) {
        return Math.max(MIN_INVENTORY_Y, rowY(parallel - 1) + 16 + INVENTORY_MARGIN);
    }

    public IExtendedFluidTank getFluidTank() {
        return fluidTank;
    }

    public InputInventorySlot[] getContainerSlots() {
        return containerSlots;
    }

    public OutputInventorySlot[] getContainerOutputSlots() {
        return containerOutputSlots;
    }

    public MachineEnergyContainer<FluidInjectorBlockEntity> getEnergyContainer() {
        return energyContainer;
    }

    public ContainerEditMode getContainerMode() {
        return containerMode;
    }

    public void setContainerMode(ContainerEditMode mode) {
        if (containerMode != mode) {
            containerMode = mode;
            if (level != null && !level.isClientSide) {
                setChanged();
                sendUpdatePacket();
            }
        }
    }

    /** Cycles Both -&gt; Fill -&gt; Empty -&gt; Both. */
    public void cycleContainerMode() {
        setContainerMode(modeFromOrdinal((containerMode.ordinal() + 1) % ContainerEditMode.values().length));
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        boolean converted = false;
        if (MekanismUtils.canFunction(this)) {
            for (int i = 0; i < containerSlots.length; i++) {
                if (handleFluidContainer(i)) {
                    converted = true;
                } else if (handleBottle(i)) {
                    converted = true;
                }
            }
        }
        setActive(converted);
    }

    private boolean hasEnergy() {
        return energyContainer.getEnergy().greaterOrEqual(FloatingLong.create(ENERGY_PER_OPERATION));
    }

    private void useEnergy() {
        energyContainer.extract(FloatingLong.create(ENERGY_PER_OPERATION), Action.EXECUTE, AutomationType.INTERNAL);
    }

    /** Buckets, Mekanism fluid tanks and any other item with a fluid handler, converted through that handler. */
    private boolean handleFluidContainer(int index) {
        ItemStack input = containerSlots[index].getStack();
        if (input.isEmpty() || isBottle(input) || !hasEnergy()) {
            return false;
        }
        // The fluid handler of a single item wraps that very stack, so it has to work on a copy: if the converted
        // container cannot be stored, the item in the input slot (and the tank) must stay untouched.
        IFluidHandlerItem handler = input.copyWithCount(1).getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
        if (handler == null) {
            return false;
        }
        boolean allowEmpty = containerMode != ContainerEditMode.FILL;
        boolean allowFill = containerMode != ContainerEditMode.EMPTY;
        FluidStack drained = FluidStack.EMPTY;
        int filled = 0;
        if (allowEmpty) {
            FluidStack contained = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
            if (!contained.isEmpty()) {
                int accepted = contained.getAmount()
                      - fluidTank.insert(contained, Action.SIMULATE, AutomationType.INTERNAL).getAmount();
                if (accepted > 0) {
                    // Executed on the copy only; the tank is filled after the container is known to fit
                    FluidStack toDrain = contained.copy();
                    toDrain.setAmount(accepted);
                    drained = handler.drain(toDrain, FluidAction.EXECUTE);
                }
            }
        }
        if (drained.isEmpty() && allowFill) {
            FluidStack stored = fluidTank.getFluid();
            if (!stored.isEmpty()) {
                FluidStack contained = handler.getFluidInTank(0);
                int free = handler.getTankCapacity(0) - contained.getAmount();
                if (free > 0) {
                    int amount = Math.min(stored.getAmount(), free);
                    FluidStack toFill = stored.copy();
                    toFill.setAmount(amount);
                    filled = handler.fill(toFill, FluidAction.EXECUTE);
                }
            }
        }
        if (drained.isEmpty() && filled <= 0) {
            return false;
        }
        ItemStack result = handler.getContainer();
        if (result.isEmpty() || !canStoreIn(containerOutputSlots[index], result)) {
            // No room for the converted container: nothing was committed, the input item is still intact
            return false;
        }
        if (!drained.isEmpty()) {
            fluidTank.insert(drained, Action.EXECUTE, AutomationType.INTERNAL);
        }
        if (filled > 0) {
            fluidTank.extract(filled, Action.EXECUTE, AutomationType.INTERNAL);
        }
        containerSlots[index].extractItem(1, Action.EXECUTE, AutomationType.INTERNAL);
        containerOutputSlots[index].insertItem(result, Action.EXECUTE, AutomationType.INTERNAL);
        useEnergy();
        return true;
    }

    /** Vanilla bottles: water bottle &lt;-&gt; glass bottle, {@link #BOTTLE_AMOUNT} mB of water each. */
    private boolean handleBottle(int index) {
        ItemStack input = containerSlots[index].getStack();
        if (input.isEmpty() || !hasEnergy()) {
            return false;
        }
        if (isWaterBottle(input)) {
            // Emptying a bottle into the tank (not allowed in Fill mode)
            if (containerMode == ContainerEditMode.FILL) {
                return false;
            }
            FluidStack toAdd = new FluidStack(Fluids.WATER, BOTTLE_AMOUNT);
            if (!fluidTank.insert(toAdd, Action.SIMULATE, AutomationType.INTERNAL).isEmpty()) {
                return false;
            }
            ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!canStoreIn(containerOutputSlots[index], emptyBottle)) {
                return false;
            }
            fluidTank.insert(toAdd, Action.EXECUTE, AutomationType.INTERNAL);
            containerSlots[index].extractItem(1, Action.EXECUTE, AutomationType.INTERNAL);
            containerOutputSlots[index].insertItem(emptyBottle, Action.EXECUTE, AutomationType.INTERNAL);
            useEnergy();
            return true;
        } else if (input.is(Items.GLASS_BOTTLE)) {
            // Filling a bottle with water (not allowed in Empty mode)
            if (containerMode == ContainerEditMode.EMPTY || fluidTank.getFluidAmount() < BOTTLE_AMOUNT
                  || fluidTank.getFluid().getFluid() != Fluids.WATER) {
                return false;
            }
            ItemStack waterBottle = createWaterBottle();
            if (!canStoreIn(containerOutputSlots[index], waterBottle)) {
                return false;
            }
            fluidTank.extract(BOTTLE_AMOUNT, Action.EXECUTE, AutomationType.INTERNAL);
            containerSlots[index].extractItem(1, Action.EXECUTE, AutomationType.INTERNAL);
            containerOutputSlots[index].insertItem(waterBottle, Action.EXECUTE, AutomationType.INTERNAL);
            useEnergy();
            return true;
        }
        return false;
    }

    private boolean isContainerInput(ItemStack stack) {
        // Only water bottles are useful here; other potions would just block the slot
        return stack.is(Items.GLASS_BOTTLE) || isWaterBottle(stack)
              || stack.copyWithCount(1).getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
    }

    private static boolean canStoreIn(OutputInventorySlot slot, ItemStack result) {
        ItemStack stored = slot.getStack();
        return stored.isEmpty()
              || ItemStack.isSameItemSameTags(stored, result) && stored.getCount() < stored.getMaxStackSize();
    }

    private static boolean isBottle(ItemStack stack) {
        return stack.is(Items.GLASS_BOTTLE) || stack.is(Items.POTION);
    }

    private static boolean isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) {
            return false;
        }
        return PotionUtils.getPotion(stack) == Potions.WATER;
    }

    private static ItemStack createWaterBottle() {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }


    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> containerMode.ordinal(),
              ordinal -> containerMode = modeFromOrdinal(ordinal)));
    }

    /** True when there is a container in the input slot that could be converted right now. */
    public boolean hasContainerInput() {
        for (InputInventorySlot slot : containerSlots) {
            if (!slot.getStack().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag tag = super.getReducedUpdateTag();
        // Let clients that did not open the GUI know the current mode (used by the mode button)
        tag.putInt("ContainerMode", containerMode.ordinal());
        return tag;
    }

    /**
     * Reads a mode from a stored ordinal. Older saves, a corrupted tag or a value written by another mod must not crash
     * the machine, so out of range ordinals fall back to the closest valid mode.
     */
    private static ContainerEditMode modeFromOrdinal(int ordinal) {
        ContainerEditMode[] modes = ContainerEditMode.values();
        return modes[Mth.clamp(ordinal, 0, modes.length - 1)];
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        containerMode = modeFromOrdinal(tag.getInt("ContainerMode"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("ContainerMode", containerMode.ordinal());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        containerMode = modeFromOrdinal(tag.getInt("ContainerMode"));
    }
}