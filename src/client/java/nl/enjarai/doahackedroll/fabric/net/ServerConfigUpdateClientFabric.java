package nl.enjarai.doahackedroll.fabric.net;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import nl.enjarai.doahackedroll.DoAHackedRoll;
import nl.enjarai.doahackedroll.net.ServerConfigUpdateClient;

public class ServerConfigUpdateClientFabric {
    public static void startListening() {
        ClientPlayNetworking.registerReceiver(DoAHackedRoll.SERVER_CONFIG_UPDATE_CHANNEL, (client, handler, buf, responseSender) -> {
            ServerConfigUpdateClient.updateAcknowledged(buf);
        });
    }
}
