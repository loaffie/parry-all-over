package com.loaffie.parryallover.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;

/**
 * The stun effect. Freezing the mob's AI and movement is done in the mixins; this class only
 * handles the particles that show above its head.
 */
public class StunEffect extends StatusEffect {
	public StunEffect(StatusEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		// Every tick, otherwise the particles come out in fits.
		return true;
	}

	@Override
	public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
		double y = entity.getY() + entity.getHeight() + 0.45;

		world.spawnParticles(ParticleTypes.CRIT, entity.getX(), y, entity.getZ(), 4, 0.35, 0.15, 0.35, 0.02);
		world.spawnParticles(ParticleTypes.END_ROD, entity.getX(), y + 0.2, entity.getZ(), 1, 0.15, 0.05, 0.15, 0.0);

		return true;
	}
}
