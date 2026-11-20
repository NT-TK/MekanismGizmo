package jp.example.mekanismgizmo;

/**
 * Placeholder for a temporary development helper that lived here: a self test which placed machines in the world and
 * ticked them, to check the behaviour that a compiler cannot check. Results of the last run (Mekanism 10.7.19.85):
 * <ul>
 *     <li>ultimate potion brewer, 15 water bottles + 15 sugar + 1,000 mB gas: 15 potions, 5 sugar and 25 mB consumed</li>
 *     <li>3 water bottles + 1 sugar: 3 potions, 1 sugar and 5 mB consumed (a partly filled machine only brews its rows)</li>
 *     <li>no ingredient: nothing brewed, nothing consumed</li>
 *     <li>ultimate ice maker with a full first output slot and 5,000 mB water: 4 ice into the remaining slots,
 *     4,000 mB water consumed (a full slot never eats water)</li>
 *     <li>fluid injector with a full output slot: the water bucket stays untouched and the tank stays empty</li>
 *     <li>item collector GUI button ids: encode/decode round trip OK</li>
 * </ul>
 */
public final class DebugBugCheck {

    private DebugBugCheck() {
    }
}
