package com.loaffie.parryallover.network;

import com.loaffie.parryallover.ParryMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * Tells a client that a mob it can see was stunned, and for how many ticks, so it can play the
 * stun animation. Sent once per stun instead of streaming particles from the server every tick.
 */
public record StunPayload(int entityId, int ticks) implements CustomPayload {
	public static final CustomPayload.Id<StunPayload> ID = new CustomPayload.Id<>(ParryMod.id("stun"));

	public static final PacketCodec<RegistryByteBuf, StunPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, StunPayload::entityId,
			PacketCodecs.VAR_INT, StunPayload::ticks,
			StunPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
