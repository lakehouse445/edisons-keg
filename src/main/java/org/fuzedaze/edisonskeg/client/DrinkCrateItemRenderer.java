package org.fuzedaze.edisonskeg.client;

import net.minecraft.resources.ResourceLocation;
import org.fuzedaze.edisonskeg.alcohol.AlcoholType;
import org.fuzedaze.edisonskeg.block.DrinkCrateBlock;
import org.fuzedaze.edisonskeg.item.DrinkCrateBlockItem;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** Renders a crate item as a full crate, in the inventory and in hand. */
public class DrinkCrateItemRenderer extends GeoItemRenderer<DrinkCrateBlockItem> {
    public DrinkCrateItemRenderer(AlcoholType type) {
        super(new Model(type));
    }

    private static class Model extends GeoModel<DrinkCrateBlockItem> {
        private final AlcoholType type;

        Model(AlcoholType type) {
            this.type = type;
        }

        @Override
        public ResourceLocation getModelResource(DrinkCrateBlockItem item) {
            return this.type.crateModel(DrinkCrateBlock.CAPACITY);
        }

        @Override
        public ResourceLocation getTextureResource(DrinkCrateBlockItem item) {
            return this.type.crateTexture();
        }

        @Override
        public ResourceLocation getAnimationResource(DrinkCrateBlockItem item) {
            return this.type.crateAnimations();
        }
    }
}
