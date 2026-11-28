package jp.example.mekanismgizmo.client;

import jp.example.mekanismgizmo.MekanismGizmo;
import jp.example.mekanismgizmo.registry.ModBlockEntities;
import jp.example.mekanismgizmo.registry.ModItems;
import jp.example.mekanismgizmo.registry.ModMenus;
import mekanism.api.NBTConstants;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MekanismGizmo.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.POTION_BREWER.get(), PotionBrewerScreen::new);
            MenuScreens.register(ModMenus.ICE_MAKER.get(), IceMakerScreen::new);
            MenuScreens.register(ModMenus.COBBLESTONE_GENERATOR.get(), CobblestoneGeneratorScreen::new);
            MenuScreens.register(ModMenus.BASALT_GENERATOR.get(), BasaltGeneratorScreen::new);
            MenuScreens.register(ModMenus.FLUID_INJECTOR.get(), FluidInjectorScreen::new);
            MenuScreens.register(ModMenus.ITEM_COLLECTOR.get(), ItemCollectorScreen::new);
            MenuScreens.register(ModMenus.LIQUID_HEATER.get(), LiquidHeaterScreen::new);
            MenuScreens.register(ModMenus.HEAT_FUSION.get(), HeatFusionScreen::new);
            MenuScreens.register(ModMenus.IMPROVED_GAS_GENERATOR.get(), ImprovedGasGeneratorScreen::new);
            MenuScreens.register(ModMenus.WIND_FUSION.get(), WindFusionScreen::new);
            MenuScreens.register(ModMenus.MACHINE_RESETTER.get(), MachineResetterScreen::new);

            // Swaps the configuration card to its lit sprite once it holds a recorded configuration.
            ItemProperties.register(ModItems.IMPROVED_CONFIGURATOR.get(),
                  ResourceLocation.tryBuild(MekanismGizmo.MOD_ID, "encoded"), (stack, level, entity, seed) ->
                        ItemDataUtils.getCompound(stack, NBTConstants.DATA).isEmpty() ? 0.0F : 1.0F);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ITEM_COLLECTOR.get(), context -> new ItemCollectorRenderer());
    }
}
