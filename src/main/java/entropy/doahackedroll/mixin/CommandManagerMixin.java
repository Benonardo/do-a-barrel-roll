package nl.enjarai.doahackedroll.mixin;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import nl.enjarai.doahackedroll.fabric.net.HandshakeServerFabric;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
    @Inject(
            method = "sendCommandTree",
            at = @At(value = "RETURN")
    )
    private void doABarrelRoll$doHandshake(ServerPlayerEntity player, CallbackInfo ci) {
        // We do the handshake here since, aside from on join, this method will most likely also trigger
        // in any situation where the player's permissions change
        HandshakeServerFabric.sendHandshake(player);
    }
}
