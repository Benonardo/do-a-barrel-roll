package nl.enjarai.doahackedroll.mixin.client.roll.entity;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import nl.enjarai.doahackedroll.mixin.roll.entity.PlayerEntityMixin;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntityMixin {
}
