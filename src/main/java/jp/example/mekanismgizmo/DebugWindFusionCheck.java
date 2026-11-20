package jp.example.mekanismgizmo;

/**
 * Placeholder for a temporary development helper. The wind generator fusion machine was verified with two self tests
 * that lived here (dedicated server start; the machine was placed at y=200 with open sky):
 * <pre>
 * slots:          accept wind generators only, one per slot (inserting 2 keeps 1)
 * 0 generators:   count=0  active=false  energy=0
 * 3 generators:   count=3  active=true   316 FE/t each -&gt; 948 FE/t, and 948 FE per tick exactly
 * 27 generators:  count=27 active=true   27 * 316 = 8532 FE/t (the single wind generator value times 27)
 * 30 ticks:       255960 FE in the buffer (30 * 8532)
 * blocked (water four blocks above): count=1 active=false 0 FE/t, no energy
 * energy export:  every side is "Energy: Output", exposes the FE capability and hands out 1000 FE per simulate
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
