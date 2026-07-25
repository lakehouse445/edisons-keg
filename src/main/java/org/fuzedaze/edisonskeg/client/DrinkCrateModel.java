package org.fuzedaze.edisonskeg.client;

import net.minecraft.resources.ResourceLocation;
import org.fuzedaze.edisonskeg.block.DrinkCrateBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/**
 * Picks the crate model matching how many bottles are left, so a crate visibly empties as
 * players take drinks out. One geo file per fill level, named after the beverage — see
 * {@link org.fuzedaze.edisonskeg.alcohol.AlcoholType#crateModel(int)}.
 */
public class DrinkCrateModel extends GeoModel<DrinkCrateBlockEntity> {

    @Override
    public ResourceLocation getModelResource(DrinkCrateBlockEntity crate) {
        return crate.getAlcoholType().crateModel(crate.getBottles());
    }

    @Override
    public ResourceLocation getTextureResource(DrinkCrateBlockEntity crate) {
        return crate.getAlcoholType().crateTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(DrinkCrateBlockEntity crate) {
        return crate.getAlcoholType().crateAnimations();
    }
}
