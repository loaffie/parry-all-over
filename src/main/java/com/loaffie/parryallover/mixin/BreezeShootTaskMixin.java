package com.loaffie.parryallover.mixin;

import com.loaffie.parryallover.sound.DeferredSounds;
import com.loaffie.parryallover.sound.ModSounds;
import net.minecraft.entity.ai.brain.task.BreezeShootTask;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Queues the wind charge warning a few ticks before the breeze actually fires.
 *
 * <p>{@code run} starts the charge and the shot goes out a fixed 15 ticks later, so the cue is
 * queued for {@code 15 - lead} ticks from the start of the wind-up.
 */
@Mixin(BreezeShootTask.class)
public abstract class BreezeShootTaskMixin {
	/** Vanilla charge length (BreezeShootTask.SHOOT_CHARGING_EXPIRY). */
	private static final int WIND_CHARGE_CHARGE_TICKS = 15;

	@Inject(
			method = "run(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/BreezeEntity;J)V",
			at = @At("HEAD"))
	private void parry$queueWindChargeWarning(ServerWorld world, BreezeEntity breeze, long time, CallbackInfo ci) {
		if (!(breeze.getTarget() instanceof PlayerEntity)) {
			return;
		}

		DeferredSounds.playLater(world, breeze.getX(), breeze.getY(), breeze.getZ(), ModSounds.BREEZE_WARNING,
				SoundCategory.HOSTILE, WIND_CHARGE_CHARGE_TICKS - ModSounds.WARNING_LEAD_TICKS, 1.0F, 1.0F);
	}
}
