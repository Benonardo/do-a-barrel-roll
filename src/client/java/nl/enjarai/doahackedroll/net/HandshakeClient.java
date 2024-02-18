package nl.enjarai.doahackedroll.net;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import nl.enjarai.doahackedroll.DoAHackedRoll;

import java.util.Optional;
import java.util.function.Consumer;

public class HandshakeClient<L, F extends L> {
    public static final int PROTOCOL_VERSION = 3;

    private final Codec<F> transferCodec;
    private final Codec<L> limitedTransferCodec;
    private final Consumer<L> updateCallback;
    private L serverConfig = null;
    private F fullServerConfig = null;
    private boolean hasConnected = false;

    public HandshakeClient(Codec<F> transferCodec, Codec<L> limitedTransferCodec, Consumer<L> updateCallback) {
        this.transferCodec = transferCodec;
        this.limitedTransferCodec = limitedTransferCodec;
        this.updateCallback = updateCallback;
    }

    /**
     * Returns the server config if the client has received one for this server,
     * returns an empty optional in any other case.
     */
    public Optional<L> getConfig() {
        return Optional.ofNullable(serverConfig);
    }

    public Optional<F> getFullConfig() {
        return Optional.ofNullable(fullServerConfig);
    }

//    public void setConfig(@Nullable L config) {
//        serverConfig = config;
//        updateCallback.accept(serverConfig);
//        hasConnected = serverConfig != null;
//    }

    public boolean hasConnected() {
        return hasConnected;
    }

    public PacketByteBuf handleConfigSync(PacketByteBuf buf) {
        serverConfig = null;
        fullServerConfig = null;

        try {
            var protocolVersion = buf.readInt();
            if (protocolVersion < 1 || protocolVersion > PROTOCOL_VERSION) {
                DoAHackedRoll.LOGGER.warn("Received config with unknown protocol version: {}, will attempt to load anyway", protocolVersion);
            }

            var data = buf.readString();
            var isLimited = true;

            if (protocolVersion >= 2) {
                isLimited = buf.readBoolean();
            }

            if (protocolVersion == 2) {
                var codec = isLimited ? limitedTransferCodec : transferCodec;
                serverConfig = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(data))
                        .getOrThrow(false, DoAHackedRoll.LOGGER::error);
                if (!isLimited) {
                    //noinspection unchecked
                    fullServerConfig = (F) serverConfig;
                }
            } else {
                serverConfig = limitedTransferCodec.parse(JsonOps.INSTANCE, JsonParser.parseString(data))
                        .getOrThrow(false, DoAHackedRoll.LOGGER::error);
                if (!isLimited) {
                    var data2 = buf.readString();

                    fullServerConfig = transferCodec.parse(JsonOps.INSTANCE, JsonParser.parseString(data2))
                            .getOrThrow(false, DoAHackedRoll.LOGGER::error);
                }
            }
        } catch (RuntimeException e) {
            DoAHackedRoll.LOGGER.error("Failed to parse config from server", e);
        }

        if (serverConfig != null) {
            updateCallback.accept(serverConfig);
            hasConnected = true;
            DoAHackedRoll.LOGGER.info("Received config from server");
        }

        var returnBuf = new PacketByteBuf(Unpooled.buffer());
        returnBuf.writeInt(PROTOCOL_VERSION);
        returnBuf.writeBoolean(serverConfig != null);
        return returnBuf;
    }

    public void reset() {
        serverConfig = null;
        hasConnected = false;
    }
}
