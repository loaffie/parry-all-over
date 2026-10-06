package com.example.mixin;

import com.example.sound.DeferredSounds;
import com.example.sound.ModSounds;
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
 * Schedules the wind charge warning a few ticks before a breeze actually launches the charge.
 *
 * <p>{@code BreezeShootTask} starts charging in {@code run} and fires the charge a fixed 15 ticks
 * later, so the cue is scheduled for {@code 15 - lead} ticks from the start of the wind-up. That
 * puts it just before the launch instead of at the beginning of the charge.
 */
@Mixin(BreezeShootTask.class)
public abstract class BreezeShootTaskMixin {
	/** Vanilla breeze charge length in ticks (see {@code BreezeShootTask.SHOOT_CHARGING_EXPIRY}). */
	private static final int WIND_CHARGE_CHARGE_TICKS = 15;

	@Inject(
			method = "run(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/BreezeEntity;J)V",
			at = @At("HEAD"))
	private void parry$scheduleWindChargeWarning(ServerWorld world, BreezeEntity breeze, long time, CallbackInfo ci) {
		// Only warn about charges aimed at a player.
		if (!(breeze.getTarget() instanceof PlayerEntity)) {
			return;
		}

		DeferredSounds.playLater(world, breeze.getX(), breeze.getY(), breeze.getZ(), ModSounds.BREEZE_WARNING,
				SoundCategory.HOSTILE, WIND_CHARGE_CHARGE_TICKS - ModSounds.WARNING_LEAD_TICKS, 1.0F, 1.0F);
	}
}
