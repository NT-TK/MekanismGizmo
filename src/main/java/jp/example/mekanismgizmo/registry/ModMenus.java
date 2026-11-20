package jp.example.mekanismgizmo.registry;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.block.BasaltGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.CobblestoneGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.FluidInjectorBlockEntity;
import jp.example.mekanismgizmo.block.IceMakerBlockEntity;
import jp.example.mekanismgizmo.block.ItemCollectorBlockEntity;
import jp.example.mekanismgizmo.block.MachineResetterBlockEntity;
import jp.example.mekanismgizmo.block.PotionBrewerBlockEntity;
import jp.example.mekanismgizmo.block.WindFusionBlockEntity;
import jp.example.mekanismgizmo.menu.BasaltGeneratorMenu;
import jp.example.mekanismgizmo.menu.CobblestoneGeneratorMenu;
import jp.example.mekanismgizmo.menu.FluidInjectorMenu;
import jp.example.mekanismgizmo.menu.IceMakerMenu;
import jp.example.mekanismgizmo.menu.ItemCollectorMenu;
import jp.example.mekanismgizmo.menu.MachineResetterMenu;
import jp.example.mekanismgizmo.menu.PotionBrewerMenu;
import jp.example.mekanismgizmo.menu.WindFusionMenu;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;

public final class ModMenus {
    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(MekanismGizmo.MOD_ID);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<PotionBrewerBlockEntity>> POTION_BREWER = CONTAINER_TYPES
          .register("potion_brewer", PotionBrewerBlockEntity.class, PotionBrewerMenu::new);
    public static final ContainerTypeRegistryObject<MekanismTileContainer<IceMakerBlockEntity>> ICE_MAKER = CONTAINER_TYPES
          .register("ice_maker", IceMakerBlockEntity.class, IceMakerMenu::new);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<CobblestoneGeneratorBlockEntity>> COBBLESTONE_GENERATOR = CONTAINER_TYPES
          .register("cobblestone_generator", CobblestoneGeneratorBlockEntity.class, CobblestoneGeneratorMenu::new);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<BasaltGeneratorBlockEntity>> BASALT_GENERATOR = CONTAINER_TYPES
          .register("basalt_generator", BasaltGeneratorBlockEntity.class, BasaltGeneratorMenu::new);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<FluidInjectorBlockEntity>> FLUID_INJECTOR = CONTAINER_TYPES
          .register("fluid_injector", FluidInjectorBlockEntity.class, FluidInjectorMenu::new);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<ItemCollectorBlockEntity>> ITEM_COLLECTOR = CONTAINER_TYPES
          .register("item_collector", ItemCollectorBlockEntity.class, ItemCollectorMenu::new);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<MachineResetterBlockEntity>> MACHINE_RESETTER = CONTAINER_TYPES
          .register("machine_resetter", MachineResetterBlockEntity.class, MachineResetterMenu::new);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<WindFusionBlockEntity>> WIND_FUSION = CONTAINER_TYPES
          .register("wind_fusion", WindFusionBlockEntity.class, WindFusionMenu::new);

    private ModMenus() {
    }
}
