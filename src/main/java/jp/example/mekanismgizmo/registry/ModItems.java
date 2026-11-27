package jp.example.mekanismgizmo.registry;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.item.ImprovedConfiguratorItem;
import jp.example.mekanismgizmo.item.ImprovedPortableTeleporterItem;
import jp.example.mekanismgizmo.item.PipetteItem;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;

/**
 * Items that are not machine blocks.
 */
public final class ModItems {
    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismGizmo.MOD_ID);

    private static final int BASIC_CAPACITY = 2_000;
    private static final int ADVANCED_CAPACITY = 4_000;
    private static final int ELITE_CAPACITY = 8_000;
    private static final int ULTIMATE_CAPACITY = 16_000;

    public static final ItemRegistryObject<PipetteItem> BASIC_PIPETTE = pipette("basic_pipette", BASIC_CAPACITY);
    public static final ItemRegistryObject<PipetteItem> ADVANCED_PIPETTE = pipette("advanced_pipette", ADVANCED_CAPACITY);
    public static final ItemRegistryObject<PipetteItem> ELITE_PIPETTE = pipette("elite_pipette", ELITE_CAPACITY);
    public static final ItemRegistryObject<PipetteItem> ULTIMATE_PIPETTE = pipette("ultimate_pipette", ULTIMATE_CAPACITY);

    public static final ItemRegistryObject<ImprovedConfiguratorItem> IMPROVED_CONFIGURATOR = ITEMS
          .register("improved_configurator", ImprovedConfiguratorItem::new);

    public static final ItemRegistryObject<ImprovedPortableTeleporterItem> IMPROVED_PORTABLE_TELEPORTER = ITEMS
          .register("improved_portable_teleporter", ImprovedPortableTeleporterItem::new);

    private static ItemRegistryObject<PipetteItem> pipette(String name, int capacity) {
        return ITEMS.register(name, properties -> new PipetteItem(properties, capacity));
    }

    private ModItems() {
    }
}
