package jp.example.mekanismgizmo.util;

import java.util.EnumMap;
import java.util.Map;
import mekanism.api.RelativeSide;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.attachments.component.AttachedSideConfig.LightConfigInfo;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;

/**
 * Mekanism's {@link ConfigInfo} starts with every side set to {@link DataType#NONE}, so a machine only accepts
 * energy, chemicals, fluids or items once a side configuration has been applied (normally the
 * {@code mekanism:side_config} component carried by the block item). Machines placed from an older item stack, saved
 * by an older version of this mod, placed with {@code /setblock} or with a copied all-"None" configuration would
 * otherwise silently refuse every connection (not even cables could insert power).
 * <p>
 * This restores the machine's default side configuration, but only when nothing was configured yet, so any
 * configuration the player made in the side configuration tab (or copied with a Configuration Card) is kept.
 */
public final class MachineSideDefaults {

    /** The generic defaults used by earlier versions (everything input, right output, energy at the back). */
    private static final Map<RelativeSide, DataType> LEGACY_ITEM_DEFAULTS = legacyItemDefaults();

    public static void applyIfUnconfigured(TileComponentConfig configComponent, AttachedSideConfig defaults) {
        for (Map.Entry<TransmissionType, LightConfigInfo> entry : defaults.configInfo().entrySet()) {
            ConfigInfo config = configComponent.getConfig(entry.getKey());
            if (config == null) {
                continue;
            }
            boolean legacy = entry.getKey() == TransmissionType.ITEM && matches(config, LEGACY_ITEM_DEFAULTS);
            if (legacy || isAllNone(config)) {
                for (Map.Entry<RelativeSide, DataType> side : entry.getValue().sideConfig().entrySet()) {
                    config.setDataType(side.getValue(), side.getKey());
                }
            }
        }
    }

    private static boolean isAllNone(ConfigInfo config) {
        for (RelativeSide side : RelativeSide.values()) {
            if (config.getDataType(side) != DataType.NONE) {
                return false;
            }
        }
        return true;
    }

    private static boolean matches(ConfigInfo config, Map<RelativeSide, DataType> expected) {
        for (RelativeSide side : RelativeSide.values()) {
            if (config.getDataType(side) != expected.get(side)) {
                return false;
            }
        }
        return true;
    }

    private static Map<RelativeSide, DataType> legacyItemDefaults() {
        EnumMap<RelativeSide, DataType> sides = new EnumMap<>(RelativeSide.class);
        for (RelativeSide side : RelativeSide.values()) {
            sides.put(side, DataType.INPUT);
        }
        sides.put(RelativeSide.RIGHT, DataType.OUTPUT);
        sides.put(RelativeSide.BACK, DataType.ENERGY);
        return sides;
    }

    private MachineSideDefaults() {
    }
}
