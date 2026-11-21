package jp.example.mekanismgizmo;

/**
 * Placeholder for a temporary development helper. The liquid heater and the improved gas generator were verified with
 * a self test that lived here (dedicated server start, machines placed at y=100, 25 ticks each with the energy topped
 * up like a cable would):
 * <pre>
 * liquid heater, untiered:  parallel=1, tank=20,000 mB, 625 FE/t,
 *                          10,000 mB sodium -> 9,500 mB left, 500 mB superheated sodium in 20 ticks
 * liquid heater, ultimate:  parallel=5, tank=100,000 mB, 3,125 FE/t,
 *                          20,000 mB sodium -> 17,500 mB left, 2,500 mB superheated sodium in 20 ticks
 * improved gas generator:   tank=1,800,000 mB, buffer=20,000,000 FE, fuel values hydrogen=200 FE/mB,
 *                          ethene=28,200 FE/mB, sodium (not a fuel)=0
 *   full tank of hydrogen:  efficiency 100%, 200 FE/mB -> 4,000 FE/t
 *   half tank of hydrogen:  efficiency 60%, 120 FE/mB -> 2,400 FE/t
 *   full tank of ethene:    efficiency 100%, 28,195 FE/mB -> 563,950 FE/t
 * </pre>
 * The implementations live in {@link jp.example.mekanismgizmo.block.LiquidHeaterBlockEntity} and
 * {@link jp.example.mekanismgizmo.block.ImprovedGasGeneratorBlockEntity}. This class intentionally does nothing and
 * can be deleted at any time.
 */
public final class DebugNewMachinesCheck {

    private DebugNewMachinesCheck() {
    }
}
