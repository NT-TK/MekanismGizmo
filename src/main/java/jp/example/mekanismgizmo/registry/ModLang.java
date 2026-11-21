package jp.example.mekanismgizmo.registry;

import jp.example.mekanismgizmo.MekanismGizmo;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.ILangEntry;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public enum ModLang implements ILangEntry {
    DESCRIPTION_POTION_BREWER("description", "potion_brewer"),
    DESCRIPTION_ICE_MAKER("description", "ice_maker"),
    DESCRIPTION_COBBLESTONE_GENERATOR("description", "cobblestone_generator"),
    DESCRIPTION_BASALT_GENERATOR("description", "basalt_generator"),
    DESCRIPTION_ITEM_COLLECTOR("description", "item_collector"),
    DESCRIPTION_FLUID_INJECTOR("description", "fluid_injector"),
    DESCRIPTION_WIND_FUSION("description", "wind_fusion"),
    DESCRIPTION_MACHINE_RESETTER("description", "machine_resetter"),
    DESCRIPTION_LIQUID_HEATER("description", "liquid_heater"),
    DESCRIPTION_IMPROVED_GAS_GENERATOR("description", "improved_gas_generator"),
    TOOLTIP_CONTAINER_MODE("tooltip", "container_mode");

    private final String key;

    ModLang(String type, String path) {
        this(Util.makeDescriptionId(type, ResourceLocation.fromNamespaceAndPath(MekanismGizmo.MOD_ID, path)));
    }

    ModLang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
