package nl.enjarai.doabarrelroll;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import nl.enjarai.doabarrelroll.api.event.RollEvents;
import nl.enjarai.doabarrelroll.api.event.RollGroup;
import nl.enjarai.doabarrelroll.api.net.HandshakeClient;
import nl.enjarai.doabarrelroll.config.ActivationBehaviour;
import nl.enjarai.doabarrelroll.config.ModConfig;
import nl.enjarai.doabarrelroll.config.Sensitivity;
import nl.enjarai.doabarrelroll.config.SyncedModConfig;
import nl.enjarai.doabarrelroll.flight.ElytraMath;
import nl.enjarai.doabarrelroll.flight.RotationModifiers;
import nl.enjarai.doabarrelroll.flight.util.RotationInstant;
import nl.enjarai.doabarrelroll.render.HorizonLineWidget;
import nl.enjarai.doabarrelroll.render.MomentumCrosshairWidget;
import nl.enjarai.doabarrelroll.util.MixinHooks;
import org.joml.Vector2d;

public class DoABarrelRollClient {
    public static final HandshakeClient<SyncedModConfig> HANDSHAKE_CLIENT = new HandshakeClient<>(
            SyncedModConfig.TRANSFER_CODEC,
            ModConfig.INSTANCE::notifyPlayerOfServerConfig
    );
    public static final SmoothUtil PITCH_SMOOTHER = new SmoothUtil();
    public static final SmoothUtil YAW_SMOOTHER = new SmoothUtil();
    public static final SmoothUtil ROLL_SMOOTHER = new SmoothUtil();
    public static final RollGroup FALL_FLYING_GROUP = RollGroup.of(DoABarrelRoll.id("fall_flying"));
    private static double lastLookUpdate;
    private static double lastLerpUpdate;
    public static double landingLerp = 1;
    public static Vec3d left;
    public static Vector2d mouseTurnVec = new Vector2d();
    public static double throttle = 0;

    public static void init() {
        FALL_FLYING_GROUP.trueIf(DoABarrelRollClient::isFallFlying);

        // Keyboard modifiers
        RollEvents.EARLY_CAMERA_MODIFIERS.register((rotationDelta, currentRotation) -> rotationDelta
                .useModifier(RotationModifiers::manageThrottle, ModConfig.INSTANCE::getEnableThrust)
                .useModifier(RotationModifiers.strafeButtons(1800)),
                10, FALL_FLYING_GROUP);

        // Mouse modifiers, including swapping axes
        RollEvents.EARLY_CAMERA_MODIFIERS.register((rotationDelta, currentRotation) -> rotationDelta
                .useModifier(ModConfig.INSTANCE::configureRotation),
                20, FALL_FLYING_GROUP);

        // Generic movement modifiers, banking and such
        RollEvents.LATE_CAMERA_MODIFIERS.register((rotationDelta, currentRotation) -> rotationDelta
                .useModifier(RotationModifiers.smoothing(
                        PITCH_SMOOTHER, YAW_SMOOTHER, ROLL_SMOOTHER,
                        ModConfig.INSTANCE.getSmoothing()
                ), ModConfig.INSTANCE::getSmoothingEnabled)
                .useModifier(RotationModifiers::banking, ModConfig.INSTANCE::getEnableBanking),
                10, FALL_FLYING_GROUP);
    }

    public static boolean updateMouse(ClientPlayerEntity player, double cursorDeltaX, double cursorDeltaY) {

        if (!isRolling()) return true;

        // reset the landing animation when flying
        landingLerp = 0;

        if (ModConfig.INSTANCE.getMomentumBasedMouse()) {

            // add the mouse movement to the current vector and normalize if needed
            mouseTurnVec.add(new Vector2d(cursorDeltaX, cursorDeltaY).mul(1f / 300));
            if (mouseTurnVec.lengthSquared() > 1.0) {
                mouseTurnVec.normalize();
            }

            // enlarge the vector and apply it to the camera
            var delta = getDelta();
            var readyTurnVec = new Vector2d(mouseTurnVec).mul(1200 * (float) delta);
            changeElytraLook(readyTurnVec.y, readyTurnVec.x, 0, ModConfig.INSTANCE.getDesktopSensitivity(), delta);

        } else {

            // if we are not using a momentum based mouse, we can reset it and apply the values directly
            mouseTurnVec.zero();
            changeElytraLook(cursorDeltaY, cursorDeltaX, 0, ModConfig.INSTANCE.getDesktopSensitivity());
        }

        return false;
    }

    public static void onWorldRender(MinecraftClient client, float tickDelta, MatrixStack matrix) {

        double time = GlfwUtil.getTime();
        double lerpDelta = time - lastLerpUpdate;
        lastLerpUpdate = time;

        if (!isRolling()) {

            landingLerp = MathHelper.lerp(MathHelper.clamp(lerpDelta * 2, 0, 1), landingLerp, 1);

            // round the lerp off when done to hopefully avoid world flickering
            if (landingLerp > 0.9) landingLerp = 1;

            clearValues();

            if (client.player != null) {
                left = left.lerp(ElytraMath.getAssumedLeft(client.player.getYaw()), landingLerp);
            }

        } else {

            if (client.isPaused()) {

                // keep updating the last look update time when paused to prevent large jumps after unpausing
                lastLookUpdate = GlfwUtil.getTime();

            } else {

                // update the camera rotation every frame to keep it smooth
                changeElytraLook(0, 0, 0, ModConfig.INSTANCE.getDesktopSensitivity());

            }
        }

        if (client.player != null && landingLerp < 1) {

            // calculate the camera angle and apply it
            double angle = -Math.acos(MathHelper.clamp(left.dotProduct(ElytraMath.getAssumedLeft(client.player.getYaw())), -1, 1)) * ElytraMath.TODEG;
            if (left.getY() < 0) angle *= -1;
            if (client.options.getPerspective().isFrontView()) angle *= -1;
            matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) angle));

        }
    }

    public static void onRenderCrosshair(MatrixStack matrices, float tickDelta, int scaledWidth, int scaledHeight) {
        if (!isFallFlying()) return;

        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            if (ModConfig.INSTANCE.getShowHorizon()) {
                HorizonLineWidget.render(matrices, scaledWidth, scaledHeight,
                        ElytraMath.getRoll(player.getYaw(tickDelta), left), player.getPitch(tickDelta));
            }

            if (ModConfig.INSTANCE.getMomentumBasedMouse() && ModConfig.INSTANCE.getShowMomentumWidget()) {
                MomentumCrosshairWidget.render(matrices, scaledWidth, scaledHeight, new Vector2d(mouseTurnVec));
            }
        }
    }


    private static void clearValues() {
        PITCH_SMOOTHER.clear();
        YAW_SMOOTHER.clear();
        ROLL_SMOOTHER.clear();
        mouseTurnVec.zero();
        lastLookUpdate = GlfwUtil.getTime();
        throttle = 0;
    }

    /**
     * Returns the time since the last look update.
     *
     * <p>
     * <b>Only call if you're going to call
     * {@link DoABarrelRollClient#changeElytraLook(double, double, double, Sensitivity, double)}
     * right after this using the returned value.</b>
     * Neglecting to do this will disrupt the smoothness of the camera.
     * </p>
     */
    private static double getDelta() {
        double time = GlfwUtil.getTime();
        double delta = time - lastLookUpdate;
        lastLookUpdate = time;
        return delta;
    }

    /**
     * Only use if you <b>haven't</b> called {@link DoABarrelRollClient#getDelta()} before this.
     */
    public static void changeElytraLook(double pitch, double yaw, double roll, Sensitivity sensitivity) {
        changeElytraLook(pitch, yaw, roll, sensitivity, getDelta());
    }

    public static void changeElytraLook(double pitch, double yaw, double roll, Sensitivity sensitivity, double delta) {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return;

        var rotDelta = new RotationInstant(pitch, yaw, roll, delta);
        var currentRoll = ElytraMath.getRoll(player.getYaw(), DoABarrelRollClient.left);
        var currentRotation = new RotationInstant(
                player.getPitch(),
                player.getYaw(),
                currentRoll,
                0
        );

        ElytraMath.changeElytraLookDirectly(player,
                RollEvents.lateCameraModifiers(
                        RollEvents.earlyCameraModifiers(rotDelta
                                        .useModifier(RotationModifiers.fixNaN("INPUT")), currentRotation)
                                .useModifier(RotationModifiers.fixNaN("EARLY_CAMERA_MODIFIERS"))
                                .applySensitivity(sensitivity)
                                .useModifier(RotationModifiers.fixNaN("SENSITIVITY")),
                        currentRotation
                ).useModifier(RotationModifiers.fixNaN("LATE_CAMERA_MODIFIERS"))
        );
    }

    public static boolean isFallFlying() {
        if (!HANDSHAKE_CLIENT.getConfig().map(SyncedModConfig::forceEnabled).orElse(false)) {
            var hybrid = ModConfig.INSTANCE.getActivationBehaviour() == ActivationBehaviour.HYBRID ||
                    ModConfig.INSTANCE.getActivationBehaviour() == ActivationBehaviour.HYBRID_TOGGLE;
            if (hybrid && !MixinHooks.thirdJump) {
                return false;
            }

            if (!ModConfig.INSTANCE.getModEnabled()) {
                return false;
            }
        }

        var player = MinecraftClient.getInstance().player;
        return player != null && player.isFallFlying();
    }

    public static boolean isRolling() {
        return RollEvents.shouldRoll();
    }
}