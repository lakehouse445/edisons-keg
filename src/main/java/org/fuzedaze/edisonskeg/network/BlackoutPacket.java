package org.fuzedaze.edisonskeg.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.fuzedaze.edisonskeg.client.BlackoutClient;

/**
 * Server -> client signal that a blackout cutscene should begin, carrying its total
 * duration so the client fade stays in step with the server-side sequence.
 */
public class BlackoutPacket {
    private final int durationTicks;

    public BlackoutPacket(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    public static void encode(BlackoutPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.durationTicks);
    }

    public static BlackoutPacket decode(FriendlyByteBuf buf) {
        return new BlackoutPacket(buf.readVarInt());
    }

    public static void handle(BlackoutPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
                // Only ever runs on the physical client; guarded so the server never loads client classes.
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BlackoutClient.start(packet.durationTicks)));
        context.setPacketHandled(true);
    }
}
