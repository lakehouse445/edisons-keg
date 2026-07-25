package org.fuzedaze.edisonskeg.item;

import java.util.function.Consumer;

import org.fuzedaze.edisonskeg.alcohol.AlcoholType;
import org.fuzedaze.edisonskeg.alcohol.IntoxicationProvider;
import org.fuzedaze.edisonskeg.client.AlcoholicDrinkRenderer;
import org.fuzedaze.edisonskeg.client.DrinkArmPose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.advancements.CriteriaTriggers;
import javax.annotation.Nullable;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * A drinkable alcoholic beverage rendered as a GeckoLib 3D model in hand.
 *
 * <p>Any beverage can reuse this class: construct it with a different {@link AlcoholType}
 * and provide assets named after the type's id.
 *
 * <p>GeckoLib renders the static 3D model; the first-person drinking motion is driven in
 * Java by {@link #applyDrinkTransform} from the real use state, rather than a GeckoLib
 * triggered animation. A triggered animation plays to completion on a single right-click
 * regardless of whether the player keeps drinking, so it desyncs from the actual action —
 * the use-state approach starts and stops exactly with the drink.
 */
public class AlcoholicDrinkItem extends Item implements GeoItem {
    private static final String CONTROLLER_NAME = "controller";

    private final AlcoholType type;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AlcoholicDrinkItem(AlcoholType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public AlcoholType getAlcoholType() {
        return this.type;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return this.type.drinkDurationTicks();
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            player.getCapability(IntoxicationProvider.INTOXICATION)
                    .ifPresent(intoxication -> intoxication.drink(this.type, player));
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            player.awardStat(Stats.ITEM_USED.get(this));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F,
                    level.getRandom().nextFloat() * 0.1F + 0.9F);
        }

        entity.gameEvent(GameEvent.DRINK);

        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            stack.shrink(1);
            ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
            if (stack.isEmpty())
                return emptyBottle;
            if (!player.getInventory().add(emptyBottle))
                player.drop(emptyBottle, false);
        }

        return stack;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static model: no GeckoLib animations are played. A single idle controller
        // satisfies GeoItem's contract while the drink motion is handled in Java.
        controllers.add(new AnimationController<>(this, CONTROLLER_NAME, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * Positions the bottle in first person while it is being drunk: eases it up to the
     * mouth over the first few ticks, tips it toward the face, and adds a gentle bob.
     * Driven by the live use state so it tracks the real drink and never plays on its own.
     *
     * @return {@code true} while drinking (we own the transform), {@code false} otherwise
     *         (vanilla handles the normal resting pose)
     */
    private static boolean applyDrinkTransform(PoseStack pose, LocalPlayer player, HumanoidArm arm,
                                               ItemStack stack, float partialTick) {
        if (!player.isUsingItem() || player.getUseItem() != stack)
            return false;

        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        float remaining = player.getUseItemRemainingTicks() - partialTick + 1.0F;
        float duration = Math.max(1.0F, stack.getUseDuration());
        float used = duration - remaining;

        // Ease up to the mouth over the first ~6 ticks, then hold there (smoothstepped).
        float reach = Mth.clamp(used / 6.0F, 0.0F, 1.0F);
        reach = reach * reach * (3.0F - 2.0F * reach);

        // Start from vanilla's normal hand placement so drinking begins where the resting
        // pose left off, then move toward the mouth and tip to pour.
        pose.translate(side * 0.56F, -0.52F, -0.72F);
        pose.translate(side * -0.50F * reach, 0.72F * reach, 0.25F * reach);
        pose.translate(0.0F, Mth.cos(remaining * 0.9F) * 0.012F * reach, 0.0F);
        pose.mulPose(Axis.XP.rotationDegrees(140.0F * reach));
        pose.mulPose(Axis.ZP.rotationDegrees(side * 6.0F * reach));
        return true;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private AlcoholicDrinkRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new AlcoholicDrinkRenderer(AlcoholicDrinkItem.this.type);
                return this.renderer;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack pose, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProcess, float swingProcess) {
                return applyDrinkTransform(pose, player, arm, stack, partialTick);
            }

            /**
             * Third person: raise the arm holding the drink while it is being drunk.
             * Returning null leaves the entity in whatever pose it would normally use.
             */
            @Nullable
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                boolean drinkingThis = entity.isUsingItem()
                        && entity.getUsedItemHand() == hand
                        && entity.getUseItem() == stack;
                return drinkingThis ? DrinkArmPose.get() : null;
            }
        });
    }
}
