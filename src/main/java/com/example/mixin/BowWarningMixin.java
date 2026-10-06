package com.example.mixin;

import com.example.sound.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Plays a warning cue the moment a bow-wielding mob starts to draw its bow.
 *
 * <p>Vanilla bow mobs - skeletons and their variants - draw for about a second before the arrow is
 * released, so this lands well before the shot and gives the player time to line up an
 * arrow-vs-arrow parry.
 *
 * <p>{@code LivingEntity#setCurrentHand} is the shared entry point for every "start using an item"
 * action (bows, crossbows, potions, tridents, ...), so the injection filters down to mobs that are
 * actually holding a bow and are aiming at a player.
 */
@Mixin(LivingEntity.class)
public abstract class BowWarningMixin {
	@Inject(method = "setCurrentHand", at = @At("HEAD"))
	private void parry$warnBeforeBowShot(Hand hand, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;

		// Players draw bows the same way; this cue is only for hostile mobs. Server side only so
		// the sound is broadcast once instead of also firing locally on every client.
		if (!(self instanceof MobEntity mob) || self.getEntityWorld().isClient()) {
			return;
		}

		// Crossbows, potions, tridents and so on draw through the same method - only warn for bows.
		if (!mob.isHolding(Items.BOW)) {
			return;
		}

		// Do not warn about shots that are not aimed at a player.
		if (!(mob.getTarget() instanceof PlayerEntity)) {
			return;
		}

		mob.playSound(ModSounds.ARROW_WARNING, 1.0F, 1.0F);
	}
}
