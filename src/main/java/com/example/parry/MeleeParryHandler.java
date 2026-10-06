package com.example.parry;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;

/**
 * Detects when a player successfully blocks a melee attack from a hostile, non-projectile mob with
 * a shield and opens the counter-attack window.
 *
 * <p>Only melee-based mobs qualify. Mobs that attack from range with a projectile (skeletons,
 * pillagers, witches, blazes, ghasts, breezes, ...) are handled by the projectile parry instead and
 * never open a melee window.
 */
public final class MeleeParryHandler {
	private MeleeParryHandler() {
	}

	public static void init() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register(MeleeParryHandler::onAfterDamage);
	}

	private static void onAfterDamage(LivingEntity entity, DamageSource source, float baseDamage,
			float damageTaken, boolean blocked) {
		if (!blocked || !(entity instanceof PlayerEntity player)) {
			return;
		}

		// Only melee attacks qualify; projectiles are handled elsewhere.
		if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
			return;
		}

		if (!(source.getAttacker() instanceof MobEntity mob) || !(mob instanceof Monster)) {
			return;
		}

		// A mob that shoots projectiles is parried with the projectile parry, not the melee one.
		if (shootsProjectiles(mob)) {
			return;
		}

		ParryTracker.openWindow(player, mob);
	}

	/**
	 * Returns {@code true} when the mob attacks from range with a projectile - a bow, crossbow,
	 * potion, trident, fireball, wind charge, wither skull, shulker bullet or dragon fireball.
	 */
	private static boolean shootsProjectiles(MobEntity mob) {
		// Skeletons (plus stray, bogged, parched and wither skeletons), witches, drowned,
		// illusioners, withers, pillagers and piglins all implement this through
		// CrossbowUser or directly.
		if (mob instanceof RangedAttackMob) {
			return true;
		}

		// These bypass the interface and implement their own ranged attack.
		return mob instanceof BlazeEntity
				|| mob instanceof GhastEntity
				|| mob instanceof BreezeEntity
				|| mob instanceof ShulkerEntity
				|| mob instanceof EnderDragonEntity;
	}
}
