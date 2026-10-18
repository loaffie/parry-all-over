package com.loaffie.parryallover.client.particle;

import com.loaffie.parryallover.client.StunAnimations;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/**
 * One of the stars circling a stunned mob's head.
 *
 * <p>Unlike a normal particle it is tied to its mob: every tick it works out its spot on the orbit
 * around the mob's current position, so the ring follows the mob through knockback and falls and
 * the renderer smooths it between ticks. It pops in, wobbles on a tilted ring, twinkles through its
 * sprite frames, and shrinks away as the stun runs out.
 */
public class DizzyStarParticle extends BillboardParticle {
	/** Radians per tick around the head. */
	private static final float ORBIT_SPEED = 0.32F;

	/** How long the pop-in takes, in ticks. */
	private static final int POP_TICKS = 6;

	/** Over the last this many ticks of the stun the star shrinks away. */
	private static final int FADE_TICKS = 8;

	private final LivingEntity anchor;
	private final SpriteProvider sprites;
	private final float phase;
	private final float baseScale;

	public DizzyStarParticle(ClientWorld world, LivingEntity anchor, float phase, float red, float green,
			float blue, SpriteProvider sprites) {
		super(world, anchor.getX(), anchor.getY() + anchor.getHeight(), anchor.getZ(), sprites.getFirst());
		this.anchor = anchor;
		this.sprites = sprites;
		this.phase = phase;
		this.baseScale = 0.14F + Math.min(anchor.getWidth(), 2.0F) * 0.04F;
		this.scale = 0.0F;
		this.maxAge = 20 * 60;
		this.gravityStrength = 0.0F;
		this.collidesWithWorld = false;
		this.velocityX = 0.0;
		this.velocityY = 0.0;
		this.velocityZ = 0.0;
		this.setColor(red, green, blue);

		this.moveToOrbit();
		this.lastX = this.x;
		this.lastY = this.y;
		this.lastZ = this.z;
	}

	@Override
	public void tick() {
		this.lastX = this.x;
		this.lastY = this.y;
		this.lastZ = this.z;
		this.lastZRotation = this.zRotation;

		int remaining = StunAnimations.remainingTicks(this.anchor);

		if (remaining <= 0 || !this.anchor.isAlive() || this.anchor.isRemoved() || this.age++ >= this.maxAge) {
			this.markDead();
			return;
		}

		this.moveToOrbit();

		// Spin against the orbit, so the star tumbles as it goes round.
		this.zRotation -= 0.22F;

		// Twinkle: walk the sprite frames back and forth.
		int frame = (this.age / 3) % 6;
		this.setSprite(this.sprites.getSprite(frame < 4 ? frame : 6 - frame, 3));

		float pop = this.age < POP_TICKS ? popCurve(this.age / (float) POP_TICKS) : 1.0F;
		float fade = remaining < FADE_TICKS ? remaining / (float) FADE_TICKS : 1.0F;
		this.scale = this.baseScale * pop * fade;

		// Leave a short trail of sparkles behind, every other tick so it stays light.
		if ((this.age & 1) == 0 && this.age > POP_TICKS && remaining > FADE_TICKS) {
			StunAnimations.spawnTrailSpark(this.world, this.x, this.y, this.z, this.red, this.green, this.blue);
		}
	}

	/** Places the star on its orbit around the mob's head for this tick. */
	private void moveToOrbit() {
		float angle = this.phase + this.age * ORBIT_SPEED;
		double radius = Math.max(0.38, this.anchor.getWidth() * 0.55 + 0.12);

		// The ring is tilted and slowly precesses, which reads as "dizzy" rather than a neat halo.
		double tilt = 0.12 * MathHelper.sin(this.age * 0.09F);
		double bob = 0.05 * MathHelper.sin(angle * 2.0F);

		double x = this.anchor.getX() + MathHelper.cos(angle) * radius;
		double z = this.anchor.getZ() + MathHelper.sin(angle) * radius;
		double y = this.anchor.getY() + this.anchor.getHeight() + 0.28 + bob + tilt * MathHelper.sin(angle);

		this.setPos(x, y, z);
	}

	/** Overshoots past full size and settles back, for a springy pop. */
	private static float popCurve(float t) {
		float c = 1.70158F + 1.0F;
		float u = t - 1.0F;
		return 1.0F + c * u * u * u + 1.70158F * u * u;
	}

	@Override
	public int getBrightness(float tint) {
		// Glows in the dark, like a cartoon.
		return LightmapTextureManager.MAX_LIGHT_COORDINATE;
	}

	@Override
	protected RenderType getRenderType() {
		return RenderType.PARTICLE_ATLAS_TRANSLUCENT;
	}
}
