package jp.example.mekanismgizmo.util;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import org.jetbrains.annotations.Nullable;

/**
 * The tier of a machine. The freshly crafted machine is untiered ("ice maker"), and Mekanism's tier installers take it
 * up one step at a time, exactly like a plain Energized Smelter becomes a Basic Smelting Factory:
 * <ul>
 *     <li>{@code ICE_MAKER} + Basic Tier Installer → {@code BASIC_ICE_MAKER} (untiered → basic, because Mekanism's
 *     basic tier installer has no "from" tier)</li>
 *     <li>basic + Advanced Tier Installer → advanced → elite → ultimate</li>
 * </ul>
 * Every tier adds one more parallel processing slot, so the untiered machine processes one item per operation and the
 * ultimate tier processes five.
 */
public enum MachineTier implements ITier {
    UNTIERED(1, null, ""),
    BASIC(2, BaseTier.BASIC, "basic"),
    ADVANCED(3, BaseTier.ADVANCED, "advanced"),
    ELITE(4, BaseTier.ELITE, "elite"),
    ULTIMATE(5, BaseTier.ULTIMATE, "ultimate");

    private static final MachineTier[] VALUES = values();

    private final int parallel;
    @Nullable
    private final BaseTier baseTier;
    private final String lowerName;

    MachineTier(int parallel, @Nullable BaseTier baseTier, String lowerName) {
        this.parallel = parallel;
        this.baseTier = baseTier;
        this.lowerName = lowerName;
    }

    /** Number of items the machine can process per operation (1 for the untiered machine, 5 for ultimate). */
    public int getParallel() {
        return parallel;
    }

    /**
     * The Mekanism tier of this machine, or {@code null} for the untiered machine. The untiered machine is never given
     * an {@code AttributeTier}, so nothing asks it for a base tier.
     */
    @Override
    @Nullable
    public BaseTier getBaseTier() {
        return baseTier;
    }

    /** Registry name prefix of this tier ({@code ""}, {@code "basic"}, ...). */
    public String getLowerName() {
        return lowerName;
    }

    /** The next tier, or {@code null} for the ultimate tier. */
    @Nullable
    public MachineTier next() {
        return this == ULTIMATE ? null : VALUES[ordinal() + 1];
    }

    /** Reads the tier from a block entity type's registry path, e.g. {@code advanced_ice_maker}. */
    public static MachineTier fromRegistryPath(String path, String machineName) {
        if (path.equals(machineName)) {
            return UNTIERED;
        }
        for (MachineTier tier : VALUES) {
            if (!tier.lowerName.isEmpty() && path.equals(tier.lowerName + "_" + machineName)) {
                return tier;
            }
        }
        return UNTIERED;
    }
}
