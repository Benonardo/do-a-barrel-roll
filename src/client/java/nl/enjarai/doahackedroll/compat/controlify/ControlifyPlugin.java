package nl.enjarai.doahackedroll.compat.controlify;

import nl.enjarai.cicada.api.compat.CompatMixinPlugin;

import java.util.Set;

public class ControlifyPlugin implements CompatMixinPlugin {
    @Override
    public Set<String> getRequiredMods() {
        return Set.of("controlify");
    }
}
