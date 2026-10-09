# Parry All Over

A Fabric mod for Minecraft 1.21.11 that turns defending into offense. Block a melee hit with your
shield and you get a short window to answer with an axe - land it and the mob is stunned for two
seconds. Projectiles can be parried too, and a parried shot is credited to whoever sent it back.

## Parries

| You | Result |
| --- | --- |
| Block a melee hit, then hit the mob with an axe within the window | Mob is stunned for 2s, movement frozen, knocked back a little |
| Swing at an incoming enemy arrow with your own arrow in flight | Enemy arrow is destroyed, your arrow homes onto the shooter and stuns it |
| Swing at an incoming breeze wind charge | Charge returns at 3x speed and stuns the breeze |
| Swing at an incoming ghast fireball | Fireball returns at 2.5x speed and hits for double damage |

A stun freezes AI, navigation, targeting and attacks, and shows particles above the mob's head.

Fireballs are fire damage, and some mobs (ghasts, blazes, magma cubes, withers) are fire immune, so
a parried fireball gets attributed to the player who sent it back - otherwise the ghast would shrug
off its own fireball. See `FireballEntityMixin`.

## Custom parry sounds

Every cue is a sound you can replace with your own audio - no code or JSON edits required. Drop
OGG Vorbis files into `src/main/resources/assets/parryallover/sounds/` at these exact paths:

| File | When it plays |
| --- | --- |
| `melee/shield_block.ogg` | Your shield blocked a melee hit and the window opened |
| `melee/stun.ogg` | Your axe counter-hit landed |
| `arrow/warning.ogg` | A bow mob is about to loose its arrow |
| `arrow/parry.ogg` | Your arrow shattered an enemy arrow |
| `breeze/warning.ogg` | A breeze is about to launch its wind charge |
| `fireball/parry.ogg` | You struck a ghast fireball and sent it back |

Both pre-shot warnings fire `ModSounds.WARNING_LEAD_TICKS` ticks (5 by default, a quarter of a
second) before the shot; that one constant moves both cues.

A missing file is harmless - the game logs a warning and plays nothing, so you can add the sounds
one at a time and rebuild (`./gradlew build`) when you are done. More detail is in
`src/main/resources/assets/parryallover/sounds/README.txt`.

## Building

```
./gradlew build
```

The mod jar lands in `build/libs/`.

## License

Released under CC0. Do whatever you like with it.
