package nl.enjarai.doahackedroll.compat.cameraoverhaul;

import nl.enjarai.cicada.api.compat.CompatMixinPlugin;

import java.util.Set;

public class CameraOverhaulPlugin implements CompatMixinPlugin {
    @Override
    public Set<String> getRequiredMods() {
        return Set.of("cameraoverhaul");
    }
}
