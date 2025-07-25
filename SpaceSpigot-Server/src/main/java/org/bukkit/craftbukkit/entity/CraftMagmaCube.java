package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;

import net.minecraft.server.EntityMagmaCube;

public class CraftMagmaCube extends CraftSlime implements MagmaCube {

	public CraftMagmaCube(CraftServer server, EntityMagmaCube entity) {
		super(server, entity);
	}

	@Override
	public EntityMagmaCube getHandle() {
		return (EntityMagmaCube) entity;
	}

	@Override
	public String toString() {
		return "CraftMagmaCube";
	}

	@Override
	public EntityType getType() {
		return EntityType.MAGMA_CUBE;
	}
}
