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
 * Maps the item part of the potion brewer's JEI page onto the machine: one bottle and one ingredient. The gas slot is
 * left out because JEI's default transfer only knows item stacks; the recipe transfer handler filters it out of the
 * page before handing the remaining slots to JEI.
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
        List<Slot> recipeSlots = new ArrayList<>(2);
        // Same order as the item inputs of the page: bottle, ingredient. The gas slot is not part of the transfer (JEI
        // cannot move a chemical into the tank) and the output slot is deliberately not part of it either: JEI moves
        // ingredients from the player inventory into these slots and an output slot would reject the result.
        Slot bottle = slots.get(tile.getBottleSlots()[0]);
        if (bottle != null) {
            recipeSlots.add(bottle);
        }
        Slot ingredient = slots.get(tile.getIngredientSlot());
        if (ingredient != null) {
            recipeSlots.add(ingredient);
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
