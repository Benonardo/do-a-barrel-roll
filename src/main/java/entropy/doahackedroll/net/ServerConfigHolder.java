package nl.enjarai.doahackedroll.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import nl.enjarai.doahackedroll.DoAHackedRoll;
import nl.enjarai.doahackedroll.config.ModConfigServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.BiConsumer;

public class ServerConfigHolder<T extends ValidatableConfig> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public final Path configFile;
    public final Codec<T> codec;
    public final T defaultConfig;
    private BiConsumer<MinecraftServer, T> updateCallback;
    public T instance;

    public ServerConfigHolder(Path configFile, Codec<T> codec, T defaultConfig, BiConsumer<MinecraftServer, T> updateCallback) {
        this.configFile = configFile;
        this.codec = codec;
        this.defaultConfig = defaultConfig;
        this.updateCallback = updateCallback;

        load();
    }

    public void load() {
        T config = null;

        if (Files.exists(configFile)) {
            // An existing config is present, we should use its values
            try (BufferedReader fileReader = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(configFile), StandardCharsets.UTF_8)
            )) {
                // Parses the config file and puts the values into config object
                config = codec.decode(JsonOps.INSTANCE, JsonParser.parseReader(fileReader))
                        .getOrThrow(false, e -> {
                            throw new RuntimeException(e);
                        })
                        .getFirst();
            } catch (IOException | RuntimeException e) {
                DoAHackedRoll.LOGGER.error("Failed to parse server config file, regenerating: ", e);
            }
        }
        // config will be null if the file doesn't exist or if it failed to parse
        if (config == null || !config.isValid()) {
            config = defaultConfig;
        }

        instance = config;
        // Saves the file in order to write new fields if they were added
        save();
    }

    public void save() {
        try {
            // Creates the file if it doesn't exist
            Files.createDirectories(configFile.getParent());
            // Writes the config to the file
            Files.writeString(configFile, GSON.toJson(codec.encodeStart(JsonOps.INSTANCE, instance)
                    .getOrThrow(false, e -> {
                        throw new RuntimeException(e);
                    })), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException | RuntimeException e) {
            DoAHackedRoll.LOGGER.error("Failed to save server config file: ", e);
        }
    }

    public PacketByteBuf clientSendsUpdate(ServerPlayerEntity player, PacketByteBuf buf) {
        var info = DoAHackedRoll.HANDSHAKE_SERVER.getHandshakeState(player);
        var accepted = info.state == HandshakeServer.HandshakeState.ACCEPTED;
        var hasPermission = ModConfigServer.canModify(player.networkHandler);

        // Only players that have accepted the handshake and have permission can update the config
        if (!accepted || !hasPermission) {
            DoAHackedRoll.LOGGER.warn(
                    "Client of {} tried to update the server config, but is not allowed to. Rejecting.",
                    player.getName().getString()
            );
            return getFailureBuf();
        }

        try {
            var protocolVersion = buf.readInt();
            if (protocolVersion != 1) {
                DoAHackedRoll.LOGGER.warn(
                        "Client of {} sent unknown protocol version for server config update, expected 1, got {}. Will attempt to proceed anyway.",
                        player.getName().getString(),
                        protocolVersion
                );
            }

            var data = buf.readString();
            var newConfig = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(data))
                    .getOrThrow(false, DoAHackedRoll.LOGGER::error);

            if (!newConfig.isValid()) {
                throw new RuntimeException("Config arrived, but contains invalid values");
            }

            // We send our response in another try block to make errors make sense
            try {
                data = codec.encodeStart(JsonOps.INSTANCE, newConfig)
                        .getOrThrow(false, DoAHackedRoll.LOGGER::error).toString();

                var res = PacketByteBufs.create();
                res.writeInt(1);
                res.writeBoolean(true);
                res.writeString(data);

                DoAHackedRoll.LOGGER.info(
                        "{} updated the server config.",
                        player.getName().getString()
                );

                // Only set our instance if everything else succeeds
                instance = newConfig;
                updateCallback.accept(player.getServer(), instance);
                save();
                return res;
            } catch (RuntimeException e) {
                DoAHackedRoll.LOGGER.error("Failed to encode config", e);
                return getFailureBuf();
            }
        } catch (RuntimeException e) {
            DoAHackedRoll.LOGGER.warn(
                    "Client of {} sent invalid server config update, rejecting.",
                    player.getName().getString(),
                    e
            );
            return getFailureBuf();
        }
    }

    private PacketByteBuf getFailureBuf() {
        var res = PacketByteBufs.create();
        res.writeInt(1);
        res.writeBoolean(false);
        return res;
    }
}
