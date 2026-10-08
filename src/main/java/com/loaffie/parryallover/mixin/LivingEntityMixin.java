package com.loaffie.parryallover.mixin;

import com.loaffie.parryallover.ParryMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Wipes a stunned mob's walking input. Velocity is left alone, so knockback and gravity still
 * apply and the stun shove stays visible.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
	private Vec3d parry$freezeMovementInput(Vec3d input) {
		LivingEntity self = (LivingEntity) (Object) this;

		return self.hasStatusEffect(ParryMod.STUN) ? Vec3d.ZERO : input;
	}
}
