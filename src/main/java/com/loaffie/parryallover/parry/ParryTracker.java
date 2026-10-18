package com.loaffie.parryallover.parry;

import com.loaffie.parryallover.ParryMod;
import com.loaffie.parryallover.network.StunSync;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps the short counter-attack window a shield block opens, and applies the stun.
 */
public final class ParryTracker {
	/** Stun length in ticks (2 seconds). */
	public static final int STUN_TICKS = 40;

	/**
	 * How long the counter-attack window stays open, in ticks. Half a second: enough to drop the
	 * shield and swing on reaction, short enough that it still has to be a deliberate answer.
	 */
	private static final long WINDOW_TICKS = 10L;

	/** Expired windows are swept out at most this often, in ticks. */
	private static final long SWEEP_INTERVAL = 100L;

	private static final Map<ParryKey, Long> WINDOWS = new HashMap<>();

	private static long nextSweep;

	private ParryTracker() {
	}

	public static void init() {
		// The tick counter starts over with each server, so windows from a previous single player
		// session must not survive into the next one.
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			WINDOWS.clear();
			nextSweep = 0L;
		});
	}

	public static void openWindow(PlayerEntity player, MobEntity mob) {
		long now = currentTick(player);

		// Drop expired entries now and then so the map can't grow forever.
		if (now >= nextSweep) {
			WINDOWS.values().removeIf(expiry -> expiry < now);
			nextSweep = now + SWEEP_INTERVAL;
		}

		WINDOWS.put(new ParryKey(player.getUuid(), mob.getUuid()), now + WINDOW_TICKS);
	}

	/**
	 * Whether the pair has a live window. Consumes it, so one block only ever buys one counter.
	 */
	public static boolean consumeWindow(PlayerEntity player, MobEntity mob) {
		if (WINDOWS.isEmpty()) {
			return false;
		}

		Long expiry = WINDOWS.remove(new ParryKey(player.getUuid(), mob.getUuid()));

		return expiry != null && currentTick(player) <= expiry;
	}

	/** Stuns {@code target} and tells nearby clients to play the stun animation. */
	public static void stun(LivingEntity target, int ticks) {
		// No vanilla swirl particles: the client draws its own animation.
		if (target.addStatusEffect(new StatusEffectInstance(ParryMod.STUN, ticks, 0, false, false, true))) {
			StunSync.broadcast(target, ticks);
		}
	}

	private static long currentTick(PlayerEntity player) {
		if (player.getEntityWorld() instanceof ServerWorld world && world.getServer() != null) {
			return world.getServer().getTicks();
		}

		return 0L;
	}

	private record ParryKey(UUID player, UUID mob) {
	}
}
