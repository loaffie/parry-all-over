package com.example.parry;

import java.util.UUID;

/**
 * Implemented (through a mixin) by projectiles that have been knocked back by a player, so the
 * returned projectile knows which entity it should strike and stun.
 */
public interface ParriedProjectile {
	void parry$markParried(UUID target);

	boolean parry$isParried();

	UUID parry$getTarget();
}
