package jp.example.mekanismgizmo.compat.jei;

import java.util.List;
import java.util.Optional;
import jp.example.mekanismgizmo.menu.PotionBrewerMenu;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

/**
 * JEI's default recipe transfer tries to match every input ingredient against item stacks in the player inventory. The
 * potion brewer page also shows blaze powder gas, which has no item stack and no slot that could receive it, so the
 * default handler would always report the page as missing an ingredient. This wrapper removes the non-item slots from
 * the page and lets JEI's normal transfer logic fill the bottle and ingredient slots.
 */
public class PotionBrewerRecipeTransferHandler implements IRecipeTransferHandler<PotionBrewerMenu, PotionBrewerJeiRecipe> {

    private final IRecipeTransferHandlerHelper helper;
    private final IRecipeTransferHandler<PotionBrewerMenu, PotionBrewerJeiRecipe> delegate;

    public PotionBrewerRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
        this.delegate = helper.createUnregisteredRecipeTransferHandler(new PotionBrewerRecipeTransferInfo());
    }

    @Override
    public Class<? extends PotionBrewerMenu> getContainerClass() {
        return delegate.getContainerClass();
    }

    @Override
    public Optional<MenuType<PotionBrewerMenu>> getMenuType() {
        return delegate.getMenuType();
    }

    @Override
    public RecipeType<PotionBrewerJeiRecipe> getRecipeType() {
        return delegate.getRecipeType();
    }

    // JEI 15.57+ calls the context overload, whose default implementation forwards to this method
    @SuppressWarnings("removal")
    @Override
    public IRecipeTransferError transferRecipe(PotionBrewerMenu container, PotionBrewerJeiRecipe recipe,
          IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        List<IRecipeSlotView> itemInputs = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT).stream()
              .filter(slot -> slot.getIngredients(VanillaTypes.ITEM_STACK).findFirst().isPresent())
              .toList();
        return delegate.transferRecipe(container, recipe, helper.createRecipeSlotsView(itemInputs), player,
              maxTransfer, doTransfer);
    }
}
