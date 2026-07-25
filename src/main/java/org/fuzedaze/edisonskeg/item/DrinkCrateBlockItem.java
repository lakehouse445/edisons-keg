package org.fuzedaze.edisonskeg.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.fuzedaze.edisonskeg.alcohol.AlcoholTypes;
import org.fuzedaze.edisonskeg.block.DrinkCrateBlock;
import org.fuzedaze.edisonskeg.client.DrinkCrateItemRenderer;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * A crate's item form. Rendered with GeckoLib so the inventory icon and held item show the
 * same 3D crate as the placed block, always full regardless of any fill level a particular
 * stack was broken at.
 */
public class DrinkCrateBlockItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DrinkCrateBlockItem(DrinkCrateBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private DrinkCrateItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new DrinkCrateItemRenderer(
                            getBlock() instanceof DrinkCrateBlock crate
                                    ? crate.getAlcoholType()
                                    : AlcoholTypes.BEER);
                return this.renderer;
            }
        });
    }
}
