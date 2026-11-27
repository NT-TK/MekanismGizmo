package jp.example.mekanismgizmo.registry;

import jp.example.mekanismgizmo.MekanismGizmo;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.common.registration.impl.GasDeferredRegister;
import mekanism.common.registration.impl.GasRegistryObject;

public final class ModChemicals {
    public static final int BLAZE_POWDER_COLOR = 0xE87524;
    public static final GasDeferredRegister CHEMICALS = new GasDeferredRegister(MekanismGizmo.MOD_ID);
    public static final GasRegistryObject<Gas> BLAZE_POWDER = CHEMICALS.register("blaze_powder",
          () -> new Gas(GasBuilder.builder().tint(BLAZE_POWDER_COLOR)));

    private ModChemicals() {
    }
}