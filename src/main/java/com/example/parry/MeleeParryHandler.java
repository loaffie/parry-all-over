package com.example.parry;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;

/**
 * Detects when a player successfully blocks a melee attack from a hostile, non-projectile mob with
 * a shield and opens the counter-attack window.
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

		ParryTracker.openWindow(player, mob);
	}
}
