package org.fuzedaze.edisonskeg.client;

import net.minecraft.resources.ResourceLocation;
import org.fuzedaze.edisonskeg.alcohol.AlcoholType;
import org.fuzedaze.edisonskeg.item.AlcoholicDrinkItem;
import software.bernie.geckolib.model.GeoModel;

/**
 * Resolves a drink's geo model, texture, and animations from its {@link AlcoholType}, so a
 * new beverage only has to ship files named after its id.
 */
public class AlcoholicDrinkModel extends GeoModel<AlcoholicDrinkItem> {
    private final AlcoholType type;

    public AlcoholicDrinkModel(AlcoholType type) {
        this.type = type;
    }

    @Override
    public ResourceLocation getModelResource(AlcoholicDrinkItem item) {
        return this.type.drinkModel();
    }

    @Override
    public ResourceLocation getTextureResource(AlcoholicDrinkItem item) {
        return this.type.drinkTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(AlcoholicDrinkItem item) {
        return this.type.drinkAnimations();
    }
}
