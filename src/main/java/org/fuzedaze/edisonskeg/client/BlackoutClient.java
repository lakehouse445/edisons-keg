package org.fuzedaze.edisonskeg.client;

import net.minecraft.util.Mth;
import org.fuzedaze.edisonskeg.alcohol.BlackoutSequence;

/**
 * Client-side state for the blackout fade. Loaded only on the physical client
 * (reached via {@code DistExecutor} from the packet handler and by
 * {@link BlackoutClientEvents}).
 */
public final class BlackoutClient {
    private static int totalTicks;
    private static int ticksRemaining;

    public static void start(int total) {
        totalTicks = total;
        ticksRemaining = total;
    }

    public static boolean isActive() {
        return ticksRemaining > 0;
    }

    /** Advances the fade by one tick; called at the end of each client tick. */
    public static void clientTick() {
        if (ticksRemaining > 0)
            ticksRemaining--;
    }

    /**
     * Black overlay opacity for the current frame, from 0 (clear) to 1 (fully black),
     * ramping up over the fade-in, holding through the black period, and easing back out.
     *
     * @param partialTick fractional progress into the current tick, for smooth fading
     */
    public static float overlayAlpha(float partialTick) {
        if (ticksRemaining <= 0)
            return 0.0F;

        float elapsed = (totalTicks - ticksRemaining) + partialTick;
        float alpha;
        if (elapsed < BlackoutSequence.FADE_IN_TICKS) {
            alpha = elapsed / BlackoutSequence.FADE_IN_TICKS;
        } else if (elapsed < BlackoutSequence.FADE_IN_TICKS + BlackoutSequence.HOLD_TICKS) {
            alpha = 1.0F;
        } else {
            float fadeOutElapsed = elapsed - BlackoutSequence.FADE_IN_TICKS - BlackoutSequence.HOLD_TICKS;
            alpha = 1.0F - fadeOutElapsed / BlackoutSequence.FADE_OUT_TICKS;
        }
        return Mth.clamp(alpha, 0.0F, 1.0F);
    }

    private BlackoutClient() {
    }
}
