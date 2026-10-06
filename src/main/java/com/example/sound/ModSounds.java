package com.example.sound;

import com.example.ParryMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * The mod's custom parry sounds.
 *
 * <p>Each constant is an entry in {@code assets/modid/sounds.json}. The audio itself is loaded from
 * {@code assets/modid/sounds/<file>.ogg}, so you swap a sound by dropping your own OGG Vorbis file
 * at that path - no code changes and no recompile of the game beyond rebuilding the mod.
 *
 * <p>Every sound is optional: if the matching {@code .ogg} file is missing, Minecraft simply logs a
 * warning and plays nothing, so the mod keeps working while you are still gathering audio.
 */
public final class ModSounds {
	/** Played when a melee mob's attack is blocked by the player's shield (the parry window opens). */
	public static final SoundEvent MELEE_SHIELD_BLOCK = register("melee.shield_block");

	/** Played when the player lands the axe counter-hit that stuns the mob. */
	public static final SoundEvent MELEE_STUN = register("melee.stun");

	/** Played the moment a bow-wielding mob starts drawing its bow, roughly a second before it fires. */
	public static final SoundEvent ARROW_WARNING = register("arrow.warning");

	/** Played when the player's arrow clashes with and destroys an enemy arrow. */
	public static final SoundEvent ARROW_PARRY = register("arrow.parry");

	/** Played when a breeze starts winding up a wind charge, shortly before it fires. */
	public static final SoundEvent BREEZE_WARNING = register("breeze.warning");

	/** Played when the player strikes an incoming ghast fireball and reflects it. */
	public static final SoundEvent FIREBALL_PARRY = register("fireball.parry");

	private ModSounds() {
	}

	/**
	 * Forces this class to load, and therefore registers every sound event above.
	 *
	 * <p>Called from the mod initialiser so the sounds are registered before the registries freeze.
	 */
	public static void init() {
	}

	private static SoundEvent register(String path) {
		Identifier id = ParryMod.id(path);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}
}
