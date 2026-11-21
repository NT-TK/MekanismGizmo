package jp.example.mekanismgizmo;

/**
 * Placeholder for a temporary development helper. The liquid heater and the improved gas generator were verified with
 * a self test that lived here (dedicated server start, machines placed at y=100, energy topped up like a cable would):
 * <pre>
 * liquid heater, untiered:  parallel=1, tank=20,000 mB, 625 FE/t,
 *                          10,000 mB sodium -> 9,500 mB left, 500 mB superheated sodium in 20 ticks
 * liquid heater, ultimate:  parallel=5, tank=100,000 mB, 3,125 FE/t,
 *                          20,000 mB sodium -> 17,500 mB left, 2,500 mB superheated sodium in 20 ticks
 * improved gas generator:   tank=1,800,000 mB, buffer=20,000,000 FE,
 *                           hydrogen: 200 FE/mB, max burn rate 100 mB/t (1 tick per mB / 100 generators)
 *                           ethene:   28,200 FE/mB, max burn rate 2.5 mB/t (40 ticks per mB / 100 generators)
 *   hydrogen 100% fill:    99.8 mB/t, 19,975 FE/t (measured over 40 ticks; the fill drops while burning)
 *   hydrogen 50% fill:     49.9 mB/t, 9,990 FE/t
 *   hydrogen 25% fill:     25.0 mB/t, 4,995 FE/t
 *   ethene 100% fill:      2.5 mB/t, 70,500 FE/t (= exactly 100 Mekanism gas generators)
 *   ethene 50% fill:       1.25 mB/t, 35,250 FE/t
 * </pre>
 * The implementations live in {@link jp.example.mekanismgizmo.block.LiquidHeaterBlockEntity} and
 * {@link jp.example.mekanismgizmo.block.ImprovedGasGeneratorBlockEntity}. This class intentionally does nothing and
 * can be deleted at any time.
 */
public final class DebugNewMachinesCheck {

    private DebugNewMachinesCheck() {
    }
}
