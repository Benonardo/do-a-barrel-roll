package nl.enjarai.doahackedroll.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

public class Compat {
    public static final Version YACL_MIN_VERSION;

    static {
        try {
            YACL_MIN_VERSION = Version.parse("3.1.0");
        } catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isYACLLoaded() {
         return FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3");
    }

    public static boolean isYACLUpToDate() {
        return FabricLoader.getInstance().getModContainer("yet_another_config_lib_v3")
                .filter(modContainer -> modContainer.getMetadata().getVersion().compareTo(YACL_MIN_VERSION) >= 0)
                .isPresent();
    }
}
