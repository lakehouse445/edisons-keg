package org.fuzedaze.edisonskeg.alcohol;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.fuzedaze.edisonskeg.EdisonsKeg;

@Mod.EventBusSubscriber(modid = EdisonsKeg.MODID)
public final class IntoxicationEvents {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player)
            event.addCapability(IntoxicationProvider.ID, new IntoxicationProvider());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player))
            return;

        player.getCapability(IntoxicationProvider.INTOXICATION).ifPresent(intoxication -> intoxication.tick(player));
    }

    // A blacked-out player is unconscious: no drinking, placing, or using anything until
    // they come round. The client cancels these too so nothing is mispredicted locally.
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        cancelIfBlackedOut(event, event.getEntity());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        cancelIfBlackedOut(event, event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        cancelIfBlackedOut(event, event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        cancelIfBlackedOut(event, event.getEntity());
    }

    private static void cancelIfBlackedOut(Event event, Player player) {
        if (player.level().isClientSide)
            return;

        player.getCapability(IntoxicationProvider.INTOXICATION).ifPresent(intoxication -> {
            if (intoxication.isBlackedOut())
                event.setCanceled(true);
        });
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        boolean died = event.isWasDeath();

        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(IntoxicationProvider.INTOXICATION).ifPresent(oldIntoxication ->
                event.getEntity().getCapability(IntoxicationProvider.INTOXICATION).ifPresent(newIntoxication -> {
                    // Dying sobers you up, but you don't forget how to hold your drink:
                    // tolerance is permanent, current intoxication is not.
                    if (died)
                        newIntoxication.copyToleranceFrom(oldIntoxication);
                    else
                        newIntoxication.copyFrom(oldIntoxication);
                }));
        event.getOriginal().invalidateCaps();
    }

    @Mod.EventBusSubscriber(modid = EdisonsKeg.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(Intoxication.class);
        }
    }

    private IntoxicationEvents() {
    }
}
