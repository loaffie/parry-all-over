package com.loaffie.parryallover.mixin;

import com.loaffie.parryallover.parry.ParriedArrow;
import com.loaffie.parryallover.parry.ParryTracker;
import com.loaffie.parryallover.sound.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

/**
 * Arrow versus arrow parrying.
 *
 * <p>Every tick a player's arrow checks whether its swept hitbox ran through an enemy arrow. If it
 * did, that arrow is discarded and the player's arrow locks onto the shooter and homes in until it
 * connects. The stun goes on the shooter when it lands.
 */
@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements ParriedArrow {
	/** Slack, in blocks, around the sweeping hitbox when looking for a clash. */
	private static final double PARRY_RANGE = 0.5;

	/** How long a parried arrow keeps homing before it gives up. */
	private static final int HOMING_TICKS = 60;

	/** Past this distance, in blocks, homing stops. */
	private static final double HOMING_RANGE = 64.0;

	@Unique
	private UUID parry$shooter;

	@Unique
	private int parry$homingTicks;

	@Override
	public void parry$setSkeletonShooter(UUID shooter) {
		this.parry$shooter = shooter;
		this.parry$homingTicks = 0;
	}

	@Override
	public UUID parry$getSkeletonShooter() {
		return this.parry$shooter;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void parry$tick(CallbackInfo ci) {
		PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;

		if (self.getEntityWorld().isClient() || !(self.getOwner() instanceof PlayerEntity)) {
			return;
		}

		// One or the other: an arrow that already clashed is chasing its target.
		if (this.parry$shooter != null) {
			this.parry$homeOntoTarget(self);
			return;
		}

		this.parry$checkArrowClash(self);
	}

	/** Steers a parried arrow at its target, so a long shot can't be beaten by gravity. */
	@Unique
	private void parry$homeOntoTarget(PersistentProjectileEntity self) {
		if (!(self.getEntityWorld() instanceof ServerWorld world)) {
			this.parry$shooter = null;
			return;
		}

		Entity target = world.getEntity(this.parry$shooter);

		if (!(target instanceof LivingEntity living) || !living.isAlive()) {
			this.parry$shooter = null;
			return;
		}

		double distanceSquared = self.getEntityPos().subtract(target.getEntityPos()).lengthSquared();
		boolean resting = self.getVelocity().lengthSquared() < 1.0E-4;
		boolean tooFar = distanceSquared > HOMING_RANGE * HOMING_RANGE;

		if (resting || tooFar || ++this.parry$homingTicks > HOMING_TICKS) {
			this.parry$shooter = null;
			return;
		}

		Vec3d direction = target.getBoundingBox().getCenter().subtract(self.getEntityPos());

		if (direction.lengthSquared() > 1.0E-6) {
			double speed = Math.max(self.getVelocity().length(), 1.0);
			self.setVelocity(direction.normalize().multiply(speed));
		}
	}

	/** Discards an enemy arrow the player's arrow has run into. */
	@Unique
	private void parry$checkArrowClash(PersistentProjectileEntity self) {
		// A stuck arrow can't parry anything.
		if (self.getVelocity().lengthSquared() < 1.0E-4) {
			return;
		}

		// Sweep the arrow's travel for this tick; without it two fast arrows can tunnel straight
		// through each other between sampled positions.
		Box swept = self.getBoundingBox().stretch(self.getVelocity()).expand(PARRY_RANGE);

		List<PersistentProjectileEntity> nearby = self.getEntityWorld().getEntitiesByType(
				TypeFilter.instanceOf(PersistentProjectileEntity.class), swept, arrow -> arrow != self);

		for (PersistentProjectileEntity other : nearby) {
			if (other == self
					|| other.getVelocity().lengthSquared() < 1.0E-4
					|| other.getOwner() instanceof PlayerEntity
					|| !(other.getOwner() instanceof LivingEntity shooter)) {
				continue;
			}

			if (!swept.intersects(other.getBoundingBox())) {
				continue;
			}

			other.discard();
			this.parry$setSkeletonShooter(shooter.getUuid());
			self.getEntityWorld().playSound(null, self.getX(), self.getY(), self.getZ(),
					ModSounds.ARROW_PARRY, SoundCategory.PLAYERS, 1.0F, 1.0F);
			break;
		}
	}

	@Inject(method = "onEntityHit", at = @At("HEAD"))
	private void parry$stunShooter(EntityHitResult result, CallbackInfo ci) {
		PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;

		if (self.getEntityWorld().isClient() || this.parry$shooter == null) {
			return;
		}

		Entity hit = result.getEntity();

		if (hit instanceof LivingEntity living && hit.getUuid().equals(this.parry$shooter)) {
			ParryTracker.stun(living, ParryTracker.STUN_TICKS);
			this.parry$shooter = null;
		}
	}
}
