package com.loaffie.parryallover.parry;

import com.loaffie.parryallover.sound.ModSounds;
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
 * Handles the player's swing.
 *
 * <ul>
 *   <li>An axe hit during a parry window stuns the mob.</li>
 *   <li>A ghast fireball is sent back at 2.5x speed and double damage.</li>
 *   <li>A breeze wind charge is returned at 3.0x speed and stuns the breeze.</li>
 * </ul>
 *
 * <p>Everything is done server side. The callback also fires on the client, and touching the parry
 * window there would eat it before the server ever saw the swing (which broke the melee parry in
 * single player). The client still answers SUCCESS for projectiles so vanilla's own deflection is
 * skipped and the server is the only one deciding.
 */
public final class ProjectileReflectionHandler {
	private static final double FIREBALL_SPEED = 2.5;
	private static final double WIND_CHARGE_SPEED = 3.0;

	private ProjectileReflectionHandler() {
	}

	public static void init() {
		AttackEntityCallback.EVENT.register(ProjectileReflectionHandler::onAttackEntity);
	}

	private static ActionResult onAttackEntity(PlayerEntity player, World world, Hand hand,
			Entity target, EntityHitResult hitResult) {
		if (target instanceof MobEntity mob) {
			// The window only exists on the server, so the client lets the swing through normally
			// and the server consumes it.
			if (!world.isClient()) {
				ItemStack stack = player.getStackInHand(hand);

				// Only an axe turns a blocked hit into a stun.
				if (stack.isIn(ItemTags.AXES) && ParryTracker.consumeWindow(player, mob)) {
					applyMeleeParry(player, world, mob);
				}
			}

			// Let the hit resolve so the axe still does its damage.
			return ActionResult.PASS;
		}

		if (target instanceof FireballEntity fireball) {
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

		// Witch potions are not handled.
		return ActionResult.PASS;
	}

	/** True while the projectile is still flying towards the player trying to parry it. */
	private static boolean isIncoming(PlayerEntity player, Entity projectile) {
		Vec3d toPlayer = player.getEntityPos()
				.add(0.0, player.getHeight() / 2.0, 0.0)
				.subtract(projectile.getEntityPos());

		return projectile.getVelocity().dotProduct(toPlayer) > 0.0;
	}

	private static void applyMeleeParry(PlayerEntity player, World world, MobEntity mob) {
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
		Entity shooter = fireball.getOwner();
		Vec3d aim = shooter != null
				? shooter.getBoundingBox().getCenter()
				: player.getEyePos().add(player.getRotationVec(1.0F).multiply(12.0));

		Vec3d direction = aim.subtract(fireball.getEntityPos());

		if (direction.lengthSquared() < 1.0E-6) {
			direction = player.getRotationVec(1.0F);
		}

		double speed = Math.max(fireball.getVelocity().length(), 0.25) * FIREBALL_SPEED;
		fireball.setVelocity(direction.normalize().multiply(speed));

		// Hand the fireball over to whoever sent it back, the same way vanilla does when you punch
		// a projectile. Without this the explosion still counts as the ghast's own and nobody gets
		// credited for the kill.
		fireball.setOwner(player);

		player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
				ModSounds.FIREBALL_PARRY, SoundCategory.PLAYERS, 1.0F, 1.0F);

		// Tells FireballEntityMixin to double the impact damage on the way back.
		((ParriedProjectile) (Object) fireball).parry$markParried(shooter != null ? shooter.getUuid() : null);
	}

	private static void reflectWindCharge(PlayerEntity player, AbstractWindChargeEntity windCharge, BreezeEntity breeze) {
		Vec3d direction = breeze.getBoundingBox().getCenter().subtract(windCharge.getEntityPos());

		if (direction.lengthSquared() < 1.0E-6) {
			direction = player.getRotationVec(1.0F);
		}

		double speed = Math.max(windCharge.getVelocity().length(), 0.25) * WIND_CHARGE_SPEED;
		windCharge.setVelocity(direction.normalize().multiply(speed));

		// The breeze has to stop owning it, or the charge will never collide with it.
		windCharge.setOwner(player);
		((ParriedProjectile) (Object) windCharge).parry$markParried(breeze.getUuid());
	}
}
