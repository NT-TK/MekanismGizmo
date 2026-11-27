package jp.example.mekanismgizmo;

/**
 * Placeholder for a temporary development helper. The machine resetter was verified with a self test that lived here
 * (dedicated server start, machine placed at y=100):
 * <pre>
 * accepts: ice maker=true, wind fusion=true, Mekanism enrichment chamber=true,
 *          Mekanism steel casing=false, dirt=false, pipette=false
 * in:      an ice maker item carrying an owner UUID and a security mode
 * out:     count=1, no owner, identical to a freshly crafted ice maker item (ItemStack.isSameItemSameTags)
 * cost:    200 FE per machine item (10 FE/t for 20 ticks), input slot emptied, nothing left in the machine
 * </pre>
 * The reset is simply the item's own default instance, so it removes everything Mekanism stores on a machine item:
 * owner, security, items, fluids, gas, energy, upgrades, side configuration and the redstone setting. The
 * implementation lives in {@link jp.example.mekanismgizmo.block.MachineResetterBlockEntity}. This class intentionally
 * does nothing and can be deleted at any time.
 */
public final class DebugResetterCheck {

    private DebugResetterCheck() {
    }
}
