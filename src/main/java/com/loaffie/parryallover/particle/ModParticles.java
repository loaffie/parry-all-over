package com.loaffie.parryallover.particle;

import com.loaffie.parryallover.ParryMod;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * The particles used by the stun animation. Their textures and behaviour live on the client; the
 * types are registered here so both sides agree on the registry.
 */
public final class ModParticles {
	/** The stars that circle a stunned mob's head. */
	public static final SimpleParticleType DIZZY_STAR = register("dizzy_star");

	/** Sparks for the impact shockwave, the star trails and the recovery pop. */
	public static final SimpleParticleType STUN_SPARK = register("stun_spark");

	private ModParticles() {
	}

	/** Called from the mod initialiser so the types are registered before the registries freeze. */
	public static void init() {
	}

	private static SimpleParticleType register(String path) {
		return Registry.register(Registries.PARTICLE_TYPE, ParryMod.id(path), FabricParticleTypes.simple());
	}
}
