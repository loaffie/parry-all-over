package com.loaffie.parryallover.mixin;

import com.loaffie.parryallover.sound.DeferredSounds;
import com.loaffie.parryallover.sound.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Queues the arrow warning a few ticks before a bow mob shoots.
 *
 * <p>{@code setCurrentHand} is the shared "start using an item" entry point the bow goal calls once
 * per shot, so queueing {@code 20 - lead} ticks from there lands the cue just before the arrow
 * flies rather than at the start of the draw. Filtered to mobs holding a bow and aiming at a
 * player, so crossbows, potions, tridents and the player's own bow stay quiet.
 */
@Mixin(LivingEntity.class)
public abstract class BowWarningMixin {
	/** Vanilla bow draw length (BowAttackGoal fires at item use time 20). */
	private static final int BOW_DRAW_TICKS = 20;

	@Inject(method = "setCurrentHand", at = @At("HEAD"))
	private void parry$queueBowWarning(Hand hand, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;

		if (!(self instanceof MobEntity mob) || !(self.getEntityWorld() instanceof ServerWorld world)) {
			return;
		}

		if (!mob.isHolding(Items.BOW) || !(mob.getTarget() instanceof PlayerEntity)) {
			return;
		}

		DeferredSounds.playLater(world, mob.getX(), mob.getY(), mob.getZ(), ModSounds.ARROW_WARNING,
				SoundCategory.HOSTILE, BOW_DRAW_TICKS - ModSounds.WARNING_LEAD_TICKS, 1.0F, 1.0F);
	}
}
