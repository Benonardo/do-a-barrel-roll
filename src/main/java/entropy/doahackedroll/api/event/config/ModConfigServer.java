package nl.enjarai.doahackedroll.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import nl.enjarai.doahackedroll.DoAHackedRoll;
import nl.enjarai.doahackedroll.net.SyncableConfig;
import nl.enjarai.doahackedroll.net.ValidatableConfig;

public record ModConfigServer(boolean allowThrusting,
                              boolean forceEnabled,
                              boolean forceInstalled,
                              int installedTimeout,
                              KineticDamage kineticDamage) implements SyncableConfig<ModConfigServer, LimitedModConfigServer>, LimitedModConfigServer, ValidatableConfig {
    public static final ModConfigServer DEFAULT = new ModConfigServer(
            true, false, false, 40, KineticDamage.VANILLA);

    public static final Codec<ModConfigServer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("allowThrusting", DEFAULT.allowThrusting()).forGetter(ModConfigServer::allowThrusting),
            Codec.BOOL.optionalFieldOf("forceEnabled", DEFAULT.forceEnabled()).forGetter(ModConfigServer::forceEnabled),
            Codec.BOOL.optionalFieldOf("forceInstalled", DEFAULT.forceInstalled()).forGetter(ModConfigServer::forceInstalled),
            Codec.INT.optionalFieldOf("installedTimeout", DEFAULT.installedTimeout()).forGetter(ModConfigServer::installedTimeout),
            KineticDamage.CODEC.optionalFieldOf("kineticDamage", DEFAULT.kineticDamage()).forGetter(ModConfigServer::kineticDamage)
    ).apply(instance, ModConfigServer::new));

    @Override
    public Integer getSyncTimeout() {
        return forceInstalled ? installedTimeout : null;
    }

    @Override
    public Text getSyncTimeoutMessage() {
        return Text.of("Please install Do a Hacked Roll 2.4.0 or later to play on this server.");
    }

    @Override
    public LimitedModConfigServer getLimited(ServerPlayNetworkHandler handler) {
        return Permissions.check(handler.getPlayer(), DoAHackedRoll.MODID + ".ignore_config", 2) ? LimitedModConfigServer.OPERATOR : this;
    }

    public static boolean canModify(ServerPlayNetworkHandler net) {
        return Permissions.check(net.getPlayer(), DoAHackedRoll.MODID + ".configure", 3);
    }

    @Override
    public boolean isValid() {
        return installedTimeout > 0;
    }
}
