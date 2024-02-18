package nl.enjarai.doahackedroll.fabric.net;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import nl.enjarai.doahackedroll.DoAHackedRoll;

public class ServerConfigUpdaterFabric {
    public static void startListening(ServerPlayNetworkHandler handler) {
        ServerPlayNetworking.registerReceiver(handler, DoAHackedRoll.SERVER_CONFIG_UPDATE_CHANNEL, (server, player, handler1, buf, responseSender) -> {
            responseSender.sendPacket(
                    DoAHackedRoll.SERVER_CONFIG_UPDATE_CHANNEL,
                    DoAHackedRoll.CONFIG_HOLDER.clientSendsUpdate(player, buf)
            );
        });
    }
}
