package com.loaffie.parryallover.client;

import com.loaffie.parryallover.client.particle.DizzyStarParticle;
import com.loaffie.parryallover.client.particle.StunSparkParticle;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

/**
 * Plays the stun animation on the client.
 *
 * <p>The server sends one packet when a mob is stunned; from there everything is local:
 * <ol>
 *   <li><b>Impact</b> - a flat shockwave ring of sparks bursts out at head height, with a few
 *   flung upwards.</li>
 *   <li><b>Dizzy</b> - three stars circle the mob's head on a tilted, wobbling ring, leaving a
 *   sparkle trail, while its head rolls around in a slow daze.</li>
 *   <li><b>Recover</b> - the stars shrink away, the head settles back, and a small pop of sparks
 *   marks the mob coming to its senses.</li>
 * </ol>
 *
 * <p>Timing is tracked against the world clock, so it pauses with the game and lines up with the
 * server's stun even if the packet arrives a tick late.
 */
public final class StunAnimations {
	/** Star tints: gold, the stun effect's lavender, and a pale cream. */
	private static final float[][] STAR_COLORS = {
			{1.0F, 0.84F, 0.22F},
			{0.66F, 0.55F, 1.0F},
			{1.0F, 0.96F, 0.78F},
	};

	private static final int STAR_COUNT = STAR_COLORS.length;

	/** Sparks in the impact shockwave ring. */
	private static final int RING_SPARKS = 24;

	/** How far the head swings either way while dazed, in degrees. */
	private static final float WOBBLE_YAW = 24.0F;
	private static final float WOBBLE_PITCH = 14.0F;

	/** How much the head droops while dazed, in degrees. */
	private static final float DROOP_PITCH = 12.0F;

	/** Over the last this many ticks the wobble eases back to rest. */
	private static final int SETTLE_TICKS = 8;

	private static final Int2ObjectMap<Stun> ACTIVE = new Int2ObjectOpenHashMap<>();

	private static SpriteProvider starSprites;
	private static SpriteProvider sparkSprites;

	private StunAnimations() {
	}

	static void setSprites(SpriteProvider stars, SpriteProvider sparks) {
		if (stars != null) {
			starSprites = stars;
		}

		if (sparks != null) {
			sparkSprites = sparks;
		}
	}

	/** Called when the server says {@code entityId} was stunned for {@code ticks}. */
	static void onStun(MinecraftClient client, int entityId, int ticks) {
		ClientWorld world = client.world;

		if (world == null || ticks <= 0 || !(world.getEntityById(entityId) instanceof LivingEntity entity)) {
			return;
		}

		long end = world.getTime() + ticks;
		Stun existing = ACTIVE.get(entityId);

		if (existing != null && existing.entity == entity) {
			// Stunned again before it wore off: keep the stars going and just extend the clock.
			existing.end = Math.max(existing.end, end);
			impact(client, world, entity);
			return;
		}

		Stun stun = new Stun(entity, world.getTime(), end);
		ACTIVE.put(entityId, stun);

		impact(client, world, entity);
		spawnStars(client, world, entity);
	}

	/** Ticks left on the animation for {@code entity}, or 0 if it isn't stunned. */
	public static int remainingTicks(Entity entity) {
		Stun stun = ACTIVE.get(entity.getId());

		if (stun == null || stun.entity != entity) {
			return 0;
		}

		return (int) Math.max(0L, stun.end - entity.getEntityWorld().getTime());
	}

	/** A spark left hanging behind a star, drifting up a touch as it fades. */
	public static void spawnTrailSpark(ClientWorld world, double x, double y, double z, float red, float green,
			float blue) {
		if (sparkSprites == null) {
			return;
		}

		MinecraftClient.getInstance().particleManager.addParticle(StunSparkParticle.create(
				world, x, y, z, 0.0, 0.01, 0.0, red, green, blue, sparkSprites));
	}

	static void tick(MinecraftClient client) {
		ClientWorld world = client.world;

		if (world == null) {
			ACTIVE.clear();
			return;
		}

		if (ACTIVE.isEmpty() || client.isPaused()) {
			return;
		}

		long now = world.getTime();
		ObjectIterator<Int2ObjectMap.Entry<Stun>> iterator = ACTIVE.int2ObjectEntrySet().iterator();

		while (iterator.hasNext()) {
			Stun stun = iterator.next().getValue();
			LivingEntity entity = stun.entity;

			if (entity.isRemoved() || entity.getEntityWorld() != world) {
				iterator.remove();
				continue;
			}

			if (!entity.isAlive()) {
				stun.restoreHead();
				iterator.remove();
				continue;
			}

			if (now >= stun.end) {
				stun.restoreHead();
				recover(client, world, entity);
				iterator.remove();
				continue;
			}

			stun.wobbleHead(now);
		}
	}

	/** The hit: a shockwave ring of sparks at head height, plus a few flung up and out. */
	private static void impact(MinecraftClient client, ClientWorld world, LivingEntity entity) {
		if (sparkSprites == null) {
			return;
		}

		Random random = world.getRandom();
		double y = entity.getY() + entity.getHeight() * 0.85;
		double speed = 0.22 + Math.min(entity.getWidth(), 2.0F) * 0.06;
		float offset = random.nextFloat() * MathHelper.TAU;

		for (int i = 0; i < RING_SPARKS; i++) {
			float angle = offset + i * MathHelper.TAU / RING_SPARKS;
			double dx = MathHelper.cos(angle);
			double dz = MathHelper.sin(angle);
			float[] color = STAR_COLORS[i % STAR_COUNT];

			client.particleManager.addParticle(StunSparkParticle.create(world,
					entity.getX() + dx * entity.getWidth() * 0.5, y, entity.getZ() + dz * entity.getWidth() * 0.5,
					dx * speed, 0.0, dz * speed, color[0], color[1], color[2], sparkSprites));
		}

		for (int i = 0; i < 6; i++) {
			float[] color = STAR_COLORS[i % STAR_COUNT];

			client.particleManager.addParticle(StunSparkParticle.create(world,
					entity.getX(), entity.getY() + entity.getHeight(), entity.getZ(),
					(random.nextDouble() - 0.5) * 0.25, 0.18 + random.nextDouble() * 0.12,
					(random.nextDouble() - 0.5) * 0.25, color[0], color[1], color[2], sparkSprites));
		}
	}

	private static void spawnStars(MinecraftClient client, ClientWorld world, LivingEntity entity) {
		if (starSprites == null) {
			return;
		}

		for (int i = 0; i < STAR_COUNT; i++) {
			float phase = i * MathHelper.TAU / STAR_COUNT;
			float[] color = STAR_COLORS[i];

			client.particleManager.addParticle(new DizzyStarParticle(world, entity, phase,
					color[0], color[1], color[2], starSprites));
		}
	}

	/** Shaking it off: a small, soft burst where the stars were. */
	private static void recover(MinecraftClient client, ClientWorld world, LivingEntity entity) {
		if (sparkSprites == null) {
			return;
		}

		Random random = world.getRandom();
		double y = entity.getY() + entity.getHeight() + 0.28;

		for (int i = 0; i < 10; i++) {
			float angle = random.nextFloat() * MathHelper.TAU;
			float[] color = STAR_COLORS[i % STAR_COUNT];

			client.particleManager.addParticle(StunSparkParticle.create(world,
					entity.getX(), y, entity.getZ(),
					MathHelper.cos(angle) * 0.12, 0.04 + random.nextDouble() * 0.06, MathHelper.sin(angle) * 0.12,
					color[0], color[1], color[2], sparkSprites));
		}
	}

	private static final class Stun {
		private final LivingEntity entity;
		private final long start;
		private final float restYaw;
		private final float restPitch;
		private final boolean wobbles;
		private long end;

		private Stun(LivingEntity entity, long start, long end) {
			this.entity = entity;
			this.start = start;
			this.end = end;
			this.restYaw = entity.getHeadYaw();
			this.restPitch = entity.getPitch();
			// Only mobs with a head that can turn; the server freezes their AI, so nothing else is
			// steering it while we do.
			this.wobbles = entity instanceof MobEntity;
		}

		/** Rolls the head round in a slow, lopsided circle, drooping a little. */
		private void wobbleHead(long now) {
			if (!this.wobbles) {
				return;
			}

			float t = now - this.start;
			long left = this.end - now;
			float strength = Math.min(1.0F, t / 4.0F) * Math.min(1.0F, left / (float) SETTLE_TICKS);

			float yaw = this.restYaw + MathHelper.sin(t * 0.38F) * WOBBLE_YAW * strength;
			float pitch = this.restPitch
					+ (DROOP_PITCH + MathHelper.cos(t * 0.38F * 2.0F) * WOBBLE_PITCH * 0.5F
							+ MathHelper.cos(t * 0.38F) * WOBBLE_PITCH * 0.5F) * strength;

			this.entity.setHeadYaw(yaw);
			this.entity.setPitch(MathHelper.clamp(pitch, -90.0F, 90.0F));
		}

		private void restoreHead() {
			if (this.wobbles) {
				this.entity.setHeadYaw(this.restYaw);
				this.entity.setPitch(this.restPitch);
			}
		}
	}
}
