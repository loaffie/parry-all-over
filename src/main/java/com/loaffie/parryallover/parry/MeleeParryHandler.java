package com.loaffie.parryallover.parry;

import com.loaffie.parryallover.sound.ModSounds;
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
import net.minecraft.sound.SoundCategory;

/**
 * Opens the counter-attack window when a player blocks a melee hit from a hostile mob.
 *
 * <p>Ranged mobs are deliberately excluded - those are handled by the projectile parry instead, so
 * a skeleton that walks into you isn't parried by the melee path.
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

		// Projectiles are the other parry path.
		if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
			return;
		}

		if (!(source.getAttacker() instanceof MobEntity mob) || !(mob instanceof Monster)) {
			return;
		}

		if (shootsProjectiles(mob)) {
			return;
		}

		ParryTracker.openWindow(player, mob);

		// Sits on top of the vanilla shield sound, so keep it short.
		player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
				ModSounds.MELEE_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
	}

	/**
	 * Whether the mob attacks from range with a projectile (bow, crossbow, potion, trident,
	 * fireball, wind charge, wither skull, shulker bullet, dragon fireball).
	 */
	private static boolean shootsProjectiles(MobEntity mob) {
		// Skeletons and their variants, witches, drowned, illusioners, withers, pillagers and
		// piglins all go through CrossbowUser, which implements this.
		if (mob instanceof RangedAttackMob) {
			return true;
		}

		// These fire their own projectiles without the interface.
		return mob instanceof BlazeEntity
				|| mob instanceof GhastEntity
				|| mob instanceof BreezeEntity
				|| mob instanceof ShulkerEntity
				|| mob instanceof EnderDragonEntity;
	}
}
