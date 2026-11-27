package jp.example.mekanismgizmo.registry;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.item.ImprovedConfiguratorItem;
import jp.example.mekanismgizmo.item.ImprovedPortableTeleporterItem;
import jp.example.mekanismgizmo.item.PipetteItem;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;

/**
 * Items that are not machine blocks. The pipettes are tiered gauge droppers: the tier only changes how much liquid and
 * gas they hold, the handling (machine tank gauges, sneak + right click to void) is Mekanism's own.
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

    /**
     * Mekanism's portable teleporter with a hundred times the energy buffer. The item itself is a subclass of
     * Mekanism's, so the screen, the frequency network, the teleport packet and its distance based cost, the owner
     * handling and the charging are all Mekanism's; only the attached energy container is ours.
     */
    /**
     * Configuration card that additionally copies the installed upgrades, the machine frequencies and the digital
     * miner's filter list, and refuses to install upgrades the player cannot pay for.
     */
    public static final ItemRegistryObject<ImprovedConfiguratorItem> IMPROVED_CONFIGURATOR = ITEMS
          .registerItem("improved_configurator", ImprovedConfiguratorItem::new);

    public static final ItemRegistryObject<ImprovedPortableTeleporterItem> IMPROVED_PORTABLE_TELEPORTER = ITEMS
          .registerItem("improved_portable_teleporter", ImprovedPortableTeleporterItem::new)
          .addAttachedContainerCapabilities(ContainerType.ENERGY, ImprovedPortableTeleporterItem::energyContainers,
                MekanismConfig.gear);

    /**
     * One liquid tank and one gas tank per pipette, both sized by the tier. Mekanism's gauge dropper shares a single
     * merged tank; separate tanks also expose the {@code IFluidHandlerItem} / {@code IChemicalHandler} capabilities the
     * machine screens and the sneak-right-click dump use, and they let the pipette hold both at once.
     */
    private static ItemRegistryObject<PipetteItem> pipette(String name, int capacity) {
        return ITEMS.registerItem(name, properties -> new PipetteItem(properties, capacity))
              .addAttachedContainerCapabilities(ContainerType.FLUID, () -> PipetteItem.fluidTanks(capacity), MekanismConfig.gear)
              .addAttachedContainerCapabilities(ContainerType.CHEMICAL, () -> PipetteItem.chemicalTanks(capacity), MekanismConfig.gear);
    }

    private ModItems() {
    }
}
