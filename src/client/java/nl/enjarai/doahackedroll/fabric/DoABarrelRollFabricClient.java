package nl.enjarai.doahackedroll.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import nl.enjarai.doahackedroll.DoABarrelRollClient;
import nl.enjarai.doahackedroll.ModKeybindings;
import nl.enjarai.doahackedroll.config.ModConfig;
import nl.enjarai.doahackedroll.fabric.net.HandshakeClientFabric;
import nl.enjarai.doahackedroll.util.StarFoxUtil;

public class DoABarrelRollFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DoABarrelRollClient.init();

        ModConfig.touch();
        HandshakeClientFabric.init();

        // Register keybindings on fabric
        ModKeybindings.FABRIC.forEach(KeyBindingHelper::registerKeyBinding);

        // Lame, cringe easter egg. Disabled.
        //StarFoxUtil.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModKeybindings.clientTick(client);

            //StarFoxUtil.clientTick(client);
        });
    }
}
