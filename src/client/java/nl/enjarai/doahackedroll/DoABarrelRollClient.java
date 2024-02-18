package nl.enjarai.doahackedroll;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.SmoothUtil;
import nl.enjarai.doahackedroll.api.RollEntity;
import nl.enjarai.doahackedroll.api.RollMouse;
import nl.enjarai.doahackedroll.api.event.ClientEvents;
import nl.enjarai.doahackedroll.api.event.RollEvents;
import nl.enjarai.doahackedroll.api.event.RollGroup;
import nl.enjarai.doahackedroll.config.ActivationBehaviour;
import nl.enjarai.doahackedroll.config.LimitedModConfigServer;
import nl.enjarai.doahackedroll.config.ModConfig;
import nl.enjarai.doahackedroll.config.ModConfigServer;
import nl.enjarai.doahackedroll.flight.RotationModifiers;
import nl.enjarai.doahackedroll.net.HandshakeClient;
import nl.enjarai.doahackedroll.render.HorizonLineWidget;
import nl.enjarai.doahackedroll.render.MomentumCrosshairWidget;
import nl.enjarai.doahackedroll.util.MixinHooks;
import org.joml.Vector2d;

public class DoABarrelRollClient {
    public static final HandshakeClient<LimitedModConfigServer, ModConfigServer> HANDSHAKE_CLIENT = new HandshakeClient<>(
            ModConfigServer.CODEC,
            LimitedModConfigServer.getCodec(),
            ClientEvents::updateServerConfig
    );
    public static final SmoothUtil PITCH_SMOOTHER = new SmoothUtil();
    public static final SmoothUtil YAW_SMOOTHER = new SmoothUtil();
    public static final SmoothUtil ROLL_SMOOTHER = new SmoothUtil();
    public static final RollGroup FALL_FLYING_GROUP = RollGroup.of(DoAHackedRoll.id("fall_flying"));
    public static double throttle = 0;
    public static final double max = 20.0;
    public static final double accel = 1.0;

    public static void init() {
        FALL_FLYING_GROUP.trueIf(DoABarrelRollClient::isFallFlying);

        // Keyboard modifiers
        RollEvents.EARLY_CAMERA_MODIFIERS.register(context -> context
                .useModifier(RotationModifiers::manageThrottle, ModConfig.INSTANCE::getEnableThrust)
                .useModifier(RotationModifiers.buttonControls(1800)),
                2000, FALL_FLYING_GROUP);

        // Mouse modifiers, including swapping axes
        RollEvents.EARLY_CAMERA_MODIFIERS.register(context -> context
                .useModifier(ModConfig.INSTANCE::configureRotation),
                1000, FALL_FLYING_GROUP);

        // Generic movement modifiers, banking and such
        RollEvents.LATE_CAMERA_MODIFIERS.register(context -> context
                .useModifier(RotationModifiers::applyControlSurfaceEfficacy, ModConfig.INSTANCE::getSimulateControlSurfaceEfficacy)
                .useModifier(RotationModifiers.smoothing(
                        PITCH_SMOOTHER, YAW_SMOOTHER, ROLL_SMOOTHER,
                        ModConfig.INSTANCE.getSmoothing()
                ))
                .useModifier(RotationModifiers::banking, ModConfig.INSTANCE::getEnableBanking),
                1000, FALL_FLYING_GROUP);

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (!isFallFlying()) {
                clearValues();
            }
        });

        ClientEvents.SERVER_CONFIG_UPDATE.register(ModConfig.INSTANCE::notifyPlayerOfServerConfig);
    }

    public static void onRenderCrosshair(DrawContext context, float tickDelta, int scaledWidth, int scaledHeight) {
        if (!isFallFlying()) return;

        var matrices = context.getMatrices();
        var entity = MinecraftClient.getInstance().getCameraEntity();
        var rollEntity = ((RollEntity) entity);
        if (entity != null) {
            if (ModConfig.INSTANCE.getShowHorizon()) {
                HorizonLineWidget.render(matrices, scaledWidth, scaledHeight,
                        rollEntity.doABarrelRoll$getRoll(tickDelta), entity.getPitch(tickDelta));
            }

            if (ModConfig.INSTANCE.getMomentumBasedMouse() && ModConfig.INSTANCE.getShowMomentumWidget()) {
                var rollMouse = (RollMouse) MinecraftClient.getInstance().mouse;

                MomentumCrosshairWidget.render(matrices, scaledWidth, scaledHeight, new Vector2d(rollMouse.doABarrelRoll$getMouseTurnVec()));
            }
        }
    }

    private static void clearValues() {
        PITCH_SMOOTHER.clear();
        YAW_SMOOTHER.clear();
        ROLL_SMOOTHER.clear();
        throttle = 0;
    }

    public static boolean isFallFlying() {
        if (!HANDSHAKE_CLIENT.getConfig().map(LimitedModConfigServer::forceEnabled).orElse(false)) {
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
        if (player == null) {
            return false;
        }
        if (ModConfig.INSTANCE.getDisableWhenSubmerged() && player.isSubmergedInWater()) {
            return false;
        }
        return player.isFallFlying();
    }

    public static boolean isConnectedToRealms() {
        var serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverInfo == null) return false;

        return serverInfo.isRealm();
    }
}
