package org.fuzedaze.edisonskeg.alcohol;

/**
 * Shared timing for the blackout cutscene, referenced by both the server-side
 * sequence in {@link Intoxication} and the client-side fade overlay. Measured in ticks
 * (20 ticks = 1 second).
 *
 * <p>The screen fades to black over {@link #FADE_IN_TICKS}, holds fully black for
 * {@link #HOLD_TICKS} while the teleport happens at {@link #TELEPORT_TICK}, then fades
 * back in over {@link #FADE_OUT_TICKS}. The player stays frozen for the whole
 * {@link #TOTAL_TICKS}.
 */
public final class BlackoutSequence {
    public static final int FADE_IN_TICKS = 25;
    public static final int HOLD_TICKS = 20;
    public static final int FADE_OUT_TICKS = 25;
    public static final int TOTAL_TICKS = FADE_IN_TICKS + HOLD_TICKS + FADE_OUT_TICKS;

    /** Ticks elapsed at which the teleport fires, halfway through the fully-black hold. */
    public static final int TELEPORT_TICK = FADE_IN_TICKS + HOLD_TICKS / 2;

    private BlackoutSequence() {
    }
}
