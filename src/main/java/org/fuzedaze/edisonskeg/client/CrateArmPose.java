package org.fuzedaze.edisonskeg.client;

import javax.annotation.Nullable;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

/**
 * The third-person pose for carrying a crate: both arms out in front, as if holding
 * something bulky. Registered as two-handed, so vanilla poses both arms rather than just
 * the one holding the item.
 *
 * <p>The transform sets both arms itself rather than only the one it is handed, so the
 * carry looks the same whether the crate is in the main hand or the off hand.
 *
 * <p>Where the crate actually sits relative to the hands is controlled by the
 * {@code thirdperson_*} display transforms in {@code models/item/<crate>.json}, not here.
 */
public final class CrateArmPose {
    /** How far the arms swing up to carry the crate. */
    private static final float HOLD_X_ROT = -0.85F;
    /** Inward turn so both hands meet in front of the body. */
    private static final float INWARD_Y_ROT = 0F;

    @Nullable
    private static HumanoidModel.ArmPose pose;

    /** Creates the pose. Called once during client setup, before anything can render. */
    public static void register() {
        if (pose == null)
            pose = HumanoidModel.ArmPose.create("EDISONSKEG_CARRY_CRATE", true, CrateArmPose::apply);
    }

    /** The carrying pose, or null before client setup has run. */
    @Nullable
    public static HumanoidModel.ArmPose get() {
        return pose;
    }

    private static void apply(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        poseArm(model.rightArm, -INWARD_Y_ROT);
        poseArm(model.leftArm, INWARD_Y_ROT);
    }

    private static void poseArm(ModelPart armPart, float yRot) {
        armPart.xRot = HOLD_X_ROT;
        armPart.yRot = yRot;
        armPart.zRot = 0.0F;
    }

    private CrateArmPose() {
    }
}
