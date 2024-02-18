package nl.enjarai.doahackedroll.net;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import nl.enjarai.doahackedroll.DoAHackedRoll;
import nl.enjarai.doahackedroll.config.LimitedModConfigServer;
import nl.enjarai.doahackedroll.config.ModConfigServer;
import nl.enjarai.doahackedroll.util.DelayedRunnable;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public class HandshakeServer {
    // Protocol version 1:
    // | Protocol version (int) | Config data (string) |
    // Protocol version 2:
    // | Protocol version (int) | Limited/full config data (string) | Is limited (boolean) |
    // Protocol version 3:
    // | Protocol version (int) | Limited config data (string) | Is limited (boolean) | [Full config data (string)] (only if not limited) |
    public static final int PROTOCOL_VERSION = 3;

    private final ServerConfigHolder<ModConfigServer> configHolder;
    private final Map<ServerPlayNetworkHandler, ClientInfo> syncStates = new WeakHashMap<>();
    private final Map<ServerPlayNetworkHandler, DelayedRunnable> scheduledKicks = new WeakHashMap<>();
    private final Function<ServerPlayNetworkHandler, Boolean> getsLimitedCheck;
    private final Codec<ModConfigServer> transferCodec = ModConfigServer.CODEC;
    private final Codec<LimitedModConfigServer> limitedTransferCodec = LimitedModConfigServer.getCodec();

    public HandshakeServer(ServerConfigHolder<ModConfigServer> configHolder, Function<ServerPlayNetworkHandler, Boolean> getsLimitedCheck) {
        this.configHolder = configHolder;
        this.getsLimitedCheck = getsLimitedCheck;
    }

    public void tick(MinecraftServer server) {
        var it = scheduledKicks.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            if (entry.getValue().isDone()) {
                it.remove();
            } else {
                entry.getValue().tick();
            }
        }
    }

    public ClientInfo getHandshakeState(ServerPlayerEntity player) {
        return getHandshakeState(player.networkHandler);
    }

    public ClientInfo getHandshakeState(ServerPlayNetworkHandler handler) {
        return syncStates.computeIfAbsent(handler, key -> new ClientInfo(HandshakeState.NOT_SENT, PROTOCOL_VERSION, true));
    }

    public PacketByteBuf getConfigSyncBuf(ServerPlayNetworkHandler handler) {
        return getConfigSyncBuf(handler, getHandshakeState(handler).protocolVersion);
    }

    @SuppressWarnings("NonStrictComparisonCanBeEquality")
    public PacketByteBuf getConfigSyncBuf(ServerPlayNetworkHandler handler, int protocolVersion) {
        protocolVersion = Math.min(protocolVersion, PROTOCOL_VERSION);
        var buf = new PacketByteBuf(Unpooled.buffer());

        // Protocol version
        buf.writeInt(protocolVersion);

        // Config data
        var isLimited = getsLimitedCheck.apply(handler);
        getHandshakeState(handler).isLimited = isLimited;
        var config = configHolder.instance;
        DataResult<JsonElement> data;
        if (protocolVersion == 2) {
            Codec<? super ModConfigServer> codec = isLimited ? limitedTransferCodec : transferCodec;
            data = codec.encodeStart(JsonOps.INSTANCE, config);
        } else {
            data = limitedTransferCodec.encodeStart(JsonOps.INSTANCE, config.getLimited(handler));
        }
        try {
            buf.writeString(data.getOrThrow(false, DoAHackedRoll.LOGGER::error).toString());
        } catch (RuntimeException e) {
            DoAHackedRoll.LOGGER.error("Failed to encode config", e);
            buf.writeString("{}");
        }

        if (protocolVersion >= 2) {
            // Limited status
            buf.writeBoolean(isLimited);
        }

        if (protocolVersion >= 3 && !isLimited) {
            // Operator modifiable config
            var data2 = transferCodec.encodeStart(JsonOps.INSTANCE, config);
            try {
                buf.writeString(data2.getOrThrow(false, DoAHackedRoll.LOGGER::error).toString());
            } catch (RuntimeException e) {
                DoAHackedRoll.LOGGER.error("Failed to encode config", e);
                buf.writeString("{}");
            }
        }

        return buf;
    }

    public void configSentToClient(ServerPlayNetworkHandler handler) {
        getHandshakeState(handler).state = HandshakeState.SENT;

        var config = configHolder.instance;
        if (config.getSyncTimeout() != null) {
            scheduledKicks.put(handler, new DelayedRunnable(config.getSyncTimeout(), () -> {
                if (getHandshakeState(handler).state != HandshakeState.ACCEPTED) {
                    DoAHackedRoll.LOGGER.warn(
                            "{} did not accept config syncing, config indicates we kick them.",
                            handler.getPlayer().getName().getString()
                    );
                    handler.disconnect(config.getSyncTimeoutMessage());
                }
            }));
        }
    }

    public HandshakeState clientReplied(ServerPlayNetworkHandler handler, PacketByteBuf buf) {
        var info = getHandshakeState(handler);
        var player = handler.getPlayer();

        if (info.state == HandshakeState.SENT) {
            try {
                var protocolVersion = buf.readInt();
                if (protocolVersion < 1 || protocolVersion > PROTOCOL_VERSION) {
                    DoAHackedRoll.LOGGER.warn(
                            "Client of {} sent unknown protocol version, expected range 1-{}, got {}. Will attempt to proceed anyway.",
                            player.getName().getString(),
                            PROTOCOL_VERSION,
                            protocolVersion
                    );
                }

                if (protocolVersion == 2 && info.protocolVersion != 2) {
                    DoAHackedRoll.LOGGER.info("Client of {} is using an older protocol version, resending.", player.getName().getString());
                    info.state = HandshakeState.RESEND;
                } else if (buf.readBoolean()) {
                    DoAHackedRoll.LOGGER.info("Client of {} accepted server config.", player.getName().getString());
                    info.state = HandshakeState.ACCEPTED;
                } else {
                    DoAHackedRoll.LOGGER.warn(
                            "Client of {} failed to process server config, check client logs find what went wrong.",
                            player.getName().getString());
                    info.state = HandshakeState.FAILED;
                }
                info.protocolVersion = protocolVersion;
            } catch (IndexOutOfBoundsException e) {
                DoAHackedRoll.LOGGER.warn(
                        "Client of {} sent invalid config reply.",
                        player.getName().getString()
                );
                info.state = HandshakeState.FAILED;
            }
        }

        return info.state;
    }

    public void playerDisconnected(ServerPlayNetworkHandler handler) {
        syncStates.remove(handler);
    }

    public static class ClientInfo {
        public HandshakeState state;
        public int protocolVersion;
        public boolean isLimited;

        public ClientInfo(HandshakeState state, int protocolVersion, boolean isLimited) {
            this.state = state;
            this.protocolVersion = protocolVersion;
            this.isLimited = isLimited;
        }
    }

    public enum HandshakeState {
        NOT_SENT,
        SENT,
        ACCEPTED,
        FAILED,
        RESEND
    }
}
