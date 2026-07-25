package org.fuzedaze.edisonskeg.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.fuzedaze.edisonskeg.EdisonsKeg;

/**
 * Client-only handlers that drive the blackout cutscene: advancing the fade timer, drawing
 * the black overlay, hiding the HUD, and locking the player out of moving or interacting
 * while it plays. The {@code Dist.CLIENT} value keeps this class off dedicated servers.
 */
@Mod.EventBusSubscriber(modid = EdisonsKeg.MODID, value = Dist.CLIENT)
public final class BlackoutClientEvents {

    /** HUD pieces hidden for the duration of a blackout. */
    private static final ResourceLocation[] HIDDEN_OVERLAYS = {
            VanillaGuiOverlay.HOTBAR.id(),
            VanillaGuiOverlay.ITEM_NAME.id(),
            VanillaGuiOverlay.CHAT_PANEL.id(),
    };

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

    /** Keeps the hotbar, held-item name, and chat off screen while blacked out. */
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (!BlackoutClient.isActive())
            return;

        ResourceLocation overlay = event.getOverlay().id();
        for (ResourceLocation hidden : HIDDEN_OVERLAYS) {
            if (hidden.equals(overlay)) {
                event.setCanceled(true);
                return;
            }
        }
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

    // Right-clicking is dead while blacked out. The server enforces this as well; cancelling
    // client-side too stops the local player mispredicting a use that never happens.

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        cancelIfBlackedOut(event);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        cancelIfBlackedOut(event);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        cancelIfBlackedOut(event);
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        cancelIfBlackedOut(event);
    }

    private static void cancelIfBlackedOut(Event event) {
        if (BlackoutClient.isActive())
            event.setCanceled(true);
    }

    private BlackoutClientEvents() {
    }
}
