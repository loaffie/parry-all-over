package com.example.mixin;

import com.example.sound.DeferredSounds;
import com.example.sound.ModSounds;
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
 * Schedules the arrow warning a few ticks before a bow-wielding mob releases its arrow.
 *
 * <p>Vanilla bow mobs - skeletons and their variants - draw for a fixed 20 ticks before firing, and
 * {@code LivingEntity#setCurrentHand} is the shared "start using an item" entry point the bow goal
 * calls exactly once per shot. From there the cue is scheduled for {@code 20 - lead} ticks, so it
 * lands right before the arrow flies rather than at the start of the draw.
 *
 * <p>The injection is filtered to mobs that are holding a bow and aiming at a player, so crossbows,
 * potions, tridents and player bow draws do not trigger it.
 */
@Mixin(LivingEntity.class)
public abstract class BowWarningMixin {
	/** Vanilla bow draw length in ticks (see {@code BowAttackGoal}, which fires at item use time 20). */
	private static final int BOW_DRAW_TICKS = 20;

	@Inject(method = "setCurrentHand", at = @At("HEAD"))
	private void parry$scheduleBowWarning(Hand hand, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;

		if (!(self instanceof MobEntity mob) || !(self.getEntityWorld() instanceof ServerWorld world)) {
			return;
		}

		// Only warn for an actual bow aimed at a player.
		if (!mob.isHolding(Items.BOW) || !(mob.getTarget() instanceof PlayerEntity)) {
			return;
		}

		DeferredSounds.playLater(world, mob.getX(), mob.getY(), mob.getZ(), ModSounds.ARROW_WARNING,
				SoundCategory.HOSTILE, BOW_DRAW_TICKS - ModSounds.WARNING_LEAD_TICKS, 1.0F, 1.0F);
	}
}
