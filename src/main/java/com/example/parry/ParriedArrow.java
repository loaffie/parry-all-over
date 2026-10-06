package com.example.parry;

import java.util.UUID;

/**
 * Implemented (through a mixin) by arrows so a player arrow that destroyed an enemy arrow can
 * remember the shooter and stun it on impact.
 */
public interface ParriedArrow {
	void parry$setSkeletonShooter(UUID shooter);

	UUID parry$getSkeletonShooter();
}
