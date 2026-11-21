package jp.example.mekanismgizmo;

/**
 * Placeholder for a temporary development helper. The bug hunt of this round was measured with a self test that lived
 * here (dedicated server start, machines placed in fresh chunks):
 * <pre>
 * brewer:   6 bottle slots + 1 nether wart -&gt; only one row brews, gas 100 -&gt; 95, the second row stays untouched;
 *           a second ingredient brews the second row, gas -&gt; 90 (5 mB per row of three bottles)
 * brewer:   a whole stack of machine items is reset in one operation (5 in -&gt; 5 clean out, 200 FE)
 * injector: water bucket -&gt; empty bucket + 1000 mB for 25 FE; glass bottle + 1000 mB -&gt; water bottle, tank 750;
 *           a corrupt ContainerMode of 99 in the save loads as EMPTY instead of throwing
 * ice:      water bucket -&gt; 1000 mB, the empty bucket stays in the input slot (nothing is voided)
 * tier:     ice maker -&gt; basic ice maker keeps 2500 mB, 4000 FE, three speed upgrades and 12 ice, slots 3 -&gt; 4
 * collector: out of range saved settings (99, 0, 42, -42) load clamped to (9, 1, 5, -5)
 * defaults: every machine item carries the side config and the ejector, with 28 stored item slots where expected
 * </pre>
 * Fixed while looking: the item collector's scan counter shadowed Mekanism's own tick counter, a corrupt container
 * mode crashed the fluid injector, the wind fusion block item was missing the ejector component every other machine
 * item has, and the wind fusion info line could run over the power bar with compressed wind generators. This class
 * intentionally does nothing and can be deleted at any time.
 */
public final class DebugBugCheck {

    private DebugBugCheck() {
    }
}
