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
| `arrow/warning.ogg` | A bow-wielding mob started drawing its bow (about a second before it fires) |
| `arrow/parry.ogg` | Your arrow clashed with and destroyed an enemy arrow |
| `breeze/warning.ogg` | A breeze started winding up a wind charge (shortly before it fires) |
| `fireball/parry.ogg` | You struck an incoming ghast fireball and reflected it |

A missing file is harmless - the game logs a warning and plays nothing, so you can add the sounds
one at a time and rebuild (`./gradlew build`) when you are done. See
`src/main/resources/assets/modid/sounds/README.txt` for more detail.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
