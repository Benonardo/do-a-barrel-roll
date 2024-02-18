package nl.enjarai.doahackedroll.mixin.client.roll;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import nl.enjarai.doahackedroll.api.RollEntity;
import nl.enjarai.doahackedroll.api.RollMouse;
import nl.enjarai.doahackedroll.config.ModConfig;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin implements RollMouse {
    @Shadow @Final private MinecraftClient client;
    @Shadow private double lastMouseUpdateTime;

    @Unique
    private final Vector2d mouseTurnVec = new Vector2d();

    @ModifyVariable(
            method = "updateMouse",
            index = 3,
            at = @At(
                    value = "STORE",
                    ordinal = 0
            )
    )
    private double doABarrelRoll$captureDelta(double original, @Share("mouseDelta") LocalDoubleRef mouseDeltaRef) {
        if (lastMouseUpdateTime != Double.MIN_VALUE) {
            mouseDeltaRef.set(original);
        }

        return original;
    }

    @Inject(
            method = "updateMouse",
            at = @At(
                    value = "RETURN",
                    ordinal = 0
            )
    )
    private void doABarrelRoll$maintainMouseMomentum(CallbackInfo ci, @Share("mouseDelta") LocalDoubleRef mouseDeltaRef) {
        if (client.player != null && !client.isPaused()) {
            doABarrelRoll$updateMouse(client.player, 0, 0, mouseDeltaRef.get());
        }
    }

    @WrapWithCondition(
            method = "updateMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
            )
    )
    private boolean doABarrelRoll$changeLookDirection(ClientPlayerEntity player, double cursorDeltaX, double cursorDeltaY, @Share("mouseDelta") LocalDoubleRef mouseDeltaRef) {
        return !doABarrelRoll$updateMouse(player, cursorDeltaX, cursorDeltaY, mouseDeltaRef.get());
    }

    @Override
    public boolean doABarrelRoll$updateMouse(ClientPlayerEntity player, double cursorDeltaX, double cursorDeltaY, double mouseDelta) {
        var rollPlayer = (RollEntity) player;

        if (rollPlayer.doABarrelRoll$isRolling()) {

            if (ModConfig.INSTANCE.getMomentumBasedMouse()) {

                // add the mouse movement to the current vector and normalize if needed
                mouseTurnVec.add(new Vector2d(cursorDeltaX, cursorDeltaY).mul(1f / 300));
                if (mouseTurnVec.lengthSquared() > 1.0) {
                    mouseTurnVec.normalize();
                }
                var readyTurnVec = new Vector2d(mouseTurnVec);

                // check if the vector is within the deadzone
                double deadzone = ModConfig.INSTANCE.getMomentumMouseDeadzone();
                if (readyTurnVec.lengthSquared() < deadzone * deadzone) readyTurnVec.zero();

                // enlarge the vector and apply it to the camera
                readyTurnVec.mul(1200 * (float) mouseDelta);
                rollPlayer.doABarrelRoll$changeElytraLook(readyTurnVec.y, readyTurnVec.x, 0, ModConfig.INSTANCE.getDesktopSensitivity(), mouseDelta);

            } else {

                // if we are not using a momentum based mouse, we can reset it and apply the values directly
                mouseTurnVec.zero();
                rollPlayer.doABarrelRoll$changeElytraLook(cursorDeltaY, cursorDeltaX, 0, ModConfig.INSTANCE.getDesktopSensitivity(), mouseDelta);
            }

            return true;
        }

        mouseTurnVec.zero();
        return false;
    }

    @Override
    public Vector2d doABarrelRoll$getMouseTurnVec() {
        return mouseTurnVec;
    }
}
