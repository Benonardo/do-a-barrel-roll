package nl.enjarai.doahackedroll.compat.controlify;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.BindingSupplier;
import dev.isxander.controlify.api.bind.ControlifyBindingsApi;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.bindings.GamepadBinds;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import nl.enjarai.doahackedroll.DoAHackedRoll;
import nl.enjarai.doahackedroll.DoABarrelRollClient;
import nl.enjarai.doahackedroll.api.event.RollContext;
import nl.enjarai.doahackedroll.api.event.RollEvents;
import nl.enjarai.doahackedroll.api.event.ThrustEvents;
import nl.enjarai.doahackedroll.api.rotation.RotationInstant;
import nl.enjarai.doahackedroll.config.ModConfig;

public class ControlifyCompat implements ControlifyEntrypoint {
    public static BindingSupplier PITCH_UP;
    public static BindingSupplier PITCH_DOWN;
    public static BindingSupplier ROLL_LEFT;
    public static BindingSupplier ROLL_RIGHT;
    public static BindingSupplier YAW_LEFT;
    public static BindingSupplier YAW_RIGHT;
    public static BindingSupplier THRUST_FORWARD;
    public static BindingSupplier THRUST_BACKWARD;

    private RotationInstant applyToRotation(RotationInstant rotationDelta, RollContext context) {
        var controller = ControlifyApi.get().currentController();
        var sensitivity = ModConfig.INSTANCE.getControllerSensitivity();

        if (PITCH_UP.onController(controller) == null) return rotationDelta;

        double multiplier = context.getRenderDelta() * 1200;

        float pitchAxis = PITCH_DOWN.onController(controller).state() - PITCH_UP.onController(controller).state();
        float yawAxis = YAW_RIGHT.onController(controller).state() - YAW_LEFT.onController(controller).state();
        float rollAxis = ROLL_RIGHT.onController(controller).state() - ROLL_LEFT.onController(controller).state();

        pitchAxis *= multiplier * sensitivity.pitch;
        yawAxis *= multiplier * sensitivity.yaw;
        rollAxis *= multiplier * sensitivity.roll;

        return rotationDelta.add(pitchAxis, yawAxis, rollAxis);
    }

    public static double getThrustModifier() {
        if (ControlifyApi.get().getCurrentController().isEmpty()) {
            return 0;
        }
        var controller = ControlifyApi.get().getCurrentController().get();

        float forward = THRUST_FORWARD.onController(controller).state();
        float backward = THRUST_BACKWARD.onController(controller).state();
        return forward - backward;
    }

    public static RotationInstant manageThrottle(RotationInstant rotationInstant, RollContext context) {
        var delta = context.getRenderDelta();

        DoABarrelRollClient.throttle += getThrustModifier() * delta;
        DoABarrelRollClient.throttle = MathHelper.clamp(DoABarrelRollClient.throttle, 0, ModConfig.INSTANCE.getMaxThrust());

        return rotationInstant;
    }

    @Override
    public void onControlifyPreInit(ControlifyApi controlifyApi) {
        var bindings = ControlifyBindingsApi.get();
        PITCH_UP = bindings.registerBind(DoAHackedRoll.id("pitch_up"), builder -> builder
                .defaultBind(GamepadBinds.RIGHT_STICK_FORWARD)
                .category(Text.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Text.translatable("controlify.bind.do_a_barrel_roll.pitch_up"))
        );
        PITCH_DOWN = bindings.registerBind(DoAHackedRoll.id("pitch_down"), builder -> builder
                .defaultBind(GamepadBinds.RIGHT_STICK_BACKWARD)
                .category(Text.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Text.translatable("controlify.bind.do_a_barrel_roll.pitch_down"))
        );
        ROLL_LEFT = bindings.registerBind(DoAHackedRoll.id("roll_left"), builder -> builder
                .defaultBind(GamepadBinds.RIGHT_STICK_LEFT)
                .category(Text.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Text.translatable("controlify.bind.do_a_barrel_roll.roll_left"))
        );
        ROLL_RIGHT = bindings.registerBind(DoAHackedRoll.id("roll_right"), builder -> builder
                .defaultBind(GamepadBinds.RIGHT_STICK_RIGHT)
                .category(Text.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Text.translatable("controlify.bind.do_a_barrel_roll.roll_right"))
        );
        YAW_LEFT = bindings.registerBind(DoAHackedRoll.id("yaw_left"), builder -> builder
                .defaultBind(GamepadBinds.LEFT_STICK_LEFT)
                .category(Text.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Text.translatable("controlify.bind.do_a_barrel_roll.yaw_left"))
        );
        YAW_RIGHT = bindings.registerBind(DoAHackedRoll.id("yaw_right"), builder -> builder
                .defaultBind(GamepadBinds.LEFT_STICK_RIGHT)
                .category(Text.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Text.translatable("controlify.bind.do_a_barrel_roll.yaw_right"))
        );
        THRUST_FORWARD = bindings.registerBind(DoAHackedRoll.id("thrust_forward"), builder -> builder
                .defaultBind(GamepadBinds.LEFT_STICK_FORWARD)
                .category(Text.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Text.translatable("controlify.bind.do_a_barrel_roll.thrust_forward"))
        );
        THRUST_BACKWARD = bindings.registerBind(DoAHackedRoll.id("thrust_backward"), builder -> builder
                .defaultBind(GamepadBinds.LEFT_STICK_BACKWARD)
                .category(Text.translatable("controlify.category.do_a_barrel_roll.do_a_barrel_roll"))
                .name(Text.translatable("controlify.bind.do_a_barrel_roll.thrust_backward"))
        );

        RollEvents.EARLY_CAMERA_MODIFIERS.register(context -> context
                .useModifier(ControlifyCompat::manageThrottle, ModConfig.INSTANCE::getEnableThrust),
                8, DoABarrelRollClient::isFallFlying);
        RollEvents.LATE_CAMERA_MODIFIERS.register(context -> context
                .useModifier(this::applyToRotation),
                5, DoABarrelRollClient::isFallFlying);

        ThrustEvents.MODIFY_THRUST_INPUT.register(input -> input + getThrustModifier());
    }

    @Override
    public void onControllersDiscovered(ControlifyApi controlifyApi) {
    }
}
