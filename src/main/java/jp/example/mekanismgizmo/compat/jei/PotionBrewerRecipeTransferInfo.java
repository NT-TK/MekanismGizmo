package jp.example.mekanismgizmo.compat.jei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jp.example.mekanismgizmo.block.PotionBrewerBlockEntity;
import jp.example.mekanismgizmo.menu.PotionBrewerMenu;
import jp.example.mekanismgizmo.registry.ModMenus;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

/**
 * Maps the potion brewer's own JEI page onto the machine: the page shows the blaze powder gas, one bottle, the
 * ingredient and the result, in that order, so the "+" button fills the bottle, the ingredient and the output slot.
 * <p>
 * The blaze powder gas has no container slot (it lives in the machine's chemical tank); it is mapped onto the blaze
 * powder slot, which is the item that produces the gas, so the recipe slot indices line up. JEI has no item stack for
 * a chemical, so it simply leaves that slot alone.
 */
public class PotionBrewerRecipeTransferInfo implements IRecipeTransferInfo<PotionBrewerMenu, PotionBrewerJeiRecipe> {

    @Override
    public Class<? extends PotionBrewerMenu> getContainerClass() {
        return PotionBrewerMenu.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<MenuType<PotionBrewerMenu>> getMenuType() {
        return Optional.of((MenuType<PotionBrewerMenu>) (MenuType<?>) ModMenus.POTION_BREWER.get());
    }

    @Override
    public RecipeType<PotionBrewerJeiRecipe> getRecipeType() {
        return PotionBrewerRecipeCategory.TYPE;
    }

    @Override
    public boolean canHandle(PotionBrewerMenu container, PotionBrewerJeiRecipe recipe) {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(PotionBrewerMenu container, PotionBrewerJeiRecipe recipe) {
        PotionBrewerBlockEntity tile = container.getTileEntity();
        Map<IInventorySlot, Slot> slots = new HashMap<>();
        for (Slot slot : container.slots) {
            if (slot instanceof InventoryContainerSlot containerSlot) {
                slots.put(containerSlot.getInventorySlot(), slot);
            }
        }
        List<Slot> recipeSlots = new ArrayList<>(4);
        // Same order as the page: gas, bottle, ingredient, result
        Slot gas = slots.get(tile.getBlazePowderSlot());
        if (gas != null) {
            recipeSlots.add(gas);
        }
        Slot bottle = slots.get(tile.getBottleSlots()[0]);
        if (bottle != null) {
            recipeSlots.add(bottle);
        }
        Slot ingredient = slots.get(tile.getIngredientSlot());
        if (ingredient != null) {
            recipeSlots.add(ingredient);
        }
        Slot output = slots.get(tile.getOutputSlots()[0]);
        if (output != null) {
            recipeSlots.add(output);
        }
        return recipeSlots;
    }

    @Override
    public List<Slot> getInventorySlots(PotionBrewerMenu container, PotionBrewerJeiRecipe recipe) {
        List<Slot> inventorySlots = new ArrayList<>();
        for (Slot slot : container.slots) {
            if (!(slot instanceof InventoryContainerSlot) && slot.container instanceof net.minecraft.world.entity.player.Inventory) {
                inventorySlots.add(slot);
            }
        }
        return inventorySlots;
    }
}
