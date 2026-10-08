package com.loaffie.parryallover.sound;

import com.loaffie.parryallover.ParryMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * The mod's sound events. Each one is an entry in {@code assets/parry-all-over/sounds.json} and
 * the audio behind it lives at {@code assets/parry-all-over/sounds/<name>.ogg}, so you swap a
 * sound by dropping in your own OGG Vorbis file - no code changes needed.
 *
 * <p>All of them are optional: if the matching .ogg is missing the game logs a warning and plays
 * nothing, so the mod works fine while you are still collecting audio.
 */
public final class ModSounds {
	/**
	 * How many ticks before a mob shoots the warning cue plays. One knob for both the bow and the
	 * breeze warning; the default of 5 is a quarter of a second.
	 */
	public static final int WARNING_LEAD_TICKS = 5;

	/** Shield blocked a melee hit - the counter window is open. */
	public static final SoundEvent MELEE_SHIELD_BLOCK = register("melee.shield_block");

	/** The axe counter-hit landed. */
	public static final SoundEvent MELEE_STUN = register("melee.stun");

	/** A bow mob is about to loose its arrow. */
	public static final SoundEvent ARROW_WARNING = register("arrow.warning");

	/** Your arrow shattered an enemy arrow. */
	public static final SoundEvent ARROW_PARRY = register("arrow.parry");

	/** A breeze is about to launch its wind charge. */
	public static final SoundEvent BREEZE_WARNING = register("breeze.warning");

	/** You struck a ghast fireball and sent it back. */
	public static final SoundEvent FIREBALL_PARRY = register("fireball.parry");

	private ModSounds() {
	}

	/** Called from the mod initialiser so the events are registered before the registries freeze. */
	public static void init() {
	}

	private static SoundEvent register(String path) {
		Identifier id = ParryMod.id(path);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}
}
