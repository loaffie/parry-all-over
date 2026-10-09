package com.loaffie.parryallover;

import com.loaffie.parryallover.effect.StunEffect;
import com.loaffie.parryallover.parry.MeleeParryHandler;
import com.loaffie.parryallover.parry.ProjectileReflectionHandler;
import com.loaffie.parryallover.sound.DeferredSounds;
import com.loaffie.parryallover.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entrypoint. Registers the stun effect and hooks up the parry listeners.
 */
public class ParryMod implements ModInitializer {
	public static final String MOD_ID = "parryallover";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** The status effect applied to a mob that lost a parry. */
	public static final RegistryEntry<StatusEffect> STUN = Registry.registerReference(
			Registries.STATUS_EFFECT,
			id("stun"),
			new StunEffect(StatusEffectCategory.HARMFUL, 0x8A6BFF));

	@Override
	public void onInitialize() {
		ModSounds.init();
		DeferredSounds.init();
		MeleeParryHandler.init();
		ProjectileReflectionHandler.init();

		LOGGER.info("Parry All Over loaded");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
