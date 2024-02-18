package nl.enjarai.doahackedroll.api.event;

import net.minecraft.client.network.ClientPlayerEntity;
import nl.enjarai.doahackedroll.impl.event.EventImpl;

public interface StarFox64Events {
    Event<DoesABarrelRollEvent> DOES_A_BARREL_ROLL = new EventImpl<>();

    static void doesABarrelRoll(ClientPlayerEntity player) {
        for (var listener : DOES_A_BARREL_ROLL.getListeners()) {
            listener.onBarrelRoll(player);
        }
    }

    interface DoesABarrelRollEvent {
        void onBarrelRoll(ClientPlayerEntity player);
    }
}
