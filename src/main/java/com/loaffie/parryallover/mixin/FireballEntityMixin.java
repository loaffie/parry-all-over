package com.loaffie.parryallover.mixin;

import com.loaffie.parryallover.parry.ParriedProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * Boosts the impact damage of a fireball a player has knocked back.
 *
 * <p>Vanilla always deals a flat 6.0 to whatever the fireball hits. A parried one deals double
 * that, and is credited to whoever knocked it back instead of the mob that fired it.
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
	private boolean parry$impactDamage(Entity target, ServerWorld world, DamageSource source, float amount) {
		if (!this.parry$parried) {
			return target.damage(world, source, amount);
		}

		return target.damage(world, parry$attributedSource(world, target, source), amount * 2.0F);
	}

	/**
	 * A parried fireball normally keeps the {@code fireball} damage type, but that one counts as
	 * fire damage and fire-immune mobs (ghasts, blazes, magma cubes, withers...) shrug it off
	 * completely - which made the fireball parry useless against the very thing that fired it.
	 * When the original would be ignored, hand the hit over to whoever sent it back.
	 */
	@Unique
	private DamageSource parry$attributedSource(ServerWorld world, Entity target, DamageSource source) {
		if (!(target instanceof LivingEntity living) || !living.isInvulnerableTo(world, source)) {
			return source;
		}

		Entity owner = ((ProjectileEntity) (Object) this).getOwner();

		if (owner instanceof PlayerEntity player) {
			return world.getDamageSources().playerAttack(player);
		}

		if (owner instanceof LivingEntity livingOwner) {
			return world.getDamageSources().mobAttack(livingOwner);
		}

		return source;
	}
}
