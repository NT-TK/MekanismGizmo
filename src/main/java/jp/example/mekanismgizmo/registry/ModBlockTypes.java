package jp.example.mekanismgizmo.registry;

import java.util.function.Supplier;
import jp.example.mekanismgizmo.block.BasaltGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.CobblestoneGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.FluidInjectorBlockEntity;
import jp.example.mekanismgizmo.block.IceMakerBlockEntity;
import jp.example.mekanismgizmo.block.ImprovedGasGeneratorBlockEntity;
import jp.example.mekanismgizmo.block.ItemCollectorBlockEntity;
import jp.example.mekanismgizmo.block.LiquidHeaterBlockEntity;
import jp.example.mekanismgizmo.block.MachineResetterBlockEntity;
import jp.example.mekanismgizmo.block.PotionBrewerBlockEntity;
import jp.example.mekanismgizmo.block.WindFusionBlockEntity;
import jp.example.mekanismgizmo.util.MachineTier;
import mekanism.api.Upgrade;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import org.jetbrains.annotations.Nullable;

/**
 * Machine descriptions used by Mekanism's block/attribute system. Going through {@link Machine} gives us
 * Mekanism's own facing/redstone/upgrade/comparator handling, the side configuration window and the
 * upgrade window without re-implementing any of it.
 * <p>
 * Every machine except the item collector exists in five steps: the untiered machine, then Mekanism's four tiers.
 * The tier is published as an {@link AttributeTier} (so Mekanism's tier installer knows what it is looking at) and
 * every step except ultimate carries an {@link AttributeUpgradeable} pointing at the next block, exactly like
 * Mekanism's factories. The untiered machine deliberately carries no tier attribute, which is what makes Mekanism's
 * Basic Tier Installer (whose "from" tier is {@code null}) upgrade it to the basic tier.
 */
public final class ModBlockTypes {

    // --- potion brewer ---
    public static final Machine<PotionBrewerBlockEntity> POTION_BREWER = potionBrewer(MachineTier.UNTIERED,
          () -> ModBlockEntities.POTION_BREWER, () -> ModBlocks.POTION_BREWER_BASIC);
    public static final Machine<PotionBrewerBlockEntity> POTION_BREWER_BASIC = potionBrewer(MachineTier.BASIC,
          () -> ModBlockEntities.POTION_BREWER_BASIC, () -> ModBlocks.POTION_BREWER_ADVANCED);
    public static final Machine<PotionBrewerBlockEntity> POTION_BREWER_ADVANCED = potionBrewer(MachineTier.ADVANCED,
          () -> ModBlockEntities.POTION_BREWER_ADVANCED, () -> ModBlocks.POTION_BREWER_ELITE);
    public static final Machine<PotionBrewerBlockEntity> POTION_BREWER_ELITE = potionBrewer(MachineTier.ELITE,
          () -> ModBlockEntities.POTION_BREWER_ELITE, () -> ModBlocks.POTION_BREWER_ULTIMATE);
    public static final Machine<PotionBrewerBlockEntity> POTION_BREWER_ULTIMATE = potionBrewer(MachineTier.ULTIMATE,
          () -> ModBlockEntities.POTION_BREWER_ULTIMATE, null);

    // --- ice maker ---
    public static final Machine<IceMakerBlockEntity> ICE_MAKER = iceMaker(MachineTier.UNTIERED,
          () -> ModBlockEntities.ICE_MAKER, () -> ModBlocks.ICE_MAKER_BASIC);
    public static final Machine<IceMakerBlockEntity> ICE_MAKER_BASIC = iceMaker(MachineTier.BASIC,
          () -> ModBlockEntities.ICE_MAKER_BASIC, () -> ModBlocks.ICE_MAKER_ADVANCED);
    public static final Machine<IceMakerBlockEntity> ICE_MAKER_ADVANCED = iceMaker(MachineTier.ADVANCED,
          () -> ModBlockEntities.ICE_MAKER_ADVANCED, () -> ModBlocks.ICE_MAKER_ELITE);
    public static final Machine<IceMakerBlockEntity> ICE_MAKER_ELITE = iceMaker(MachineTier.ELITE,
          () -> ModBlockEntities.ICE_MAKER_ELITE, () -> ModBlocks.ICE_MAKER_ULTIMATE);
    public static final Machine<IceMakerBlockEntity> ICE_MAKER_ULTIMATE = iceMaker(MachineTier.ULTIMATE,
          () -> ModBlockEntities.ICE_MAKER_ULTIMATE, null);

    // --- cobblestone generator ---
    public static final Machine<CobblestoneGeneratorBlockEntity> COBBLESTONE_GENERATOR = cobblestoneGenerator(MachineTier.UNTIERED,
          () -> ModBlockEntities.COBBLESTONE_GENERATOR, () -> ModBlocks.COBBLESTONE_GENERATOR_BASIC);
    public static final Machine<CobblestoneGeneratorBlockEntity> COBBLESTONE_GENERATOR_BASIC = cobblestoneGenerator(MachineTier.BASIC,
          () -> ModBlockEntities.COBBLESTONE_GENERATOR_BASIC, () -> ModBlocks.COBBLESTONE_GENERATOR_ADVANCED);
    public static final Machine<CobblestoneGeneratorBlockEntity> COBBLESTONE_GENERATOR_ADVANCED = cobblestoneGenerator(MachineTier.ADVANCED,
          () -> ModBlockEntities.COBBLESTONE_GENERATOR_ADVANCED, () -> ModBlocks.COBBLESTONE_GENERATOR_ELITE);
    public static final Machine<CobblestoneGeneratorBlockEntity> COBBLESTONE_GENERATOR_ELITE = cobblestoneGenerator(MachineTier.ELITE,
          () -> ModBlockEntities.COBBLESTONE_GENERATOR_ELITE, () -> ModBlocks.COBBLESTONE_GENERATOR_ULTIMATE);
    public static final Machine<CobblestoneGeneratorBlockEntity> COBBLESTONE_GENERATOR_ULTIMATE = cobblestoneGenerator(MachineTier.ULTIMATE,
          () -> ModBlockEntities.COBBLESTONE_GENERATOR_ULTIMATE, null);

    // --- basalt generator ---
    public static final Machine<BasaltGeneratorBlockEntity> BASALT_GENERATOR = basaltGenerator(MachineTier.UNTIERED,
          () -> ModBlockEntities.BASALT_GENERATOR, () -> ModBlocks.BASALT_GENERATOR_BASIC);
    public static final Machine<BasaltGeneratorBlockEntity> BASALT_GENERATOR_BASIC = basaltGenerator(MachineTier.BASIC,
          () -> ModBlockEntities.BASALT_GENERATOR_BASIC, () -> ModBlocks.BASALT_GENERATOR_ADVANCED);
    public static final Machine<BasaltGeneratorBlockEntity> BASALT_GENERATOR_ADVANCED = basaltGenerator(MachineTier.ADVANCED,
          () -> ModBlockEntities.BASALT_GENERATOR_ADVANCED, () -> ModBlocks.BASALT_GENERATOR_ELITE);
    public static final Machine<BasaltGeneratorBlockEntity> BASALT_GENERATOR_ELITE = basaltGenerator(MachineTier.ELITE,
          () -> ModBlockEntities.BASALT_GENERATOR_ELITE, () -> ModBlocks.BASALT_GENERATOR_ULTIMATE);
    public static final Machine<BasaltGeneratorBlockEntity> BASALT_GENERATOR_ULTIMATE = basaltGenerator(MachineTier.ULTIMATE,
          () -> ModBlockEntities.BASALT_GENERATOR_ULTIMATE, null);

    // --- fluid injector ---
    public static final Machine<FluidInjectorBlockEntity> FLUID_INJECTOR = fluidInjector(MachineTier.UNTIERED,
          () -> ModBlockEntities.FLUID_INJECTOR, () -> ModBlocks.FLUID_INJECTOR_BASIC);
    public static final Machine<FluidInjectorBlockEntity> FLUID_INJECTOR_BASIC = fluidInjector(MachineTier.BASIC,
          () -> ModBlockEntities.FLUID_INJECTOR_BASIC, () -> ModBlocks.FLUID_INJECTOR_ADVANCED);
    public static final Machine<FluidInjectorBlockEntity> FLUID_INJECTOR_ADVANCED = fluidInjector(MachineTier.ADVANCED,
          () -> ModBlockEntities.FLUID_INJECTOR_ADVANCED, () -> ModBlocks.FLUID_INJECTOR_ELITE);
    public static final Machine<FluidInjectorBlockEntity> FLUID_INJECTOR_ELITE = fluidInjector(MachineTier.ELITE,
          () -> ModBlockEntities.FLUID_INJECTOR_ELITE, () -> ModBlocks.FLUID_INJECTOR_ULTIMATE);
    public static final Machine<FluidInjectorBlockEntity> FLUID_INJECTOR_ULTIMATE = fluidInjector(MachineTier.ULTIMATE,
          () -> ModBlockEntities.FLUID_INJECTOR_ULTIMATE, null);

    // --- liquid heater ---
    public static final Machine<LiquidHeaterBlockEntity> LIQUID_HEATER = liquidHeater(MachineTier.UNTIERED,
          () -> ModBlockEntities.LIQUID_HEATER, () -> ModBlocks.LIQUID_HEATER_BASIC);
    public static final Machine<LiquidHeaterBlockEntity> LIQUID_HEATER_BASIC = liquidHeater(MachineTier.BASIC,
          () -> ModBlockEntities.LIQUID_HEATER_BASIC, () -> ModBlocks.LIQUID_HEATER_ADVANCED);
    public static final Machine<LiquidHeaterBlockEntity> LIQUID_HEATER_ADVANCED = liquidHeater(MachineTier.ADVANCED,
          () -> ModBlockEntities.LIQUID_HEATER_ADVANCED, () -> ModBlocks.LIQUID_HEATER_ELITE);
    public static final Machine<LiquidHeaterBlockEntity> LIQUID_HEATER_ELITE = liquidHeater(MachineTier.ELITE,
          () -> ModBlockEntities.LIQUID_HEATER_ELITE, () -> ModBlocks.LIQUID_HEATER_ULTIMATE);
    public static final Machine<LiquidHeaterBlockEntity> LIQUID_HEATER_ULTIMATE = liquidHeater(MachineTier.ULTIMATE,
          () -> ModBlockEntities.LIQUID_HEATER_ULTIMATE, null);

    // --- improved gas generator (single tier) ---
    public static final Machine<ImprovedGasGeneratorBlockEntity> IMPROVED_GAS_GENERATOR = Machine.MachineBuilder
          .createMachine(() -> ModBlockEntities.IMPROVED_GAS_GENERATOR, ModLang.DESCRIPTION_IMPROVED_GAS_GENERATOR)
          .withGui(() -> ModMenus.IMPROVED_GAS_GENERATOR)
          // Storage only: the machine produces energy instead of using it
          .withEnergyConfig(() -> ImprovedGasGeneratorBlockEntity.ENERGY_CAPACITY)
          .withSupportedUpgrades(Upgrade.MUFFLING)
          .with(AttributeSideConfig.create(TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.CHEMICAL))
          .replace(Attributes.ACTIVE)
          .build();

    // --- item collector (single tier) ---
    public static final Machine<ItemCollectorBlockEntity> ITEM_COLLECTOR = Machine.MachineBuilder
          .createMachine(() -> ModBlockEntities.ITEM_COLLECTOR, ModLang.DESCRIPTION_ITEM_COLLECTOR)
          .withGui(() -> ModMenus.ITEM_COLLECTOR)
          .withEnergyConfig(() -> 10L, () -> 12_000L)
          .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY)
          .with(AttributeSideConfig.create(TransmissionType.ITEM, TransmissionType.ENERGY))
          .replace(Attributes.ACTIVE)
          .build();

    // --- wind generator fusion (single tier) ---
    public static final Machine<WindFusionBlockEntity> WIND_FUSION = Machine.MachineBuilder
          .createMachine(() -> ModBlockEntities.WIND_FUSION, ModLang.DESCRIPTION_WIND_FUSION)
          .withGui(() -> ModMenus.WIND_FUSION)
          // Storage only: the machine produces energy instead of using it
          .withEnergyConfig(() -> WindFusionBlockEntity.ENERGY_CAPACITY)
          .withSupportedUpgrades(Upgrade.MUFFLING)
          .with(AttributeSideConfig.create(TransmissionType.ITEM, TransmissionType.ENERGY))
          .replace(Attributes.ACTIVE)
          .build();

    // --- machine resetter (single tier) ---
    public static final Machine<MachineResetterBlockEntity> MACHINE_RESETTER = Machine.MachineBuilder
          .createMachine(() -> ModBlockEntities.MACHINE_RESETTER, ModLang.DESCRIPTION_MACHINE_RESETTER)
          .withGui(() -> ModMenus.MACHINE_RESETTER)
          .withEnergyConfig(() -> 10L, () -> 12_000L)
          .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY)
          .with(AttributeSideConfig.create(TransmissionType.ITEM, TransmissionType.ENERGY))
          .replace(Attributes.ACTIVE)
          .build();

    private static Machine<PotionBrewerBlockEntity> potionBrewer(MachineTier tier,
          Supplier<TileEntityTypeRegistryObject<PotionBrewerBlockEntity>> be, @Nullable Supplier<BlockRegistryObject<?, ?>> next) {
        Machine.MachineBuilder<Machine<PotionBrewerBlockEntity>, PotionBrewerBlockEntity, ?> builder = Machine.MachineBuilder
              .createMachine(be, ModLang.DESCRIPTION_POTION_BREWER)
              .withGui(() -> ModMenus.POTION_BREWER)
              .withEnergyConfig(() -> 3L * tier.getParallel(), () -> 12_000L * tier.getParallel())
              .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY)
              .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
              .replace(Attributes.ACTIVE);
        addTier(builder, tier, next);
        return builder.build();
    }

    private static Machine<IceMakerBlockEntity> iceMaker(MachineTier tier,
          Supplier<TileEntityTypeRegistryObject<IceMakerBlockEntity>> be, @Nullable Supplier<BlockRegistryObject<?, ?>> next) {
        Machine.MachineBuilder<Machine<IceMakerBlockEntity>, IceMakerBlockEntity, ?> builder = Machine.MachineBuilder
              .createMachine(be, ModLang.DESCRIPTION_ICE_MAKER)
              .withGui(() -> ModMenus.ICE_MAKER)
              .withEnergyConfig(() -> 20L * tier.getParallel(), () -> 12_000L * tier.getParallel())
              .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY)
              .with(AttributeSideConfig.create(TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.FLUID))
              .replace(Attributes.ACTIVE);
        addTier(builder, tier, next);
        return builder.build();
    }

    private static Machine<CobblestoneGeneratorBlockEntity> cobblestoneGenerator(MachineTier tier,
          Supplier<TileEntityTypeRegistryObject<CobblestoneGeneratorBlockEntity>> be, @Nullable Supplier<BlockRegistryObject<?, ?>> next) {
        Machine.MachineBuilder<Machine<CobblestoneGeneratorBlockEntity>, CobblestoneGeneratorBlockEntity, ?> builder = Machine.MachineBuilder
              .createMachine(be, ModLang.DESCRIPTION_COBBLESTONE_GENERATOR)
              .withGui(() -> ModMenus.COBBLESTONE_GENERATOR)
              .withEnergyConfig(() -> 10L * tier.getParallel(), () -> 12_000L * tier.getParallel())
              .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY)
              .with(AttributeSideConfig.create(TransmissionType.ITEM, TransmissionType.ENERGY))
              .replace(Attributes.ACTIVE);
        addTier(builder, tier, next);
        return builder.build();
    }

    private static Machine<BasaltGeneratorBlockEntity> basaltGenerator(MachineTier tier,
          Supplier<TileEntityTypeRegistryObject<BasaltGeneratorBlockEntity>> be, @Nullable Supplier<BlockRegistryObject<?, ?>> next) {
        Machine.MachineBuilder<Machine<BasaltGeneratorBlockEntity>, BasaltGeneratorBlockEntity, ?> builder = Machine.MachineBuilder
              .createMachine(be, ModLang.DESCRIPTION_BASALT_GENERATOR)
              .withGui(() -> ModMenus.BASALT_GENERATOR)
              .withEnergyConfig(() -> 10L * tier.getParallel(), () -> 12_000L * tier.getParallel())
              .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY)
              .with(AttributeSideConfig.create(TransmissionType.ITEM, TransmissionType.ENERGY))
              .replace(Attributes.ACTIVE);
        addTier(builder, tier, next);
        return builder.build();
    }

    private static Machine<LiquidHeaterBlockEntity> liquidHeater(MachineTier tier,
          Supplier<TileEntityTypeRegistryObject<LiquidHeaterBlockEntity>> be, @Nullable Supplier<BlockRegistryObject<?, ?>> next) {
        Machine.MachineBuilder<Machine<LiquidHeaterBlockEntity>, LiquidHeaterBlockEntity, ?> builder = Machine.MachineBuilder
              .createMachine(be, ModLang.DESCRIPTION_LIQUID_HEATER)
              .withGui(() -> ModMenus.LIQUID_HEATER)
              // 25 FE per mB of sodium: 500 mB per operation and 20 ticks per operation at tier one
              .withEnergyConfig(() -> 625L * tier.getParallel(), () -> 12_000L * tier.getParallel())
              .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY)
              .with(AttributeSideConfig.create(TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.CHEMICAL))
              .replace(Attributes.ACTIVE);
        addTier(builder, tier, next);
        return builder.build();
    }

    private static Machine<FluidInjectorBlockEntity> fluidInjector(MachineTier tier,
          Supplier<TileEntityTypeRegistryObject<FluidInjectorBlockEntity>> be, @Nullable Supplier<BlockRegistryObject<?, ?>> next) {
        Machine.MachineBuilder<Machine<FluidInjectorBlockEntity>, FluidInjectorBlockEntity, ?> builder = Machine.MachineBuilder
              .createMachine(be, ModLang.DESCRIPTION_FLUID_INJECTOR)
              .withGui(() -> ModMenus.FLUID_INJECTOR)
              .withEnergyConfig(() -> FluidInjectorBlockEntity.ENERGY_PER_OPERATION * tier.getParallel(),
                    () -> 12_000L * tier.getParallel())
              .with(AttributeSideConfig.create(TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.ENERGY))
              .replace(Attributes.ACTIVE);
        addTier(builder, tier, next);
        return builder.build();
    }

    private static void addTier(Machine.MachineBuilder<?, ?, ?> builder, MachineTier tier,
          @Nullable Supplier<BlockRegistryObject<?, ?>> next) {
        // The untiered machine must not carry a tier attribute: Mekanism's basic tier installer upgrades blocks whose
        // tier is null, which is how a plain Energized Smelter becomes a Basic Smelting Factory.
        if (tier.getBaseTier() != null) {
            builder.with(new AttributeTier<>(tier));
        }
        if (next != null) {
            builder.with(new AttributeUpgradeable(next));
        }
    }

    private ModBlockTypes() {
    }
}
