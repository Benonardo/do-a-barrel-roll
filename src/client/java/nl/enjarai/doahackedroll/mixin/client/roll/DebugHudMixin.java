package nl.enjarai.doahackedroll.mixin.client.roll;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import nl.enjarai.doahackedroll.api.RollEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    @Shadow @Final private MinecraftClient client;

    @ModifyArgs(
            method = "getLeftText",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
                    ordinal = 6
            )
    )
    private void doABarrelRoll$modifyDebugHudText(Args args) {
        var cameraEntity = client.getCameraEntity();
        if (cameraEntity == null) return;

        // Carefully insert a new number format specifier into the facing string
        var originalString = (String) args.get(1);
        var firstHalf = originalString.substring(0, originalString.length() - 1);
        var secondHalf = originalString.substring(originalString.length() - 1);
        args.set(1, firstHalf + " / %.1f" + secondHalf);

        // Add the roll value to the format arguments
        var roll = ((RollEntity) client.getCameraEntity()).doABarrelRoll$getRoll();
        var fmtArgs = (Object[]) args.get(2);
        var newFmtArgs = new Object[fmtArgs.length + 1];
        System.arraycopy(fmtArgs, 0, newFmtArgs, 0, fmtArgs.length);
        newFmtArgs[fmtArgs.length] = roll;
        args.set(2, newFmtArgs);
    }
}
