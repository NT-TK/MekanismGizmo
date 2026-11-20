package jp.example.mekanismgizmo;

import jp.example.mekanismgizmo.registry.ModBlockEntities;
import jp.example.mekanismgizmo.registry.ModBlocks;
import jp.example.mekanismgizmo.registry.ModChemicals;
import jp.example.mekanismgizmo.registry.ModCreativeTabs;
import jp.example.mekanismgizmo.registry.ModItems;
import jp.example.mekanismgizmo.registry.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(MekanismGizmo.MOD_ID)
public final class MekanismGizmo {
    public static final String MOD_ID = "mekanismgizmo";

    public MekanismGizmo(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.TILE_ENTITIES.register(modBus);
        ModMenus.CONTAINER_TYPES.register(modBus);
        ModCreativeTabs.CREATIVE_TABS.register(modBus);
        ModChemicals.CHEMICALS.register(modBus);
    }
}
