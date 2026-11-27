package jp.example.mekanismgizmo.util;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.RelativeSide;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.resources.ResourceLocation;

/**
 * Restores the sensible default side configuration of a machine when it has never been configured.
 * <p>
 * In 1.20.1 the side configuration is not carried by the block item, so a freshly placed machine starts with every
 * side set to {@link DataType#NONE}. Without this repair a machine would silently refuse all items, fluids, gases and
 * energy until the player opened the side configuration tab. A layout the player has already changed is never touched.
 */
public final class MachineSideDefaults {

    private static final Map<String, Map<TransmissionType, Map<RelativeSide, DataType>>> DEFAULTS = new HashMap<>();

    static {
        // Tierless machines and all four Mekanism tiers share the same layout.
        put("potion_brewer", config(
              item(map(RelativeSide.FRONT, DataType.INPUT, RelativeSide.LEFT, DataType.INPUT,
                    RelativeSide.RIGHT, DataType.OUTPUT, RelativeSide.TOP, DataType.INPUT_2,
                    RelativeSide.BOTTOM, DataType.EXTRA, RelativeSide.BACK, DataType.ENERGY)),
              all(TransmissionType.GAS, DataType.INPUT),
              all(TransmissionType.ENERGY, DataType.INPUT)));
        put("ice_maker", config(
              item(map(RelativeSide.FRONT, DataType.OUTPUT, RelativeSide.LEFT, DataType.INPUT,
                    RelativeSide.RIGHT, DataType.OUTPUT, RelativeSide.TOP, DataType.INPUT,
                    RelativeSide.BOTTOM, DataType.INPUT, RelativeSide.BACK, DataType.ENERGY)),
              all(TransmissionType.FLUID, DataType.INPUT),
              all(TransmissionType.ENERGY, DataType.INPUT)));
        for (String generator : new String[]{"cobblestone_generator", "basalt_generator"}) {
            put(generator, config(
                  item(map(RelativeSide.FRONT, DataType.OUTPUT, RelativeSide.LEFT, DataType.OUTPUT,
                        RelativeSide.RIGHT, DataType.OUTPUT, RelativeSide.TOP, DataType.OUTPUT,
                        RelativeSide.BOTTOM, DataType.OUTPUT, RelativeSide.BACK, DataType.ENERGY)),
                  all(TransmissionType.ENERGY, DataType.INPUT)));
        }
        put("fluid_injector", config(
              item(map(RelativeSide.FRONT, DataType.OUTPUT, RelativeSide.LEFT, DataType.INPUT,
                    RelativeSide.RIGHT, DataType.OUTPUT, RelativeSide.TOP, DataType.INPUT,
                    RelativeSide.BOTTOM, DataType.INPUT, RelativeSide.BACK, DataType.ENERGY)),
              all(TransmissionType.FLUID, DataType.INPUT_OUTPUT),
              all(TransmissionType.ENERGY, DataType.INPUT)));
        put("liquid_heater", config(
              item(map(RelativeSide.FRONT, DataType.OUTPUT, RelativeSide.LEFT, DataType.INPUT,
                    RelativeSide.RIGHT, DataType.OUTPUT, RelativeSide.TOP, DataType.INPUT,
                    RelativeSide.BOTTOM, DataType.INPUT, RelativeSide.BACK, DataType.ENERGY)),
              all(TransmissionType.GAS, DataType.INPUT_OUTPUT),
              all(TransmissionType.ENERGY, DataType.INPUT)));
        put("improved_gas_generator", config(
              item(map(RelativeSide.FRONT, DataType.NONE, RelativeSide.LEFT, DataType.NONE,
                    RelativeSide.RIGHT, DataType.NONE, RelativeSide.TOP, DataType.NONE,
                    RelativeSide.BOTTOM, DataType.NONE, RelativeSide.BACK, DataType.ENERGY)),
              all(TransmissionType.GAS, DataType.INPUT),
              all(TransmissionType.ENERGY, DataType.OUTPUT)));
        put("heat_fusion", config(
              item(map(RelativeSide.FRONT, DataType.NONE, RelativeSide.LEFT, DataType.NONE,
                    RelativeSide.RIGHT, DataType.NONE, RelativeSide.TOP, DataType.NONE,
                    RelativeSide.BOTTOM, DataType.NONE, RelativeSide.BACK, DataType.ENERGY)),
              all(TransmissionType.FLUID, DataType.INPUT),
              all(TransmissionType.ENERGY, DataType.OUTPUT)));
        put("item_collector", config(
              item(map(RelativeSide.FRONT, DataType.OUTPUT, RelativeSide.LEFT, DataType.OUTPUT,
                    RelativeSide.RIGHT, DataType.OUTPUT, RelativeSide.TOP, DataType.OUTPUT,
                    RelativeSide.BOTTOM, DataType.OUTPUT, RelativeSide.BACK, DataType.ENERGY)),
              all(TransmissionType.ENERGY, DataType.INPUT)));
        put("machine_resetter", config(
              item(map(RelativeSide.FRONT, DataType.OUTPUT, RelativeSide.LEFT, DataType.INPUT,
                    RelativeSide.RIGHT, DataType.OUTPUT, RelativeSide.TOP, DataType.INPUT,
                    RelativeSide.BOTTOM, DataType.INPUT, RelativeSide.BACK, DataType.ENERGY)),
              all(TransmissionType.ENERGY, DataType.INPUT)));
        put("wind_fusion", config(
              item(map(RelativeSide.FRONT, DataType.NONE, RelativeSide.LEFT, DataType.NONE,
                    RelativeSide.RIGHT, DataType.NONE, RelativeSide.TOP, DataType.NONE,
                    RelativeSide.BOTTOM, DataType.NONE, RelativeSide.BACK, DataType.ENERGY)),
              all(TransmissionType.ENERGY, DataType.OUTPUT)));
    }

    public static void applyIfUnconfigured(TileComponentConfig configComponent, ResourceLocation blockId) {
        if (configComponent == null || blockId == null) {
            return;
        }
        String path = blockId.getPath();
        for (String tier : new String[]{"basic_", "advanced_", "elite_", "ultimate_"}) {
            if (path.startsWith(tier)) {
                path = path.substring(tier.length());
                break;
            }
        }
        Map<TransmissionType, Map<RelativeSide, DataType>> defaults = DEFAULTS.get(path);
        if (defaults == null) {
            return;
        }
        for (Map.Entry<TransmissionType, Map<RelativeSide, DataType>> entry : defaults.entrySet()) {
            ConfigInfo info = configComponent.getConfig(entry.getKey());
            if (info == null || !isAllNone(info)) {
                continue;
            }
            for (Map.Entry<RelativeSide, DataType> side : entry.getValue().entrySet()) {
                info.setDataType(side.getValue(), side.getKey());
            }
        }
    }

    private static boolean isAllNone(ConfigInfo info) {
        for (RelativeSide side : RelativeSide.values()) {
            if (info.getDataType(side) != DataType.NONE) {
                return false;
            }
        }
        return true;
    }

    private static void put(String machine, Map<TransmissionType, Map<RelativeSide, DataType>> defaults) {
        DEFAULTS.put(machine, defaults);
    }

    @SafeVarargs
    private static Map<TransmissionType, Map<RelativeSide, DataType>> config(
          Map.Entry<TransmissionType, Map<RelativeSide, DataType>>... entries) {
        Map<TransmissionType, Map<RelativeSide, DataType>> config = new EnumMap<>(TransmissionType.class);
        for (Map.Entry<TransmissionType, Map<RelativeSide, DataType>> entry : entries) {
            config.put(entry.getKey(), entry.getValue());
        }
        return config;
    }

    private static Map.Entry<TransmissionType, Map<RelativeSide, DataType>> item(Map<RelativeSide, DataType> sides) {
        return Map.entry(TransmissionType.ITEM, sides);
    }

    private static Map.Entry<TransmissionType, Map<RelativeSide, DataType>> all(TransmissionType type, DataType dataType) {
        Map<RelativeSide, DataType> sides = new EnumMap<>(RelativeSide.class);
        for (RelativeSide side : RelativeSide.values()) {
            sides.put(side, dataType);
        }
        return Map.entry(type, sides);
    }

    private static Map<RelativeSide, DataType> map(Object... sides) {
        Map<RelativeSide, DataType> map = new EnumMap<>(RelativeSide.class);
        for (int i = 0; i < sides.length; i += 2) {
            map.put((RelativeSide) sides[i], (DataType) sides[i + 1]);
        }
        return map;
    }

    private MachineSideDefaults() {
    }
}
