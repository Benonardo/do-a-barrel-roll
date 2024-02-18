package nl.enjarai.doahackedroll.fabric;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import com.bawnorton.mixinsquared.tools.MixinAnnotationReader;
import net.fabricmc.api.ModInitializer;
import nl.enjarai.doahackedroll.DoAHackedRoll;
import nl.enjarai.doahackedroll.fabric.net.HandshakeServerFabric;

import java.util.List;

public class DoABarrelRollFabric implements ModInitializer, MixinCanceller {
    @Override
    public void onInitialize() {
        // Init server and client common code.
        DoAHackedRoll.init();

        // Register server-side listeners for config syncing, this is done on
        // both client and server to ensure everything works in LAN worlds as well.
        HandshakeServerFabric.init();
    }

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        if (mixinClassName.equals("com.anthonyhilyard.equipmentcompare.mixin.KeyMappingMixin") && MixinAnnotationReader.getPriority(mixinClassName) == 1000) {
            DoAHackedRoll.LOGGER.warn("Equipment Compare detected, disabling their overly invasive keybinding mixin. Report any relevant issues to them.");
            DoAHackedRoll.LOGGER.warn("If the author of Equipment Compare is reading this: see #31 on your github. Once the issue is resolved, you can set the priority of this mixin to anything other than 1000 to stop it being disabled.");
            return true;
        }
//        if (mixinClassName.equals("me.fzzyhmstrs.keybind_fix.mixins.KeybindingMixin") && MixinAnnotationReader.getPriority(mixinClassName) == 1000) {
//            DoABarrelRoll.LOGGER.warn("Keybind Fix detected, disabling their overly invasive keybinding mixin (Ironic, I know). Report any relevant issues to them.");
//            DoABarrelRoll.LOGGER.warn("If the author of Keybind Fix is reading this: please don't use unconditionally cancelled injects... try looking into MixinExtras! Once the issue is resolved, you can set the priority of this mixin to anything other than 1000 to stop it being disabled.");
//            return true;
//        }
        return false;
    }
}
