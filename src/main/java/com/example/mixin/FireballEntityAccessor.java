package com.example.mixin;

import net.minecraft.entity.projectile.FireballEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the ghast fireball's explosion power so it can be doubled on a parry. */
@Mixin(FireballEntity.class)
public interface FireballEntityAccessor {
	@Accessor("explosionPower")
	int parry$getExplosionPower();

	@Accessor("explosionPower")
	void parry$setExplosionPower(int power);
}
