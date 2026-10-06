package com.example;

import com.example.effect.StunEffect;
import com.example.parry.MeleeParryHandler;
import com.example.parry.ProjectileReflectionHandler;
import com.example.sound.ModSounds;
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
 * Entry point for the Parry Mechanics mod.
 *
 * <p>Owns the shared identifiers, the {@code modid:stun} status effect registration and the
 * initialisation of the parry event listeners.
 */
public class ParryMod implements ModInitializer {
	public static final String MOD_ID = "modid";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** The registered {@code stun} status effect. */
	public static final RegistryEntry<StatusEffect> STUN = Registry.registerReference(
			Registries.STATUS_EFFECT,
			id("stun"),
			new StunEffect(StatusEffectCategory.HARMFUL, 0x8A6BFF));

	@Override
	public void onInitialize() {
		ModSounds.init();
		MeleeParryHandler.init();
		ProjectileReflectionHandler.init();

		LOGGER.info("Parry Mechanics initialised.");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
