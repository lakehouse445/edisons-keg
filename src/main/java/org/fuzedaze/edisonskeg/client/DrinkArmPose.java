package org.fuzedaze.edisonskeg.client;

import javax.annotation.Nullable;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

/**
 * The third-person pose for someone drinking: the arm holding the bottle is lifted well
 * above where a normally-held item sits, so onlookers can tell they're having a drink.
 * Deliberately plain — a raised arm, nothing more.
 *
 * <p>Only the arm actually holding the drink is posed; Forge passes the matching
 * {@link HumanoidArm}, so it works from either hand with no extra branching.
 */
public final class DrinkArmPose {
    /** How far the arm swings up. Around -1.0 rad is "held out and up" without looking silly. */
    private static final float RAISE_X_ROT = -1.0F;
    /** Slight inward turn so the bottle sits toward the body's midline. */
    private static final float INWARD_Y_ROT = 0.25F;

    @Nullable
    private static HumanoidModel.ArmPose pose;

    /** Creates the pose. Called once during client setup, before anything can render. */
    public static void register() {
        if (pose == null)
            pose = HumanoidModel.ArmPose.create("EDISONSKEG_DRINKING", false, DrinkArmPose::apply);
    }

    /** The drinking pose, or null before client setup has run. */
    @Nullable
    public static HumanoidModel.ArmPose get() {
        return pose;
    }

    private static void apply(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        ModelPart armPart = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        armPart.xRot = RAISE_X_ROT;
        armPart.yRot = arm == HumanoidArm.RIGHT ? -INWARD_Y_ROT : INWARD_Y_ROT;
        armPart.zRot = 0.0F;
    }

    private DrinkArmPose() {
    }
}
