package jp.example.mekanismgizmo.registry;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.block.BasaltGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.CobblestoneGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.FluidInjectorBlockEntity;
import jp.example.mekanismgizmo.block.IceMakerBlockEntity;
import jp.example.mekanismgizmo.block.HeatFusionBlockEntity;
import jp.example.mekanismgizmo.block.ImprovedGasGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.ItemCollectorBlockEntity;
import jp.example.mekanismgizmo.block.LiquidHeaterBlockEntity;
import jp.example.mekanismgizmo.block.MachineResetterBlockEntity;
import jp.example.mekanismgizmo.block.PotionBrewerBlockEntity;
import jp.example.mekanismgizmo.block.WindFusionBlockEntity;
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

    public static final TileEntityTypeRegistryObject<LiquidHeaterBlockEntity> LIQUID_HEATER = liquidHeater(ModBlocks.LIQUID_HEATER);
    public static final TileEntityTypeRegistryObject<LiquidHeaterBlockEntity> LIQUID_HEATER_BASIC = liquidHeater(ModBlocks.LIQUID_HEATER_BASIC);
    public static final TileEntityTypeRegistryObject<LiquidHeaterBlockEntity> LIQUID_HEATER_ADVANCED = liquidHeater(ModBlocks.LIQUID_HEATER_ADVANCED);
    public static final TileEntityTypeRegistryObject<LiquidHeaterBlockEntity> LIQUID_HEATER_ELITE = liquidHeater(ModBlocks.LIQUID_HEATER_ELITE);
    public static final TileEntityTypeRegistryObject<LiquidHeaterBlockEntity> LIQUID_HEATER_ULTIMATE = liquidHeater(ModBlocks.LIQUID_HEATER_ULTIMATE);

    public static final TileEntityTypeRegistryObject<ImprovedGasGeneratorBlockEntity> IMPROVED_GAS_GENERATOR = TILE_ENTITIES
          .mekBuilder(ModBlocks.IMPROVED_GAS_GENERATOR,
                (pos, state) -> new ImprovedGasGeneratorBlockEntity(ModBlocks.IMPROVED_GAS_GENERATOR, pos, state))
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();

    public static final TileEntityTypeRegistryObject<HeatFusionBlockEntity> HEAT_FUSION = TILE_ENTITIES
          .mekBuilder(ModBlocks.HEAT_FUSION, (pos, state) -> new HeatFusionBlockEntity(ModBlocks.HEAT_FUSION, pos, state))
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();

    public static final TileEntityTypeRegistryObject<ItemCollectorBlockEntity> ITEM_COLLECTOR = TILE_ENTITIES
          .mekBuilder(ModBlocks.ITEM_COLLECTOR, (pos, state) -> new ItemCollectorBlockEntity(ModBlocks.ITEM_COLLECTOR, pos, state))
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();

    public static final TileEntityTypeRegistryObject<MachineResetterBlockEntity> MACHINE_RESETTER = TILE_ENTITIES
          .mekBuilder(ModBlocks.MACHINE_RESETTER, (pos, state) -> new MachineResetterBlockEntity(ModBlocks.MACHINE_RESETTER, pos, state))
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();

    public static final TileEntityTypeRegistryObject<WindFusionBlockEntity> WIND_FUSION = TILE_ENTITIES
          .mekBuilder(ModBlocks.WIND_FUSION, (pos, state) -> new WindFusionBlockEntity(ModBlocks.WIND_FUSION, pos, state))
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

    private static TileEntityTypeRegistryObject<LiquidHeaterBlockEntity> liquidHeater(BlockRegistryObject<?, ?> block) {
        return TILE_ENTITIES.mekBuilder(block, (pos, state) -> new LiquidHeaterBlockEntity(block, pos, state))
              .clientTicker(TileEntityMekanism::tickClient)
              .serverTicker(TileEntityMekanism::tickServer)
              .build();
    }

    private ModBlockEntities() {
    }
}
