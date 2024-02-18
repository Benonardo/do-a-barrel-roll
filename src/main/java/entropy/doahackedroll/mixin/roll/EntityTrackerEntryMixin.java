package nl.enjarai.doahackedroll.mixin.roll;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import nl.enjarai.doahackedroll.api.RollEntity;
import nl.enjarai.doahackedroll.net.RollSyncServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin {
    @Shadow @Final private Entity entity;

    @Unique
    private boolean lastIsRolling;
    @Unique
    private float lastRoll;

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void doABarrelRoll$syncRollS2C(CallbackInfo ci) {
        var rollEntity = (RollEntity) entity;
        var isRolling = rollEntity.doABarrelRoll$isRolling();
        var roll = rollEntity.doABarrelRoll$getRoll();

        if (isRolling != lastIsRolling || roll != lastRoll) {
            RollSyncServer.sendUpdates(entity);

            lastIsRolling = isRolling;
            lastRoll = roll;
        }
    }
}
