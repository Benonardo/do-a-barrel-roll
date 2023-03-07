package nl.enjarai.doabarrelroll.flight;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.util.math.MathHelper;
import nl.enjarai.doabarrelroll.DoABarrelRollClient;
import nl.enjarai.doabarrelroll.config.ModConfig;
import nl.enjarai.doabarrelroll.config.Sensitivity;
import nl.enjarai.doabarrelroll.flight.util.ConfiguresRotation;
import nl.enjarai.doabarrelroll.flight.util.RotationInstant;

public class RotationModifiers {
    public static ConfiguresRotation strafeButtons(double power) {
        return (rotationInstant) -> {
            var client = MinecraftClient.getInstance();

            var yawDelta = power * rotationInstant.getRenderDelta();
            var yaw = 0;

            if (client.options.leftKey.isPressed()) {
                yaw -= yawDelta;
            }
            if (client.options.rightKey.isPressed()) {
                yaw += yawDelta;
            }

            // Putting this in the roll value, since it'll be swapped later
            return rotationInstant.add(0, 0, yaw);
        };
    }

    public static ConfiguresRotation smoothing(SmoothUtil pitchSmoother, SmoothUtil yawSmoother, SmoothUtil rollSmoother, Sensitivity smoothness) {
        return (rotationInstant) -> new RotationInstant(
                pitchSmoother.smooth(rotationInstant.getPitch(), smoothness.pitch * rotationInstant.getRenderDelta()),
                yawSmoother.smooth(rotationInstant.getYaw(), smoothness.yaw * rotationInstant.getRenderDelta()),
                rollSmoother.smooth(rotationInstant.getRoll(), smoothness.roll * rotationInstant.getRenderDelta()),
                rotationInstant.getRenderDelta()
        );
    }

    public static RotationInstant banking(RotationInstant rotationInstant) {
        var client = MinecraftClient.getInstance();
        var player = client.player;
        if (player == null) return rotationInstant;

        var delta = rotationInstant.getRenderDelta();
        var currentRoll = ElytraMath.getRoll(player.getYaw(), DoABarrelRollClient.left) * ElytraMath.TORAD;
        var strength = 10 * Math.cos(player.getPitch() * ElytraMath.TORAD) * ModConfig.INSTANCE.getBankingStrength();

        var dX = Math.sin(currentRoll) * strength;
        var dY = -strength + Math.cos(currentRoll) * strength;

        // check if we accidentally got NaN, for some reason this happens sometimes
        if (Double.isNaN(dX)) dX = 0;
        if (Double.isNaN(dY)) dY = 0;

        return rotationInstant.addAbsolute(dX * delta, dY * delta, currentRoll);
    }

    public static RotationInstant manageThrottle(RotationInstant rotationInstant) {
        var client = MinecraftClient.getInstance();

        var delta = rotationInstant.getRenderDelta();

        if (client.options.forwardKey.isPressed()) {
            DoABarrelRollClient.throttle += 0.1 * delta;
        } else if (client.options.backKey.isPressed()) {
            DoABarrelRollClient.throttle -= 0.1 * delta;
        } else {
            DoABarrelRollClient.throttle -= DoABarrelRollClient.throttle * 0.95 * delta;
        }

        DoABarrelRollClient.throttle = MathHelper.clamp(DoABarrelRollClient.throttle, 0, ModConfig.INSTANCE.getMaxThrust());

        return rotationInstant;
    }
}
