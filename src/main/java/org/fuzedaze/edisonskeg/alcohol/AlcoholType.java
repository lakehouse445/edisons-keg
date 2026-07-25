package org.fuzedaze.edisonskeg.alcohol;

import net.minecraft.resources.ResourceLocation;
import org.fuzedaze.edisonskeg.EdisonsKeg;

/**
 * Everything that makes one beverage hit differently from another: how strong it is, when
 * it starts making you sick, and when it starts making you black out.
 *
 * <p>Build one with {@link #builder(String)}. The tuning values are all expressed in
 * <em>drinks of this beverage</em>, which is the way you actually think about it
 * ("nausea after 6 beers"), and are converted to internal alcohol units on build. So a
 * spirit twice as strong as beer needs only half as many drinks to reach the same place:
 *
 * <pre>{@code
 * AlcoholType.builder("whiskey")
 *         .potency(2.5F)                     // one shot = 2.5 units, vs beer's 1.0
 *         .nauseaAfterDrinks(3)              // sick after 3 shots
 *         .blackoutAfterDrinks(5)            // blackouts possible from the 5th
 *         .build();
 * }</pre>
 *
 * <p>Assets are resolved from the {@link #id()}, so adding a beverage is a matter of
 * dropping files into the matching folders — see {@link #drinkModel()} and friends.
 */
public final class AlcoholType {
    private final String id;
    private final float unitsPerDrink;
    private final int drinkDurationTicks;
    private final float nauseaThresholdUnits;
    private final float unitsPerNauseaLevel;
    private final int maxNauseaAmplifier;
    private final float blackoutThresholdUnits;
    private final float blackoutChancePerUnit;

    private AlcoholType(Builder builder) {
        this.id = builder.id;
        this.unitsPerDrink = builder.unitsPerDrink;
        this.drinkDurationTicks = builder.drinkDurationTicks;
        this.nauseaThresholdUnits = builder.nauseaAfterDrinks * builder.unitsPerDrink;
        this.unitsPerNauseaLevel = builder.nauseaLevelEveryDrinks * builder.unitsPerDrink;
        this.maxNauseaAmplifier = Math.max(0, builder.maxNauseaLevel - 1);
        this.blackoutThresholdUnits = builder.blackoutAfterDrinks * builder.unitsPerDrink;
        this.blackoutChancePerUnit = builder.blackoutChancePerExtraDrink / builder.unitsPerDrink;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    // ---------------------------------------------------------------- identity

    public String id() {
        return this.id;
    }

    // ---------------------------------------------------------------- strength

    /** Alcohol units added by one drink. Beer is the baseline at 1.0. */
    public float unitsPerDrink() {
        return this.unitsPerDrink;
    }

    /** How long the drinking action takes, in ticks (32 = vanilla potion speed). */
    public int drinkDurationTicks() {
        return this.drinkDurationTicks;
    }

    // ---------------------------------------------------------------- nausea

    /** Units a player must exceed before nausea begins. */
    public float nauseaThresholdUnits() {
        return this.nauseaThresholdUnits;
    }

    /** Extra units past the threshold needed for each further level of nausea. */
    public float unitsPerNauseaLevel() {
        return this.unitsPerNauseaLevel;
    }

    /** Highest nausea amplifier this drink can inflict (0 = Nausea I). */
    public int maxNauseaAmplifier() {
        return this.maxNauseaAmplifier;
    }

    // ---------------------------------------------------------------- blackouts

    /** Units at which blackouts become possible. */
    public float blackoutThresholdUnits() {
        return this.blackoutThresholdUnits;
    }

    /** Added blackout chance per check, for every unit past the blackout threshold. */
    public float blackoutChancePerUnit() {
        return this.blackoutChancePerUnit;
    }

    // ---------------------------------------------------------------- assets

    /** {@code assets/edisonskeg/geo/drink/<id>.geo.json} */
    public ResourceLocation drinkModel() {
        return asset("geo/drink/" + this.id + ".geo.json");
    }

    /** {@code assets/edisonskeg/animations/drink/<id>.animation.json} */
    public ResourceLocation drinkAnimations() {
        return asset("animations/drink/" + this.id + ".animation.json");
    }

    /** {@code assets/edisonskeg/textures/drink/<id>.png} */
    public ResourceLocation drinkTexture() {
        return asset("textures/drink/" + this.id + ".png");
    }

    /** {@code assets/edisonskeg/geo/crate/<id>_<bottles>.geo.json} — one per fill level. */
    public ResourceLocation crateModel(int bottles) {
        return asset("geo/crate/" + this.id + "_" + bottles + ".geo.json");
    }

    /** {@code assets/edisonskeg/animations/crate/<id>.animation.json} */
    public ResourceLocation crateAnimations() {
        return asset("animations/crate/" + this.id + ".animation.json");
    }

    /** {@code assets/edisonskeg/textures/crate/<id>.png} */
    public ResourceLocation crateTexture() {
        return asset("textures/crate/" + this.id + ".png");
    }

    private static ResourceLocation asset(String path) {
        return new ResourceLocation(EdisonsKeg.MODID, path);
    }

    @Override
    public String toString() {
        return "AlcoholType[" + this.id + "]";
    }

    /**
     * Fluent tuning for a beverage. Every value has a beer-like default, so a new type
     * only has to state what makes it different.
     */
    public static final class Builder {
        private final String id;
        private float unitsPerDrink = 1.0F;
        private int drinkDurationTicks = 32;
        private float nauseaAfterDrinks = 6.0F;
        private float nauseaLevelEveryDrinks = 2.0F;
        private int maxNauseaLevel = 5;
        private float blackoutAfterDrinks = 10.0F;
        private float blackoutChancePerExtraDrink = 0.02F;

        private Builder(String id) {
            this.id = id;
        }

        /** How strong one drink is relative to a beer (beer = 1.0). */
        public Builder potency(float unitsPerDrink) {
            this.unitsPerDrink = unitsPerDrink;
            return this;
        }

        /** Ticks the drinking action takes. */
        public Builder drinkDuration(int ticks) {
            this.drinkDurationTicks = ticks;
            return this;
        }

        /** Drinks a player can have before nausea starts; the next one tips them over. */
        public Builder nauseaAfterDrinks(float drinks) {
            this.nauseaAfterDrinks = drinks;
            return this;
        }

        /** Further drinks needed to step nausea up one level. */
        public Builder nauseaLevelEveryDrinks(float drinks) {
            this.nauseaLevelEveryDrinks = drinks;
            return this;
        }

        /** Strongest nausea this drink can cause, as a potion level (1 = Nausea I). */
        public Builder maxNauseaLevel(int level) {
            this.maxNauseaLevel = level;
            return this;
        }

        /** Drinks a player can have before blackouts become possible. */
        public Builder blackoutAfterDrinks(float drinks) {
            this.blackoutAfterDrinks = drinks;
            return this;
        }

        /** Blackout chance added per check (every 2s) for each drink past the threshold. */
        public Builder blackoutChancePerExtraDrink(float chance) {
            this.blackoutChancePerExtraDrink = chance;
            return this;
        }

        public AlcoholType build() {
            return new AlcoholType(this);
        }
    }
}
