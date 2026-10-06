package com.example.mixin;

import com.example.parry.ParriedArrow;
import com.example.parry.ParryTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
 * Arrow-vs-arrow parrying.
 *
 * <p>Every tick a player arrow checks whether its swept hitbox clashes with an enemy arrow. When it
 * does, that arrow is discarded and the player's arrow locks onto the shooter, homing in so it
 * reliably lands. Rolling the shooter then stuns it.
 */
@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements ParriedArrow {
	/** Extra tolerance, in blocks, around the sweeping arrow box when testing for a clash. */
	private static final double PARRY_RANGE = 0.5;

	/** Maximum number of ticks a parried arrow keeps homing onto its target before giving up. */
	private static final int HOMING_TICKS = 60;

	/** Beyond this distance, in blocks, a parried arrow stops homing. */
	private static final double HOMING_RANGE = 64.0;

	@Unique
	private UUID parry$skeletonShooter;

	@Unique
	private int parry$homingTicks;

	@Override
	public void parry$setSkeletonShooter(UUID shooter) {
		this.parry$skeletonShooter = shooter;
		this.parry$homingTicks = 0;
	}

	@Override
	public UUID parry$getSkeletonShooter() {
		return this.parry$skeletonShooter;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void parry$tick(CallbackInfo ci) {
		PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;

		if (self.getEntityWorld().isClient() || !(self.getOwner() instanceof PlayerEntity)) {
			return;
		}

		if (this.parry$skeletonShooter != null) {
			this.parry$homeOntoTarget(self);
			return;
		}

		this.parry$checkArrowClash(self);
	}

	/** Steers a parried arrow at its recorded target so the roll cannot be dodged by gravity. */
	@Unique
	private void parry$homeOntoTarget(PersistentProjectileEntity self) {
		if (!(self.getEntityWorld() instanceof ServerWorld world)) {
			this.parry$skeletonShooter = null;
			return;
		}

		Entity target = world.getEntity(this.parry$skeletonShooter);

		if (!(target instanceof LivingEntity living) || !living.isAlive()) {
			this.parry$skeletonShooter = null;
			return;
		}

		double distanceSquared = self.getEntityPos().subtract(target.getEntityPos()).lengthSquared();
		boolean resting = self.getVelocity().lengthSquared() < 1.0E-4;
		boolean outOfRange = distanceSquared > HOMING_RANGE * HOMING_RANGE;

		if (resting || outOfRange || ++this.parry$homingTicks > HOMING_TICKS) {
			this.parry$skeletonShooter = null;
			return;
		}

		Vec3d direction = target.getBoundingBox().getCenter().subtract(self.getEntityPos());

		if (direction.lengthSquared() > 1.0E-6) {
			double speed = Math.max(self.getVelocity().length(), 1.0);
			self.setVelocity(direction.normalize().multiply(speed));
		}
	}

	/** Discards an enemy arrow whose swept hitbox the player's arrow has run into. */
	@Unique
	private void parry$checkArrowClash(PersistentProjectileEntity self) {
		// A resting arrow cannot parry anything.
		if (self.getVelocity().lengthSquared() < 1.0E-4) {
			return;
		}

		// Sweep the arrow's own movement for this tick so fast arrows cannot tunnel through one
		// another without the two hitboxes ever intersecting on a sampled position.
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
					SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
			break;
		}
	}

	@Inject(method = "onEntityHit", at = @At("HEAD"))
	private void parry$stunShooter(EntityHitResult result, CallbackInfo ci) {
		PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;

		if (self.getEntityWorld().isClient() || this.parry$skeletonShooter == null) {
			return;
		}

		Entity hit = result.getEntity();

		if (hit instanceof LivingEntity living && hit.getUuid().equals(this.parry$skeletonShooter)) {
			ParryTracker.stun(living, ParryTracker.STUN_TICKS);
			this.parry$skeletonShooter = null;
		}
	}
}
