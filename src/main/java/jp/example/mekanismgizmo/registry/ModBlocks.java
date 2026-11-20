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
import jp.example.mekanismgizmo.block.ItemCollectorBlock;
import jp.example.mekanismgizmo.block.ItemCollectorBlockEntity;
import jp.example.mekanismgizmo.block.PotionBrewerBlock;
import jp.example.mekanismgizmo.block.PotionBrewerBlockEntity;
import jp.example.mekanismgizmo.util.MachineTier;
import mekanism.common.attachments.component.AttachedEjector;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.fluid.FluidTanksBuilder;
import mekanism.common.attachments.containers.item.ItemSlotsBuilder;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.world.item.Item;
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

    // --- item collector (single tier) ---
    public static final BlockRegistryObject<ItemCollectorBlock, ItemBlockTooltip<ItemCollectorBlock>> ITEM_COLLECTOR = BLOCKS
          .register("item_collector", () -> new ItemCollectorBlock(ModBlockTypes.ITEM_COLLECTOR),
                (block, properties) -> machineItem(block, properties, ModSideConfigs.ITEM_COLLECTOR))
          .forItemHolder(item -> item
                // 3x9 output slots + energy slot
                .addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                      .addOutput(ItemCollectorBlockEntity.SLOT_COUNT)
                      .addEnergy()
                      .build()));

    private static BlockRegistryObject<PotionBrewerBlock, ItemBlockTooltip<PotionBrewerBlock>> potionBrewer(
          String name, Machine<PotionBrewerBlockEntity> type, MachineTier tier) {
        int bottles = PotionBrewerBlockEntity.bottleCount(tier);
        return BLOCKS.register(name, () -> new PotionBrewerBlock(type, tier),
                    (block, properties) -> machineItem(block, properties, ModSideConfigs.POTION_BREWER))
              .forItemHolder(item -> item
                    // Same slot layout as the block entity: [bottle, output] per brew, blaze powder, ingredient, energy
                    .addAttachmentOnlyContainers(ContainerType.ITEM, () -> {
                        ItemSlotsBuilder builder = ItemSlotsBuilder.builder();
                        for (int i = 0; i < bottles; i++) {
                            builder.addInput(1).addOutput();
                        }
                        return builder.addInput(1).addInput(1).addEnergy().build();
                    })
                    .addAttachmentOnlyContainers(ContainerType.CHEMICAL, () -> ChemicalTanksBuilder.builder()
                          .addBasic(PotionBrewerBlockEntity.chemicalCapacity(tier.getParallel()))
                          .build()));
    }

    private static BlockRegistryObject<IceMakerBlock, ItemBlockTooltip<IceMakerBlock>> iceMaker(
          String name, Machine<IceMakerBlockEntity> type, MachineTier tier) {
        int parallel = tier.getParallel();
        return BLOCKS.register(name, () -> new IceMakerBlock(type, tier),
                    (block, properties) -> machineItem(block, properties, ModSideConfigs.ICE_MAKER))
              .forItemHolder(item -> item
                    // Water bucket slot, one ice output per tier, energy slot
                    .addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                          .addFluidFillSlot(0)
                          .addOutput(parallel)
                          .addEnergy()
                          .build())
                    .addAttachmentOnlyContainers(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                          .addBasic(IceMakerBlockEntity.waterCapacity(parallel))
                          .build()));
    }

    private static BlockRegistryObject<CobblestoneGeneratorBlock, ItemBlockTooltip<CobblestoneGeneratorBlock>> cobblestoneGenerator(
          String name, Machine<CobblestoneGeneratorBlockEntity> type, MachineTier tier) {
        int parallel = tier.getParallel();
        return BLOCKS.register(name, () -> new CobblestoneGeneratorBlock(type, tier),
                    (block, properties) -> machineItem(block, properties, ModSideConfigs.COBBLESTONE_GENERATOR))
              .forItemHolder(item -> item
                    // One cobblestone output per tier + energy slot
                    .addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                          .addOutput(parallel)
                          .addEnergy()
                          .build()));
    }

    private static BlockRegistryObject<BasaltGeneratorBlock, ItemBlockTooltip<BasaltGeneratorBlock>> basaltGenerator(
          String name, Machine<BasaltGeneratorBlockEntity> type, MachineTier tier) {
        int parallel = tier.getParallel();
        return BLOCKS.register(name, () -> new BasaltGeneratorBlock(type, tier),
                    (block, properties) -> machineItem(block, properties, ModSideConfigs.BASALT_GENERATOR))
              .forItemHolder(item -> item
                    // One basalt output per tier + energy slot
                    .addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder()
                          .addOutput(parallel)
                          .addEnergy()
                          .build()));
    }

    private static BlockRegistryObject<FluidInjectorBlock, ItemBlockTooltip<FluidInjectorBlock>> fluidInjector(
          String name, Machine<FluidInjectorBlockEntity> type, MachineTier tier) {
        int parallel = tier.getParallel();
        return BLOCKS.register(name, () -> new FluidInjectorBlock(type, tier),
                    (block, properties) -> machineItem(block, properties, ModSideConfigs.FLUID_INJECTOR))
              .forItemHolder(item -> item
                    // One [container input, converted container] pair per tier + energy slot
                    .addAttachmentOnlyContainers(ContainerType.ITEM, () -> {
                        ItemSlotsBuilder builder = ItemSlotsBuilder.builder();
                        for (int i = 0; i < parallel; i++) {
                            builder.addInput(1).addOutput();
                        }
                        return builder.addEnergy().build();
                    })
                    .addAttachmentOnlyContainers(ContainerType.FLUID, () -> FluidTanksBuilder.builder()
                          .addBasic(FluidInjectorBlockEntity.tankCapacity(parallel))
                          .build()));
    }

    /**
     * Mekanism machines carry their default side configuration, ejector state and container attachments on the block
     * item; placing the item applies them to the newly created tile. Without these components a freshly placed machine
     * has every side set to "None" (so cables and pipes cannot insert anything, not even energy), and without the
     * attachment creators Mekanism cannot read the contents stored on a dropped machine ("No known containers").
     */
    private static <BLOCK extends Block & IHasDescription> ItemBlockTooltip<BLOCK> machineItem(BLOCK block, Item.Properties properties,
          AttachedSideConfig sideConfig) {
        return new ItemBlockTooltip<>(block, true, properties
              .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
              .component(MekanismDataComponents.SIDE_CONFIG, sideConfig));
    }

    private ModBlocks() {
    }
}
