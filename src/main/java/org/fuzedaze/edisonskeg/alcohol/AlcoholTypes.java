package org.fuzedaze.edisonskeg.alcohol;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Registry of every known {@link AlcoholType}.
 *
 * <p>To add a new beverage: register its type here, register an
 * {@link org.fuzedaze.edisonskeg.item.AlcoholicDrinkItem} with it in
 * {@link org.fuzedaze.edisonskeg.registry.ModItems}, and add the matching assets.
 */
public final class AlcoholTypes {
    private static final Map<String, AlcoholType> TYPES = new LinkedHashMap<>();

    public static final AlcoholType BEER = register(new AlcoholType("beer", 1.0F, 32));

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
