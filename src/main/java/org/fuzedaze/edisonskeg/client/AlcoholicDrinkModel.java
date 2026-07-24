package org.fuzedaze.edisonskeg.client;

import net.minecraft.resources.ResourceLocation;
import org.fuzedaze.edisonskeg.EdisonsKeg;
import org.fuzedaze.edisonskeg.alcohol.AlcoholType;
import org.fuzedaze.edisonskeg.item.AlcoholicDrinkItem;
import software.bernie.geckolib.model.GeoModel;

/**
 * Resolves geo model, texture, and animation assets for a drink from its
 * {@link AlcoholType} id, so every new beverage just ships identically-named assets.
 */
public class AlcoholicDrinkModel extends GeoModel<AlcoholicDrinkItem> {
    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final ResourceLocation animations;

    public AlcoholicDrinkModel(AlcoholType type) {
        this.model = new ResourceLocation(EdisonsKeg.MODID, "geo/" + type.id() + ".geo.json");
        this.texture = new ResourceLocation(EdisonsKeg.MODID, "textures/item/" + type.id() + ".png");
        this.animations = new ResourceLocation(EdisonsKeg.MODID, "animations/" + type.id() + ".animation.json");
    }

    @Override
    public ResourceLocation getModelResource(AlcoholicDrinkItem item) {
        return this.model;
    }

    @Override
    public ResourceLocation getTextureResource(AlcoholicDrinkItem item) {
        return this.texture;
    }

    @Override
    public ResourceLocation getAnimationResource(AlcoholicDrinkItem item) {
        return this.animations;
    }
}
