package com.loaffie.parryallover.network;

import com.loaffie.parryallover.ParryMod;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Keeps clients told about stuns: everyone watching gets a packet when the stun lands, and a
 * player who walks into view of a mob that is already stunned gets the time it has left.
 */
public final class StunSync {
	private StunSync() {
	}

	public static void init() {
		PayloadTypeRegistry.playS2C().register(StunPayload.ID, StunPayload.CODEC);

		EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
			if (!(entity instanceof LivingEntity living)) {
				return;
			}

			StatusEffectInstance stun = living.getStatusEffect(ParryMod.STUN);

			if (stun != null) {
				send(player, new StunPayload(living.getId(), stun.getDuration()));
			}
		});
	}

	/** Tells everyone tracking {@code target} that it has just been stunned. */
	public static void broadcast(LivingEntity target, int ticks) {
		StunPayload payload = new StunPayload(target.getId(), ticks);

		for (ServerPlayerEntity player : PlayerLookup.tracking(target)) {
			send(player, payload);
		}
	}

	private static void send(ServerPlayerEntity player, StunPayload payload) {
		if (ServerPlayNetworking.canSend(player, StunPayload.ID)) {
			ServerPlayNetworking.send(player, payload);
		}
	}
}
