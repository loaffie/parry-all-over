package com.example.parry;

import com.example.ParryMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Shared state for the parry system.
 *
 * <p>Holds the short-lived "parry window" that is opened for a specific player/mob pair when the
 * player blocks a melee attack, and provides a single helper for applying the stun effect.
 */
public final class ParryTracker {
	/** Duration of a stun in ticks (2 seconds). */
	public static final int STUN_TICKS = 40;

	/** Length of the counter-attack window opened by a successful shield block, in ticks. */
	private static final long PARRY_WINDOW_TICKS = 6L;

	private static final Map<ParryKey, Long> PARRY_WINDOWS = new HashMap<>();

	private ParryTracker() {
	}

	/** Opens a 6-tick counter-attack window for the given player/mob pair. */
	public static void openWindow(PlayerEntity player, MobEntity mob) {
		long now = currentTick(player);

		// Opportunistically drop stale entries so the map cannot grow without bound.
		PARRY_WINDOWS.values().removeIf(expiry -> expiry < now);

		PARRY_WINDOWS.put(new ParryKey(player.getUuid(), mob.getUuid()), now + PARRY_WINDOW_TICKS);
	}

	/**
	 * Returns {@code true} if a valid parry window existed for this pair, consuming it in the
	 * process so a single block can only be countered once.
	 */
	public static boolean consumeWindow(PlayerEntity player, MobEntity mob) {
		ParryKey key = new ParryKey(player.getUuid(), mob.getUuid());
		Long expiry = PARRY_WINDOWS.get(key);

		if (expiry == null) {
			return false;
		}

		PARRY_WINDOWS.remove(key);
		return currentTick(player) <= expiry;
	}

	/** Applies the stun effect to the target for the given number of ticks. */
	public static void stun(LivingEntity target, int ticks) {
		target.addStatusEffect(new StatusEffectInstance(ParryMod.STUN, ticks, 0, false, true, true));
	}

	private static long currentTick(PlayerEntity player) {
		if (player.getEntityWorld() instanceof ServerWorld serverWorld && serverWorld.getServer() != null) {
			return serverWorld.getServer().getTicks();
		}

		return 0L;
	}

	private record ParryKey(UUID player, UUID mob) {
	}
}
