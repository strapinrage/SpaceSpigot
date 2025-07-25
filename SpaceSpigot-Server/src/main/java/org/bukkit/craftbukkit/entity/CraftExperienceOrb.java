package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;

import net.minecraft.server.EntityExperienceOrb;

public class CraftExperienceOrb extends CraftEntity implements ExperienceOrb {
	public CraftExperienceOrb(CraftServer server, EntityExperienceOrb entity) {
		super(server, entity);
	}

	@Override
	public int getExperience() {
		return getHandle().value;
	}

	@Override
	public void setExperience(int value) {
		getHandle().value = value;
	}

	@Override
	public EntityExperienceOrb getHandle() {
		return (EntityExperienceOrb) entity;
	}

	@Override
	public String toString() {
		return "CraftExperienceOrb";
	}

	@Override
	public EntityType getType() {
		return EntityType.EXPERIENCE_ORB;
	}
}
