package nl.enjarai.doahackedroll.net;

import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import nl.enjarai.doahackedroll.DoAHackedRoll;
import nl.enjarai.doahackedroll.config.ModConfigServer;
import nl.enjarai.doahackedroll.util.ToastUtil;

public class ServerConfigUpdateClient {
    private static boolean waitingForAck = false;

    public static void sendUpdate(ModConfigServer config) {
        var buf = PacketByteBufs.create();

        // Protocol version
        buf.writeInt(1);

        // Config
        try {
            var data = ModConfigServer.CODEC.encodeStart(JsonOps.INSTANCE, config)
                    .getOrThrow(false, DoAHackedRoll.LOGGER::warn).toString();
            buf.writeString(data);
        } catch (RuntimeException e) {
            DoAHackedRoll.LOGGER.warn("Failed to send server config update to server: ", e);
        }

        ClientPlayNetworking.send(DoAHackedRoll.SERVER_CONFIG_UPDATE_CHANNEL, buf);
        waitingForAck = true;
    }

    public static void updateAcknowledged(PacketByteBuf buf) {
        if (waitingForAck) {
            waitingForAck = false;

            try {
                var protocolVersion = buf.readInt();
                if (protocolVersion != 1) {
                    DoAHackedRoll.LOGGER.warn("Received config update ack with unknown protocol version: {}, will attempt to read anyway", protocolVersion);
                }

                var success = buf.readBoolean();
                if (success) {
                    // I don't think we need this
//                    var data = ModConfigServer.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(buf.readString()))
//                            .getOrThrow(false, DoABarrelRoll.LOGGER::warn).getFirst();
//                    DoABarrelRollClient.HANDSHAKE_CLIENT.setConfig(data);

                    ToastUtil.toasty("server_config_updated");
                } else {
                    ToastUtil.toasty("server_config_update_failed");
                }
            } catch (RuntimeException e) {
                DoAHackedRoll.LOGGER.warn("Failed to read config update ack from server: ", e);
            }
        }
    }
}
