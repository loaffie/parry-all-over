package com.loaffie.parryallover.parry;

import com.loaffie.parryallover.ParryMod;
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

	/** How long the counter-attack window stays open, in ticks. */
	private static final long WINDOW_TICKS = 6L;

	private static final Map<ParryKey, Long> WINDOWS = new HashMap<>();

	private ParryTracker() {
	}

	public static void openWindow(PlayerEntity player, MobEntity mob) {
		long now = currentTick(player);

		// Drop expired entries while we're here so the map can't grow forever.
		WINDOWS.values().removeIf(expiry -> expiry < now);

		WINDOWS.put(new ParryKey(player.getUuid(), mob.getUuid()), now + WINDOW_TICKS);
	}

	/**
	 * Whether the pair has a live window. Consumes it, so one block only ever buys one counter.
	 */
	public static boolean consumeWindow(PlayerEntity player, MobEntity mob) {
		ParryKey key = new ParryKey(player.getUuid(), mob.getUuid());
		Long expiry = WINDOWS.remove(key);

		if (expiry == null) {
			return false;
		}

		return currentTick(player) <= expiry;
	}

	public static void stun(LivingEntity target, int ticks) {
		target.addStatusEffect(new StatusEffectInstance(ParryMod.STUN, ticks, 0, false, true, true));
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
