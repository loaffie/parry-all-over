package com.loaffie.parryallover.parry;

import java.util.UUID;

/**
 * Implemented by projectiles that a player knocked back, so the return flight knows what it is
 * supposed to hit.
 */
public interface ParriedProjectile {
	void parry$markParried(UUID target);

	boolean parry$isParried();

	UUID parry$getTarget();
}
