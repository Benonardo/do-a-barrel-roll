package nl.enjarai.doahackedroll.fabric.net;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import nl.enjarai.doahackedroll.DoAHackedRoll;
import nl.enjarai.doahackedroll.api.event.ServerEvents;
import nl.enjarai.doahackedroll.net.HandshakeServer;
import nl.enjarai.doahackedroll.net.RollSyncServer;

public class HandshakeServerFabric {
    public static void init() {
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.registerReceiver(handler, DoAHackedRoll.HANDSHAKE_CHANNEL, (server1, player, handler1, buf, responseSender) -> {
                var reply = DoAHackedRoll.HANDSHAKE_SERVER.clientReplied(handler1, buf);
                if (reply == HandshakeServer.HandshakeState.ACCEPTED) {
                    RollSyncServer.startListening(handler1);
                    ServerConfigUpdaterFabric.startListening(handler1);
                } else if (reply == HandshakeServer.HandshakeState.RESEND) {
                    // Resending can happen when the client has a different protocol version than expected.
                    sendHandshake(player);
                }
            });

            // The initial handshake is sent in the CommandManagerMixin.
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            DoAHackedRoll.HANDSHAKE_SERVER.playerDisconnected(handler);
        });

        ServerTickEvents.END_SERVER_TICK.register(DoAHackedRoll.HANDSHAKE_SERVER::tick);

        ServerEvents.SERVER_CONFIG_UPDATE.register((server, config) -> {
            for (var player : server.getPlayerManager().getPlayerList()) {
                sendHandshake(player);
            }
        });
    }

    public static void sendHandshake(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, DoAHackedRoll.HANDSHAKE_CHANNEL,
                DoAHackedRoll.HANDSHAKE_SERVER.getConfigSyncBuf(player.networkHandler));

        DoAHackedRoll.HANDSHAKE_SERVER.configSentToClient(player.networkHandler);
    }
}
