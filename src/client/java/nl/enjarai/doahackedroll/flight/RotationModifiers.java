package nl.enjarai.doahackedroll.flight;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.util.math.MathHelper;
import nl.enjarai.doahackedroll.DoAHackedRoll;
import nl.enjarai.doahackedroll.DoABarrelRollClient;
import nl.enjarai.doahackedroll.ModKeybindings;
import nl.enjarai.doahackedroll.api.event.RollContext;
import nl.enjarai.doahackedroll.api.rotation.RotationInstant;
import nl.enjarai.doahackedroll.config.ModConfig;
import nl.enjarai.doahackedroll.config.Sensitivity;
import nl.enjarai.doahackedroll.math.MagicNumbers;

import java.util.HashMap;
import java.util.Map;

public class RotationModifiers {
    public static RollContext.ConfiguresRotation buttonControls(double power) {
        return (rotationInstant, context) -> {
            var delta = power * context.getRenderDelta();
            var pitch = 0;
            var yaw = 0;
            var roll = 0;

            if (ModKeybindings.PITCH_UP.isPressed()) {
                pitch -= delta;
            }
            if (ModKeybindings.PITCH_DOWN.isPressed()) {
                pitch += delta;
            }
            if (ModKeybindings.YAW_LEFT.isPressed()) {
                yaw -= delta;
            }
            if (ModKeybindings.YAW_RIGHT.isPressed()) {
                yaw += delta;
            }
            if (ModKeybindings.ROLL_LEFT.isPressed()) {
                roll -= delta;
            }
            if (ModKeybindings.ROLL_RIGHT.isPressed()) {
                roll += delta;
            }

            // Putting this in the roll value, since it'll be swapped later
            return rotationInstant.add(pitch, yaw, roll);
        };
    }

    public static RollContext.ConfiguresRotation smoothing(SmoothUtil pitchSmoother, SmoothUtil yawSmoother, SmoothUtil rollSmoother, Sensitivity smoothness) {
        return (rotationInstant, context) -> RotationInstant.of(
                smoothness.pitch == 0 ? rotationInstant.pitch() : pitchSmoother.smooth(rotationInstant.pitch(), 1 / smoothness.pitch * context.getRenderDelta()),
                smoothness.yaw == 0 ? rotationInstant.yaw() : yawSmoother.smooth(rotationInstant.yaw(), 1 / smoothness.yaw * context.getRenderDelta()),
                smoothness.roll == 0 ? rotationInstant.roll() : rollSmoother.smooth(rotationInstant.roll(), 1 / smoothness.roll * context.getRenderDelta())
        );
    }

    public static RotationInstant banking(RotationInstant rotationInstant, RollContext context) {
        var delta = context.getRenderDelta();
        var currentRotation = context.getCurrentRotation();
        var currentRoll = currentRotation.roll() * MagicNumbers.TORAD;

        var xExpression = ModConfig.INSTANCE.getBankingXFormula().getCompiledOrDefaulting(0);
        var yExpression = ModConfig.INSTANCE.getBankingYFormula().getCompiledOrDefaulting(0);

        var vars = getVars(context);
        vars.put("banking_strength", ModConfig.INSTANCE.getBankingStrength());

        var dX = xExpression.eval(vars);
        var dY = yExpression.eval(vars);

        // check if we accidentally got NaN, for some reason this happens sometimes
        if (Double.isNaN(dX)) dX = 0;
        if (Double.isNaN(dY)) dY = 0;

        return rotationInstant.addAbsolute(dX * delta, dY * delta, currentRoll);
    }

    public static RotationInstant manageThrottle(RotationInstant rotationInstant, RollContext context) {
        var delta = context.getRenderDelta();

        if (ModKeybindings.THRUST_FORWARD.isPressed()) {
            DoABarrelRollClient.throttle += 0.1 * delta;
        } else if (ModKeybindings.THRUST_BACKWARD.isPressed()) {
            DoABarrelRollClient.throttle -= 0.1 * delta;
        } else {
            DoABarrelRollClient.throttle -= DoABarrelRollClient.throttle * 0.95 * delta;
        }

        DoABarrelRollClient.throttle = MathHelper.clamp(DoABarrelRollClient.throttle, 0, ModConfig.INSTANCE.getMaxThrust());

        return rotationInstant;
    }

    public static RollContext.ConfiguresRotation fixNaN(String name) {
        return (rotationInstant, context) -> {
            if (Double.isNaN(rotationInstant.pitch())) {
                rotationInstant = RotationInstant.of(0, rotationInstant.yaw(), rotationInstant.roll());
                DoAHackedRoll.LOGGER.warn("NaN found in pitch for " + name + ", setting to 0 as fallback");
            }
            if (Double.isNaN(rotationInstant.yaw())) {
                rotationInstant = RotationInstant.of(rotationInstant.pitch(), 0, rotationInstant.roll());
                DoAHackedRoll.LOGGER.warn("NaN found in yaw for " + name + ", setting to 0 as fallback");
            }
            if (Double.isNaN(rotationInstant.roll())) {
                rotationInstant = RotationInstant.of(rotationInstant.pitch(), rotationInstant.yaw(), 0);
                DoAHackedRoll.LOGGER.warn("NaN found in roll for " + name + ", setting to 0 as fallback");
            }
            return rotationInstant;
        };
    }

    public static RotationInstant applyControlSurfaceEfficacy(RotationInstant rotationInstant, RollContext context) {
        var elevatorExpression = ModConfig.INSTANCE.getElevatorEfficacyFormula().getCompiledOrDefaulting(1);
        var aileronExpression = ModConfig.INSTANCE.getAileronEfficacyFormula().getCompiledOrDefaulting(1);
        var rudderExpression = ModConfig.INSTANCE.getRudderEfficacyFormula().getCompiledOrDefaulting(1);

        var vars = getVars(context);
        return rotationInstant.multiply(elevatorExpression.eval(vars), rudderExpression.eval(vars), aileronExpression.eval(vars));
    }

    private static Map<String, Double> getVars(RollContext context) {
        var player = MinecraftClient.getInstance().player;
        assert player != null;

        var currentRotation = context.getCurrentRotation();
        var rotationVector = player.getRotationVector();
        return new HashMap<>() {{
            put("pitch", currentRotation.pitch());
            put("yaw", currentRotation.yaw());
            put("roll", currentRotation.roll());
            put("velocity_length", player.getVelocity().length());
            put("velocity_x", player.getVelocity().getX());
            put("velocity_y", player.getVelocity().getY());
            put("velocity_z", player.getVelocity().getZ());
            put("look_x", rotationVector.getX());
            put("look_y", rotationVector.getY());
            put("look_z", rotationVector.getZ());
        }};
    }
}
