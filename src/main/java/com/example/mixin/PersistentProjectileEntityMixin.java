package com.example.mixin;

import com.example.parry.ParriedArrow;
import com.example.parry.ParryTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
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
 * <p>Every tick a player arrow checks for nearby enemy arrows within half a block. When one is
 * found it is discarded, the shield block sound plays and the player arrow keeps flying. If that
 * same player arrow later strikes the enemy's shooter, the shooter is stunned.
 */
@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements ParriedArrow {
	private static final double PARRY_RANGE = 0.5;

	@Unique
	private UUID parry$skeletonShooter;

	@Override
	public void parry$setSkeletonShooter(UUID shooter) {
		this.parry$skeletonShooter = shooter;
	}

	@Override
	public UUID parry$getSkeletonShooter() {
		return this.parry$skeletonShooter;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void parry$checkArrowCollision(CallbackInfo ci) {
		PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;

		if (self.getEntityWorld().isClient() || !(self.getOwner() instanceof PlayerEntity)) {
			return;
		}

		// Ignore arrows that have come to rest.
		if (self.getVelocity().lengthSquared() < 1.0E-4) {
			return;
		}

		Box searchBox = self.getBoundingBox().expand(PARRY_RANGE);
		List<PersistentProjectileEntity> nearby = self.getEntityWorld().getEntitiesByType(
				TypeFilter.instanceOf(PersistentProjectileEntity.class), searchBox, arrow -> arrow != self);

		for (PersistentProjectileEntity other : nearby) {
			if (other == self) {
				continue;
			}

			if (other.getVelocity().lengthSquared() < 1.0E-4 || other.getOwner() instanceof PlayerEntity) {
				continue;
			}

			if (!(other.getOwner() instanceof LivingEntity shooter) || self.distanceTo(other) > PARRY_RANGE) {
				continue;
			}

			other.discard();
			this.parry$skeletonShooter = shooter.getUuid();
			self.getEntityWorld().playSound(null, self.getX(), self.getY(), self.getZ(),
					SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
			break;
		}
	}

	@Inject(method = "onEntityHit", at = @At("HEAD"))
	private void parry$stunShooter(EntityHitResult result, CallbackInfo ci) {
		if (this.parry$skeletonShooter == null) {
			return;
		}

		Entity hit = result.getEntity();

		if (hit instanceof LivingEntity living && hit.getUuid().equals(this.parry$skeletonShooter)) {
			ParryTracker.stun(living, ParryTracker.STUN_TICKS);
			this.parry$skeletonShooter = null;
		}
	}
}
