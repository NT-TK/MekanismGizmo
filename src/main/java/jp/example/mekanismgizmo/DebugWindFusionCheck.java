package jp.example.mekanismgizmo;

/**
 * Placeholder for a temporary development helper. The wind generator fusion machine was verified with self tests that
 * lived here (dedicated server start, machine at y=200 with open sky):
 * <pre>
 * units:   mekanismgenerators:wind_generator -&gt; 1; every Compact Mekanism Machines
 *          compressed_wind_generator_xN -&gt; N (x2, x8, x32, x128, x512, x2048, x8192, x32768, x131072, x532480),
 *          the older wind_generator_xN names give the same numbers, an item without the xN suffix, another mod's
 *          namespace and a machine of that mod -&gt; 0, and a huge x999999999 is clamped to 1000000
 * slots:   a slot holds a stack of 64: limit 64, inserting 65 keeps 64, pipes can neither insert nor extract, only
 *          the back face exposes an item capability (the energy slot)
 * full:    27 slots x 64 = 1728 basic generators, 316 FE/t each -&gt; 546048 FE/t, 25 ticks give exactly 13651200 FE
 * limits:  MAX_UNITS = 920125440 (27 stacks of an x532480), buffer = 200000 FE each -&gt; 184025088000000 FE
 * numbers: 1728 -&gt; "1,728", 546048 -&gt; "546,048", 920125440 -&gt; "920.1M", 184025088000000 -&gt; "184.0T"
 * </pre>
 * A compressed wind generator counts as the generators it replaces because that is exactly what it produces on its
 * own: Compact Mekanism Machines multiplies Mekanism's height dependent wind output by its compression factor. The
 * implementation lives in {@link jp.example.mekanismgizmo.block.WindFusionBlockEntity} and
 * {@link jp.example.mekanismgizmo.util.WindGeneratorUnits}. This class intentionally does nothing and can be deleted at
 * any time.
 */
public final class DebugWindFusionCheck {

    private DebugWindFusionCheck() {
    }
}
