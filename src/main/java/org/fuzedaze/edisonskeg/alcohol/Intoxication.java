package org.fuzedaze.edisonskeg.alcohol;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import org.fuzedaze.edisonskeg.EKConfig;
import org.fuzedaze.edisonskeg.network.BlackoutPacket;
import org.fuzedaze.edisonskeg.network.ModNetworking;

/**
 * Per-player intoxication state. Tracks accumulated alcohol units, slowly sobers the
 * player up, applies escalating nausea past the nausea threshold, and rolls for
 * blackouts past the blackout threshold.
 *
 * <p>A blackout is a short cutscene: the player is frozen and the screen fades to black,
 * a random multi-chunk teleport happens while fully black, and the screen fades back in.
 * Blacking out fully sobers the player up, so nausea clears as well.
 */
public class Intoxication implements INBTSerializable<CompoundTag> {
    /** How often (in ticks) nausea is refreshed and blackouts are rolled. */
    public static final int EFFECT_CHECK_INTERVAL = 40;
    private static final int TELEPORT_ATTEMPTS = 24;
    private static final String TAG_UNITS = "AlcoholUnits";

    private float units;
    /** Ticks left in the current blackout cutscene, or 0 when not blacking out. Not persisted. */
    private int blackoutRemaining;

    /** Called when the player finishes an alcoholic drink. */
    public void drink(AlcoholType type, ServerPlayer player) {
        this.units += type.unitsPerDrink();
        applyNausea(player);
    }

    public float getUnits() {
        return this.units;
    }

    public void copyFrom(Intoxication other) {
        this.units = other.units;
    }

    /** Called every server tick for the owning player. */
    public void tick(ServerPlayer player) {
        if (this.blackoutRemaining > 0) {
            tickBlackout(player);
            return;
        }

        if (this.units <= 0.0F)
            return;

        this.units = Math.max(0.0F, this.units - 1.0F / EKConfig.TICKS_PER_UNIT_DECAY.get());

        if (player.tickCount % EFFECT_CHECK_INTERVAL != 0)
            return;

        applyNausea(player);
        rollBlackout(player);
    }

    private void applyNausea(ServerPlayer player) {
        double threshold = EKConfig.NAUSEA_THRESHOLD.get();
        if (this.units <= threshold)
            return;

        int amplifier = (int) ((this.units - threshold) / EKConfig.UNITS_PER_NAUSEA_LEVEL.get());
        amplifier = Math.min(amplifier, EKConfig.MAX_NAUSEA_AMPLIFIER.get());
        // Keep the effect alive a little past the next check so it never flickers off.
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, EFFECT_CHECK_INTERVAL + 80, amplifier));
    }

    private void rollBlackout(ServerPlayer player) {
        double threshold = EKConfig.BLACKOUT_THRESHOLD.get();
        if (this.units < threshold)
            return;

        double chance = Math.min(0.75D, (this.units - threshold + 1.0D) * EKConfig.BLACKOUT_CHANCE_PER_UNIT.get());
        if (player.getRandom().nextDouble() < chance)
            startBlackout(player);
    }

    /** Begins the blackout cutscene and tells the client to start its fade. */
    private void startBlackout(ServerPlayer player) {
        this.blackoutRemaining = BlackoutSequence.TOTAL_TICKS;
        ModNetworking.sendToPlayer(new BlackoutPacket(BlackoutSequence.TOTAL_TICKS), player);
    }

    /** Runs one tick of the blackout cutscene: hold the player still, teleport mid-fade, then release. */
    private void tickBlackout(ServerPlayer player) {
        // Freeze in place server-side; the client blocks its own input for the same window.
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        player.hurtMarked = true;

        this.blackoutRemaining--;
        int elapsed = BlackoutSequence.TOTAL_TICKS - this.blackoutRemaining;

        if (elapsed == BlackoutSequence.TELEPORT_TICK) {
            teleportRandomly(player);
            // Blacking out sobers the player up completely, clearing nausea with it.
            this.units = 0.0F;
            player.removeEffect(MobEffects.CONFUSION);
            player.displayClientMessage(Component.translatable("message.edisonskeg.blackout"), true);
        }
    }

    /** Teleports the player a random multi-chunk distance to a safe surface spot nearby. */
    private void teleportRandomly(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        RandomSource random = player.getRandom();
        int min = EKConfig.BLACKOUT_MIN_DISTANCE.get();
        int max = Math.max(min, EKConfig.BLACKOUT_MAX_DISTANCE.get());

        for (int attempt = 0; attempt < TELEPORT_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = min + random.nextDouble() * (max - min);
            int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
            // getHeight loads/generates the target chunk and returns the first open Y above the surface.
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y <= level.getMinBuildHeight())
                continue;

            BlockState ground = level.getBlockState(new BlockPos(x, y - 1, z));
            // Reject open air (no floor) and liquid surfaces (water/lava).
            if (ground.isAir() || !ground.getFluidState().isEmpty())
                continue;

            player.teleportTo(level, x + 0.5D, y, z + 0.5D, player.getYRot(), player.getXRot());
            return;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat(TAG_UNITS, this.units);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.units = tag.getFloat(TAG_UNITS);
    }
}
