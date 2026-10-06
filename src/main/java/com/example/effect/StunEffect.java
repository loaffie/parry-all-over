package com.example.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;

/**
 * The {@code stun} effect.
 *
 * <p>The gameplay behaviour (AI, movement and attacks being disabled) is enforced by
 * {@code MobMixin} and {@code LivingEntityMixin}. This class is responsible for the continuous
 * particle feedback shown above the stunned entity's head.
 */
public class StunEffect extends StatusEffect {
	public StunEffect(StatusEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		// Apply every tick so the particles are continuous.
		return true;
	}

	@Override
	public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
		double aboveHead = entity.getY() + entity.getHeight() + 0.45;

		world.spawnParticles(
				ParticleTypes.CRIT,
				entity.getX(),
				aboveHead,
				entity.getZ(),
				4,
				0.35,
				0.15,
				0.35,
				0.02);

		world.spawnParticles(
				ParticleTypes.END_ROD,
				entity.getX(),
				aboveHead + 0.2,
				entity.getZ(),
				1,
				0.15,
				0.05,
				0.15,
				0.0);

		return true;
	}
}
