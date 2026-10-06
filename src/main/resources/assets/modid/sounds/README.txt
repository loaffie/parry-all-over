PARRY MECHANICS - CUSTOM SOUND FILES
====================================

This folder is where you drop your own audio. Just create the folders below and
place each recording at the exact path shown. The file name and folder matter:
Minecraft looks up the sound by that path, so "shield_block.ogg" in "melee/"
will only work if it is spelled exactly like this.

All files must be OGG Vorbis (.ogg). Mono, 44.1 kHz is the safest choice.
Anything else may still work but is not guaranteed.

PASTE YOUR FILES HERE (paths are relative to this folder)
---------------------------------------------------------
  melee/shield_block.ogg    ->  melee parry: your shield blocked a mob's hit
  melee/stun.ogg            ->  melee parry: your axe hit landed and stunned the mob
  arrow/warning.ogg         ->  a bow-wielding mob just started drawing its bow
  arrow/parry.ogg           ->  your arrow clashed with and destroyed an enemy arrow
  breeze/warning.ogg        ->  a breeze just started winding up a wind charge
  fireball/parry.ogg        ->  you struck an incoming ghast fireball and reflected it

So, for example, the first one lives at:
  src/main/resources/assets/modid/sounds/melee/shield_block.ogg

and would look like this inside your project:

  src/main/resources/assets/modid/sounds/
    melee/
      shield_block.ogg
      stun.ogg
    arrow/
      warning.ogg
      parry.ogg
    breeze/
      warning.ogg
    fireball/
      parry.ogg

NOTES
-----
- You do NOT need to edit any Java or any JSON. Just drop the files in.
- A missing file is not an error. The game logs a warning and plays nothing, so
  you can add the sounds one at a time.
- After adding or replacing a file, rebuild the mod (./gradlew build) so the new
  audio is packed into the jar.
- Sounds are positional: they play from the mob/player that caused them, so they
  get quieter with distance and are stereo-panned by direction.
- Do not rename the folders or files unless you also update
  ../../sounds.json (the file one level up next to this README).
