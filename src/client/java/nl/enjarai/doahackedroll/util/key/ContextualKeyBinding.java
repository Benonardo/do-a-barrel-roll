package nl.enjarai.doahackedroll.util.key;

import nl.enjarai.doahackedroll.api.key.InputContext;

import java.util.List;

public interface ContextualKeyBinding {
    void doABarrelRoll$addToContext(InputContext context);

    List<InputContext> doABarrelRoll$getContexts();
}
