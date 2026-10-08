package com.loaffie.parryallover.sound;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Plays sound cues a set number of server ticks after they are queued.
 *
 * <p>This is how the parry warnings land just before the shot. A mob commits to a fixed wind-up
 * (a bow draws for 20 ticks, a breeze charges for 15), so queueing from the start of it is enough
 * to place the cue accurately without hooking the middle of the mob's AI.
 */
public final class DeferredSounds {
	private static final List<Pending> QUEUE = new ArrayList<>();

	private DeferredSounds() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> tick());
	}

	/**
	 * Plays {@code sound} at the position after {@code delayTicks} server ticks. Zero or less
	 * plays it right away.
	 */
	public static void playLater(ServerWorld world, double x, double y, double z, SoundEvent sound,
			SoundCategory category, int delayTicks, float volume, float pitch) {
		if (delayTicks <= 0) {
			world.playSound(null, x, y, z, sound, category, volume, pitch);
			return;
		}

		QUEUE.add(new Pending(world, x, y, z, sound, category, delayTicks, volume, pitch));
	}

	private static void tick() {
		if (QUEUE.isEmpty()) {
			return;
		}

		Iterator<Pending> iterator = QUEUE.iterator();

		while (iterator.hasNext()) {
			if (iterator.next().advance()) {
				iterator.remove();
			}
		}
	}

	private static final class Pending {
		private final ServerWorld world;
		private final double x;
		private final double y;
		private final double z;
		private final SoundEvent sound;
		private final SoundCategory category;
		private final float volume;
		private final float pitch;
		private int ticksLeft;

		private Pending(ServerWorld world, double x, double y, double z, SoundEvent sound,
				SoundCategory category, int ticksLeft, float volume, float pitch) {
			this.world = world;
			this.x = x;
			this.y = y;
			this.z = z;
			this.sound = sound;
			this.category = category;
			this.ticksLeft = ticksLeft;
			this.volume = volume;
			this.pitch = pitch;
		}

		/** Counts down, plays once it is due, and reports whether the entry is finished. */
		private boolean advance() {
			if (--this.ticksLeft > 0) {
				return false;
			}

			this.world.playSound(null, this.x, this.y, this.z, this.sound, this.category, this.volume, this.pitch);
			return true;
		}
	}
}
