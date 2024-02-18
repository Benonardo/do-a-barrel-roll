package nl.enjarai.doahackedroll.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface LimitedModConfigServer {
    LimitedModConfigServer OPERATOR = new Impl(true, false);

    static Codec<LimitedModConfigServer> getCodec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("allowThrusting", ModConfigServer.DEFAULT.allowThrusting()).forGetter(LimitedModConfigServer::allowThrusting),
                Codec.BOOL.optionalFieldOf("forceEnabled", ModConfigServer.DEFAULT.forceEnabled()).forGetter(LimitedModConfigServer::forceEnabled)
        ).apply(instance, Impl::new));
    }

    boolean allowThrusting();

    boolean forceEnabled();

    record Impl(boolean allowThrusting, boolean forceEnabled) implements LimitedModConfigServer {
    }
}
