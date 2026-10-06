# Example Mod

## Setup

For setup instructions, please see the [Fabric Documentation page](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) related to the IDE that you are using.

## Custom parry sounds

Every parry cue is a custom sound you can replace with your own audio - no code or JSON edits
required. Drop OGG Vorbis files into `src/main/resources/assets/modid/sounds/` at these exact paths:

| File | When it plays |
| --- | --- |
| `melee/shield_block.ogg` | Your shield blocked a melee mob's hit (the parry window opens) |
| `melee/stun.ogg` | Your axe counter-hit landed and stunned the mob |
| `arrow/warning.ogg` | A bow-wielding mob is about to release its arrow (a few ticks before) |
| `arrow/parry.ogg` | Your arrow clashed with and destroyed an enemy arrow |
| `breeze/warning.ogg` | A breeze is about to launch its wind charge (a few ticks before) |
| `fireball/parry.ogg` | You struck an incoming ghast fireball and reflected it |

Both pre-shot warnings fire `ModSounds.WARNING_LEAD_TICKS` ticks (5 by default, a quarter of a
second) before the shot; change that one constant to move them closer to or further from it.

A missing file is harmless - the game logs a warning and plays nothing, so you can add the sounds
one at a time and rebuild (`./gradlew build`) when you are done. See
`src/main/resources/assets/modid/sounds/README.txt` for more detail.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
