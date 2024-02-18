package entropy.doahackedroll;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import nl.enjarai.cicada.api.conversation.ConversationManager;
import nl.enjarai.cicada.api.util.CicadaEntrypoint;
import nl.enjarai.cicada.api.util.JsonSource;
import nl.enjarai.cicada.api.util.ProperLogger;
import nl.enjarai.doahackedroll.api.event.ServerEvents;
import nl.enjarai.doahackedroll.config.ModConfigServer;
import nl.enjarai.doahackedroll.net.HandshakeServer;
import nl.enjarai.doahackedroll.net.ServerConfigHolder;
import org.slf4j.Logger;

public class DoAHackedRoll implements CicadaEntrypoint {
    public static final String MODID = "do_a_hacked_roll";
    public static final Logger LOGGER = ProperLogger.getLogger(MODID);

    public static ServerConfigHolder<ModConfigServer> CONFIG_HOLDER;
    public static HandshakeServer HANDSHAKE_SERVER;
    public static final Identifier HANDSHAKE_CHANNEL = id("handshake");
    public static final Identifier SERVER_CONFIG_UPDATE_CHANNEL = id("server_config_update");
    public static final Identifier ROLL_CHANNEL = id("player_roll");

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static void init() {
        var configFile = FabricLoader.getInstance().getConfigDir().resolve(DoAHackedRoll.MODID + "-server.json");

        CONFIG_HOLDER = new ServerConfigHolder<>(configFile,
                ModConfigServer.CODEC, ModConfigServer.DEFAULT, ServerEvents::updateServerConfig);
        HANDSHAKE_SERVER = new HandshakeServer(CONFIG_HOLDER, player -> !ModConfigServer.canModify(player));
    }

    @Override
    public void registerConversations(ConversationManager conversationManager) {
        conversationManager.registerSource(
                JsonSource.fromUrl("https://raw.githubusercontent.com/enjarai/do-a-hacked-roll/1.20.4/dev/src/main/resources/cicada/do-a-barrel-roll/conversations.json")
                        .or(JsonSource.fromResource("cicada/do-a-hacked-roll/conversations.json")),
                LOGGER::info
        );
    }
}
