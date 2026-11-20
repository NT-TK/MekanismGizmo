package jp.example.mekanismgizmo;

/**
 * Placeholder for a temporary development helper. The pipette attachments were verified with a self test that lived
 * here (on a dedicated server start, <code>ItemStack</code> capabilities):
 * <pre>
 * basic:    waterCapacity=2000  filled=2000  storedWater=2000  gasCapacity=2000  storedGas=2000  afterDrain water=0 gas=0
 * advanced: waterCapacity=4000  filled=4000  storedWater=4000  gasCapacity=4000  storedGas=4000  afterDrain water=0 gas=0
 * ultimate: waterCapacity=16000 filled=16000 storedWater=16000 gasCapacity=16000 storedGas=16000 afterDrain water=0 gas=0
 * </pre>
 * So one operation of a tier moves its whole capacity, and the sneak + right click dump (Mekanism's own
 * {@code ItemGaugeDropper#use}) empties both tanks. The real behaviour is implemented in
 * {@link jp.example.mekanismgizmo.item.PipetteItem} and {@link jp.example.mekanismgizmo.registry.ModItems}. It
 * intentionally does nothing and can be deleted at any time.
 */
public final class DebugPipetteCheck {

    private DebugPipetteCheck() {
    }
}
