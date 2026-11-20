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
 * Maps JEI's brewing recipe layout (3 potion slots, 1 ingredient slot, 1 output slot) onto the potion brewer's
 * slots, so the "+" button of the brewing category can fill the machine just like it fills a brewing stand.
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
        List<Slot> recipeSlots = new ArrayList<>(5);
        // Same order as JEI's brewing category: three potion slots, the ingredient, then the output
        // The brewing category only knows the three bottle slots of a brewing stand, so only the first row is offered
        var bottleSlots = tile.getBottleSlots();
        for (int i = 0; i < Math.min(PotionBrewerBlockEntity.BOTTLES_PER_BREW, bottleSlots.length); i++) {
            Slot slot = slots.get(bottleSlots[i]);
            if (slot != null) {
                recipeSlots.add(slot);
            }
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
