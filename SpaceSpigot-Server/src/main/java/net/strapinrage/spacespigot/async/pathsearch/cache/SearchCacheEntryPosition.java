package net.strapinrage.spacespigot.async.pathsearch.cache;

import net.minecraft.server.EntityInsentient;
import net.minecraft.server.PathEntity;

public class SearchCacheEntryPosition extends SearchCacheEntry {
	
	private final int x;
	private final int y;
	private final int z;

	public SearchCacheEntryPosition(int x, int y, int z, EntityInsentient targetingEntity, PathEntity path) {
		super(targetingEntity, path);
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

}
