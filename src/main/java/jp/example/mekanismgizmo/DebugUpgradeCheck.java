package jp.example.mekanismgizmo;

/**
 * Placeholder for a temporary development helper that lived here: it placed an untiered ice maker, filled it, ran the
 * same steps as Mekanism's tier installer (get the upgrade payload, replace the block, hand the payload to the new
 * tile) and printed the result. Last run (Mekanism 10.7.19.85):
 * <pre>
 * before: slots=3 water=2345 energy=4321 item0=water_bucket item1=7
 * after:  tier=BASIC parallel=2 slots=4 water=2345/8000 energy=4321 item0=water_bucket item1=7
 * </pre>
 * which confirms that a tier upgrade keeps the contents, the energy and the tank fill level.
 */
public final class DebugUpgradeCheck {

    private DebugUpgradeCheck() {
    }
}
