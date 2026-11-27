package jp.example.mekanismgizmo.block;

import java.util.function.Supplier;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;

/**
 * A {@link Machine} with this addon's own description. 1.20.1's {@link Machine} constructor insists on a
 * {@link MekanismLang} constant, but the block type's description is only used for the tooltip, so we override it
 * with the addon's own {@link ILangEntry}.
 */
public class GizmoMachine<TILE extends TileEntityMekanism> extends Machine<TILE> {

    private final ILangEntry description;

    public GizmoMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, ILangEntry description) {
        super(tileEntityRegistrar, MekanismLang.MEKANISM);
        this.description = description;
    }

    @Override
    public ILangEntry getDescription() {
        return description;
    }

    public static class Builder<TILE extends TileEntityMekanism> extends Machine.MachineBuilder<GizmoMachine<TILE>, TILE, Builder<TILE>> {

        protected Builder(GizmoMachine<TILE> holder) {
            super(holder);
        }

        public static <TILE extends TileEntityMekanism> Builder<TILE> createMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar,
              ILangEntry description) {
            return new Builder<>(new GizmoMachine<>(tileEntityRegistrar, description));
        }
    }
}