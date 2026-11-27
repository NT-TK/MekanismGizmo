package jp.example.mekanismgizmo.client;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.registry.ModBlockEntities;
import jp.example.mekanismgizmo.registry.ModItems;
import jp.example.mekanismgizmo.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = MekanismGizmo.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.POTION_BREWER.get(), PotionBrewerScreen::new);
        event.register(ModMenus.ICE_MAKER.get(), IceMakerScreen::new);
        event.register(ModMenus.COBBLESTONE_GENERATOR.get(), CobblestoneGeneratorScreen::new);
        event.register(ModMenus.BASALT_GENERATOR.get(), BasaltGeneratorScreen::new);
        event.register(ModMenus.FLUID_INJECTOR.get(), FluidInjectorScreen::new);
        event.register(ModMenus.ITEM_COLLECTOR.get(), ItemCollectorScreen::new);
        event.register(ModMenus.LIQUID_HEATER.get(), LiquidHeaterScreen::new);
        event.register(ModMenus.HEAT_FUSION.get(), HeatFusionScreen::new);
        event.register(ModMenus.IMPROVED_GAS_GENERATOR.get(), ImprovedGasGeneratorScreen::new);
        event.register(ModMenus.WIND_FUSION.get(), WindFusionScreen::new);
        event.register(ModMenus.MACHINE_RESETTER.get(), MachineResetterScreen::new);
    }

    /**
     * Swaps the configuration card to its lit sprite once it holds a recorded configuration, the same way Mekanism's
     * own card switches to {@code configuration_card_encoded}.
     */
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ModItems.IMPROVED_CONFIGURATOR.get(),
              net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MekanismGizmo.MOD_ID, "encoded"), (stack, level, entity, seed) ->
                    stack.has(MekanismDataComponents.CONFIGURATION_DATA.get()) ? 1.0F : 0.0F));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ITEM_COLLECTOR.get(), context -> new ItemCollectorRenderer());
    }
}
