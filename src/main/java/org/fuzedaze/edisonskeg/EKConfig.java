package org.fuzedaze.edisonskeg;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Server-wide tuning that is <em>not</em> specific to any one beverage.
 *
 * <p>How strong a drink is, and when it starts causing nausea or blackouts, lives on the
 * drink itself — see {@link org.fuzedaze.edisonskeg.alcohol.AlcoholTypes}. This file is
 * only for values that apply to every drink equally: sobering-up speed, how far a blackout
 * throws you, and how quickly players build tolerance.
 */
public final class EKConfig {
    public static final ForgeConfigSpec SPEC;

    /** Ticks it takes to sober up by one alcohol unit. */
    public static final ForgeConfigSpec.IntValue TICKS_PER_UNIT_DECAY;
    /** Minimum horizontal distance (blocks) a blackout teleports the player. */
    public static final ForgeConfigSpec.IntValue BLACKOUT_MIN_DISTANCE;
    /** Maximum horizontal distance (blocks) a blackout teleports the player. */
    public static final ForgeConfigSpec.IntValue BLACKOUT_MAX_DISTANCE;
    /** How much further out every threshold moves per blackout survived. */
    public static final ForgeConfigSpec.DoubleValue TOLERANCE_GAIN_PER_BLACKOUT;
    /** Ceiling on the tolerance multiplier, so veterans can still get drunk. */
    public static final ForgeConfigSpec.DoubleValue MAX_TOLERANCE_MULTIPLIER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("intoxication");
        TICKS_PER_UNIT_DECAY = builder
                .comment("Ticks needed to sober up by one alcohol unit (2400 = 2 minutes per unit).")
                .defineInRange("ticksPerUnitDecay", 2400, 20, 72000);
        builder.pop();

        builder.push("blackout");
        BLACKOUT_MIN_DISTANCE = builder
                .comment("Minimum horizontal distance in blocks a blackout teleports the player (48 = 3 chunks).")
                .defineInRange("minDistance", 48, 1, 2048);
        BLACKOUT_MAX_DISTANCE = builder
                .comment("Maximum horizontal distance in blocks a blackout teleports the player (128 = 8 chunks).")
                .defineInRange("maxDistance", 128, 1, 2048);
        builder.pop();

        builder.push("tolerance");
        TOLERANCE_GAIN_PER_BLACKOUT = builder
                .comment("Extra drinking capacity gained per blackout, as a fraction.",
                        "0.15 means each blackout pushes nausea and blackout thresholds 15% further out.",
                        "Set to 0 to disable tolerance entirely.")
                .defineInRange("gainPerBlackout", 0.15D, 0.0D, 10.0D);
        MAX_TOLERANCE_MULTIPLIER = builder
                .comment("Hard cap on the tolerance multiplier (3.0 = never needs more than 3x a newcomer).")
                .defineInRange("maxMultiplier", 3.0D, 1.0D, 20.0D);
        builder.pop();

        SPEC = builder.build();
    }

    private EKConfig() {
    }
}
