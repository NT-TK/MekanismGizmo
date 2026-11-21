package jp.example.mekanismgizmo;

/**
 * Placeholder for a temporary development helper. The wind generator fusion machine was verified with self tests that
 * lived here (dedicated server start, machine at y=200 with open sky):
 * <pre>
 * slab:    a slot holds a stack of wind generators: limit 64, inserting 65 keeps 64 and returns 1
 * access:  the slots are internal storage - a pipe insert is refused, a pipe extract returns nothing, and only the
 *          back face exposes an item capability (the energy slot)
 * full:    27 slots x 64 = 1728 generators, 316 FE/t each -&gt; 546048 FE/t, 25 ticks give exactly 13651200 FE
 * buffer:  345600000 FE (Mekanism's wind generator storage of 200000 FE per fusable generator)
 * earlier: 0/3/27 generators gave 0/948/8532 FE/t, blocked wind gave 0, every side exported energy
 * </pre>
 * The multiplier is Mekanism's own ({@code windGenerationMin}/{@code Max} between {@code windGenerationMinY} and
 * {@code windGenerationMaxY}, requiring sky four blocks above), so the fusion machine produces exactly what the same
 * wind generators would produce at that position. The implementation lives in
 * {@link jp.example.mekanismgizmo.block.WindFusionBlockEntity}. This class intentionally does nothing and can be
 * deleted at any time.
 */
public final class DebugWindFusionCheck {

    private DebugWindFusionCheck() {
    }
}
