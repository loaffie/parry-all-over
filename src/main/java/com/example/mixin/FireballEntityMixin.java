package com.example.mixin;

import com.example.parry.ParriedProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * Doubles the impact damage of a fireball that a player has knocked back.
 *
 * <p>Vanilla {@code FireballEntity.onEntityHit} always deals a flat {@code 6.0F} to whatever it
 * strikes. A fireball marked by a parry deals double that instead.
 */
@Mixin(FireballEntity.class)
public abstract class FireballEntityMixin implements ParriedProjectile {
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

	@Redirect(method = "onEntityHit", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean parry$doubleImpactDamage(Entity target, ServerWorld world, DamageSource source, float amount) {
		return target.damage(world, source, this.parry$parried ? amount * 2.0F : amount);
	}
}
