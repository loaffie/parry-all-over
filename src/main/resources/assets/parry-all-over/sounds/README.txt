PARRY ALL OVER - CUSTOM SOUND FILES
===================================

This folder is where your audio goes. Put each file at the exact path below; the
folder and file name are the lookup key, so "melee/shield_block.ogg" only works
spelled exactly like that, all lowercase.

All files must be OGG Vorbis (.ogg). Mono at 44.1 kHz is the safest choice.

DROP YOUR FILES HERE (paths relative to this folder)
----------------------------------------------------
  melee/shield_block.ogg    ->  your shield blocked a melee hit
  melee/stun.ogg            ->  your axe counter-hit landed
  arrow/warning.ogg         ->  a bow mob is about to loose its arrow
  arrow/parry.ogg           ->  your arrow shattered an enemy arrow
  breeze/warning.ogg        ->  a breeze is about to launch its wind charge
  fireball/parry.ogg        ->  you struck a ghast fireball and sent it back

So the first one lives at:

  src/main/resources/assets/parry-all-over/sounds/melee/shield_block.ogg

and the finished tree looks like:

  src/main/resources/assets/parry-all-over/sounds/
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
- No Java or JSON edits needed. Just drop the files in.
- A missing file is not an error. The game logs a warning and plays nothing, so
  you can add the sounds one at a time.
- Rebuild after adding or replacing a file (./gradlew build) so the new audio is
  packed into the jar.
- Both "warning" cues play a few ticks before the shot (5 ticks by default, a
  quarter of a second). To move them closer to or further from the shot, change
  WARNING_LEAD_TICKS in
  src/main/java/com/loaffie/parryallover/sound/ModSounds.java.
- Sounds are positional, so they fade with distance and pan by direction.
- If you rename anything here, update ../../sounds.json to match.
