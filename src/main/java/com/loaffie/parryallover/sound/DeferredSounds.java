package com.loaffie.parryallover.sound;

import com.loaffie.parryallover.ParryMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Plays a mob's warning cue a set number of server ticks after it is queued.
 *
 * <p>This is how the parry warnings land just before the shot. A mob commits to a fixed wind-up
 * (a bow draws for 20 ticks, a breeze charges for 15), so queueing from the start of it is enough
 * to place the cue accurately without hooking the middle of the mob's AI.
 *
 * <p>A cue is dropped if the shot is never going to happen - the mob died, was stunned, or the
 * caller's check says the wind-up was abandoned - so a warning never plays for nothing. It plays
 * from wherever the mob is when it goes off, not where it started winding up.
 */
public final class DeferredSounds {
	private static final List<Pending> QUEUE = new ArrayList<>();

	private DeferredSounds() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> tick());

		// Entries hold on to their mob (and through it, its world); don't leak them between worlds.
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> QUEUE.clear());
	}

	/**
	 * Plays {@code sound} at {@code mob} after {@code delayTicks} server ticks, as long as the mob
	 * is still alive, unstunned and passes {@code stillValid} at that point. Zero or less plays it
	 * right away.
	 */
	public static void playLater(MobEntity mob, SoundEvent sound, SoundCategory category, int delayTicks,
			float volume, float pitch, Predicate<MobEntity> stillValid) {
		Pending pending = new Pending(mob, sound, category, delayTicks, volume, pitch, stillValid);

		if (delayTicks <= 0) {
			pending.play();
			return;
		}

		QUEUE.add(pending);
	}

	private static void tick() {
		if (!QUEUE.isEmpty()) {
			QUEUE.removeIf(Pending::advance);
		}
	}

	private static final class Pending {
		private final MobEntity mob;
		private final SoundEvent sound;
		private final SoundCategory category;
		private final float volume;
		private final float pitch;
		private final Predicate<MobEntity> stillValid;
		private int ticksLeft;

		private Pending(MobEntity mob, SoundEvent sound, SoundCategory category, int ticksLeft,
				float volume, float pitch, Predicate<MobEntity> stillValid) {
			this.mob = mob;
			this.sound = sound;
			this.category = category;
			this.ticksLeft = ticksLeft;
			this.volume = volume;
			this.pitch = pitch;
			this.stillValid = stillValid;
		}

		/** Counts down, plays once it is due, and reports whether the entry is finished. */
		private boolean advance() {
			if (!this.mob.isAlive() || this.mob.isRemoved()) {
				return true;
			}

			if (--this.ticksLeft > 0) {
				return false;
			}

			this.play();
			return true;
		}

		private void play() {
			if (this.mob.hasStatusEffect(ParryMod.STUN) || !this.stillValid.test(this.mob)) {
				return;
			}

			if (this.mob.getEntityWorld() instanceof ServerWorld world) {
				world.playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
						this.sound, this.category, this.volume, this.pitch);
			}
		}
	}
}
