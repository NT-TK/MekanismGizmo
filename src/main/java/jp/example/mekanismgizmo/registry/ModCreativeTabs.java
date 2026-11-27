package jp.example.mekanismgizmo.registry;

import jp.example.mekanismgizmo.MekanismGizmo;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MekanismGizmo.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
          () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.mekanismgizmo"))
                .icon(() -> ModBlocks.POTION_BREWER.asItem().getDefaultInstance())
                .displayItems((parameters, output) -> {
                    output.accept(ModBlocks.POTION_BREWER.asItem());
                    output.accept(ModBlocks.POTION_BREWER_BASIC.asItem());
                    output.accept(ModBlocks.POTION_BREWER_ADVANCED.asItem());
                    output.accept(ModBlocks.POTION_BREWER_ELITE.asItem());
                    output.accept(ModBlocks.POTION_BREWER_ULTIMATE.asItem());
                    output.accept(ModBlocks.ICE_MAKER.asItem());
                    output.accept(ModBlocks.ICE_MAKER_BASIC.asItem());
                    output.accept(ModBlocks.ICE_MAKER_ADVANCED.asItem());
                    output.accept(ModBlocks.ICE_MAKER_ELITE.asItem());
                    output.accept(ModBlocks.ICE_MAKER_ULTIMATE.asItem());
                    output.accept(ModBlocks.COBBLESTONE_GENERATOR.asItem());
                    output.accept(ModBlocks.COBBLESTONE_GENERATOR_BASIC.asItem());
                    output.accept(ModBlocks.COBBLESTONE_GENERATOR_ADVANCED.asItem());
                    output.accept(ModBlocks.COBBLESTONE_GENERATOR_ELITE.asItem());
                    output.accept(ModBlocks.COBBLESTONE_GENERATOR_ULTIMATE.asItem());
                    output.accept(ModBlocks.BASALT_GENERATOR.asItem());
                    output.accept(ModBlocks.BASALT_GENERATOR_BASIC.asItem());
                    output.accept(ModBlocks.BASALT_GENERATOR_ADVANCED.asItem());
                    output.accept(ModBlocks.BASALT_GENERATOR_ELITE.asItem());
                    output.accept(ModBlocks.BASALT_GENERATOR_ULTIMATE.asItem());
                    output.accept(ModBlocks.FLUID_INJECTOR.asItem());
                    output.accept(ModBlocks.FLUID_INJECTOR_BASIC.asItem());
                    output.accept(ModBlocks.FLUID_INJECTOR_ADVANCED.asItem());
                    output.accept(ModBlocks.FLUID_INJECTOR_ELITE.asItem());
                    output.accept(ModBlocks.FLUID_INJECTOR_ULTIMATE.asItem());
                    output.accept(ModBlocks.LIQUID_HEATER.asItem());
                    output.accept(ModBlocks.LIQUID_HEATER_BASIC.asItem());
                    output.accept(ModBlocks.LIQUID_HEATER_ADVANCED.asItem());
                    output.accept(ModBlocks.LIQUID_HEATER_ELITE.asItem());
                    output.accept(ModBlocks.LIQUID_HEATER_ULTIMATE.asItem());
                    output.accept(ModBlocks.IMPROVED_GAS_GENERATOR.asItem());
                    output.accept(ModBlocks.HEAT_FUSION.asItem());
                    output.accept(ModBlocks.ITEM_COLLECTOR.asItem());
                    output.accept(ModBlocks.WIND_FUSION.asItem());
                    output.accept(ModBlocks.MACHINE_RESETTER.asItem());
                    output.accept(ModItems.BASIC_PIPETTE.asItem());
                    output.accept(ModItems.ADVANCED_PIPETTE.asItem());
                    output.accept(ModItems.ELITE_PIPETTE.asItem());
                    output.accept(ModItems.ULTIMATE_PIPETTE.asItem());
                    output.accept(ModItems.IMPROVED_PORTABLE_TELEPORTER.asItem());
                    output.accept(ModItems.IMPROVED_CONFIGURATOR.asItem());
                })
                .build());

    private ModCreativeTabs() {
    }
}
