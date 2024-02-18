package nl.enjarai.doahackedroll.fabric.net;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import nl.enjarai.doahackedroll.DoAHackedRoll;
import nl.enjarai.doahackedroll.DoABarrelRollClient;
import nl.enjarai.doahackedroll.net.RollSyncClient;

public class HandshakeClientFabric {
    public static void init() {
        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            ClientPlayNetworking.registerReceiver(DoAHackedRoll.HANDSHAKE_CHANNEL, (client1, handler1, buf, responseSender) -> {
                var returnBuf = DoABarrelRollClient.HANDSHAKE_CLIENT.handleConfigSync(buf);
                responseSender.sendPacket(DoAHackedRoll.HANDSHAKE_CHANNEL, returnBuf);

                if (DoABarrelRollClient.HANDSHAKE_CLIENT.hasConnected()) {
                    RollSyncClient.startListening();
                    ServerConfigUpdateClientFabric.startListening();
                }
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            DoABarrelRollClient.HANDSHAKE_CLIENT.reset();
        });
    }
}
