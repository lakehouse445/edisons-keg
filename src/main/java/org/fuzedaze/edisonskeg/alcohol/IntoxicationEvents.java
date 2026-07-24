package org.fuzedaze.edisonskeg.alcohol;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
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

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Dying sobers you up; only carry intoxication across dimension changes.
        if (event.isWasDeath())
            return;

        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(IntoxicationProvider.INTOXICATION).ifPresent(oldIntoxication ->
                event.getEntity().getCapability(IntoxicationProvider.INTOXICATION)
                        .ifPresent(newIntoxication -> newIntoxication.copyFrom(oldIntoxication)));
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
