package com.example.parry;

import com.example.sound.ModSounds;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Handles the player's melee swing.
 *
 * <ul>
 *     <li>Counter-attacking a mob with an axe during a parry window stuns it.</li>
 *     <li>Striking an incoming ghast fireball reflects it at 2.5x speed and doubles its damage.</li>
 *     <li>Striking an incoming breeze wind charge returns it at 3.0x speed so it stuns the breeze.</li>
 * </ul>
 *
 * <p>All state changes happen on the server. The callback also runs on the client, and mutating
 * shared state there would consume the parry window before the server sees the swing (which broke
 * the melee parry in single-player). The client still reports {@link ActionResult#SUCCESS} for a
 * projectile it would reflect so that vanilla's own attack prediction - including the built-in
 * melee deflection of redirectable projectiles - is skipped and only the server decides.
 */
public final class ProjectileReflectionHandler {
	private static final double FIREBALL_REFLECTION_MULTIPLIER = 2.5;
	private static final double WIND_CHARGE_REFLECTION_MULTIPLIER = 3.0;

	private ProjectileReflectionHandler() {
	}

	public static void init() {
		AttackEntityCallback.EVENT.register(ProjectileReflectionHandler::onAttackEntity);
	}

	private static ActionResult onAttackEntity(PlayerEntity player, World world, Hand hand,
			Entity target, EntityHitResult hitResult) {
		if (target instanceof MobEntity mob) {
			// The parry window only ever exists on the server. The client lets the normal attack run
			// so the server receives the swing and can consume the window itself.
			if (!world.isClient()) {
				ItemStack stack = player.getStackInHand(hand);

				// The axe is required to convert a blocked hit into a stun.
				if (stack.isIn(ItemTags.AXES) && ParryTracker.consumeWindow(player, mob)) {
					applyMeleeParry(player, world, mob);
				}
			}

			// Let the hit resolve normally so the axe still deals its damage.
			return ActionResult.PASS;
		}

		if (target instanceof FireballEntity fireball) {
			// Only a projectile still on its way in can be parried.
			if (!isIncoming(player, fireball)) {
				return ActionResult.PASS;
			}

			if (!world.isClient()) {
				reflectFireball(player, fireball);
			}

			return ActionResult.SUCCESS;
		}

		if (target instanceof AbstractWindChargeEntity windCharge
				&& windCharge.getOwner() instanceof BreezeEntity breeze) {
			if (!isIncoming(player, windCharge)) {
				return ActionResult.PASS;
			}

			if (!world.isClient()) {
				reflectWindCharge(player, windCharge, breeze);
			}

			return ActionResult.SUCCESS;
		}

		// Witches are never reflected: they attack with potions, which are not handled here.
		return ActionResult.PASS;
	}

	/** True while a projectile is still flying towards the player that is trying to parry it. */
	private static boolean isIncoming(PlayerEntity player, Entity projectile) {
		Vec3d toPlayer = player.getEntityPos()
				.add(0.0, player.getHeight() / 2.0, 0.0)
				.subtract(projectile.getEntityPos());

		return projectile.getVelocity().dotProduct(toPlayer) > 0.0;
	}

	private static void applyMeleeParry(PlayerEntity player, World world, MobEntity mob) {
		// Cue for the axe counter-hit that actually applies the stun.
		world.playSound(null, player.getX(), player.getY(), player.getZ(),
				ModSounds.MELEE_STUN, SoundCategory.PLAYERS, 1.0F, 1.0F);

		ParryTracker.stun(mob, ParryTracker.STUN_TICKS);

		Vec3d away = mob.getEntityPos().subtract(player.getEntityPos());

		if (away.lengthSquared() < 1.0E-4) {
			away = player.getRotationVec(1.0F);
		}

		Vec3d knockback = away.normalize().multiply(0.45);
		mob.addVelocity(knockback.x, 0.35, knockback.z);
	}

	private static void reflectFireball(PlayerEntity player, FireballEntity fireball) {
		Entity owner = fireball.getOwner();
		Vec3d aim = owner != null
				? owner.getBoundingBox().getCenter()
				: player.getEyePos().add(player.getRotationVec(1.0F).multiply(12.0));

		Vec3d direction = aim.subtract(fireball.getEntityPos());

		if (direction.lengthSquared() < 1.0E-6) {
			direction = player.getRotationVec(1.0F);
		}

		double speed = Math.max(fireball.getVelocity().length(), 0.25) * FIREBALL_REFLECTION_MULTIPLIER;
		fireball.setVelocity(direction.normalize().multiply(speed));

		player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
				ModSounds.FIREBALL_PARRY, SoundCategory.PLAYERS, 1.0F, 1.0F);

		// Mark the fireball so FireballEntityMixin doubles the damage it deals on the way back.
		((ParriedProjectile) (Object) fireball)
				.parry$markParried(owner != null ? owner.getUuid() : null);
	}

	private static void reflectWindCharge(PlayerEntity player, AbstractWindChargeEntity windCharge, BreezeEntity breeze) {
		Vec3d direction = breeze.getBoundingBox().getCenter().subtract(windCharge.getEntityPos());

		if (direction.lengthSquared() < 1.0E-6) {
			direction = player.getRotationVec(1.0F);
		}

		double speed = Math.max(windCharge.getVelocity().length(), 0.25) * WIND_CHARGE_REFLECTION_MULTIPLIER;
		windCharge.setVelocity(direction.normalize().multiply(speed));

		// Re-own the charge to the player so it is allowed to collide with the breeze.
		windCharge.setOwner(player);
		((ParriedProjectile) (Object) windCharge).parry$markParried(breeze.getUuid());
	}
}
