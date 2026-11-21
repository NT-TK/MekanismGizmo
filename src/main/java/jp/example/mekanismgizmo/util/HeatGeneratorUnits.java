package jp.example.mekanismgizmo.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * How many plain Mekanism heat generators one item of the fusion machine is worth.
 * <p>
 * A normal {@code mekanismgenerators:heat_generator} counts once. Compressed heat generators of Compact Mekanism
 * Machines (when that mod has them) produce exactly {@code multiplier} times what a heat generator produces, so an
 * {@code x532480} block would count as 532480 generators. Both naming schemes of that mod are recognised - the 1.21
 * port calls its blocks {@code compressed_heat_generator_xN}, the 1.20 original {@code heat_generator_xN}.
 * <p>
 * The lookup is purely name based, so Compact Mekanism Machines stays an optional mod and nothing is compiled against
 * it.
 */
public final class HeatGeneratorUnits {

    /** Mod id of Compact Mekanism Machines (the 1.21 unofficial port uses the same namespace). */
    public static final String COMPRESSED_NAMESPACE = "compactmekanismmachines";
    /** {@code compressed_heat_generator_x532480} and {@code heat_generator_x532480}. */
    private static final Pattern COMPRESSED_PATH = Pattern.compile("(?:compressed_)?heat_generator_x(\\d+)");
    /** The largest compression Compact Mekanism Machines ships, used to size the machine's buffer. */
    public static final int LARGEST_COMPRESSION = 532_480;
    /** No item is worth more than this, which keeps the machine's numbers inside a {@code long}. */
    public static final int MAX_UNITS_PER_ITEM = 1_000_000;

    private static final Map<Item, Integer> CACHE = new ConcurrentHashMap<>();

    private HeatGeneratorUnits() {
    }

    /** Number of heat generators the stack is worth (0 when it is not a heat generator at all). */
    public static int of(ItemStack stack) {
        return stack.isEmpty() ? 0 : of(stack.getItem());
    }

    /** Number of heat generators the item is worth (0 when it is not a heat generator at all). */
    public static int of(Item item) {
        if (item == GeneratorsBlocks.HEAT_GENERATOR.asItem()) {
            return 1;
        }
        return CACHE.computeIfAbsent(item, key -> unitsForId(BuiltInRegistries.ITEM.getKey(key)));
    }

    /** Number of heat generators the item with that registry name is worth, without touching the registry. */
    public static int unitsForId(ResourceLocation id) {
        if (id.getNamespace().equals("mekanismgenerators") && id.getPath().equals("heat_generator")) {
            return 1;
        }
        if (!id.getNamespace().equals(COMPRESSED_NAMESPACE)) {
            return 0;
        }
        Matcher matcher = COMPRESSED_PATH.matcher(id.getPath());
        if (!matcher.matches()) {
            return 0;
        }
        try {
            return (int) Math.min(Long.parseLong(matcher.group(1)), MAX_UNITS_PER_ITEM);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
