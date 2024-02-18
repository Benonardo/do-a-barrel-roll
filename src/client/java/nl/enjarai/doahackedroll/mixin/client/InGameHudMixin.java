package nl.enjarai.doahackedroll.mixin.client;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import nl.enjarai.doahackedroll.DoABarrelRollClient;
import nl.enjarai.doahackedroll.util.StarFoxUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void doABarrelRoll$captureTickDelta(DrawContext context, float tickDelta, CallbackInfo ci, @Share("tickDelta") LocalFloatRef tickDeltaRef) {
        tickDeltaRef.set(tickDelta);
    }

    @Inject(
            method = "renderCrosshair",
            at = @At(value = "HEAD")
    )
    private void doABarrelRoll$renderCrosshairHead(DrawContext context, CallbackInfo ci, @Share("tickDelta") LocalFloatRef tickDeltaRef) {
        context.getMatrices().push();
        DoABarrelRollClient.onRenderCrosshair(context, tickDeltaRef.get(), scaledWidth, scaledHeight);
    }

    @Inject(
            method = "renderCrosshair",
            at = @At(value = "RETURN")
    )
    private void doABarrelRoll$renderCrosshairReturn(DrawContext context, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"
            )
    )
    private void doABarrelRoll$renderPeppy(DrawContext context, float tickDelta, CallbackInfo ci) {
        StarFoxUtil.renderPeppy(context, tickDelta, scaledWidth, scaledHeight);
    }
}
