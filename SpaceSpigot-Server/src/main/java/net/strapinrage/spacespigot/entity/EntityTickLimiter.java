package net.strapinrage.spacespigot.entity;

import java.util.List;

import org.bukkit.entity.EntityType;
import org.spigotmc.TickLimiter;

import com.google.common.collect.Lists;

import net.minecraft.server.Entity;

public class EntityTickLimiter extends TickLimiter {
	
	private static final List<EntityType> skippableEntities = Lists.newArrayList();

	public EntityTickLimiter(int maxtime) {
		super(maxtime);
	}

	public boolean canSkip(Entity entity) {
		return skippableEntities.contains(entity.getBukkitEntity().getType());
	}
	
	public static void addSkippableEntities(List<EntityType> entityTypes) {
		skippableEntities.addAll(entityTypes);
	}

}
