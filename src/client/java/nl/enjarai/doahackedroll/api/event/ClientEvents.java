package nl.enjarai.doahackedroll.api.event;

import nl.enjarai.doahackedroll.config.LimitedModConfigServer;
import nl.enjarai.doahackedroll.impl.event.EventImpl;

public interface ClientEvents {
    Event<ServerConfigUpdateEvent> SERVER_CONFIG_UPDATE = new EventImpl<>();

    interface ServerConfigUpdateEvent {
        void updateServerConfig(LimitedModConfigServer config);
    }

    static void updateServerConfig(LimitedModConfigServer config) {
        for (var listener : SERVER_CONFIG_UPDATE.getListeners()) {
            listener.updateServerConfig(config);
        }
    }
}
