package com.example.sound;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Plays sound cues a fixed number of server ticks after they are scheduled.
 *
 * <p>Used to fire the parry warnings a few ticks before a mob actually shoots. A mob commits to its
 * wind-up at a known, fixed length - a bow draws for 20 ticks, a breeze charges for 15 - so
 * scheduling from that moment places the cue precisely relative to the shot without having to hook
 * inside the middle of the mob's AI.
 */
public final class DeferredSounds {
	private static final List<Pending> PENDING = new ArrayList<>();

	private DeferredSounds() {
	}

	/** Starts the server tick listener that drains the queue. */
	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> tick());
	}

	/**
	 * Schedules {@code sound} to play at the given position after {@code delayTicks} server ticks.
	 * A non-positive delay plays it immediately.
	 */
	public static void playLater(ServerWorld world, double x, double y, double z, SoundEvent sound,
			SoundCategory category, int delayTicks, float volume, float pitch) {
		if (delayTicks <= 0) {
			world.playSound(null, x, y, z, sound, category, volume, pitch);
			return;
		}

		PENDING.add(new Pending(world, x, y, z, sound, category, delayTicks, volume, pitch));
	}

	private static void tick() {
		if (PENDING.isEmpty()) {
			return;
		}

		Iterator<Pending> iterator = PENDING.iterator();

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

		/** Counts down and plays the cue once due, reporting when the entry can be dropped. */
		private boolean advance() {
			if (--this.ticksLeft > 0) {
				return false;
			}

			this.world.playSound(null, this.x, this.y, this.z, this.sound, this.category, this.volume, this.pitch);
			return true;
		}
	}
}
