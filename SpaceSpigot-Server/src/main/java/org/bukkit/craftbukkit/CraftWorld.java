package org.bukkit.craftbukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.BlockChangeDelegate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Difficulty;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.entity.CraftLightningStrike;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.metadata.BlockMetadataStore;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.LongHash;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Boat;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Cow;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Weather;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.SpawnChangeEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.util.Vector;

import com.google.common.base.Preconditions;

import net.strapinrage.spacespigot.cache.Constants;
import net.strapinrage.spacespigot.random.FastRandom;
import net.minecraft.server.*;

public class CraftWorld implements World {
	public static final int CUSTOM_DIMENSION_OFFSET = 10;

	private final WorldServer world;
	private WorldBorder worldBorder;
	private Environment environment;
	private final CraftServer server = (CraftServer) Bukkit.getServer();
	private final ChunkGenerator generator;
	private final List<BlockPopulator> populators = new ArrayList<BlockPopulator>();
	private final BlockMetadataStore blockMetadata = new BlockMetadataStore(this);
	private int monsterSpawn = -1;
	private int animalSpawn = -1;
	private int waterAnimalSpawn = -1;
	private int ambientSpawn = -1;
	private int chunkLoadCount = 0;
	private int chunkGCTickCount;

	private static final Random rand = new FastRandom(); // SpaceSpigot - use more fast randoms

	public CraftWorld(WorldServer world, ChunkGenerator gen, Environment env) {
		this.world = world;
		this.generator = gen;

		environment = env;

		if (server.chunkGCPeriod > 0) {
			chunkGCTickCount = rand.nextInt(server.chunkGCPeriod);
		}
	}

	@Override
	public Block getBlockAt(int x, int y, int z) {
		return getChunkAt(x >> 4, z >> 4).getBlock(x & 0xF, y, z & 0xF);
	}

	@Override
	public int getBlockTypeIdAt(int x, int y, int z) {
		return CraftMagicNumbers.getId(world.getType(x, y, z).getBlock());
	}

	@Override
	public int getHighestBlockYAt(int x, int z) {
		if (!isChunkLoaded(x >> 4, z >> 4)) {
			loadChunk(x >> 4, z >> 4);
		}

		return world.getHighestBlockYAt(x, 0, z);
	}

	@Override
	public Location getSpawnLocation() {
		BlockPosition spawn = world.getSpawn();
		return new Location(this, spawn.getX(), spawn.getY(), spawn.getZ());
	}

	@Override
	public boolean setSpawnLocation(int x, int y, int z) {
		try {
			Location previousLocation = getSpawnLocation();
			world.worldData.setSpawn(new BlockPosition(x, y, z));

			// Notify anyone who's listening.
			SpawnChangeEvent event = new SpawnChangeEvent(this, previousLocation);
			server.getPluginManager().callEvent(event);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// PaperSpigot start - Async chunk load API
	@Override
	public void getChunkAtAsync(final int x, final int z, final ChunkLoadCallback callback) {
		final ChunkProviderServer cps = this.world.chunkProviderServer;
		cps.getChunkAt(x, z, new Runnable() {
			@Override
			public void run() {
				callback.onLoad(cps.getChunkAt(x, z).bukkitChunk);
			}
		});
	}

	@Override
	public void getChunkAtAsync(Block block, ChunkLoadCallback callback) {
		getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, callback);
	}

	@Override
	public void getChunkAtAsync(Location location, ChunkLoadCallback callback) {
		getChunkAtAsync(location.getBlockX() >> 4, location.getBlockZ() >> 4, callback);
	}
	// PaperSpigot end

	@Override
	public Chunk getChunkAt(int x, int z) {
		return this.world.chunkProviderServer.getChunkAt(x, z).bukkitChunk;
	}

	@Override
	public Chunk getChunkAt(Block block) {
		return getChunkAt(block.getX() >> 4, block.getZ() >> 4);
	}

	@Override
	public boolean isChunkLoaded(int x, int z) {
		return world.chunkProviderServer.isChunkLoaded(x, z);
	}

	@Override
	public Chunk[] getLoadedChunks() {
		Object[] chunks = world.chunkProviderServer.chunks.values().toArray();
		org.bukkit.Chunk[] craftChunks = new CraftChunk[chunks.length];

		for (int i = 0; i < chunks.length; i++) {
			net.minecraft.server.Chunk chunk = (net.minecraft.server.Chunk) chunks[i];
			craftChunks[i] = chunk.bukkitChunk;
		}

		return craftChunks;
	}

	@Override
	public void loadChunk(int x, int z) {
		loadChunk(x, z, true);
	}

	@Override
	public boolean unloadChunk(Chunk chunk) {
		return unloadChunk(chunk.getX(), chunk.getZ());
	}

	@Override
	public boolean unloadChunk(int x, int z) {
		return unloadChunk(x, z, true);
	}

	@Override
	public boolean unloadChunk(int x, int z, boolean save) {
		return unloadChunk(x, z, save, false);
	}

	@Override
	public boolean unloadChunkRequest(int x, int z) {
		return unloadChunkRequest(x, z, true);
	}

	@Override
	public boolean unloadChunkRequest(int x, int z, boolean safe) {
		org.spigotmc.AsyncCatcher.catchOp("chunk unload"); // Spigot
		if (safe && isChunkInUse(x, z)) {
			return false;
		}

		world.chunkProviderServer.queueUnload(x, z);

		return true;
	}

	@Override
	public boolean unloadChunk(int x, int z, boolean save, boolean safe) {
		org.spigotmc.AsyncCatcher.catchOp("chunk unload"); // Spigot
		if (safe && isChunkInUse(x, z)) {
			return false;
		}

		net.minecraft.server.Chunk chunk = world.chunkProviderServer.getChunkIfLoaded(x, z);
		// PaperSpigot start - Don't create a chunk just to unload it
		if (chunk == null) {
			return false;
		}
		// PaperSpigot end
		if (chunk.mustSave) { // If chunk had previously been queued to save, must do save to avoid loss of
								// that data
			save = true;
		}

		chunk.removeEntities(); // Always remove entities - even if discarding, need to get them out of world
								// table

		if (save && !(chunk instanceof EmptyChunk)) {
			world.chunkProviderServer.saveChunk(chunk);
			world.chunkProviderServer.saveChunkNOP(chunk);
		}

		world.chunkProviderServer.unloadQueue.remove(LongHash.toLong(x, z)); // TacoSpigot - invoke LongHash directly
		world.chunkProviderServer.chunks.remove(LongHash.toLong(x, z));

		return true;
	}

	@Override
	public boolean regenerateChunk(int x, int z) {
		unloadChunk(x, z, false, false);

		world.chunkProviderServer.unloadQueue.remove(LongHash.toLong(x, z)); // TacoSpigot - invoke LongHash directly

		net.minecraft.server.Chunk chunk = null;

		if (world.chunkProviderServer.chunkProvider == null) {
			chunk = world.chunkProviderServer.emptyChunk;
		} else {
			chunk = world.chunkProviderServer.chunkProvider.getOrCreateChunk(x, z);
		}

		chunkLoadPostProcess(chunk, x, z);

		refreshChunk(x, z);

		return chunk != null;
	}

	@Override
	public boolean refreshChunk(int x, int z) {
		if (!isChunkLoaded(x, z)) {
			return false;
		}

		int px = x << 4;
		int pz = z << 4;

		// If there are more than 64 updates to a chunk at once, it will update all
		// 'touched' sections within the chunk
		// And will include biome data if all sections have been 'touched'
		// This flags 65 blocks distributed across all the sections of the chunk, so
		// that everything is sent, including biomes
		int height = getMaxHeight() / 16;
		for (int idx = 0; idx < 64; idx++) {
			world.notify(new BlockPosition(px + (idx / height), ((idx % height) * 16), pz));
		}
		world.notify(new BlockPosition(px + 15, (height * 16) - 1, pz + 15));

		return true;
	}

	@Override
	public boolean isChunkInUse(int x, int z) {
		return world.getPlayerChunkMap().isChunkInUse(x, z);
	}

	@Override
	public boolean loadChunk(int x, int z, boolean generate) {
		org.spigotmc.AsyncCatcher.catchOp("chunk load"); // Spigot
		chunkLoadCount++;
		if (generate) {
			// Use the default variant of loadChunk when generate == true.
			return world.chunkProviderServer.getChunkAt(x, z) != null;
		}

		world.chunkProviderServer.unloadQueue.remove(LongHash.toLong(x, z)); // TacoSpigot - invoke LongHash directly
		net.minecraft.server.Chunk chunk = world.chunkProviderServer.chunks.get(LongHash.toLong(x, z));

		if (chunk == null) {
			world.timings.syncChunkLoadTimer.startTiming(); // Spigot
			chunk = world.chunkProviderServer.loadChunk(x, z);

			chunkLoadPostProcess(chunk, x, z);
			world.timings.syncChunkLoadTimer.stopTiming(); // Spigot
		}
		return chunk != null;
	}

	private void chunkLoadPostProcess(net.minecraft.server.Chunk chunk, int cx, int cz) {
		if (chunk != null) {
			world.chunkProviderServer.chunks.put(LongHash.toLong(cx, cz), chunk);

			chunk.addEntities();

			// Update neighbor counts
			for (int x = -2; x < 3; x++) {
				for (int z = -2; z < 3; z++) {
					if (x == 0 && z == 0) {
						continue;
					}

					net.minecraft.server.Chunk neighbor = world.chunkProviderServer.getChunkIfLoaded(chunk.locX + x,
							chunk.locZ + z);
					if (neighbor != null) {
						neighbor.setNeighborLoaded(-x, -z);
						chunk.setNeighborLoaded(x, z);
					}
				}
			}
			// CraftBukkit end

			chunk.loadNearby(world.chunkProviderServer, world.chunkProviderServer, cx, cz);
		}
	}

	@Override
	public boolean isChunkLoaded(Chunk chunk) {
		return isChunkLoaded(chunk.getX(), chunk.getZ());
	}

	@Override
	public void loadChunk(Chunk chunk) {
		loadChunk(chunk.getX(), chunk.getZ());
		((CraftChunk) getChunkAt(chunk.getX(), chunk.getZ())).getHandle().bukkitChunk = chunk;
	}

	public WorldServer getHandle() {
		return world;
	}

	@Override
	public org.bukkit.entity.Item dropItem(Location loc, ItemStack item) {
		Validate.notNull(item, "Cannot drop a Null item.");
		Validate.isTrue(item.getTypeId() != 0, "Cannot drop AIR.");
		EntityItem entity = new EntityItem(world, loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(item));
		entity.pickupDelay = 10;

		if (!world.isMainThread()) {
			world.postToMainThread(() -> world.addEntity(entity));
		} else {
			world.addEntity(entity);
		}

		// TODO this is inconsistent with how Entity.getBukkitEntity() works.
		// However, this entity is not at the moment backed by a server entity class so
		// it may be left.
		return new CraftItem(world.getServer(), entity);
	}

	private static void randomLocationWithinBlock(Location loc, double xs, double ys, double zs) {
		double prevX = loc.getX();
		double prevY = loc.getY();
		double prevZ = loc.getZ();
		loc.add(xs, ys, zs);
		if (loc.getX() < Math.floor(prevX)) {
			loc.setX(Math.floor(prevX));
		}
		if (loc.getX() >= Math.ceil(prevX)) {
			loc.setX(Math.ceil(prevX - 0.01));
		}
		if (loc.getY() < Math.floor(prevY)) {
			loc.setY(Math.floor(prevY));
		}
		if (loc.getY() >= Math.ceil(prevY)) {
			loc.setY(Math.ceil(prevY - 0.01));
		}
		if (loc.getZ() < Math.floor(prevZ)) {
			loc.setZ(Math.floor(prevZ));
		}
		if (loc.getZ() >= Math.ceil(prevZ)) {
			loc.setZ(Math.ceil(prevZ - 0.01));
		}
	}

	@Override
	public org.bukkit.entity.Item dropItemNaturally(Location loc, ItemStack item) {
		double xs = world.random.nextFloat() * 0.7F - 0.35D;
		double ys = world.random.nextFloat() * 0.7F - 0.35D;
		double zs = world.random.nextFloat() * 0.7F - 0.35D;
		loc = loc.clone();
		// Makes sure the new item is created within the block the location points to.
		// This prevents item spill in 1-block wide farms.
		randomLocationWithinBlock(loc, xs, ys, zs);
		return dropItem(loc, item);
	}

	@Override
	public Arrow spawnArrow(Location loc, Vector velocity, float speed, float spread) {
		Validate.notNull(loc, "Can not spawn arrow with a null location");
		Validate.notNull(velocity, "Can not spawn arrow with a null velocity");

		EntityArrow arrow = new EntityArrow(world);
		arrow.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		arrow.shoot(velocity.getX(), velocity.getY(), velocity.getZ(), speed, spread);
		world.addEntity(arrow);
		return (Arrow) arrow.getBukkitEntity();
	}

	@Override
	@Deprecated
	public LivingEntity spawnCreature(Location loc, CreatureType creatureType) {
		return spawnCreature(loc, creatureType.toEntityType());
	}

	@Override
	@Deprecated
	public LivingEntity spawnCreature(Location loc, EntityType creatureType) {
		Validate.isTrue(creatureType.isAlive(), "EntityType not instance of LivingEntity");
		return (LivingEntity) spawnEntity(loc, creatureType);
	}

	@Override
	public Entity spawnEntity(Location loc, EntityType entityType) {
		return spawn(loc, entityType.getEntityClass());
	}

	@Override
	public LightningStrike strikeLightning(Location loc) {
		EntityLightning lightning = new EntityLightning(world, loc.getX(), loc.getY(), loc.getZ());
		world.strikeLightning(lightning);
		return new CraftLightningStrike(server, lightning);
	}

	@Override
	public LightningStrike strikeLightningEffect(Location loc) {
		EntityLightning lightning = new EntityLightning(world, loc.getX(), loc.getY(), loc.getZ(), true);
		world.strikeLightning(lightning);
		return new CraftLightningStrike(server, lightning);
	}

	@Override
	public boolean generateTree(Location loc, TreeType type) {
		net.minecraft.server.WorldGenerator gen;
		switch (type) {
		case BIG_TREE:
			gen = new WorldGenBigTree(true);
			break;
		case BIRCH:
			gen = new WorldGenForest(true, false);
			break;
		case REDWOOD:
			gen = new WorldGenTaiga2(true);
			break;
		case TALL_REDWOOD:
			gen = new WorldGenTaiga1();
			break;
		case JUNGLE:
			IBlockData iblockdata1 = Blocks.LOG.getBlockData().set(BlockLog1.VARIANT, BlockWood.EnumLogVariant.JUNGLE);
			IBlockData iblockdata2 = Blocks.LEAVES.getBlockData()
					.set(BlockLeaves1.VARIANT, BlockWood.EnumLogVariant.JUNGLE)
					.set(BlockLeaves.CHECK_DECAY, false);
			gen = new WorldGenJungleTree(true, 10, 20, iblockdata1, iblockdata2); // Magic values as in BlockSapling
			break;
		case SMALL_JUNGLE:
			iblockdata1 = Blocks.LOG.getBlockData().set(BlockLog1.VARIANT, BlockWood.EnumLogVariant.JUNGLE);
			iblockdata2 = Blocks.LEAVES.getBlockData().set(BlockLeaves1.VARIANT, BlockWood.EnumLogVariant.JUNGLE)
					.set(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
			gen = new WorldGenTrees(true, 4 + rand.nextInt(7), iblockdata1, iblockdata2, false);
			break;
		case COCOA_TREE:
			iblockdata1 = Blocks.LOG.getBlockData().set(BlockLog1.VARIANT, BlockWood.EnumLogVariant.JUNGLE);
			iblockdata2 = Blocks.LEAVES.getBlockData().set(BlockLeaves1.VARIANT, BlockWood.EnumLogVariant.JUNGLE)
					.set(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
			gen = new WorldGenTrees(true, 4 + rand.nextInt(7), iblockdata1, iblockdata2, true);
			break;
		case JUNGLE_BUSH:
			iblockdata1 = Blocks.LOG.getBlockData().set(BlockLog1.VARIANT, BlockWood.EnumLogVariant.JUNGLE);
			iblockdata2 = Blocks.LEAVES.getBlockData().set(BlockLeaves1.VARIANT, BlockWood.EnumLogVariant.OAK)
					.set(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
			gen = new WorldGenGroundBush(iblockdata1, iblockdata2);
			break;
		case RED_MUSHROOM:
			gen = new WorldGenHugeMushroom(Blocks.RED_MUSHROOM_BLOCK);
			break;
		case BROWN_MUSHROOM:
			gen = new WorldGenHugeMushroom(Blocks.BROWN_MUSHROOM_BLOCK);
			break;
		case SWAMP:
			gen = new WorldGenSwampTree();
			break;
		case ACACIA:
			gen = new WorldGenAcaciaTree(true);
			break;
		case DARK_OAK:
			gen = new WorldGenForestTree(true);
			break;
		case MEGA_REDWOOD:
			gen = new WorldGenMegaTree(false, rand.nextBoolean());
			break;
		case TALL_BIRCH:
			gen = new WorldGenForest(true, true);
			break;
		case TREE:
		default:
			gen = new WorldGenTrees(true);
			break;
		}

		return gen.generate(world, rand, new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
	}

	@Override
	public boolean generateTree(Location loc, TreeType type, BlockChangeDelegate delegate) {
		world.captureTreeGeneration = true;
		world.captureBlockStates = true;
		boolean grownTree = generateTree(loc, type);
		world.captureBlockStates = false;
		world.captureTreeGeneration = false;
		if (grownTree) { // Copy block data to delegate
			for (BlockState blockstate : world.capturedBlockStates) {
				int x = blockstate.getX();
				int y = blockstate.getY();
				int z = blockstate.getZ();
				BlockPosition position = new BlockPosition(x, y, z);
				net.minecraft.server.Block oldBlock = world.getType(position).getBlock();
				int typeId = blockstate.getTypeId();
				int data = blockstate.getRawData();
				int flag = ((CraftBlockState) blockstate).getFlag();
				delegate.setTypeIdAndData(x, y, z, typeId, data);
				net.minecraft.server.Block newBlock = world.getType(position).getBlock();
				world.notifyAndUpdatePhysics(position, null, oldBlock, newBlock, flag);
			}
			world.capturedBlockStates.clear();
			return true;
		} else {
			world.capturedBlockStates.clear();
			return false;
		}
	}

	public TileEntity getTileEntityAt(final int x, final int y, final int z) {
		return world.getTileEntity(new BlockPosition(x, y, z));
	}

	@Override
	public String getName() {
		return world.worldData.getName();
	}

	@Deprecated
	public long getId() {
		return world.worldData.getSeed();
	}

	@Override
	public UUID getUID() {
		return world.getDataManager().getUUID();
	}

	@Override
	public String toString() {
		return "CraftWorld{name=" + getName() + '}';
	}

	@Override
	public long getTime() {
		long time = getFullTime() % 24000;
		if (time < 0) {
			time += 24000;
		}
		return time;
	}

	@Override
	public void setTime(long time) {
		long margin = (time - getFullTime()) % 24000;
		if (margin < 0) {
			margin += 24000;
		}
		setFullTime(getFullTime() + margin);
	}

	@Override
	public long getFullTime() {
		return world.getDayTime();
	}

	@Override
	public void setFullTime(long time) {
		world.setDayTime(time);

		// Forces the client to update to the new time immediately
		for (Player p : getPlayers()) {
			CraftPlayer cp = (CraftPlayer) p;
			if (cp.getHandle().playerConnection == null) {
				continue;
			}

			cp.getHandle().playerConnection.sendPacket(new PacketPlayOutUpdateTime(cp.getHandle().world.getTime(),
					cp.getHandle().getPlayerTime(), cp.getHandle().world.getGameRules().getBoolean("doDaylightCycle")));
		}
	}

	@Override
	public boolean createExplosion(double x, double y, double z, float power) {
		return createExplosion(x, y, z, power, false, true);
	}

	@Override
	public boolean createExplosion(double x, double y, double z, float power, boolean setFire) {
		return createExplosion(x, y, z, power, setFire, true);
	}

	@Override
	public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
		return !world.createExplosion(null, x, y, z, power, setFire, breakBlocks).wasCanceled;
	}

	@Override
	public boolean createExplosion(Location loc, float power) {
		return createExplosion(loc, power, false);
	}

	@Override
	public boolean createExplosion(Location loc, float power, boolean setFire) {
		return createExplosion(loc.getX(), loc.getY(), loc.getZ(), power, setFire);
	}

	@Override
	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment env) {
		if (environment != env) {
			environment = env;
			world.worldProvider = WorldProvider.byDimension(environment.getId());
		}
	}

	@Override
	public Block getBlockAt(Location location) {
		return getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	@Override
	public int getBlockTypeIdAt(Location location) {
		return getBlockTypeIdAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	@Override
	public int getHighestBlockYAt(Location location) {
		return getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
	}

	@Override
	public Chunk getChunkAt(Location location) {
		return getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
	}

	@Override
	public ChunkGenerator getGenerator() {
		return generator;
	}

	@Override
	public List<BlockPopulator> getPopulators() {
		return populators;
	}

	@Override
	public Block getHighestBlockAt(int x, int z) {
		return getBlockAt(x, getHighestBlockYAt(x, z), z);
	}

	@Override
	public Block getHighestBlockAt(Location location) {
		return getHighestBlockAt(location.getBlockX(), location.getBlockZ());
	}

	@Override
	public Biome getBiome(int x, int z) {
		return CraftBlock.biomeBaseToBiome(this.world.getBiome(new BlockPosition(x, 0, z)));
	}

	@Override
	public void setBiome(int x, int z, Biome bio) {
		BiomeBase bb = CraftBlock.biomeToBiomeBase(bio);
		if (this.world.isLoaded(new BlockPosition(x, 0, z))) {
			net.minecraft.server.Chunk chunk = this.world.getChunkAtWorldCoords(new BlockPosition(x, 0, z));

			if (chunk != null) {
				byte[] biomevals = chunk.getBiomeIndex();
				biomevals[((z & 0xF) << 4) | (x & 0xF)] = (byte) bb.id;
			}
		}
	}

	@Override
	public double getTemperature(int x, int z) {
		return this.world.getBiome(new BlockPosition(x, 0, z)).temperature;
	}

	@Override
	public double getHumidity(int x, int z) {
		return this.world.getBiome(new BlockPosition(x, 0, z)).humidity;
	}

	@Override
	public List<Entity> getEntities() {
		List<Entity> list = new ArrayList<Entity>();

		for (Object o : world.entityList) {
			if (o instanceof net.minecraft.server.Entity) {
				net.minecraft.server.Entity mcEnt = (net.minecraft.server.Entity) o;
				Entity bukkitEntity = mcEnt.getBukkitEntity();

				// Assuming that bukkitEntity isn't null
				if (bukkitEntity != null) {
					list.add(bukkitEntity);
				}
			}
		}

		return list;
	}

	@Override
	public List<LivingEntity> getLivingEntities() {
		List<LivingEntity> list = new ArrayList<LivingEntity>();

		for (net.minecraft.server.Entity o : world.entityList) {
			//if (o instanceof net.minecraft.server.Entity) {
				net.minecraft.server.Entity mcEnt = o;
				Entity bukkitEntity = mcEnt.getBukkitEntity();

				// Assuming that bukkitEntity isn't null
				if (bukkitEntity != null && bukkitEntity instanceof LivingEntity) {
					list.add((LivingEntity) bukkitEntity);
				}
			//}
		}

		return list;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Deprecated
	public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... classes) {
		return (Collection<T>) getEntitiesByClasses(classes);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> clazz) {
		Collection<T> list = new ArrayList<T>();

		for (net.minecraft.server.Entity entity : world.entityList) {
			if (entity instanceof net.minecraft.server.Entity) {
				Entity bukkitEntity = entity.getBukkitEntity();

				if (bukkitEntity == null) {
					continue;
				}

				Class<?> bukkitClass = bukkitEntity.getClass();

				if (clazz.isAssignableFrom(bukkitClass)) {
					list.add((T) bukkitEntity);
				}
			}
		}

		return list;
	}

	@Override
	public Collection<Entity> getEntitiesByClasses(Class<?>... classes) {
		Collection<Entity> list = new ArrayList<Entity>();

		for (net.minecraft.server.Entity entity : world.entityList) {
			//if (entity instanceof net.minecraft.server.Entity) {
				Entity bukkitEntity = entity.getBukkitEntity();

				if (bukkitEntity == null) {
					continue;
				}

				Class<?> bukkitClass = bukkitEntity.getClass();

				for (Class<?> clazz : classes) {
					if (clazz.isAssignableFrom(bukkitClass)) {
						list.add(bukkitEntity);
						break;
					}
				}
			//}
		}

		return list;
	}

	@Override
	public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
		if (location == null || !this.equals(location.getWorld())) {
			return Collections.emptyList();
		}

		AxisAlignedBB bb = new AxisAlignedBB(location.getX() - x, location.getY() - y, location.getZ() - z,
				location.getX() + x, location.getY() + y, location.getZ() + z);
		List<net.minecraft.server.Entity> entityList = getHandle().a((net.minecraft.server.Entity) null, bb, null); // PAIL
																													// :
																													// rename
		List<Entity> bukkitEntityList = new ArrayList<org.bukkit.entity.Entity>(entityList.size());
		for (net.minecraft.server.Entity entity : entityList) {
			bukkitEntityList.add(entity.getBukkitEntity());
		}
		return bukkitEntityList;
	}

	@Override
	public List<Player> getPlayers() {
		List<Player> list = new ArrayList<Player>(world.players.size());

		for (EntityHuman human : world.players) {
			HumanEntity bukkitEntity = human.getBukkitEntity();

			if ((bukkitEntity != null) && (bukkitEntity instanceof Player)) {
				list.add((Player) bukkitEntity);
			}
		}

		return list;
	}

	@Override
	public void save() {
		// Spigot start
		save(true);
	}

	public void save(boolean forceSave) {
		// Spigot end
		this.server.checkSaveState();
		try {
			boolean oldSave = world.savingDisabled;

			world.savingDisabled = false;
			world.save(forceSave, null); // Spigot

			world.savingDisabled = oldSave;
		} catch (ExceptionWorldConflict ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean isAutoSave() {
		return !world.savingDisabled;
	}

	@Override
	public void setAutoSave(boolean value) {
		world.savingDisabled = !value;
	}

	@Override
	public void setDifficulty(Difficulty difficulty) {
		this.getHandle().worldData.setDifficulty(EnumDifficulty.getById(difficulty.getValue()));
	}

	@Override
	public Difficulty getDifficulty() {
		return Difficulty.getByValue(this.getHandle().getDifficulty().ordinal());
	}

	public BlockMetadataStore getBlockMetadata() {
		return blockMetadata;
	}

	@Override
	public boolean hasStorm() {
		return world.worldData.hasStorm();
	}

	@Override
	public void setStorm(boolean hasStorm) {
		world.worldData.setStorm(hasStorm);
	}

	@Override
	public int getWeatherDuration() {
		return world.worldData.getWeatherDuration();
	}

	@Override
	public void setWeatherDuration(int duration) {
		world.worldData.setWeatherDuration(duration);
	}

	@Override
	public boolean isThundering() {
		return world.worldData.isThundering();
	}

	@Override
	public void setThundering(boolean thundering) {
		world.worldData.setThundering(thundering);
	}

	@Override
	public int getThunderDuration() {
		return world.worldData.getThunderDuration();
	}

	@Override
	public void setThunderDuration(int duration) {
		world.worldData.setThunderDuration(duration);
	}

	@Override
	public long getSeed() {
		return world.worldData.getSeed();
	}

	@Override
	public boolean getPVP() {
		return world.pvpMode;
	}

	@Override
	public void setPVP(boolean pvp) {
		world.pvpMode = pvp;
	}

	public void playEffect(Player player, Effect effect, int data) {
		playEffect(player.getLocation(), effect, data, 0);
	}

	@Override
	public void playEffect(Location location, Effect effect, int data) {
		playEffect(location, effect, data, 64);
	}

	@Override
	public <T> void playEffect(Location loc, Effect effect, T data) {
		playEffect(loc, effect, data, 64);
	}

	@Override
	public <T> void playEffect(Location loc, Effect effect, T data, int radius) {
		if (data != null) {
			Validate.isTrue(data.getClass().isAssignableFrom(effect.getData()), "Wrong kind of data for this effect!");
		} else {
			Validate.isTrue(effect.getData() == null, "Wrong kind of data for this effect!");
		}

		if (data != null && data.getClass().equals(org.bukkit.material.MaterialData.class)) {
			org.bukkit.material.MaterialData materialData = (org.bukkit.material.MaterialData) data;
			Validate.isTrue(materialData.getItemType().isBlock(), "Material must be block");
			spigot().playEffect(loc, effect, materialData.getItemType().getId(), materialData.getData(), 0, 0, 0, 1, 1,
					radius);
		} else {
			int dataValue = data == null ? 0 : CraftEffect.getDataValue(effect, data);
			playEffect(loc, effect, dataValue, radius);
		}
	}

	@Override
	public void playEffect(Location location, Effect effect, int data, int radius) {
		spigot().playEffect(location, effect, data, 0, 0, 0, 0, 1, 1, radius);
	}

	@Override
	public <T extends Entity> T spawn(Location location, Class<T> clazz) throws IllegalArgumentException {
		return spawn(location, clazz, SpawnReason.CUSTOM);
	}

	@Override
	public FallingBlock spawnFallingBlock(Location location, org.bukkit.Material material, byte data)
			throws IllegalArgumentException {
		Validate.notNull(location, "Location cannot be null");
		Validate.notNull(material, "Material cannot be null");
		Validate.isTrue(material.isBlock(), "Material must be a block");

		double x = location.getBlockX() + 0.5;
		double y = location.getBlockY() + 0.5;
		double z = location.getBlockZ() + 0.5;

		// PaperSpigot start - Add FallingBlock source location API
		location = location.clone();
		EntityFallingBlock entity = new EntityFallingBlock(location, world, x, y, z,
				net.minecraft.server.Block.getById(material.getId()).fromLegacyData(data));
		// PaperSpigot end
		entity.ticksLived = 1;

		world.addEntity(entity, SpawnReason.CUSTOM);
		return (FallingBlock) entity.getBukkitEntity();
	}

	@Override
	public FallingBlock spawnFallingBlock(Location location, int blockId, byte blockData)
			throws IllegalArgumentException {
		return spawnFallingBlock(location, org.bukkit.Material.getMaterial(blockId), blockData);
	}

	@SuppressWarnings("unchecked")
	public net.minecraft.server.Entity createEntity(Location location, Class<? extends Entity> clazz)
			throws IllegalArgumentException {
		if (location == null || clazz == null) {
			throw new IllegalArgumentException("Location or entity class cannot be null");
		}

		net.minecraft.server.Entity entity = null;

		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		float pitch = location.getPitch();
		float yaw = location.getYaw();

		// order is important for some of these
		if (Boat.class.isAssignableFrom(clazz)) {
			entity = new EntityBoat(world, x, y, z);
		} else if (FallingBlock.class.isAssignableFrom(clazz)) {
			x = location.getBlockX();
			y = location.getBlockY();
			z = location.getBlockZ();
			IBlockData blockData = world.getType(new BlockPosition(x, y, z));
			int type = CraftMagicNumbers.getId(blockData.getBlock());
			int data = blockData.getBlock().toLegacyData(blockData);
			// PaperSpigot start - Add FallingBlock source location API
			location = location.clone();
			entity = new EntityFallingBlock(location, world, x + 0.5, y + 0.5, z + 0.5,
					net.minecraft.server.Block.getById(type).fromLegacyData(data));
			// PaperSpigot end
		} else if (Projectile.class.isAssignableFrom(clazz)) {
			if (Snowball.class.isAssignableFrom(clazz)) {
				entity = new EntitySnowball(world, x, y, z);
			} else if (Egg.class.isAssignableFrom(clazz)) {
				entity = new EntityEgg(world, x, y, z);
			} else if (Arrow.class.isAssignableFrom(clazz)) {
				entity = new EntityArrow(world);
				entity.setPositionRotation(x, y, z, 0, 0);
			} else if (ThrownExpBottle.class.isAssignableFrom(clazz)) {
				entity = new EntityThrownExpBottle(world);
				entity.setPositionRotation(x, y, z, 0, 0);
			} else if (EnderPearl.class.isAssignableFrom(clazz)) {
				entity = new EntityEnderPearl(world, null);
				entity.setPositionRotation(x, y, z, 0, 0);
			} else if (ThrownPotion.class.isAssignableFrom(clazz)) {
				entity = new EntityPotion(world, x, y, z,
						CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.POTION, 1)));
			} else if (Fireball.class.isAssignableFrom(clazz)) {
				if (SmallFireball.class.isAssignableFrom(clazz)) {
					entity = new EntitySmallFireball(world);
				} else if (WitherSkull.class.isAssignableFrom(clazz)) {
					entity = new EntityWitherSkull(world);
				} else {
					entity = new EntityLargeFireball(world);
				}
				entity.setPositionRotation(x, y, z, yaw, pitch);
				Vector direction = location.getDirection().multiply(10);
				((EntityFireball) entity).setDirection(direction.getX(), direction.getY(), direction.getZ());
			}
		} else if (Minecart.class.isAssignableFrom(clazz)) {
			if (PoweredMinecart.class.isAssignableFrom(clazz)) {
				entity = new EntityMinecartFurnace(world, x, y, z);
			} else if (StorageMinecart.class.isAssignableFrom(clazz)) {
				entity = new EntityMinecartChest(world, x, y, z);
			} else if (ExplosiveMinecart.class.isAssignableFrom(clazz)) {
				entity = new EntityMinecartTNT(world, x, y, z);
			} else if (HopperMinecart.class.isAssignableFrom(clazz)) {
				entity = new EntityMinecartHopper(world, x, y, z);
			} else if (SpawnerMinecart.class.isAssignableFrom(clazz)) {
				entity = new EntityMinecartMobSpawner(world, x, y, z);
			} else { // Default to rideable minecart for pre-rideable compatibility
				entity = new EntityMinecartRideable(world, x, y, z);
			}
		} else if (EnderSignal.class.isAssignableFrom(clazz)) {
			entity = new EntityEnderSignal(world, x, y, z);
		} else if (EnderCrystal.class.isAssignableFrom(clazz)) {
			entity = new EntityEnderCrystal(world);
			entity.setPositionRotation(x, y, z, 0, 0);
		} else if (LivingEntity.class.isAssignableFrom(clazz)) {
			if (Chicken.class.isAssignableFrom(clazz)) {
				entity = new EntityChicken(world);
			} else if (Cow.class.isAssignableFrom(clazz)) {
				if (MushroomCow.class.isAssignableFrom(clazz)) {
					entity = new EntityMushroomCow(world);
				} else {
					entity = new EntityCow(world);
				}
			} else if (Golem.class.isAssignableFrom(clazz)) {
				if (Snowman.class.isAssignableFrom(clazz)) {
					entity = new EntitySnowman(world);
				} else if (IronGolem.class.isAssignableFrom(clazz)) {
					entity = new EntityIronGolem(world);
				}
			} else if (Creeper.class.isAssignableFrom(clazz)) {
				entity = new EntityCreeper(world);
			} else if (Ghast.class.isAssignableFrom(clazz)) {
				entity = new EntityGhast(world);
			} else if (Pig.class.isAssignableFrom(clazz)) {
				entity = new EntityPig(world);
			} else if (Player.class.isAssignableFrom(clazz)) {
				// need a net server handler for this one
			} else if (Sheep.class.isAssignableFrom(clazz)) {
				entity = new EntitySheep(world);
			} else if (Horse.class.isAssignableFrom(clazz)) {
				entity = new EntityHorse(world);
			} else if (Skeleton.class.isAssignableFrom(clazz)) {
				entity = new EntitySkeleton(world);
			} else if (Slime.class.isAssignableFrom(clazz)) {
				if (MagmaCube.class.isAssignableFrom(clazz)) {
					entity = new EntityMagmaCube(world);
				} else {
					entity = new EntitySlime(world);
				}
			} else if (Spider.class.isAssignableFrom(clazz)) {
				if (CaveSpider.class.isAssignableFrom(clazz)) {
					entity = new EntityCaveSpider(world);
				} else {
					entity = new EntitySpider(world);
				}
			} else if (Squid.class.isAssignableFrom(clazz)) {
				entity = new EntitySquid(world);
			} else if (Tameable.class.isAssignableFrom(clazz)) {
				if (Wolf.class.isAssignableFrom(clazz)) {
					entity = new EntityWolf(world);
				} else if (Ocelot.class.isAssignableFrom(clazz)) {
					entity = new EntityOcelot(world);
				}
			} else if (PigZombie.class.isAssignableFrom(clazz)) {
				entity = new EntityPigZombie(world);
			} else if (Zombie.class.isAssignableFrom(clazz)) {
				entity = new EntityZombie(world);
			} else if (Giant.class.isAssignableFrom(clazz)) {
				entity = new EntityGiantZombie(world);
			} else if (Silverfish.class.isAssignableFrom(clazz)) {
				entity = new EntitySilverfish(world);
			} else if (Enderman.class.isAssignableFrom(clazz)) {
				entity = new EntityEnderman(world);
			} else if (Blaze.class.isAssignableFrom(clazz)) {
				entity = new EntityBlaze(world);
			} else if (Villager.class.isAssignableFrom(clazz)) {
				entity = new EntityVillager(world);
			} else if (Witch.class.isAssignableFrom(clazz)) {
				entity = new EntityWitch(world);
			} else if (Wither.class.isAssignableFrom(clazz)) {
				entity = new EntityWither(world);
			} else if (ComplexLivingEntity.class.isAssignableFrom(clazz)) {
				if (EnderDragon.class.isAssignableFrom(clazz)) {
					entity = new EntityEnderDragon(world);
				}
			} else if (Ambient.class.isAssignableFrom(clazz)) {
				if (Bat.class.isAssignableFrom(clazz)) {
					entity = new EntityBat(world);
				}
			} else if (Rabbit.class.isAssignableFrom(clazz)) {
				entity = new EntityRabbit(world);
			} else if (Endermite.class.isAssignableFrom(clazz)) {
				entity = new EntityEndermite(world);
			} else if (Guardian.class.isAssignableFrom(clazz)) {
				entity = new EntityGuardian(world);
			} else if (ArmorStand.class.isAssignableFrom(clazz)) {
				entity = new EntityArmorStand(world, x, y, z);
			}

			if (entity != null) {
				entity.setLocation(x, y, z, yaw, pitch);
			}
		} else if (Hanging.class.isAssignableFrom(clazz)) {
			Block block = getBlockAt(location);
			BlockFace face = BlockFace.SELF;

			int width = 16; // 1 full block, also painting smallest size.
			int height = 16; // 1 full block, also painting smallest size.

			if (ItemFrame.class.isAssignableFrom(clazz)) {
				width = 12;
				height = 12;
			} else if (LeashHitch.class.isAssignableFrom(clazz)) {
				width = 9;
				height = 9;
			}

			BlockFace[] faces = new BlockFace[] { BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH };
			final BlockPosition pos = new BlockPosition((int) x, (int) y, (int) z);
			for (BlockFace dir : faces) {
				net.minecraft.server.Block nmsBlock = CraftMagicNumbers.getBlock(block.getRelative(dir));
				if (nmsBlock.getMaterial().isBuildable() || BlockDiodeAbstract.d(nmsBlock)) {
					boolean taken = false;
					AxisAlignedBB bb = EntityHanging.calculateBoundingBox(pos,
							CraftBlock.blockFaceToNotch(dir).opposite(), width, height);
					List<net.minecraft.server.Entity> list = world.getEntities(null, bb);
					for (Iterator<net.minecraft.server.Entity> it = list.iterator(); !taken && it.hasNext();) {
						net.minecraft.server.Entity e = it.next();
						if (e instanceof EntityHanging) {
							taken = true; // Hanging entities do not like hanging entities which intersect them.
						}
					}

					if (!taken) {
						face = dir;
						break;
					}
				}
			}

			EnumDirection dir = CraftBlock.blockFaceToNotch(face).opposite();

			if (Painting.class.isAssignableFrom(clazz)) {
				entity = new EntityPainting(world, new BlockPosition((int) x, (int) y, (int) z), dir);
			} else if (ItemFrame.class.isAssignableFrom(clazz)) {
				entity = new EntityItemFrame(world, new BlockPosition((int) x, (int) y, (int) z), dir);
			} else if (LeashHitch.class.isAssignableFrom(clazz)) {
				entity = new EntityLeash(world, new BlockPosition((int) x, (int) y, (int) z));
				entity.attachedToPlayer = true;
			}

			if (entity != null && !((EntityHanging) entity).survives()) {
				throw new IllegalArgumentException(
						"Cannot spawn hanging entity for " + clazz.getName() + " at " + location);
			}
		} else if (TNTPrimed.class.isAssignableFrom(clazz)) {
			org.bukkit.Location loc = new org.bukkit.Location(world.getWorld(), x, y, z); // PaperSpigot
			entity = new EntityTNTPrimed(loc, world, x, y, z, null);
		} else if (ExperienceOrb.class.isAssignableFrom(clazz)) {
			entity = new EntityExperienceOrb(world, x, y, z, 0);
		} else if (Weather.class.isAssignableFrom(clazz)) {
			// not sure what this can do
			if (LightningStrike.class.isAssignableFrom(clazz)) {
				entity = new EntityLightning(world, x, y, z);
				// what is this, I don't even
			}
		} else if (Firework.class.isAssignableFrom(clazz)) {
			entity = new EntityFireworks(world, x, y, z, null);
		}

		if (entity != null) {
			// Spigot start
			if (entity instanceof EntityOcelot) {
				((EntityOcelot) entity).spawnBonus = false;
			}
			// Spigot end
			return entity;
		}

		throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> T addEntity(net.minecraft.server.Entity entity, SpawnReason reason)
			throws IllegalArgumentException {
		Preconditions.checkArgument(entity != null, "Cannot spawn null entity");

		if (entity instanceof EntityInsentient) {
			((EntityInsentient) entity).prepare(getHandle().E(new BlockPosition(entity)), (GroupDataEntity) null);
		}

		world.addEntity(entity, reason);
		return (T) entity.getBukkitEntity();
	}

	public <T extends Entity> T spawn(Location location, Class<T> clazz, SpawnReason reason)
			throws IllegalArgumentException {
		net.minecraft.server.Entity entity = createEntity(location, clazz);

		return addEntity(entity, reason);
	}

	@Override
	public ChunkSnapshot getEmptyChunkSnapshot(int x, int z, boolean includeBiome, boolean includeBiomeTempRain) {
		return CraftChunk.getEmptyChunkSnapshot(x, z, this, includeBiome, includeBiomeTempRain);
	}

	@Override
	public void setSpawnFlags(boolean allowMonsters, boolean allowAnimals) {
		world.setSpawnFlags(allowMonsters, allowAnimals);
	}

	@Override
	public boolean getAllowAnimals() {
		return world.allowAnimals;
	}

	@Override
	public boolean getAllowMonsters() {
		return world.allowMonsters;
	}

	@Override
	public int getMaxHeight() {
		return world.getHeight();
	}

	@Override
	public int getSeaLevel() {
		return 64;
	}

	@Override
	public boolean getKeepSpawnInMemory() {
		return world.keepSpawnInMemory;
	}

	@Override
	public void setKeepSpawnInMemory(boolean keepLoaded) {
		world.keepSpawnInMemory = keepLoaded;
		// Grab the worlds spawn chunk
		BlockPosition chunkcoordinates = this.world.getSpawn();
		int chunkCoordX = chunkcoordinates.getX() >> 4;
		int chunkCoordZ = chunkcoordinates.getZ() >> 4;
		// Cycle through the 25x25 Chunks around it to load/unload the chunks.
		for (int x = -12; x <= 12; x++) {
			for (int z = -12; z <= 12; z++) {
				if (keepLoaded) {
					loadChunk(chunkCoordX + x, chunkCoordZ + z);
				} else if (isChunkLoaded(chunkCoordX + x, chunkCoordZ + z)) {
					if (this.getHandle().getChunkAt(chunkCoordX + x, chunkCoordZ + z) instanceof EmptyChunk) {
						unloadChunk(chunkCoordX + x, chunkCoordZ + z, false);
					} else {
						unloadChunk(chunkCoordX + x, chunkCoordZ + z);
					}
				}
			}
		}
	}

	@Override
	public int hashCode() {
		return getUID().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final CraftWorld other = (CraftWorld) obj;

		return this.getUID() == other.getUID();
	}

	@Override
	public File getWorldFolder() {
		return ((WorldNBTStorage) world.getDataManager()).getDirectory();
	}

	@Override
	public void sendPluginMessage(Plugin source, String channel, byte[] message) {
		StandardMessenger.validatePluginMessage(server.getMessenger(), source, channel, message);

		for (Player player : getPlayers()) {
			player.sendPluginMessage(source, channel, message);
		}
	}

	@Override
	public Set<String> getListeningPluginChannels() {
		Set<String> result = new HashSet<String>();

		for (Player player : getPlayers()) {
			result.addAll(player.getListeningPluginChannels());
		}

		return result;
	}

	@Override
	public org.bukkit.WorldType getWorldType() {
		return org.bukkit.WorldType.getByName(world.getWorldData().getType().name());
	}

	@Override
	public boolean canGenerateStructures() {
		return world.getWorldData().shouldGenerateMapFeatures();
	}

	@Override
	public long getTicksPerAnimalSpawns() {
		return world.ticksPerAnimalSpawns;
	}

	@Override
	public void setTicksPerAnimalSpawns(int ticksPerAnimalSpawns) {
		world.ticksPerAnimalSpawns = ticksPerAnimalSpawns;
	}

	@Override
	public long getTicksPerMonsterSpawns() {
		return world.ticksPerMonsterSpawns;
	}

	@Override
	public void setTicksPerMonsterSpawns(int ticksPerMonsterSpawns) {
		world.ticksPerMonsterSpawns = ticksPerMonsterSpawns;
	}

	@Override
	public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
		server.getWorldMetadata().setMetadata(this, metadataKey, newMetadataValue);
	}

	@Override
	public List<MetadataValue> getMetadata(String metadataKey) {
		return server.getWorldMetadata().getMetadata(this, metadataKey);
	}

	@Override
	public boolean hasMetadata(String metadataKey) {
		return server.getWorldMetadata().hasMetadata(this, metadataKey);
	}

	@Override
	public void removeMetadata(String metadataKey, Plugin owningPlugin) {
		server.getWorldMetadata().removeMetadata(this, metadataKey, owningPlugin);
	}

	@Override
	public int getMonsterSpawnLimit() {
		if (monsterSpawn < 0) {
			return server.getMonsterSpawnLimit();
		}

		return monsterSpawn;
	}

	@Override
	public void setMonsterSpawnLimit(int limit) {
		monsterSpawn = limit;
	}

	@Override
	public int getAnimalSpawnLimit() {
		if (animalSpawn < 0) {
			return server.getAnimalSpawnLimit();
		}

		return animalSpawn;
	}

	@Override
	public void setAnimalSpawnLimit(int limit) {
		animalSpawn = limit;
	}

	@Override
	public int getWaterAnimalSpawnLimit() {
		if (waterAnimalSpawn < 0) {
			return server.getWaterAnimalSpawnLimit();
		}

		return waterAnimalSpawn;
	}

	@Override
	public void setWaterAnimalSpawnLimit(int limit) {
		waterAnimalSpawn = limit;
	}

	@Override
	public int getAmbientSpawnLimit() {
		if (ambientSpawn < 0) {
			return server.getAmbientSpawnLimit();
		}

		return ambientSpawn;
	}

	@Override
	public void setAmbientSpawnLimit(int limit) {
		ambientSpawn = limit;
	}

	@Override
	public void playSound(Location loc, Sound sound, float volume, float pitch) {
		if (loc == null || sound == null) {
			return;
		}

		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();

		getHandle().makeSound(x, y, z, CraftSound.getSound(sound), volume, pitch);
	}

	@Override
	public String getGameRuleValue(String rule) {
		return getHandle().getGameRules().get(rule);
	}

	@Override
	public boolean setGameRuleValue(String rule, String value) {
		// No null values allowed
		if (rule == null || value == null) {
			return false;
		}

		if (!isGameRule(rule)) {
			return false;
		}

		getHandle().getGameRules().set(rule, value);
		return true;
	}

	@Override
	public String[] getGameRules() {
		return getHandle().getGameRules().getGameRules();
	}

	@Override
	public boolean isGameRule(String rule) {
		return getHandle().getGameRules().contains(rule);
	}

	@Override
	public WorldBorder getWorldBorder() {
		if (this.worldBorder == null) {
			this.worldBorder = new CraftWorldBorder(this);
		}

		return this.worldBorder;
	}

	public void processChunkGC() {
		chunkGCTickCount++;

		if (chunkLoadCount >= server.chunkGCLoadThresh && server.chunkGCLoadThresh > 0) {
			chunkLoadCount = 0;
		} else if (chunkGCTickCount >= server.chunkGCPeriod && server.chunkGCPeriod > 0) {
			chunkGCTickCount = 0;
		} else {
			return;
		}

		ChunkProviderServer cps = world.chunkProviderServer;
		for (net.minecraft.server.Chunk chunk : cps.chunks.values()) {
			// If in use, skip it
			if (isChunkInUse(chunk.locX, chunk.locZ)) {
				continue;
			}

			// Already unloading?
			if (cps.unloadQueue.contains(LongHash.toLong(chunk.locX, chunk.locZ))) { // TacoSpigot - invoke LongHash
																						// directly
				continue;
			}

			// Add unload request
			cps.queueUnload(chunk.locX, chunk.locZ);
		}
	}

	// Spigot start
	private final Spigot spigot = new Spigot() {
		@Override
		public void playEffect(Location location, Effect effect, int id, int data, float offsetX, float offsetY,
				float offsetZ, float speed, int particleCount, int radius) {
			Validate.notNull(location, "Location cannot be null");
			Validate.notNull(effect, "Effect cannot be null");
			Validate.notNull(location.getWorld(), "World cannot be null");
			Packet packet;
			if (effect.getType() != Effect.Type.PARTICLE) {
				int packetData = effect.getId();
				packet = new PacketPlayOutWorldEvent(packetData,
						new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()), id, false);
			} else {
				net.minecraft.server.EnumParticle particle = null;
				int[] extra = null;
				for (net.minecraft.server.EnumParticle p : net.minecraft.server.EnumParticle.values()) {
					if (effect.getName().startsWith(p.b().replace("_", ""))) {
						particle = p;
						if (effect.getData() != null) {
							if (effect.getData().equals(org.bukkit.Material.class)) {
								extra = new int[] { id };
							} else {
								extra = new int[] { (data << 12) | (id & 0xFFF) };
							}
						}
						break;
					}
				}
				if (extra == null) {
					extra = Constants.EMPTY_ARRAY;
				}
				packet = new PacketPlayOutWorldParticles(particle, true, (float) location.getX(),
						(float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed,
						particleCount, extra);
			}
			int distance;
			radius *= radius;
			for (Player player : getPlayers()) {
				if (((CraftPlayer) player).getHandle().playerConnection == null) {
					continue;
				}
				if (!location.getWorld().equals(player.getWorld())) {
					continue;
				}
				distance = (int) player.getLocation().distanceSquared(location);
				if (distance <= radius) {
					((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
				}
			}
		}

		@Override
		public void playEffect(Location location, Effect effect) {
			CraftWorld.this.playEffect(location, effect, 0);
		}

		@Override
		public LightningStrike strikeLightning(Location loc, boolean isSilent) {
			EntityLightning lightning = new EntityLightning(world, loc.getX(), loc.getY(), loc.getZ(), false, isSilent);
			world.strikeLightning(lightning);
			return new CraftLightningStrike(server, lightning);
		}

		@Override
		public LightningStrike strikeLightningEffect(Location loc, boolean isSilent) {
			EntityLightning lightning = new EntityLightning(world, loc.getX(), loc.getY(), loc.getZ(), true, isSilent);
			world.strikeLightning(lightning);
			return new CraftLightningStrike(server, lightning);
		}
	};

	@Override
	public Spigot spigot() {
		return spigot;
	}
	// Spigot end
}
