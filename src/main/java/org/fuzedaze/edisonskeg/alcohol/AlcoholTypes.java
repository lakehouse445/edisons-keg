package org.fuzedaze.edisonskeg.alcohol;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Every beverage the mod knows about. This is the one place to tune how drunk each drink
 * makes you.
 *
 * <p>Adding a beverage takes three steps:
 * <ol>
 *   <li>Register its {@link AlcoholType} here with the stats you want.</li>
 *   <li>Register an {@link org.fuzedaze.edisonskeg.item.AlcoholicDrinkItem} for it (and a
 *       crate, if it gets one) in the {@code registry} package.</li>
 *   <li>Drop in assets named after its id — {@code geo/drink/<id>.geo.json},
 *       {@code textures/drink/<id>.png}, and so on. See {@link AlcoholType#drinkModel()}.</li>
 * </ol>
 */
public final class AlcoholTypes {
    private static final Map<String, AlcoholType> TYPES = new LinkedHashMap<>();

    /**
     * Plain beer, and the yardstick every other drink is measured against: one bottle is
     * 1.0 alcohol units. Nausea creeps in after the 6th, and past the 10th there is a
     * growing chance of blacking out entirely.
     */
    public static final AlcoholType BEER = register(AlcoholType.builder("beer")
            .potency(1.0F)
            .drinkDuration(32)
            .nauseaAfterDrinks(6)
            .nauseaLevelEveryDrinks(2)
            .maxNauseaLevel(5)
            .blackoutAfterDrinks(10)
            .blackoutChancePerExtraDrink(0.02F)
            .build());

    public static synchronized AlcoholType register(AlcoholType type) {
        if (TYPES.putIfAbsent(type.id(), type) != null)
            throw new IllegalArgumentException("Duplicate alcohol type: " + type.id());
        return type;
    }

    @Nullable
    public static AlcoholType byId(String id) {
        return TYPES.get(id);
    }

    public static Collection<AlcoholType> all() {
        return Collections.unmodifiableCollection(TYPES.values());
    }

    private AlcoholTypes() {
    }
}
