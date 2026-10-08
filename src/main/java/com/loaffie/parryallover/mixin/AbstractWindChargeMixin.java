package com.loaffie.parryallover.mixin;

import com.loaffie.parryallover.parry.ParriedProjectile;
import com.loaffie.parryallover.parry.ParryTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Remembers that a wind charge was sent back, and stuns the breeze the moment it lands.
 */
@Mixin(AbstractWindChargeEntity.class)
public abstract class AbstractWindChargeMixin implements ParriedProjectile {
	@Unique
	private boolean parry$parried;

	@Unique
	private UUID parry$target;

	@Override
	public void parry$markParried(UUID target) {
		this.parry$parried = true;
		this.parry$target = target;
	}

	@Override
	public boolean parry$isParried() {
		return this.parry$parried;
	}

	@Override
	public UUID parry$getTarget() {
		return this.parry$target;
	}

	@Inject(method = "onEntityHit", at = @At("HEAD"))
	private void parry$stunBreeze(EntityHitResult result, CallbackInfo ci) {
		if (!this.parry$parried || this.parry$target == null) {
			return;
		}

		Entity hit = result.getEntity();

		if (hit instanceof LivingEntity living && living.getUuid().equals(this.parry$target)) {
			ParryTracker.stun(living, ParryTracker.STUN_TICKS);
			this.parry$parried = false;
		}
	}
}
