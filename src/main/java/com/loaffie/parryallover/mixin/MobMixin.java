package com.loaffie.parryallover.mixin;

import com.loaffie.parryallover.ParryMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * While stunned a mob does nothing: AI, navigation and targeting are skipped and attacks refused.
 */
@Mixin(MobEntity.class)
public abstract class MobMixin {
	@Inject(method = "tickNewAi", at = @At("HEAD"), cancellable = true)
	private void parry$disableAi(CallbackInfo ci) {
		MobEntity self = (MobEntity) (Object) this;

		if (self.hasStatusEffect(ParryMod.STUN)) {
			self.setTarget(null);
			self.getNavigation().stop();
			ci.cancel();
		}
	}

	@Inject(method = "tryAttack", at = @At("HEAD"), cancellable = true)
	private void parry$disableMelee(ServerWorld world, Entity target, CallbackInfoReturnable<Boolean> cir) {
		MobEntity self = (MobEntity) (Object) this;

		if (self.hasStatusEffect(ParryMod.STUN)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	private void parry$disableTargeting(LivingEntity target, CallbackInfo ci) {
		MobEntity self = (MobEntity) (Object) this;

		if (target != null && self.hasStatusEffect(ParryMod.STUN)) {
			ci.cancel();
		}
	}
}
