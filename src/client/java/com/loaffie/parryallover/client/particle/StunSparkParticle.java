package com.loaffie.parryallover.client.particle;

import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

/**
 * A small four-pointed spark. Flies off with the velocity it was given, slows down quickly, and
 * shrinks through its frames. Used for the stun shockwave, the star trails and the recovery pop.
 */
public class StunSparkParticle extends BillboardParticle {
	private final SpriteProvider sprites;

	protected StunSparkParticle(ClientWorld world, double x, double y, double z, double velocityX,
			double velocityY, double velocityZ, SpriteProvider sprites, Random random) {
		super(world, x, y, z, velocityX, velocityY, velocityZ, sprites.getFirst());
		this.sprites = sprites;
		// The base constructor adds its own jitter; keep exactly what was asked for.
		this.velocityX = velocityX;
		this.velocityY = velocityY;
		this.velocityZ = velocityZ;
		this.velocityMultiplier = 0.8F;
		this.gravityStrength = 0.0F;
		this.collidesWithWorld = false;
		this.maxAge = 8 + random.nextInt(6);
		this.scale = 0.06F + random.nextFloat() * 0.03F;
		this.zRotation = random.nextFloat() * 6.2831855F;
		this.lastZRotation = this.zRotation;
		this.updateSprite(sprites);
	}

	@Override
	public void tick() {
		super.tick();

		if (this.isAlive()) {
			this.updateSprite(this.sprites);
			this.setAlpha(1.0F - (float) this.age / this.maxAge * 0.6F);
		}
	}

	@Override
	public int getBrightness(float tint) {
		return LightmapTextureManager.MAX_LIGHT_COORDINATE;
	}

	@Override
	protected RenderType getRenderType() {
		return RenderType.PARTICLE_ATLAS_TRANSLUCENT;
	}

	/** Spawns sparks from the particle type (e.g. {@code /particle}); these come out gold. */
	public record Factory(SpriteProvider sprites) implements ParticleFactory<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y,
				double z, double velocityX, double velocityY, double velocityZ, Random random) {
			StunSparkParticle spark = new StunSparkParticle(world, x, y, z, velocityX, velocityY, velocityZ,
					this.sprites, random);
			spark.setColor(1.0F, 0.86F, 0.35F);
			return spark;
		}
	}

	/** Builds a spark directly with a chosen colour; used by the stun animation. */
	public static StunSparkParticle create(ClientWorld world, double x, double y, double z, double velocityX,
			double velocityY, double velocityZ, float red, float green, float blue, SpriteProvider sprites) {
		StunSparkParticle spark = new StunSparkParticle(world, x, y, z, velocityX, velocityY, velocityZ,
				sprites, world.getRandom());
		spark.setColor(red, green, blue);
		return spark;
	}
}
