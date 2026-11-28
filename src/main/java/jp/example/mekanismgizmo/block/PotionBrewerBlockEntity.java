package jp.example.mekanismgizmo.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import jp.example.mekanismgizmo.util.MachineTier;
import jp.example.mekanismgizmo.registry.ModChemicals;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import mekanism.api.providers.IBlockProvider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

/**
 * Potion brewer built on Mekanism's {@link TileEntityMekanism}. Everything the machine exposes - item,
 * chemical and energy capabilities, side configuration, redstone control, upgrades, ejector and the
 * data synced to the screen - is handled by Mekanism itself; this class only implements the brewing.
 */
public class PotionBrewerBlockEntity extends TieredMachineBlockEntity {
    public IGasTank blazePowderTank;
    private MachineEnergyContainer<PotionBrewerBlockEntity> energyContainer;
    private InputInventorySlot[] bottleSlots;
    private OutputInventorySlot[] outputSlots;
    private InputInventorySlot blazePowderSlot;
    private InputInventorySlot ingredientSlot;
    private EnergyInventorySlot energySlot;

    private int brewTicks;
    private int ticksRequired = BASE_TICKS_REQUIRED;
    @Nullable
    private ItemStack brewingIngredient;

    public static final int CHEMICAL_CAPACITY = 1_000;
    public static final int CHEMICAL_PER_BREW = 5;
    public static final int BASE_TICKS_REQUIRED = 400;
    /** mB of blaze powder gas per blaze powder and per brew. */
    private static final int POWDER_CHEMICAL = 100;
    /** Bottles one brew processes at once (a brewing stand has three bottle slots). */
    public static final int BOTTLES_PER_BREW = 3;
    /** Screen layout: a grid of three bottle pairs per parallel unit. */
    public static final int BOTTLE_COLUMNS = BOTTLES_PER_BREW;
    public static final int INPUT_X = 46;
    public static final int OUTPUT_X = 138;
    public static final int FIRST_ROW_Y = 33;
    /** One arrow per brewing row, between the bottle slots and the potion slots. */
    public static final int ARROW_X = 102;
    public static final int ARROW_WIDTH = 32;
    public static final int ARROW_HEIGHT = 8;
    public static final int POWDER_X = 26;
    public static final int POWDER_Y = 13;
    public static final int INGREDIENT_X = 26;
    public static final int INGREDIENT_Y = 35;
    /** Energy slot, top right next to the power bar. */
    public static final int ENERGY_X = OUTPUT_X + 2 * 18;
    public static final int ENERGY_Y = 13;
    public static final int GUI_WIDTH = 208;
    /** x offset of the player inventory (the screen is wider than vanilla's 176). */
    public static final int INVENTORY_X = 23;

    /**
     * Number of bottle slots of a tier. One brew handles three bottles at once, so every parallel unit of the tier
     * adds three more bottles: 3 (untiered), 6 (basic), 9 (advanced), 12 (elite), 15 (ultimate).
     */
    public static int bottleCount(MachineTier tier) {
        return tier.getParallel() * BOTTLES_PER_BREW;
    }

    /** Number of bottle slots of this machine. */
    public int bottleCount() {
        return bottleCount(getTier());
    }

    /** x position of the {@code index}-th bottle input slot (three per row). */
    public static int bottleSlotX(int index) {
        return INPUT_X + (index % BOTTLE_COLUMNS) * 18;
    }

    /** x position of the {@code index}-th potion output slot (three per row). */
    public static int outputSlotX(int index) {
        return OUTPUT_X + (index % BOTTLE_COLUMNS) * 18;
    }

    /** y position of the {@code index}-th bottle slot; one row per parallel unit. */
    public static int pairY(int index) {
        return FIRST_ROW_Y + (index / BOTTLE_COLUMNS) * 18;
    }

    /** x position of the energy slot (top row, between the blaze powder slot and the first bottle). */
    public static int energySlotX(int bottles) {
        return ENERGY_X;
    }

    /** Blaze powder gas capacity of a tier: every parallel unit adds another 1,000 mB (ten powders). */
    public static int chemicalCapacity(int parallel) {
        return CHEMICAL_CAPACITY * parallel;
    }

    /** Number of brewing rows (one per parallel unit). */
    public static int rows(int bottles) {
        return Math.max(1, (bottles + BOTTLE_COLUMNS - 1) / BOTTLE_COLUMNS);
    }

    /** y offset of the player inventory inside the machine screen. */
    public static int inventoryYOffset(int bottles) {
        int lastRow = pairY(bottles - 1);
        return Math.max(84, lastRow + 16 + 12);
    }

    /** Total height of the machine screen for the given number of bottle slots. */
    public static int guiHeight(int bottles) {
        return inventoryYOffset(bottles) + (166 - 84);
    }

    /** Total width of the machine screen (fixed: the brewing grid is three bottles wide). */
    public static int guiWidth(int bottles) {
        return GUI_WIDTH;
    }

    /** x offset of the player inventory inside the machine screen. */
    public static int inventoryXOffset(int bottles) {
        return INVENTORY_X;
    }

    public PotionBrewerBlockEntity(IBlockProvider block, BlockPos pos, BlockState state) {
        super(block, pos, state);
        // Item slot groups, following Mekanism's own colours: bottles are a normal "Input" (red),
        // the blaze powder slot is "Input (2)" (orange) and the brewing ingredient slot is "Extra" (yellow).
        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        List<IInventorySlot> itemInputs = new ArrayList<>(List.of(bottleSlots));
        itemInputs.add(blazePowderSlot);
        itemInputs.add(ingredientSlot);
        List<IInventorySlot> itemOutputs = List.of(outputSlots);
        List<IInventorySlot> allItemSlots = new ArrayList<>(itemInputs);
        allItemSlots.addAll(itemOutputs);
        itemConfig.addSlotInfo(DataType.INPUT_2, new InventorySlotInfo(true, false, List.of(blazePowderSlot)));
        itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, false, List.of(ingredientSlot)));
        itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, false, itemInputs));
        itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(false, true, itemOutputs));
        itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, allItemSlots));
        itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, List.of(energySlot)));
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        configComponent.setupInputConfig(TransmissionType.GAS, blazePowderTank);
        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);
        applySideDefaults();
    }


    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        int bottles = bottleCount();
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        bottleSlots = new InputInventorySlot[bottles];
        outputSlots = new OutputInventorySlot[bottles];
        for (int i = 0; i < bottles; i++) {
            builder.addSlot(bottleSlots[i] = new BottleSlot(this::isBottleInput, listener, bottleSlotX(i), pairY(i)));
            builder.addSlot(outputSlots[i] = OutputInventorySlot.at(listener, outputSlotX(i), pairY(i)));
        }
        builder.addSlot(blazePowderSlot = InputInventorySlot.at(stack -> stack.is(Items.BLAZE_POWDER), listener, POWDER_X, POWDER_Y));
        builder.addSlot(ingredientSlot = InputInventorySlot.at(this::isBrewingIngredient, listener, INGREDIENT_X, INGREDIENT_Y));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, energySlotX(bottles), ENERGY_Y));
        return builder.build();
    }

    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        blazePowderTank = ChemicalTankBuilder.GAS.input(chemicalCapacity(parallel()),
              gas -> gas == ModChemicals.BLAZE_POWDER.get(), listener);
        builder.addTank(blazePowderTank);
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    /**
     * A bottle slot holds exactly one bottle, like the three potion slots of a vanilla brewing stand. Besides matching
     * that layout, this keeps {@code BrewingRecipeRegistry.getOutput} usable: it rejects any input stack whose count is
     * not one, so a stacked bottle would otherwise stall the whole row.
     */
    private static class BottleSlot extends InputInventorySlot {

        private BottleSlot(Predicate<ItemStack> valid, IContentsListener listener, int x, int y) {
            super(stack -> true, valid, listener, x, y);
        }

        @Override
        public int getLimit(ItemStack stack) {
            return 1;
        }
    }

    private boolean isBottleInput(ItemStack stack) {
        if (stack.is(Items.GLASS_BOTTLE)) {
            return true;
        }
        return BrewingRecipeRegistry.isValidInput(stack);
    }

    private boolean isBrewingIngredient(ItemStack stack) {
        return BrewingRecipeRegistry.isValidIngredient(stack);
    }

    public MachineEnergyContainer<PotionBrewerBlockEntity> getEnergyContainer() {
        return energyContainer;
    }

    /** All bottle input slots of this tier (JEI transfers into the first one; the machine uses every bottle slot). */
    public InputInventorySlot[] getBottleSlots() {
        return bottleSlots;
    }

    /** The blaze powder slot, the item source of the blaze powder gas (used by the JEI recipe transfer). */
    public InputInventorySlot getBlazePowderSlot() {
        return blazePowderSlot;
    }

    /** The brewing ingredient slot (used by the JEI recipe transfer). */
    public InputInventorySlot getIngredientSlot() {
        return ingredientSlot;
    }

    /** All potion output slots of this tier (not part of the JEI transfer: they only receive brewed potions). */
    public OutputInventorySlot[] getOutputSlots() {
        return outputSlots;
    }

    public double getScaledProgress() {
        if (brewTicks <= 0 || ticksRequired <= 0) {
            return 0;
        }
        return 1D - (double) brewTicks / ticksRequired;
    }

    public int getBrewTicks() {
        return brewTicks;
    }

    public void setBrewTicks(int brewTicks) {
        this.brewTicks = brewTicks;
    }

    public int getTicksRequired() {
        return ticksRequired;
    }

    public void setTicksRequired(int ticksRequired) {
        this.ticksRequired = ticksRequired;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();

        if (MekanismUtils.canFunction(this)) {
            ItemStack powder = blazePowderSlot.getStack();
            if (!powder.isEmpty() && powder.is(Items.BLAZE_POWDER) && blazePowderTank.getNeeded() >= POWDER_CHEMICAL) {
                GasStack remainder = blazePowderTank.insert(
                      new GasStack(ModChemicals.BLAZE_POWDER.get(), POWDER_CHEMICAL), Action.EXECUTE, AutomationType.INTERNAL);
                if (remainder.isEmpty()) {
                    blazePowderSlot.shrinkStack(1, Action.EXECUTE);
                }
            }

            if (brewTicks > 0) {
                if (brewingIngredientChanged()) {
                    // A different ingredient was put in: the running brew is no longer valid
                    brewTicks = 0;
                    brewingIngredient = null;
                } else if (isBrewable()) {
                    // While the output slots are full or the gas ran out the brew simply pauses
                    FloatingLong energyPerTick = energyContainer.getEnergyPerTick();
                    if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
                        energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                        if (--brewTicks <= 0) {
                            finishBrew();
                        }
                    }
                }
            } else if (isBrewable()) {
                // isBrewable() already checks the gas for every row that can brew, so a partly filled machine
                // cannot start a cycle it would have to abort at the end
                brewTicks = ticksRequired;
                brewingIngredient = ingredientSlot.getStack().copy();
            }
        }
        setActive(brewTicks > 0);
    }

    private boolean brewingIngredientChanged() {
        // Compare the full stack, not just the item: a data pack or another mod can register two ingredient variants
        // of the same item and carrying progress from one recipe over to the other would be wrong. The count is
        // deliberately ignored, because brewing consumes the ingredient while the cycle runs.
        return brewingIngredient == null
              || !ItemStack.isSameItemSameTags(brewingIngredient, ingredientSlot.getStack());
    }

    /** True when a valid brew exists but the matching output slot is occupied (Mekanism warning hook). */
    public boolean isOutputBlocked() {
        if (level == null) {
            return false;
        }
        ItemStack ingredient = ingredientSlot.getStack();
        if (ingredient.isEmpty() || !BrewingRecipeRegistry.isValidIngredient(ingredient)) {
            return false;
        }
        for (int i = 0; i < bottleSlots.length; i++) {
            ItemStack bottle = bottleSlots[i].getStack();
            if (!bottle.isEmpty() && BrewingRecipeRegistry.hasOutput(bottle, ingredient) && !outputSlots[i].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Number of three-bottle rows of this tier: one per parallel unit. */
    public int rowCount() {
        return Math.max(1, bottleSlots.length / BOTTLES_PER_BREW);
    }

    /** True when the {@code i}-th bottle would be brewed by the given ingredient and its output slot is free. */
    private boolean isBrewableAt(ItemStack ingredient, int i) {
        ItemStack bottle = bottleSlots[i].getStack();
        return !bottle.isEmpty() && BrewingRecipeRegistry.hasOutput(bottle, ingredient) && outputSlots[i].isEmpty();
    }

    /** True when at least one bottle of the {@code row}-th row of three can be brewed right now. */
    private boolean rowHasWork(ItemStack ingredient, int row) {
        int start = row * BOTTLES_PER_BREW;
        int end = Math.min(start + BOTTLES_PER_BREW, bottleSlots.length);
        for (int i = start; i < end; i++) {
            if (isBrewableAt(ingredient, i)) {
                return true;
            }
        }
        return false;
    }

    /** Number of rows of three bottles that have at least one bottle this machine can brew right now. */
    private int rowsWithWork() {
        if (level == null) {
            return 0;
        }

        ItemStack ingredient = ingredientSlot.getStack();
        if (ingredient.isEmpty() || !BrewingRecipeRegistry.isValidIngredient(ingredient)) {
            return 0;
        }
        int rows = 0;
        for (int row = 0; row < rowCount(); row++) {
            if (rowHasWork(ingredient, row)) {
                rows++;
            }
        }
        return rows;
    }

    /**
     * How many brews the next cycle actually performs: one per row of three bottles that has something to brew, and
     * never more than the ingredient items stored in the ingredient slot (a brewing stand also uses one ingredient
     * per brew). One gas charge is used per brew, so a partly filled machine does not need the full gas of every
     * parallel unit.
     */
    public int brewsThisCycle() {
        return Math.max(1, Math.min(rowsWithWork(), ingredientSlot == null ? 0 : ingredientSlot.getStack().getCount()));
    }

    private boolean isBrewable() {
        int rows = rowsWithWork();
        if (rows == 0) {
            return false;
        }
        int brews = Math.min(rows, ingredientSlot.getStack().getCount());
        return brews > 0 && blazePowderTank.getStored() >= CHEMICAL_PER_BREW * brews;
    }

    private void finishBrew() {
        if (level == null || rowsWithWork() == 0) {
            // Nothing left to brew (the bottles were taken out while it ran): do not charge any gas
            brewTicks = 0;
            brewingIngredient = null;
            return;
        }
        int brews = brewsThisCycle();
        if (blazePowderTank.getStored() < CHEMICAL_PER_BREW * brews) {
            // More rows were added while this cycle ran: keep the finished progress and wait for gas instead of
            // throwing the whole cycle away.
            brewTicks = 1;
            return;
        }
        brewTicks = 0;
        brewingIngredient = null;
        int brewed = doBrew(brews);
        if (brewed > 0) {
            // Charge only the rows that really brewed: a brew event that cancels a row must not eat the gas
            blazePowderTank.extract(CHEMICAL_PER_BREW * Math.min(brewed, brews),
                  Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    /**
     * Brews up to {@code maxBrews} batches of three bottles, exactly like a brewing stand would (three bottles and
     * one ingredient per batch). The Forge brewing events are fired once per batch with the four vanilla slots.
     * Returns the number of rows that really brewed, so gas is charged for exactly those rows.
     */
    private int doBrew(int maxBrews) {
        if (level == null) {
            return 0;
        }
        int brewedRows = 0;
        // One ingredient item is consumed per successful row, so never brew more rows than were counted and
        // charged above. A crafting remainder that is itself a valid ingredient must not add a free extra row.
        for (int row = 0; row < rowCount() && brewedRows < maxBrews; row++) {
            int start = row * BOTTLES_PER_BREW;
            int end = Math.min(start + BOTTLES_PER_BREW, bottleSlots.length);
            ItemStack currentIngredient = ingredientSlot.getStack();
            // Rows without a single brewable bottle are skipped completely, so they do not eat an ingredient item
            if (currentIngredient.isEmpty() || !BrewingRecipeRegistry.isValidIngredient(currentIngredient)
                  || !rowHasWork(currentIngredient, row)) {
                continue;
            }
            NonNullList<ItemStack> brewingItems = NonNullList.withSize(4, ItemStack.EMPTY);
            for (int i = start; i < end; i++) {
                brewingItems.set(i - start, bottleSlots[i].getStack().copy());
            }
            brewingItems.set(3, currentIngredient.copy());
            if (ForgeEventFactory.onPotionAttemptBrew(brewingItems)) {
                continue;
            }
            ItemStack ingredient = brewingItems.get(3);
            boolean[] brewed = new boolean[end - start];
            boolean any = false;
            for (int i = start; i < end; i++) {
                ItemStack bottle = brewingItems.get(i - start);
                // An occupied output must never be overwritten. rowHasWork() only guarantees that at least one
                // bottle of this row has room; the other two may still be blocked.
                if (!bottle.isEmpty() && outputSlots[i].isEmpty() && BrewingRecipeRegistry.hasOutput(bottle, ingredient)) {
                    brewingItems.set(i - start, BrewingRecipeRegistry.getOutput(bottle, ingredient));
                    brewed[i - start] = true;
                    any = true;
                }
            }
            if (!any) {
                continue;
            }
            ForgeEventFactory.onPotionBrewed(brewingItems);
            for (int i = start; i < end; i++) {
                ItemStack result = brewingItems.get(i - start);
                if (brewed[i - start] && !result.isEmpty()) {
                    // Re-check the output: an event handler could have used it in the meantime. Keep the result
                    // in the input slot instead of dropping it on top of a different item.
                    if (outputSlots[i].isEmpty()) {
                        outputSlots[i].setStack(result);
                        bottleSlots[i].setEmpty();
                    } else {
                        bottleSlots[i].setStack(result);
                    }
                } else if (!brewed[i - start]) {
                    // Bottles that had no recipe for this ingredient stay in the input slot, like vanilla.
                    bottleSlots[i].setStack(result);
                }
            }
            // One ingredient item per brewed row, taken from the machine's ingredient slot
            consumeIngredient();
            level.levelEvent(1035, worldPosition, 0);
            brewedRows++;
        }
        return brewedRows;
    }

    /** Consumes one ingredient item, keeping (or dropping) its crafting remainder, like a brewing stand. */
    private void consumeIngredient() {
        ItemStack ingredient = ingredientSlot.getStack();
        if (ingredient.isEmpty()) {
            return;
        }
        ItemStack remainder = ingredient.hasCraftingRemainingItem() ? ingredient.getCraftingRemainingItem() : ItemStack.EMPTY;
        ingredient.shrink(1);
        if (ingredient.isEmpty() && !remainder.isEmpty()) {
            ingredientSlot.setStack(remainder);
        } else if (!remainder.isEmpty() && level != null) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), remainder);
            ingredientSlot.setStack(ingredient);
        } else {
            ingredientSlot.setStack(ingredient);
        }
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(blazePowderTank.getStored(), blazePowderTank.getCapacity());
    }


    @Override
    public java.util.List<net.minecraft.network.chat.Component> getInfo(Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(this::getBrewTicks, this::setBrewTicks));
        container.track(SyncableInt.create(this::getTicksRequired, this::setTicksRequired));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("BrewTicks", brewTicks);
        tag.putInt("TicksRequired", ticksRequired);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        brewTicks = tag.getInt("BrewTicks");
        ticksRequired = tag.contains("TicksRequired") ? tag.getInt("TicksRequired") : BASE_TICKS_REQUIRED;
        brewingIngredient = brewTicks > 0 && ingredientSlot != null && !ingredientSlot.isEmpty() ? ingredientSlot.getStack().copy() : null;
    }
}