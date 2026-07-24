package org.fuzedaze.edisonskeg.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.fuzedaze.edisonskeg.EdisonsKeg;

/**
 * Client-only handlers that drive the blackout cutscene: advancing the fade timer,
 * drawing the black overlay, and locking player movement while it plays. The
 * {@code Dist.CLIENT} value keeps this class from loading on a dedicated server.
 */
@Mod.EventBusSubscriber(modid = EdisonsKeg.MODID, value = Dist.CLIENT)
public final class BlackoutClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END)
            BlackoutClient.clientTick();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!BlackoutClient.isActive())
            return;

        float alpha = BlackoutClient.overlayAlpha(event.getPartialTick());
        if (alpha <= 0.0F)
            return;

        int color = ((int) (alpha * 255.0F) & 0xFF) << 24;
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), color);
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!BlackoutClient.isActive())
            return;

        // Freeze the player: zero every movement input for the duration of the blackout.
        Input input = event.getInput();
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

    private BlackoutClientEvents() {
    }
}
