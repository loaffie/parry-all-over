package com.loaffie.parryallover.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * The stun effect. Freezing the mob's AI and movement is done in the mixins, and the animation is
 * played on the client (see {@code StunAnimations}) from a single packet sent when the stun lands,
 * so the effect itself never needs to tick.
 */
public class StunEffect extends StatusEffect {
	public StunEffect(StatusEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return false;
	}
}
