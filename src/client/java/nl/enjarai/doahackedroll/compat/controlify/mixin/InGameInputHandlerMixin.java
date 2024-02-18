package nl.enjarai.doahackedroll.compat.controlify.mixin;

import dev.isxander.controlify.ingame.InGameInputHandler;
import net.minecraft.client.MinecraftClient;
import nl.enjarai.doahackedroll.api.RollEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameInputHandler.class)
public abstract class InGameInputHandlerMixin {
    @Shadow @Final private MinecraftClient minecraft;

    @Inject(
            method = "processPlayerLook",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void doABarrelRoll$cancelDefaultLookHandling(CallbackInfo ci) {
        if (minecraft.player instanceof RollEntity rollEntity && rollEntity.doABarrelRoll$isRolling()) {
            ci.cancel();
        }
    }
}
