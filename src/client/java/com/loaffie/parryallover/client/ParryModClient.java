package com.loaffie.parryallover.client;

import com.loaffie.parryallover.client.particle.StunSparkParticle;
import com.loaffie.parryallover.network.StunPayload;
import com.loaffie.parryallover.particle.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

/**
 * Client entrypoint. The parry logic all runs on the server; the client only draws the stun
 * animation.
 */
public class ParryModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ParticleFactoryRegistry particles = ParticleFactoryRegistry.getInstance();

		// The stars are spawned straight from StunAnimations (they need to know their mob), so the
		// factory is only here to hand over the sprites. Spawned on their own they act as sparks.
		particles.register(ModParticles.DIZZY_STAR, sprites -> {
			StunAnimations.setSprites(sprites, null);
			return new StunSparkParticle.Factory(sprites);
		});

		particles.register(ModParticles.STUN_SPARK, sprites -> {
			StunAnimations.setSprites(null, sprites);
			return new StunSparkParticle.Factory(sprites);
		});

		ClientPlayNetworking.registerGlobalReceiver(StunPayload.ID,
				(payload, context) -> StunAnimations.onStun(context.client(), payload.entityId(), payload.ticks()));

		ClientTickEvents.END_CLIENT_TICK.register(StunAnimations::tick);
	}
}
