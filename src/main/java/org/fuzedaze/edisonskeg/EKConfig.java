package org.fuzedaze.edisonskeg;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Server-synced tuning values for the intoxication system.
 *
 * <p>All thresholds are measured in "alcohol units". One standard beer is worth 1.0 units;
 * stronger drink types contribute more units per drink via {@link org.fuzedaze.edisonskeg.alcohol.AlcoholType}.
 */
public final class EKConfig {
    public static final ForgeConfigSpec SPEC;

    /** Units a player must exceed before nausea sets in. */
    public static final ForgeConfigSpec.DoubleValue NAUSEA_THRESHOLD;
    /** Units above the nausea threshold required per additional nausea amplifier level. */
    public static final ForgeConfigSpec.DoubleValue UNITS_PER_NAUSEA_LEVEL;
    /** Cap on the nausea amplifier no matter how drunk the player gets. */
    public static final ForgeConfigSpec.IntValue MAX_NAUSEA_AMPLIFIER;
    /** Units at which blackouts become possible. */
    public static final ForgeConfigSpec.DoubleValue BLACKOUT_THRESHOLD;
    /** Blackout chance per effect check, per unit past the blackout threshold. */
    public static final ForgeConfigSpec.DoubleValue BLACKOUT_CHANCE_PER_UNIT;
    /** Minimum horizontal distance (blocks) a blackout teleports the player. */
    public static final ForgeConfigSpec.IntValue BLACKOUT_MIN_DISTANCE;
    /** Maximum horizontal distance (blocks) a blackout teleports the player. */
    public static final ForgeConfigSpec.IntValue BLACKOUT_MAX_DISTANCE;
    /** Ticks it takes to sober up by one unit. */
    public static final ForgeConfigSpec.IntValue TICKS_PER_UNIT_DECAY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("intoxication");
        NAUSEA_THRESHOLD = builder
                .comment("Alcohol units a player must exceed before nausea starts (one beer = 1.0 units).")
                .defineInRange("nauseaThreshold", 6.0D, 0.0D, 100.0D);
        UNITS_PER_NAUSEA_LEVEL = builder
                .comment("Units past the nausea threshold needed for each extra level of nausea strength.")
                .defineInRange("unitsPerNauseaLevel", 2.0D, 0.1D, 100.0D);
        MAX_NAUSEA_AMPLIFIER = builder
                .comment("Maximum nausea amplifier (0 = Nausea I).")
                .defineInRange("maxNauseaAmplifier", 4, 0, 9);
        TICKS_PER_UNIT_DECAY = builder
                .comment("Ticks needed to sober up by one alcohol unit (2400 = 2 minutes per unit).")
                .defineInRange("ticksPerUnitDecay", 2400, 20, 72000);
        builder.pop();

        builder.push("blackout");
        BLACKOUT_THRESHOLD = builder
                .comment("Alcohol units at which blackouts become possible.")
                .defineInRange("blackoutThreshold", 10.0D, 0.0D, 100.0D);
        BLACKOUT_CHANCE_PER_UNIT = builder
                .comment("Chance of a blackout per check (every 2 seconds), per unit past the blackout threshold.")
                .defineInRange("blackoutChancePerUnit", 0.02D, 0.0D, 1.0D);
        BLACKOUT_MIN_DISTANCE = builder
                .comment("Minimum horizontal distance in blocks a blackout teleports the player (48 = 3 chunks).")
                .defineInRange("blackoutMinDistance", 48, 1, 2048);
        BLACKOUT_MAX_DISTANCE = builder
                .comment("Maximum horizontal distance in blocks a blackout teleports the player (128 = 8 chunks).")
                .defineInRange("blackoutMaxDistance", 128, 1, 2048);
        builder.pop();

        SPEC = builder.build();
    }

    private EKConfig() {
    }
}
