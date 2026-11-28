package jp.example.mekanismgizmo.compat.emi;

import java.util.List;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * One page of a Mekanism Gizmo machine in EMI: a few fixed slots (items, fluids and chemicals), an optional arrow
 * and a couple of lines of text. All four machines share this class, they only differ in what they place where.
 */
public final class GizmoEmiRecipe implements EmiRecipe {

    /** A slot of the page; {@code x}/{@code y} are the top left corner inside the page. */
    public record Slot(EmiIngredient stack, int x, int y) {
    }

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final int width;
    private final int height;
    private final List<Slot> inputs;
    private final List<Slot> outputs;
    private final List<Component> lines;
    private final int arrowX;
    private final int arrowY;

    public GizmoEmiRecipe(EmiRecipeCategory category, ResourceLocation id, int width, int height, List<Slot> inputs,
          List<Slot> outputs, List<Component> lines, int arrowX, int arrowY) {
        this.category = category;
        this.id = synthetic(id);
        this.width = width;
        this.height = height;
        this.inputs = inputs;
        this.outputs = outputs;
        this.lines = lines;
        this.arrowX = arrowX;
        this.arrowY = arrowY;
    }

    /**
     * EMI only treats a recipe as synthetic (and skips looking it up in the vanilla recipe manager) when its path
     * starts with a slash. Every page here is generated from Mekanism Gizmo's own data, so without this prefix EMI
     * logs one "not present in recipe manager" error per page.
     */
    private static ResourceLocation synthetic(ResourceLocation id) {
        if (id == null || id.getPath().startsWith("/")) {
            return id;
        }
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "/" + id.getPath());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs.stream().map(Slot::stack).toList();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs.stream().flatMap(slot -> slot.stack().getEmiStacks().stream()).toList();
    }

    @Override
    public int getDisplayWidth() {
        return width;
    }

    @Override
    public int getDisplayHeight() {
        return height;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (Slot slot : inputs) {
            widgets.addSlot(slot.stack(), slot.x(), slot.y()).recipeContext(this);
        }
        for (Slot slot : outputs) {
            widgets.addSlot(slot.stack(), slot.x(), slot.y()).recipeContext(this);
        }
        if (arrowX >= 0) {
            widgets.addTexture(EmiTexture.EMPTY_ARROW, arrowX, arrowY);
        }
        // The info lines sit at the bottom of the page
        int y = height - lines.size() * 10 - 1;
        for (Component line : lines) {
            widgets.addText(line, 4, y, 0x404040, false);
            y += 10;
        }
    }
}
