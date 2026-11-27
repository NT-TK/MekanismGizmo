package jp.example.mekanismgizmo.registry;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.block.BasaltGeneratorBlock;
import jp.example.mekanismgizmo.block.BasaltGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.CobblestoneGeneratorBlock;
import jp.example.mekanismgizmo.block.CobblestoneGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.FluidInjectorBlock;
import jp.example.mekanismgizmo.block.FluidInjectorBlockEntity;
import jp.example.mekanismgizmo.block.IceMakerBlock;
import jp.example.mekanismgizmo.block.IceMakerBlockEntity;
import jp.example.mekanismgizmo.block.HeatFusionBlock;
import jp.example.mekanismgizmo.block.HeatFusionBlockEntity;
import jp.example.mekanismgizmo.block.ImprovedGasGeneratorBlock;
import jp.example.mekanismgizmo.block.ImprovedGasGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.ItemCollectorBlock;
import jp.example.mekanismgizmo.block.ItemCollectorBlockEntity;
import jp.example.mekanismgizmo.block.LiquidHeaterBlock;
import jp.example.mekanismgizmo.block.LiquidHeaterBlockEntity;
import jp.example.mekanismgizmo.block.MachineResetterBlock;
import jp.example.mekanismgizmo.block.MachineResetterBlockEntity;
import jp.example.mekanismgizmo.block.PotionBrewerBlock;
import jp.example.mekanismgizmo.block.PotionBrewerBlockEntity;
import jp.example.mekanismgizmo.block.WindFusionBlock;
import jp.example.mekanismgizmo.block.WindFusionBlockEntity;
import jp.example.mekanismgizmo.util.MachineTier;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.world.level.block.Block;

/**
 * Machine blocks. Everything except the item collector is registered untiered plus in Mekanism's four tiers; the
 * untiered machine keeps the original registry name ({@code ice_maker}) so existing worlds and recipes keep working.
 */
public final class ModBlocks {
    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekanismGizmo.MOD_ID);

    // --- potion brewer ---
    public static final BlockRegistryObject<PotionBrewerBlock, ItemBlockTooltip<PotionBrewerBlock>> POTION_BREWER =
          potionBrewer("potion_brewer", ModBlockTypes.POTION_BREWER, MachineTier.UNTIERED);
    public static final BlockRegistryObject<PotionBrewerBlock, ItemBlockTooltip<PotionBrewerBlock>> POTION_BREWER_BASIC =
          potionBrewer("basic_potion_brewer", ModBlockTypes.POTION_BREWER_BASIC, MachineTier.BASIC);
    public static final BlockRegistryObject<PotionBrewerBlock, ItemBlockTooltip<PotionBrewerBlock>> POTION_BREWER_ADVANCED =
          potionBrewer("advanced_potion_brewer", ModBlockTypes.POTION_BREWER_ADVANCED, MachineTier.ADVANCED);
    public static final BlockRegistryObject<PotionBrewerBlock, ItemBlockTooltip<PotionBrewerBlock>> POTION_BREWER_ELITE =
          potionBrewer("elite_potion_brewer", ModBlockTypes.POTION_BREWER_ELITE, MachineTier.ELITE);
    public static final BlockRegistryObject<PotionBrewerBlock, ItemBlockTooltip<PotionBrewerBlock>> POTION_BREWER_ULTIMATE =
          potionBrewer("ultimate_potion_brewer", ModBlockTypes.POTION_BREWER_ULTIMATE, MachineTier.ULTIMATE);

    // --- ice maker ---
    public static final BlockRegistryObject<IceMakerBlock, ItemBlockTooltip<IceMakerBlock>> ICE_MAKER =
          iceMaker("ice_maker", ModBlockTypes.ICE_MAKER, MachineTier.UNTIERED);
    public static final BlockRegistryObject<IceMakerBlock, ItemBlockTooltip<IceMakerBlock>> ICE_MAKER_BASIC =
          iceMaker("basic_ice_maker", ModBlockTypes.ICE_MAKER_BASIC, MachineTier.BASIC);
    public static final BlockRegistryObject<IceMakerBlock, ItemBlockTooltip<IceMakerBlock>> ICE_MAKER_ADVANCED =
          iceMaker("advanced_ice_maker", ModBlockTypes.ICE_MAKER_ADVANCED, MachineTier.ADVANCED);
    public static final BlockRegistryObject<IceMakerBlock, ItemBlockTooltip<IceMakerBlock>> ICE_MAKER_ELITE =
          iceMaker("elite_ice_maker", ModBlockTypes.ICE_MAKER_ELITE, MachineTier.ELITE);
    public static final BlockRegistryObject<IceMakerBlock, ItemBlockTooltip<IceMakerBlock>> ICE_MAKER_ULTIMATE =
          iceMaker("ultimate_ice_maker", ModBlockTypes.ICE_MAKER_ULTIMATE, MachineTier.ULTIMATE);

    // --- cobblestone generator ---
    public static final BlockRegistryObject<CobblestoneGeneratorBlock, ItemBlockTooltip<CobblestoneGeneratorBlock>> COBBLESTONE_GENERATOR =
          cobblestoneGenerator("cobblestone_generator", ModBlockTypes.COBBLESTONE_GENERATOR, MachineTier.UNTIERED);
    public static final BlockRegistryObject<CobblestoneGeneratorBlock, ItemBlockTooltip<CobblestoneGeneratorBlock>> COBBLESTONE_GENERATOR_BASIC =
          cobblestoneGenerator("basic_cobblestone_generator", ModBlockTypes.COBBLESTONE_GENERATOR_BASIC, MachineTier.BASIC);
    public static final BlockRegistryObject<CobblestoneGeneratorBlock, ItemBlockTooltip<CobblestoneGeneratorBlock>> COBBLESTONE_GENERATOR_ADVANCED =
          cobblestoneGenerator("advanced_cobblestone_generator", ModBlockTypes.COBBLESTONE_GENERATOR_ADVANCED, MachineTier.ADVANCED);
    public static final BlockRegistryObject<CobblestoneGeneratorBlock, ItemBlockTooltip<CobblestoneGeneratorBlock>> COBBLESTONE_GENERATOR_ELITE =
          cobblestoneGenerator("elite_cobblestone_generator", ModBlockTypes.COBBLESTONE_GENERATOR_ELITE, MachineTier.ELITE);
    public static final BlockRegistryObject<CobblestoneGeneratorBlock, ItemBlockTooltip<CobblestoneGeneratorBlock>> COBBLESTONE_GENERATOR_ULTIMATE =
          cobblestoneGenerator("ultimate_cobblestone_generator", ModBlockTypes.COBBLESTONE_GENERATOR_ULTIMATE, MachineTier.ULTIMATE);

    // --- basalt generator ---
    public static final BlockRegistryObject<BasaltGeneratorBlock, ItemBlockTooltip<BasaltGeneratorBlock>> BASALT_GENERATOR =
          basaltGenerator("basalt_generator", ModBlockTypes.BASALT_GENERATOR, MachineTier.UNTIERED);
    public static final BlockRegistryObject<BasaltGeneratorBlock, ItemBlockTooltip<BasaltGeneratorBlock>> BASALT_GENERATOR_BASIC =
          basaltGenerator("basic_basalt_generator", ModBlockTypes.BASALT_GENERATOR_BASIC, MachineTier.BASIC);
    public static final BlockRegistryObject<BasaltGeneratorBlock, ItemBlockTooltip<BasaltGeneratorBlock>> BASALT_GENERATOR_ADVANCED =
          basaltGenerator("advanced_basalt_generator", ModBlockTypes.BASALT_GENERATOR_ADVANCED, MachineTier.ADVANCED);
    public static final BlockRegistryObject<BasaltGeneratorBlock, ItemBlockTooltip<BasaltGeneratorBlock>> BASALT_GENERATOR_ELITE =
          basaltGenerator("elite_basalt_generator", ModBlockTypes.BASALT_GENERATOR_ELITE, MachineTier.ELITE);
    public static final BlockRegistryObject<BasaltGeneratorBlock, ItemBlockTooltip<BasaltGeneratorBlock>> BASALT_GENERATOR_ULTIMATE =
          basaltGenerator("ultimate_basalt_generator", ModBlockTypes.BASALT_GENERATOR_ULTIMATE, MachineTier.ULTIMATE);

    // --- fluid injector ---
    public static final BlockRegistryObject<FluidInjectorBlock, ItemBlockTooltip<FluidInjectorBlock>> FLUID_INJECTOR =
          fluidInjector("fluid_injector", ModBlockTypes.FLUID_INJECTOR, MachineTier.UNTIERED);
    public static final BlockRegistryObject<FluidInjectorBlock, ItemBlockTooltip<FluidInjectorBlock>> FLUID_INJECTOR_BASIC =
          fluidInjector("basic_fluid_injector", ModBlockTypes.FLUID_INJECTOR_BASIC, MachineTier.BASIC);
    public static final BlockRegistryObject<FluidInjectorBlock, ItemBlockTooltip<FluidInjectorBlock>> FLUID_INJECTOR_ADVANCED =
          fluidInjector("advanced_fluid_injector", ModBlockTypes.FLUID_INJECTOR_ADVANCED, MachineTier.ADVANCED);
    public static final BlockRegistryObject<FluidInjectorBlock, ItemBlockTooltip<FluidInjectorBlock>> FLUID_INJECTOR_ELITE =
          fluidInjector("elite_fluid_injector", ModBlockTypes.FLUID_INJECTOR_ELITE, MachineTier.ELITE);
    public static final BlockRegistryObject<FluidInjectorBlock, ItemBlockTooltip<FluidInjectorBlock>> FLUID_INJECTOR_ULTIMATE =
          fluidInjector("ultimate_fluid_injector", ModBlockTypes.FLUID_INJECTOR_ULTIMATE, MachineTier.ULTIMATE);

    // --- liquid heater ---
    public static final BlockRegistryObject<LiquidHeaterBlock, ItemBlockTooltip<LiquidHeaterBlock>> LIQUID_HEATER =
          liquidHeater("liquid_heater", ModBlockTypes.LIQUID_HEATER, MachineTier.UNTIERED);
    public static final BlockRegistryObject<LiquidHeaterBlock, ItemBlockTooltip<LiquidHeaterBlock>> LIQUID_HEATER_BASIC =
          liquidHeater("basic_liquid_heater", ModBlockTypes.LIQUID_HEATER_BASIC, MachineTier.BASIC);
    public static final BlockRegistryObject<LiquidHeaterBlock, ItemBlockTooltip<LiquidHeaterBlock>> LIQUID_HEATER_ADVANCED =
          liquidHeater("advanced_liquid_heater", ModBlockTypes.LIQUID_HEATER_ADVANCED, MachineTier.ADVANCED);
    public static final BlockRegistryObject<LiquidHeaterBlock, ItemBlockTooltip<LiquidHeaterBlock>> LIQUID_HEATER_ELITE =
          liquidHeater("elite_liquid_heater", ModBlockTypes.LIQUID_HEATER_ELITE, MachineTier.ELITE);
    public static final BlockRegistryObject<LiquidHeaterBlock, ItemBlockTooltip<LiquidHeaterBlock>> LIQUID_HEATER_ULTIMATE =
          liquidHeater("ultimate_liquid_heater", ModBlockTypes.LIQUID_HEATER_ULTIMATE, MachineTier.ULTIMATE);

    // --- improved gas generator (single tier) ---
    public static final BlockRegistryObject<ImprovedGasGeneratorBlock, ItemBlockTooltip<ImprovedGasGeneratorBlock>> IMPROVED_GAS_GENERATOR = BLOCKS
          .register("improved_gas_generator", () -> new ImprovedGasGeneratorBlock(ModBlockTypes.IMPROVED_GAS_GENERATOR),
                block -> machineItem(block))
          ;

    // --- heat generator fusion (single tier) ---
    public static final BlockRegistryObject<HeatFusionBlock, ItemBlockTooltip<HeatFusionBlock>> HEAT_FUSION = BLOCKS
          .register("heat_fusion", () -> new HeatFusionBlock(ModBlockTypes.HEAT_FUSION),
                block -> machineItem(block))
          ;

    // --- item collector (single tier) ---
    public static final BlockRegistryObject<ItemCollectorBlock, ItemBlockTooltip<ItemCollectorBlock>> ITEM_COLLECTOR = BLOCKS
          .register("item_collector", () -> new ItemCollectorBlock(ModBlockTypes.ITEM_COLLECTOR),
                block -> machineItem(block))
          ;

    // --- machine resetter (single tier) ---
    public static final BlockRegistryObject<MachineResetterBlock, ItemBlockTooltip<MachineResetterBlock>> MACHINE_RESETTER = BLOCKS
          .register("machine_resetter", () -> new MachineResetterBlock(ModBlockTypes.MACHINE_RESETTER),
                block -> machineItem(block))
          ;

    // --- wind generator fusion (single tier) ---
    public static final BlockRegistryObject<WindFusionBlock, ItemBlockTooltip<WindFusionBlock>> WIND_FUSION = BLOCKS
          .register("wind_fusion", () -> new WindFusionBlock(ModBlockTypes.WIND_FUSION),
                block -> machineItem(block))
          ;

    private static BlockRegistryObject<PotionBrewerBlock, ItemBlockTooltip<PotionBrewerBlock>> potionBrewer(
          String name, Machine<PotionBrewerBlockEntity> type, MachineTier tier) {
        int bottles = PotionBrewerBlockEntity.bottleCount(tier);
        return BLOCKS.register(name, () -> new PotionBrewerBlock(type, tier),
                    block -> machineItem(block))
              ;
    }

    private static BlockRegistryObject<IceMakerBlock, ItemBlockTooltip<IceMakerBlock>> iceMaker(
          String name, Machine<IceMakerBlockEntity> type, MachineTier tier) {
        int parallel = tier.getParallel();
        return BLOCKS.register(name, () -> new IceMakerBlock(type, tier),
                    block -> machineItem(block))
              ;
    }

    private static BlockRegistryObject<CobblestoneGeneratorBlock, ItemBlockTooltip<CobblestoneGeneratorBlock>> cobblestoneGenerator(
          String name, Machine<CobblestoneGeneratorBlockEntity> type, MachineTier tier) {
        int parallel = tier.getParallel();
        return BLOCKS.register(name, () -> new CobblestoneGeneratorBlock(type, tier),
                    block -> machineItem(block))
              ;
    }

    private static BlockRegistryObject<LiquidHeaterBlock, ItemBlockTooltip<LiquidHeaterBlock>> liquidHeater(
          String name, Machine<LiquidHeaterBlockEntity> type, MachineTier tier) {
        int parallel = tier.getParallel();
        return BLOCKS.register(name, () -> new LiquidHeaterBlock(type, tier),
                    block -> machineItem(block))
              ;
    }

    private static BlockRegistryObject<BasaltGeneratorBlock, ItemBlockTooltip<BasaltGeneratorBlock>> basaltGenerator(
          String name, Machine<BasaltGeneratorBlockEntity> type, MachineTier tier) {
        int parallel = tier.getParallel();
        return BLOCKS.register(name, () -> new BasaltGeneratorBlock(type, tier),
                    block -> machineItem(block))
              ;
    }

    private static BlockRegistryObject<FluidInjectorBlock, ItemBlockTooltip<FluidInjectorBlock>> fluidInjector(
          String name, Machine<FluidInjectorBlockEntity> type, MachineTier tier) {
        int parallel = tier.getParallel();
        return BLOCKS.register(name, () -> new FluidInjectorBlock(type, tier),
                    block -> machineItem(block))
              ;
    }

    private static <BLOCK extends Block & IHasDescription> ItemBlockTooltip<BLOCK> machineItem(BLOCK block) {
        return new ItemBlockTooltip<>(block);
    }

    private ModBlocks() {
    }
}
