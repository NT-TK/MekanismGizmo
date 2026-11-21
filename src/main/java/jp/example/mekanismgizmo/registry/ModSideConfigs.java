package jp.example.mekanismgizmo.registry;

import java.util.EnumMap;
import java.util.Map;
import mekanism.api.RelativeSide;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.attachments.component.AttachedSideConfig.LightConfigInfo;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.DataType;

/**
 * Default side configurations of the machines, modelled after Mekanism's own presets
 * ({@code AttachedSideConfig.MACHINE}, {@code EXTRA_MACHINE}, {@code INPUT_ONLY}, ...) and the way
 * Mekanism-Extras assigns them per machine.
 * <p>
 * They are used both as the {@code mekanism:side_config} component of the machine item and by
 * {@link jp.example.mekanismgizmo.util.MachineSideDefaults} when a machine has no configuration yet, so a freshly
 * placed machine already has sensible input, output and input/output faces instead of everything being "None".
 */
public final class ModSideConfigs {

    /** Blaze powder is an auxiliary input ("Input (2)") on top, the brewing ingredient ("Extra") underneath. */
    public static final AttachedSideConfig POTION_BREWER = new AttachedSideConfig(config(
          TransmissionType.ITEM, item(map(
                RelativeSide.FRONT, DataType.INPUT,
                RelativeSide.LEFT, DataType.INPUT,
                RelativeSide.RIGHT, DataType.OUTPUT,
                RelativeSide.TOP, DataType.INPUT_2,
                RelativeSide.BOTTOM, DataType.EXTRA,
                RelativeSide.BACK, DataType.ENERGY)),
          TransmissionType.CHEMICAL, all(DataType.INPUT),
          TransmissionType.ENERGY, all(DataType.INPUT)));

    /** Water goes in as a fluid (or a water bucket item), ice comes out on the front and the right side. */
    public static final AttachedSideConfig ICE_MAKER = new AttachedSideConfig(config(
          TransmissionType.ITEM, item(map(
                RelativeSide.FRONT, DataType.OUTPUT,
                RelativeSide.LEFT, DataType.INPUT,
                RelativeSide.RIGHT, DataType.OUTPUT,
                RelativeSide.TOP, DataType.INPUT,
                RelativeSide.BOTTOM, DataType.INPUT,
                RelativeSide.BACK, DataType.ENERGY)),
          TransmissionType.FLUID, all(DataType.INPUT),
          TransmissionType.ENERGY, all(DataType.INPUT)));

    /** The generator only produces: cobblestone can be pulled from every face except the energy port. */
    public static final AttachedSideConfig COBBLESTONE_GENERATOR = new AttachedSideConfig(config(
          TransmissionType.ITEM, item(map(
                RelativeSide.FRONT, DataType.OUTPUT,
                RelativeSide.LEFT, DataType.OUTPUT,
                RelativeSide.RIGHT, DataType.OUTPUT,
                RelativeSide.TOP, DataType.OUTPUT,
                RelativeSide.BOTTOM, DataType.OUTPUT,
                RelativeSide.BACK, DataType.ENERGY)),
          TransmissionType.ENERGY, all(DataType.INPUT)));

    /** The generator only produces: basalt can be pulled from every face except the energy port. */
    public static final AttachedSideConfig BASALT_GENERATOR = new AttachedSideConfig(config(
          TransmissionType.ITEM, item(map(
                RelativeSide.FRONT, DataType.OUTPUT,
                RelativeSide.LEFT, DataType.OUTPUT,
                RelativeSide.RIGHT, DataType.OUTPUT,
                RelativeSide.TOP, DataType.OUTPUT,
                RelativeSide.BOTTOM, DataType.OUTPUT,
                RelativeSide.BACK, DataType.ENERGY)),
          TransmissionType.ENERGY, all(DataType.INPUT)));

    /** Container inputs on the left/top/bottom, converted containers out on the front/right, fluid in/out everywhere. */
    public static final AttachedSideConfig FLUID_INJECTOR = new AttachedSideConfig(config(
          TransmissionType.ITEM, item(map(
                RelativeSide.FRONT, DataType.OUTPUT,
                RelativeSide.LEFT, DataType.INPUT,
                RelativeSide.RIGHT, DataType.OUTPUT,
                RelativeSide.TOP, DataType.INPUT,
                RelativeSide.BOTTOM, DataType.INPUT,
                RelativeSide.BACK, DataType.ENERGY)),
          TransmissionType.FLUID, all(DataType.INPUT_OUTPUT),
          TransmissionType.ENERGY, all(DataType.INPUT)));

    /** Sodium comes in as a chemical, the superheated sodium leaves on the front and the right side. */
    public static final AttachedSideConfig LIQUID_HEATER = new AttachedSideConfig(config(
          TransmissionType.ITEM, item(map(
                RelativeSide.FRONT, DataType.OUTPUT,
                RelativeSide.LEFT, DataType.INPUT,
                RelativeSide.RIGHT, DataType.OUTPUT,
                RelativeSide.TOP, DataType.INPUT,
                RelativeSide.BOTTOM, DataType.INPUT,
                RelativeSide.BACK, DataType.ENERGY)),
          TransmissionType.CHEMICAL, all(DataType.INPUT_OUTPUT),
          TransmissionType.ENERGY, all(DataType.INPUT)));

    /** Fuel goes in on every face, the produced energy leaves through every face as well. */
    public static final AttachedSideConfig IMPROVED_GAS_GENERATOR = new AttachedSideConfig(config(
          TransmissionType.ITEM, item(map(
                RelativeSide.FRONT, DataType.NONE,
                RelativeSide.LEFT, DataType.NONE,
                RelativeSide.RIGHT, DataType.NONE,
                RelativeSide.TOP, DataType.NONE,
                RelativeSide.BOTTOM, DataType.NONE,
                RelativeSide.BACK, DataType.ENERGY)),
          TransmissionType.CHEMICAL, all(DataType.INPUT),
          TransmissionType.ENERGY, all(DataType.OUTPUT)));

    /** The collector pushes what it picked up out to every face except the energy port. */
    public static final AttachedSideConfig ITEM_COLLECTOR = new AttachedSideConfig(config(
          TransmissionType.ITEM, item(map(
                RelativeSide.FRONT, DataType.OUTPUT,
                RelativeSide.LEFT, DataType.OUTPUT,
                RelativeSide.RIGHT, DataType.OUTPUT,
                RelativeSide.TOP, DataType.OUTPUT,
                RelativeSide.BOTTOM, DataType.OUTPUT,
                RelativeSide.BACK, DataType.ENERGY)),
          TransmissionType.ENERGY, all(DataType.INPUT)));

    /** Machine items go in on the left/top/bottom, the clean machine items come out on the front/right. */
    public static final AttachedSideConfig MACHINE_RESETTER = new AttachedSideConfig(config(
          TransmissionType.ITEM, item(map(
                RelativeSide.FRONT, DataType.OUTPUT,
                RelativeSide.LEFT, DataType.INPUT,
                RelativeSide.RIGHT, DataType.OUTPUT,
                RelativeSide.TOP, DataType.INPUT,
                RelativeSide.BOTTOM, DataType.INPUT,
                RelativeSide.BACK, DataType.ENERGY)),
          TransmissionType.ENERGY, all(DataType.INPUT)));

    /**
     * The wind generators are internal storage that only the player can fill, so no side exposes item transport except
     * the back (the energy slot). The produced energy leaves through every face of the block.
     */
    public static final AttachedSideConfig WIND_FUSION = new AttachedSideConfig(config(
          TransmissionType.ITEM, item(map(
                RelativeSide.FRONT, DataType.NONE,
                RelativeSide.LEFT, DataType.NONE,
                RelativeSide.RIGHT, DataType.NONE,
                RelativeSide.TOP, DataType.NONE,
                RelativeSide.BOTTOM, DataType.NONE,
                RelativeSide.BACK, DataType.ENERGY)),
          TransmissionType.ENERGY, all(DataType.OUTPUT)));

    private static LightConfigInfo item(Map<RelativeSide, DataType> sides) {
        return new LightConfigInfo(sides, false);
    }

    private static LightConfigInfo all(DataType dataType) {
        EnumMap<RelativeSide, DataType> sides = new EnumMap<>(RelativeSide.class);
        for (RelativeSide side : RelativeSide.values()) {
            sides.put(side, dataType);
        }
        return new LightConfigInfo(sides, false);
    }

    private static Map<RelativeSide, DataType> map(Object... sideToType) {
        EnumMap<RelativeSide, DataType> sides = new EnumMap<>(RelativeSide.class);
        for (int i = 0; i < sideToType.length; i += 2) {
            sides.put((RelativeSide) sideToType[i], (DataType) sideToType[i + 1]);
        }
        return sides;
    }

    private static Map<TransmissionType, LightConfigInfo> config(Object... typeToInfo) {
        EnumMap<TransmissionType, LightConfigInfo> config = new EnumMap<>(TransmissionType.class);
        for (int i = 0; i < typeToInfo.length; i += 2) {
            config.put((TransmissionType) typeToInfo[i], (LightConfigInfo) typeToInfo[i + 1]);
        }
        return config;
    }

    private ModSideConfigs() {
    }
}
