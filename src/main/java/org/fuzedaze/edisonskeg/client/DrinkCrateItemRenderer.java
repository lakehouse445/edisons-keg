package org.fuzedaze.edisonskeg.client;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.fuzedaze.edisonskeg.alcohol.CrateAssets;
import org.fuzedaze.edisonskeg.alcohol.CrateContents;
import org.fuzedaze.edisonskeg.item.DrinkCrateBlockItem;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * Draws a crate item as whatever that particular stack is holding, so a crate in the
 * hotbar reads as empty or full of its drink at a glance.
 *
 * <p>The contents live on the stack rather than the item, so the model has to ask the
 * renderer which stack is currently being drawn — hence the supplier wiring below.
 */
public class DrinkCrateItemRenderer extends GeoItemRenderer<DrinkCrateBlockItem> {

    public DrinkCrateItemRenderer() {
        this(new StackAwareModel());
    }

    private DrinkCrateItemRenderer(StackAwareModel model) {
        super(model);
        // 'this' is available here but not in the super() argument above, which is why the
        // model is handed in first and wired to the renderer afterwards.
        model.currentStack = this::getCurrentItemStack;
    }

    private static class StackAwareModel extends GeoModel<DrinkCrateBlockItem> {
        private Supplier<ItemStack> currentStack = () -> ItemStack.EMPTY;

        private CrateContents contents() {
            return CrateContents.fromStack(this.currentStack.get());
        }

        @Override
        public ResourceLocation getModelResource(DrinkCrateBlockItem item) {
            return CrateAssets.modelFor(contents());
        }

        @Override
        public ResourceLocation getTextureResource(DrinkCrateBlockItem item) {
            return CrateAssets.textureFor(contents());
        }

        @Override
        public ResourceLocation getAnimationResource(DrinkCrateBlockItem item) {
            return CrateAssets.animationsFor(contents());
        }
    }
}
