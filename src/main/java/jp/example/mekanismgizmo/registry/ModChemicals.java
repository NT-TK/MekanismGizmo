package jp.example.mekanismgizmo.registry;

import jp.example.mekanismgizmo.MekanismGizmo;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.registration.impl.ChemicalDeferredRegister;
import mekanism.common.registration.impl.DeferredChemical;

public final class ModChemicals {
    public static final int BLAZE_POWDER_COLOR = 0xE87524;
    public static final ChemicalDeferredRegister CHEMICALS = new ChemicalDeferredRegister(MekanismGizmo.MOD_ID);
    public static final DeferredChemical<Chemical> BLAZE_POWDER = CHEMICALS.register("blaze_powder",
          () -> new Chemical(ChemicalBuilder.builder().gaseous().tint(BLAZE_POWDER_COLOR)));

    private ModChemicals() {
    }
}
