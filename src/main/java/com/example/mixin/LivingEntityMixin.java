package com.example.mixin;

import com.example.ParryMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Zeroes a stunned entity's intended movement input.
 *
 * <p>Only the walking input is removed; the entity's velocity is left untouched so knockback and
 * gravity still apply and stun knockback remains visible.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
	private Vec3d parry$freezeMovementInput(Vec3d input) {
		LivingEntity self = (LivingEntity) (Object) this;

		if (self.hasStatusEffect(ParryMod.STUN)) {
			return Vec3d.ZERO;
		}

		return input;
	}
}
