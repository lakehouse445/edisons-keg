package org.fuzedaze.edisonskeg.client;

import net.minecraft.resources.ResourceLocation;
import org.fuzedaze.edisonskeg.alcohol.CrateAssets;
import org.fuzedaze.edisonskeg.block.DrinkCrateBlockEntity;
import software.bernie.geckolib.model.GeoModel;

/**
 * Draws a placed crate as whatever it currently holds — a bare crate when empty, or the
 * beverage's crate filled to the right level.
 */
public class DrinkCrateModel extends GeoModel<DrinkCrateBlockEntity> {

    @Override
    public ResourceLocation getModelResource(DrinkCrateBlockEntity crate) {
        return CrateAssets.modelFor(crate.getContents());
    }

    @Override
    public ResourceLocation getTextureResource(DrinkCrateBlockEntity crate) {
        return CrateAssets.textureFor(crate.getContents());
    }

    @Override
    public ResourceLocation getAnimationResource(DrinkCrateBlockEntity crate) {
        return CrateAssets.animationsFor(crate.getContents());
    }
}
