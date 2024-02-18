package nl.enjarai.doahackedroll.impl.key;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import nl.enjarai.doahackedroll.api.key.InputContext;
import nl.enjarai.doahackedroll.util.key.ContextualKeyBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class InputContextImpl implements InputContext {
    private static final List<InputContext> CONTEXTS = new ReferenceArrayList<>();

    public static List<InputContext> getContexts() {
        return CONTEXTS;
    }

    public static boolean contextsContain(KeyBinding binding) {
        for (var context : InputContextImpl.getContexts()) {
            if (context.getKeyBindings().contains(binding)) {
                return true;
            }
        }

        return false;
    }

    private final Identifier id;
    private final Supplier<Boolean> activeCondition;
    private final List<KeyBinding> keyBindings = new ReferenceArrayList<>();
    private final Map<InputUtil.Key, KeyBinding> bindingsByKey = new HashMap<>();
    private boolean active;

    public InputContextImpl(Identifier id, Supplier<Boolean> activeCondition) {
        this.id = id;
        this.activeCondition = activeCondition;
        CONTEXTS.add(this);
        ClientTickEvents.START_CLIENT_TICK.register(client -> tick());
    }

    public void tick() {
        boolean active = activeCondition.get();
        if (active != this.active) {
            this.active = active;
            KeyBinding.updatePressedStates();
        }
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void addKeyBinding(KeyBinding keyBinding) {
        Objects.requireNonNull(keyBinding);
        keyBindings.add(keyBinding);
        ((ContextualKeyBinding) keyBinding).doABarrelRoll$addToContext(this);
    }

    @Override
    public List<KeyBinding> getKeyBindings() {
        return keyBindings;
    }

    @Override
    public KeyBinding getKeyBinding(InputUtil.Key key) {
        return bindingsByKey.get(key);
    }

    @Override
    public void updateKeysByCode() {
        bindingsByKey.clear();
        for (KeyBinding keyBinding : keyBindings) {
            bindingsByKey.put(keyBinding.boundKey, keyBinding);
        }
    }
}
