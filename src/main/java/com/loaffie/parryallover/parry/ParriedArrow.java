package com.loaffie.parryallover.parry;

import java.util.UUID;

/**
 * Implemented by arrows (through a mixin) so a player arrow that destroyed an enemy arrow
 * remembers who shot it, and can stun them on impact.
 */
public interface ParriedArrow {
	void parry$setSkeletonShooter(UUID shooter);

	UUID parry$getSkeletonShooter();
}
