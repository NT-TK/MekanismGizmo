package jp.example.mekanismgizmo.client;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.registry.ModBlockEntities;
import jp.example.mekanismgizmo.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ITEM_COLLECTOR.get(), context -> new ItemCollectorRenderer());
    }
}
