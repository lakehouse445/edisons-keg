package org.fuzedaze.edisonskeg.alcohol;

/**
 * The strength profile for one kind of alcoholic beverage.
 *
 * <p>New drink types only need a new entry in {@link AlcoholTypes} plus their assets
 * (geo model, animation, and texture named after {@link #id()}). The {@code unitsPerDrink}
 * value is what makes one drink stronger than another: a beer contributes 1.0 units,
 * so a spirit registered with 2.5 units hits the nausea and blackout thresholds
 * in far fewer drinks.
 *
 * @param id                 lowercase identifier, also used for asset lookup
 *                           ({@code geo/<id>.geo.json}, {@code animations/<id>.animation.json},
 *                           {@code textures/item/<id>.png})
 * @param unitsPerDrink      alcohol units added per drink consumed (beer = 1.0)
 * @param drinkDurationTicks how long the drink takes to consume (32 = vanilla potion speed)
 */
public record AlcoholType(String id, float unitsPerDrink, int drinkDurationTicks) {
}
