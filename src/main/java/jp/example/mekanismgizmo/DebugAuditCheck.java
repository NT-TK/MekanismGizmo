package jp.example.mekanismgizmo;

/**
 * Placeholder for a temporary development helper. The resource / behaviour audit that lived here (35 blockstates,
 * 11 machines, 41 recipes, drop and place round trips, gas generator burn rate, collector area, pipettes) was
 * verified with a self test and has been removed from the shipped code: it registered a {@code ServerStartedEvent}
 * handler that built a test rig next to the world spawn and halted the server afterwards, which stopped every world
 * from actually being playable. It intentionally does nothing now and can be deleted at any time.
 *
 * <p>Debug classes are also excluded from the jar in {@code build.gradle}, so nothing under this name is ever
 * shipped again.
 */
public final class DebugAuditCheck {

    private DebugAuditCheck() {
    }
}
