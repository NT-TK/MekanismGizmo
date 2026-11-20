package jp.example.mekanismgizmo.registry;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.block.BasaltGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.CobblestoneGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.FluidInjectorBlockEntity;
import jp.example.mekanismgizmo.block.IceMakerBlockEntity;
import jp.example.mekanismgizmo.block.ItemCollectorBlockEntity;
import jp.example.mekanismgizmo.block.PotionBrewerBlockEntity;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;

/**
 * One block entity type per machine and tier, like Mekanism's own factories: the tile type is what tells the tile
 * which tier block created it, which in turn decides how many slots it has. The untiered machine, its basic tier and
 * the three higher tiers each get their own type.
 */
public final class ModBlockEntities {
    public static final TileEntityTypeDeferredRegister TILE_ENTITIES = new TileEntityTypeDeferredRegister(MekanismGizmo.MOD_ID);

    public static final TileEntityTypeRegistryObject<PotionBrewerBlockEntity> POTION_BREWER = potionBrewer(ModBlocks.POTION_BREWER);
    public static final TileEntityTypeRegistryObject<PotionBrewerBlockEntity> POTION_BREWER_BASIC = potionBrewer(ModBlocks.POTION_BREWER_BASIC);
    public static final TileEntityTypeRegistryObject<PotionBrewerBlockEntity> POTION_BREWER_ADVANCED = potionBrewer(ModBlocks.POTION_BREWER_ADVANCED);
    public static final TileEntityTypeRegistryObject<PotionBrewerBlockEntity> POTION_BREWER_ELITE = potionBrewer(ModBlocks.POTION_BREWER_ELITE);
    public static final TileEntityTypeRegistryObject<PotionBrewerBlockEntity> POTION_BREWER_ULTIMATE = potionBrewer(ModBlocks.POTION_BREWER_ULTIMATE);

    public static final TileEntityTypeRegistryObject<IceMakerBlockEntity> ICE_MAKER = iceMaker(ModBlocks.ICE_MAKER);
    public static final TileEntityTypeRegistryObject<IceMakerBlockEntity> ICE_MAKER_BASIC = iceMaker(ModBlocks.ICE_MAKER_BASIC);
    public static final TileEntityTypeRegistryObject<IceMakerBlockEntity> ICE_MAKER_ADVANCED = iceMaker(ModBlocks.ICE_MAKER_ADVANCED);
    public static final TileEntityTypeRegistryObject<IceMakerBlockEntity> ICE_MAKER_ELITE = iceMaker(ModBlocks.ICE_MAKER_ELITE);
    public static final TileEntityTypeRegistryObject<IceMakerBlockEntity> ICE_MAKER_ULTIMATE = iceMaker(ModBlocks.ICE_MAKER_ULTIMATE);

    public static final TileEntityTypeRegistryObject<CobblestoneGeneratorBlockEntity> COBBLESTONE_GENERATOR = cobblestoneGenerator(ModBlocks.COBBLESTONE_GENERATOR);
    public static final TileEntityTypeRegistryObject<CobblestoneGeneratorBlockEntity> COBBLESTONE_GENERATOR_BASIC = cobblestoneGenerator(ModBlocks.COBBLESTONE_GENERATOR_BASIC);
    public static final TileEntityTypeRegistryObject<CobblestoneGeneratorBlockEntity> COBBLESTONE_GENERATOR_ADVANCED = cobblestoneGenerator(ModBlocks.COBBLESTONE_GENERATOR_ADVANCED);
    public static final TileEntityTypeRegistryObject<CobblestoneGeneratorBlockEntity> COBBLESTONE_GENERATOR_ELITE = cobblestoneGenerator(ModBlocks.COBBLESTONE_GENERATOR_ELITE);
    public static final TileEntityTypeRegistryObject<CobblestoneGeneratorBlockEntity> COBBLESTONE_GENERATOR_ULTIMATE = cobblestoneGenerator(ModBlocks.COBBLESTONE_GENERATOR_ULTIMATE);

    public static final TileEntityTypeRegistryObject<BasaltGeneratorBlockEntity> BASALT_GENERATOR = basaltGenerator(ModBlocks.BASALT_GENERATOR);
    public static final TileEntityTypeRegistryObject<BasaltGeneratorBlockEntity> BASALT_GENERATOR_BASIC = basaltGenerator(ModBlocks.BASALT_GENERATOR_BASIC);
    public static final TileEntityTypeRegistryObject<BasaltGeneratorBlockEntity> BASALT_GENERATOR_ADVANCED = basaltGenerator(ModBlocks.BASALT_GENERATOR_ADVANCED);
    public static final TileEntityTypeRegistryObject<BasaltGeneratorBlockEntity> BASALT_GENERATOR_ELITE = basaltGenerator(ModBlocks.BASALT_GENERATOR_ELITE);
    public static final TileEntityTypeRegistryObject<BasaltGeneratorBlockEntity> BASALT_GENERATOR_ULTIMATE = basaltGenerator(ModBlocks.BASALT_GENERATOR_ULTIMATE);

    public static final TileEntityTypeRegistryObject<FluidInjectorBlockEntity> FLUID_INJECTOR = fluidInjector(ModBlocks.FLUID_INJECTOR);
    public static final TileEntityTypeRegistryObject<FluidInjectorBlockEntity> FLUID_INJECTOR_BASIC = fluidInjector(ModBlocks.FLUID_INJECTOR_BASIC);
    public static final TileEntityTypeRegistryObject<FluidInjectorBlockEntity> FLUID_INJECTOR_ADVANCED = fluidInjector(ModBlocks.FLUID_INJECTOR_ADVANCED);
    public static final TileEntityTypeRegistryObject<FluidInjectorBlockEntity> FLUID_INJECTOR_ELITE = fluidInjector(ModBlocks.FLUID_INJECTOR_ELITE);
    public static final TileEntityTypeRegistryObject<FluidInjectorBlockEntity> FLUID_INJECTOR_ULTIMATE = fluidInjector(ModBlocks.FLUID_INJECTOR_ULTIMATE);

    public static final TileEntityTypeRegistryObject<ItemCollectorBlockEntity> ITEM_COLLECTOR = TILE_ENTITIES
          .mekBuilder(ModBlocks.ITEM_COLLECTOR, (pos, state) -> new ItemCollectorBlockEntity(ModBlocks.ITEM_COLLECTOR, pos, state))
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();

    private static TileEntityTypeRegistryObject<PotionBrewerBlockEntity> potionBrewer(BlockRegistryObject<?, ?> block) {
        return TILE_ENTITIES.mekBuilder(block, (pos, state) -> new PotionBrewerBlockEntity(block, pos, state))
              .clientTicker(TileEntityMekanism::tickClient)
              .serverTicker(TileEntityMekanism::tickServer)
              .build();
    }

    private static TileEntityTypeRegistryObject<IceMakerBlockEntity> iceMaker(BlockRegistryObject<?, ?> block) {
        return TILE_ENTITIES.mekBuilder(block, (pos, state) -> new IceMakerBlockEntity(block, pos, state))
              .clientTicker(TileEntityMekanism::tickClient)
              .serverTicker(TileEntityMekanism::tickServer)
              .build();
    }

    private static TileEntityTypeRegistryObject<CobblestoneGeneratorBlockEntity> cobblestoneGenerator(BlockRegistryObject<?, ?> block) {
        return TILE_ENTITIES.mekBuilder(block, (pos, state) -> new CobblestoneGeneratorBlockEntity(block, pos, state))
              .clientTicker(TileEntityMekanism::tickClient)
              .serverTicker(TileEntityMekanism::tickServer)
              .build();
    }

    private static TileEntityTypeRegistryObject<BasaltGeneratorBlockEntity> basaltGenerator(BlockRegistryObject<?, ?> block) {
        return TILE_ENTITIES.mekBuilder(block, (pos, state) -> new BasaltGeneratorBlockEntity(block, pos, state))
              .clientTicker(TileEntityMekanism::tickClient)
              .serverTicker(TileEntityMekanism::tickServer)
              .build();
    }

    private static TileEntityTypeRegistryObject<FluidInjectorBlockEntity> fluidInjector(BlockRegistryObject<?, ?> block) {
        return TILE_ENTITIES.mekBuilder(block, (pos, state) -> new FluidInjectorBlockEntity(block, pos, state))
              .clientTicker(TileEntityMekanism::tickClient)
              .serverTicker(TileEntityMekanism::tickServer)
              .build();
    }

    private ModBlockEntities() {
    }
}
