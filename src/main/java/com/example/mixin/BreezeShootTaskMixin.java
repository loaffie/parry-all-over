package com.example.mixin;

import com.example.sound.ModSounds;
import net.minecraft.entity.ai.brain.task.BreezeShootTask;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Plays a warning cue when a breeze starts winding up its wind charge.
 *
 * <p>Once this task starts the breeze "charges" for 15 ticks (0.75 s) before the wind charge is
 * actually launched, so the cue fires before the shot and gives the player time to prepare the
 * deflection.
 */
@Mixin(BreezeShootTask.class)
public abstract class BreezeShootTaskMixin {
	@Inject(
			method = "run(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/BreezeEntity;J)V",
			at = @At("HEAD"))
	private void parry$warnBeforeWindCharge(ServerWorld world, BreezeEntity breeze, long time, CallbackInfo ci) {
		// Only warn about charges aimed at a player.
		if (!(breeze.getTarget() instanceof PlayerEntity)) {
			return;
		}

		breeze.playSound(ModSounds.BREEZE_WARNING, 1.0F, 1.0F);
	}
}
