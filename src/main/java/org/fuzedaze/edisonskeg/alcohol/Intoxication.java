package org.fuzedaze.edisonskeg.alcohol;

import java.util.Locale;
import javax.annotation.Nullable;
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
 * How drunk one player is, and how well they hold it.
 *
 * <p>Three pieces of state, each with a different lifetime:
 * <ul>
 *   <li><b>units</b> — current intoxication. Rises with every drink, decays over time, and
 *       is wiped by a blackout or by dying.</li>
 *   <li><b>lastDrink</b> — the beverage most recently drunk. Its {@link AlcoholType}
 *       profile decides where nausea and blackouts kick in, so what you are drinking now
 *       determines how it hits you.</li>
 *   <li><b>blackouts</b> — lifetime blackout count, i.e. <b>tolerance</b>. Never decays and
 *       survives death; each blackout pushes both thresholds further out.</li>
 * </ul>
 *
 * <p>To change how a drink behaves, edit its profile in {@link AlcoholTypes} — not this
 * class. This class only decides <em>when</em> those numbers apply.
 */
public class Intoxication implements INBTSerializable<CompoundTag> {
    /** How often nausea is refreshed and blackouts are rolled, in ticks. */
    public static final int EFFECT_CHECK_INTERVAL = 40;
    private static final int TELEPORT_ATTEMPTS = 24;
    private static final double MAX_BLACKOUT_CHANCE = 0.75D;

    private static final String TAG_UNITS = "AlcoholUnits";
    private static final String TAG_BLACKOUTS = "Blackouts";
    private static final String TAG_LAST_DRINK = "LastDrink";

    private float units;
    private int blackouts;
    @Nullable
    private AlcoholType lastDrink;
    private int blackoutTicksLeft;

    // ------------------------------------------------------------------ drinking

    /** Called when a player finishes a drink. */
    public void drink(AlcoholType type, ServerPlayer player) {
        this.lastDrink = type;
        this.units += type.unitsPerDrink();
        applyNausea(player);
    }

    // ------------------------------------------------------------------ state

    /** Current intoxication, in alcohol units (one beer = 1.0). */
    public float getUnits() {
        return this.units;
    }

    /** Lifetime blackouts, which is this player's tolerance level. */
    public int getBlackouts() {
        return this.blackouts;
    }

    /**
     * How much further out this player's thresholds sit compared to someone who has never
     * blacked out. 1.0 means no tolerance; 1.5 means it takes 50% more to feel it. Always
     * within the configured cap, however many blackouts have piled up.
     */
    public double getToleranceMultiplier() {
        double raw = 1.0D + this.blackouts * EKConfig.TOLERANCE_GAIN_PER_BLACKOUT.get();
        return Math.min(raw, EKConfig.MAX_TOLERANCE_MULTIPLIER.get());
    }

    /** Tolerance as shown to players, e.g. {@code "1.45x"} — the capped multiplier, not the raw count. */
    public String getToleranceDisplay() {
        return String.format(Locale.ROOT, "%.2fx", getToleranceMultiplier());
    }

    /** True while the blackout cutscene is playing, during which the player is frozen. */
    public boolean isBlackedOut() {
        return this.blackoutTicksLeft > 0;
    }

    /** The profile currently governing this player, or beer before they've drunk anything. */
    private AlcoholType profile() {
        return this.lastDrink != null ? this.lastDrink : AlcoholTypes.BEER;
    }

    /** Units this player needs to exceed before nausea starts, tolerance included. */
    public double nauseaThreshold() {
        return profile().nauseaThresholdUnits() * getToleranceMultiplier();
    }

    /** Units this player needs before blackouts become possible, tolerance included. */
    public double blackoutThreshold() {
        return profile().blackoutThresholdUnits() * getToleranceMultiplier();
    }

    /** Carries everything over — used when the same player changes dimension. */
    public void copyFrom(Intoxication other) {
        this.units = other.units;
        this.blackouts = other.blackouts;
        this.lastDrink = other.lastDrink;
    }

    /** Carries only tolerance over — used on respawn, since dying sobers you up. */
    public void copyToleranceFrom(Intoxication other) {
        this.blackouts = other.blackouts;
    }

    // ------------------------------------------------------------------ ticking

    /** Called every server tick for the owning player. */
    public void tick(ServerPlayer player) {
        if (this.blackoutTicksLeft > 0) {
            tickBlackout(player);
            return;
        }

        if (this.units <= 0.0F)
            return;

        soberUp();

        if (player.tickCount % EFFECT_CHECK_INTERVAL == 0) {
            applyNausea(player);
            rollBlackout(player);
        }
    }

    private void soberUp() {
        this.units = Math.max(0.0F, this.units - 1.0F / EKConfig.TICKS_PER_UNIT_DECAY.get());
    }

    // ------------------------------------------------------------------ nausea

    private void applyNausea(ServerPlayer player) {
        double threshold = nauseaThreshold();
        if (this.units <= threshold)
            return;

        AlcoholType profile = profile();
        double overshoot = this.units - threshold;
        int amplifier = (int) (overshoot / (profile.unitsPerNauseaLevel() * getToleranceMultiplier()));
        amplifier = Math.min(amplifier, profile.maxNauseaAmplifier());

        // Outlast the next check so the effect never visibly flickers off.
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, EFFECT_CHECK_INTERVAL + 80, amplifier));
    }

    // ------------------------------------------------------------------ blackouts

    private void rollBlackout(ServerPlayer player) {
        double threshold = blackoutThreshold();
        if (this.units < threshold)
            return;

        double overshoot = this.units - threshold + 1.0D;
        double chance = Math.min(MAX_BLACKOUT_CHANCE, overshoot * profile().blackoutChancePerUnit());
        if (player.getRandom().nextDouble() < chance)
            startBlackout(player);
    }

    /** Begins the blackout cutscene and tells the client to start its fade. */
    private void startBlackout(ServerPlayer player) {
        this.blackoutTicksLeft = BlackoutSequence.TOTAL_TICKS;
        ModNetworking.sendToPlayer(new BlackoutPacket(BlackoutSequence.TOTAL_TICKS), player);
    }

    /** One tick of the cutscene: hold the player still, teleport mid-fade, then release. */
    private void tickBlackout(ServerPlayer player) {
        // Freeze server-side; the client blocks its own input for the same window.
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        player.hurtMarked = true;

        this.blackoutTicksLeft--;
        int elapsed = BlackoutSequence.TOTAL_TICKS - this.blackoutTicksLeft;
        if (elapsed == BlackoutSequence.TELEPORT_TICK)
            wakeUpElsewhere(player);
    }

    /** The moment the screen is fully black: move the player, sober them up, toughen them up. */
    private void wakeUpElsewhere(ServerPlayer player) {
        teleportRandomly(player);

        this.units = 0.0F;
        player.removeEffect(MobEffects.CONFUSION);
        this.blackouts++;

        player.displayClientMessage(
                Component.translatable("message.edisonskeg.blackout", getToleranceDisplay()), true);
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
            // Loads/generates the target chunk and gives the first open Y above the surface.
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y <= level.getMinBuildHeight())
                continue;

            BlockState ground = level.getBlockState(new BlockPos(x, y - 1, z));
            // Reject open air (nothing to stand on) and liquid surfaces.
            if (ground.isAir() || !ground.getFluidState().isEmpty())
                continue;

            player.teleportTo(level, x + 0.5D, y, z + 0.5D, player.getYRot(), player.getXRot());
            return;
        }
    }

    // ------------------------------------------------------------------ persistence

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat(TAG_UNITS, this.units);
        tag.putInt(TAG_BLACKOUTS, this.blackouts);
        if (this.lastDrink != null)
            tag.putString(TAG_LAST_DRINK, this.lastDrink.id());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.units = tag.getFloat(TAG_UNITS);
        this.blackouts = tag.getInt(TAG_BLACKOUTS);
        // Resolves to null if the drink was removed from the mod since this player last played.
        this.lastDrink = tag.contains(TAG_LAST_DRINK) ? AlcoholTypes.byId(tag.getString(TAG_LAST_DRINK)) : null;
    }
}
