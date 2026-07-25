package org.fuzedaze.edisonskeg.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.fuzedaze.edisonskeg.alcohol.AlcoholType;
import org.fuzedaze.edisonskeg.alcohol.AlcoholTypes;
import org.fuzedaze.edisonskeg.registry.ModBlockEntities;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Exists so the crate can be drawn by a GeckoLib renderer. The fill level itself lives in
 * the block state ({@link DrinkCrateBlock#BOTTLES}), which the renderer reads to pick the
 * matching geo model. No animations are played.
 */
public class DrinkCrateBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DrinkCrateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRINK_CRATE.get(), pos, state);
    }

    /** Which beverage this crate holds, falling back to beer if the block state is odd. */
    public AlcoholType getAlcoholType() {
        return getBlockState().getBlock() instanceof DrinkCrateBlock crate
                ? crate.getAlcoholType()
                : AlcoholTypes.BEER;
    }

    /** How many bottles are left, or a full crate if the state has already been replaced. */
    public int getBottles() {
        BlockState state = getBlockState();
        return state.hasProperty(DrinkCrateBlock.BOTTLES)
                ? state.getValue(DrinkCrateBlock.BOTTLES)
                : DrinkCrateBlock.CAPACITY;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
