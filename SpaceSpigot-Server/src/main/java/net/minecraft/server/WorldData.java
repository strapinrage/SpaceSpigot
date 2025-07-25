package net.minecraft.server;

import java.util.concurrent.Callable;

// CraftBukkit start
import org.bukkit.Bukkit;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import net.strapinrage.spacespigot.config.SpaceSpigotConfig;
// CraftBukkit end

public class WorldData {

	public static final EnumDifficulty a = EnumDifficulty.NORMAL;
	private long b;
	private WorldType c;
	private String d;
	private int e;
	private int f;
	private int g;
	private long h;
	private long i;
	private long j;
	private long k;
	private NBTTagCompound l;
	private int m;
	private String n;
	private int o;
	private int p;
	private boolean q;
	private int r;
	private boolean s;
	private int t;
	private WorldSettings.EnumGamemode u;
	private boolean v; // generate-structures
	private boolean w;
	private boolean x;
	private boolean y;
	private EnumDifficulty z;
	private boolean A;
	private double B;
	private double C;
	private double D;
	private long E;
	private double F;
	private double G;
	private double H;
	private int I;
	private int J;
	private GameRules K;
	public WorldServer world; // CraftBukkit

	protected WorldData() {
		this.c = WorldType.NORMAL;
		this.d = "";
		this.B = 0.0D;
		this.C = 0.0D;
		this.D = 6.0E7D;
		this.E = 0L;
		this.F = 0.0D;
		this.G = 5.0D;
		this.H = 0.2D;
		this.I = 5;
		this.J = 15;
		this.K = new GameRules();
	}

	public WorldData(NBTTagCompound nbttagcompound) {
		this.c = WorldType.NORMAL;
		this.d = "";
		this.B = 0.0D;
		this.C = 0.0D;
		this.D = 6.0E7D;
		this.E = 0L;
		this.F = 0.0D;
		this.G = 5.0D;
		this.H = 0.2D;
		this.I = 5;
		this.J = 15;
		this.K = new GameRules();
		this.b = nbttagcompound.getLong("RandomSeed");
		if (nbttagcompound.hasKeyOfType("generatorName", 8)) {
			String s = nbttagcompound.getString("generatorName");

			this.c = WorldType.getType(s);
			if (this.c == null) {
				this.c = WorldType.NORMAL;
			} else if (this.c.f()) {
				int i = 0;

				if (nbttagcompound.hasKeyOfType("generatorVersion", 99)) {
					i = nbttagcompound.getInt("generatorVersion");
				}

				this.c = this.c.a(i);
			}

			if (nbttagcompound.hasKeyOfType("generatorOptions", 8)) {
				this.d = nbttagcompound.getString("generatorOptions");
			}
		}

		this.u = WorldSettings.EnumGamemode.getById(nbttagcompound.getInt("GameType"));
		if (nbttagcompound.hasKeyOfType("MapFeatures", 99)) {
			this.v = nbttagcompound.getBoolean("MapFeatures");
		} else {
			this.v = true;
		}

		this.e = nbttagcompound.getInt("SpawnX");
		this.f = nbttagcompound.getInt("SpawnY");
		this.g = nbttagcompound.getInt("SpawnZ");
		this.h = nbttagcompound.getLong("Time");
		if (nbttagcompound.hasKeyOfType("DayTime", 99)) {
			this.i = nbttagcompound.getLong("DayTime");
		} else {
			this.i = this.h;
		}

		this.j = nbttagcompound.getLong("LastPlayed");
		this.k = nbttagcompound.getLong("SizeOnDisk");
		this.n = nbttagcompound.getString("LevelName");
		this.o = nbttagcompound.getInt("version");
		this.p = nbttagcompound.getInt("clearWeatherTime");
		this.r = nbttagcompound.getInt("rainTime");
		this.q = nbttagcompound.getBoolean("raining");
		this.t = nbttagcompound.getInt("thunderTime");
		this.s = nbttagcompound.getBoolean("thundering");
		this.w = nbttagcompound.getBoolean("hardcore");
		if (nbttagcompound.hasKeyOfType("initialized", 99)) {
			this.y = nbttagcompound.getBoolean("initialized");
		} else {
			this.y = true;
		}

		if (nbttagcompound.hasKeyOfType("allowCommands", 99)) {
			this.x = nbttagcompound.getBoolean("allowCommands");
		} else {
			this.x = this.u == WorldSettings.EnumGamemode.CREATIVE;
		}

		if (nbttagcompound.hasKeyOfType("Player", 10)) {
			this.l = nbttagcompound.getCompound("Player");
			this.m = this.l.getInt("Dimension");
		}

		if (nbttagcompound.hasKeyOfType("GameRules", 10)) {
			this.K.a(nbttagcompound.getCompound("GameRules"));
		}

		if (nbttagcompound.hasKeyOfType("Difficulty", 99)) {
			this.z = EnumDifficulty.getById(nbttagcompound.getByte("Difficulty"));
		}

		if (nbttagcompound.hasKeyOfType("DifficultyLocked", 1)) {
			this.A = nbttagcompound.getBoolean("DifficultyLocked");
		}

		if (nbttagcompound.hasKeyOfType("BorderCenterX", 99)) {
			this.B = nbttagcompound.getDouble("BorderCenterX");
		}

		if (nbttagcompound.hasKeyOfType("BorderCenterZ", 99)) {
			this.C = nbttagcompound.getDouble("BorderCenterZ");
		}

		if (nbttagcompound.hasKeyOfType("BorderSize", 99)) {
			this.D = nbttagcompound.getDouble("BorderSize");
		}

		if (nbttagcompound.hasKeyOfType("BorderSizeLerpTime", 99)) {
			this.E = nbttagcompound.getLong("BorderSizeLerpTime");
		}

		if (nbttagcompound.hasKeyOfType("BorderSizeLerpTarget", 99)) {
			this.F = nbttagcompound.getDouble("BorderSizeLerpTarget");
		}

		if (nbttagcompound.hasKeyOfType("BorderSafeZone", 99)) {
			this.G = nbttagcompound.getDouble("BorderSafeZone");
		}

		if (nbttagcompound.hasKeyOfType("BorderDamagePerBlock", 99)) {
			this.H = nbttagcompound.getDouble("BorderDamagePerBlock");
		}

		if (nbttagcompound.hasKeyOfType("BorderWarningBlocks", 99)) {
			this.I = nbttagcompound.getInt("BorderWarningBlocks");
		}

		if (nbttagcompound.hasKeyOfType("BorderWarningTime", 99)) {
			this.J = nbttagcompound.getInt("BorderWarningTime");
		}

	}

	public WorldData(WorldSettings worldsettings, String s) {
		this.c = WorldType.NORMAL;
		this.d = "";
		this.B = 0.0D;
		this.C = 0.0D;
		this.D = 6.0E7D;
		this.E = 0L;
		this.F = 0.0D;
		this.G = 5.0D;
		this.H = 0.2D;
		this.I = 5;
		this.J = 15;
		this.K = new GameRules();
		this.a(worldsettings);
		this.n = s;
		this.z = WorldData.a;
		this.y = false;
	}

	public void a(WorldSettings worldsettings) {
		this.b = worldsettings.d();
		this.u = worldsettings.e();
		this.v = worldsettings.g();
		this.w = worldsettings.f();
		this.c = worldsettings.h();
		this.d = worldsettings.j();
		this.x = worldsettings.i();
	}

	public WorldData(WorldData worlddata) {
		this.c = WorldType.NORMAL;
		this.d = "";
		this.B = 0.0D;
		this.C = 0.0D;
		this.D = 6.0E7D;
		this.E = 0L;
		this.F = 0.0D;
		this.G = 5.0D;
		this.H = 0.2D;
		this.I = 5;
		this.J = 15;
		this.K = new GameRules();
		this.b = worlddata.b;
		this.c = worlddata.c;
		this.d = worlddata.d;
		this.u = worlddata.u;
		this.v = worlddata.v;
		this.e = worlddata.e;
		this.f = worlddata.f;
		this.g = worlddata.g;
		this.h = worlddata.h;
		this.i = worlddata.i;
		this.j = worlddata.j;
		this.k = worlddata.k;
		this.l = worlddata.l;
		this.m = worlddata.m;
		this.n = worlddata.n;
		this.o = worlddata.o;
		this.r = worlddata.r;
		this.q = worlddata.q;
		this.t = worlddata.t;
		this.s = worlddata.s;
		this.w = worlddata.w;
		this.x = worlddata.x;
		this.y = worlddata.y;
		this.K = worlddata.K;
		this.z = worlddata.z;
		this.A = worlddata.A;
		this.B = worlddata.B;
		this.C = worlddata.C;
		this.D = worlddata.D;
		this.E = worlddata.E;
		this.F = worlddata.F;
		this.G = worlddata.G;
		this.H = worlddata.H;
		this.J = worlddata.J;
		this.I = worlddata.I;
	}

	public NBTTagCompound a() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();

		this.a(nbttagcompound, this.l);
		return nbttagcompound;
	}

	public NBTTagCompound a(NBTTagCompound nbttagcompound) {
		NBTTagCompound nbttagcompound1 = new NBTTagCompound();

		this.a(nbttagcompound1, nbttagcompound);
		return nbttagcompound1;
	}

	private void a(NBTTagCompound nbttagcompound, NBTTagCompound nbttagcompound1) {
		nbttagcompound.setLong("RandomSeed", this.b);
		nbttagcompound.setString("generatorName", this.c.name());
		nbttagcompound.setInt("generatorVersion", this.c.getVersion());
		nbttagcompound.setString("generatorOptions", this.d);
		nbttagcompound.setInt("GameType", this.u.getId());
		nbttagcompound.setBoolean("MapFeatures", this.v);
		nbttagcompound.setInt("SpawnX", this.e);
		nbttagcompound.setInt("SpawnY", this.f);
		nbttagcompound.setInt("SpawnZ", this.g);
		nbttagcompound.setLong("Time", this.h);
		nbttagcompound.setLong("DayTime", this.i);
		nbttagcompound.setLong("SizeOnDisk", this.k);
		nbttagcompound.setLong("LastPlayed", MinecraftServer.az());
		nbttagcompound.setString("LevelName", this.n);
		nbttagcompound.setInt("version", this.o);
		nbttagcompound.setInt("clearWeatherTime", this.p);
		nbttagcompound.setInt("rainTime", this.r);
		nbttagcompound.setBoolean("raining", this.q);
		nbttagcompound.setInt("thunderTime", this.t);
		nbttagcompound.setBoolean("thundering", this.s);
		nbttagcompound.setBoolean("hardcore", this.w);
		nbttagcompound.setBoolean("allowCommands", this.x);
		nbttagcompound.setBoolean("initialized", this.y);
		nbttagcompound.setDouble("BorderCenterX", this.B);
		nbttagcompound.setDouble("BorderCenterZ", this.C);
		nbttagcompound.setDouble("BorderSize", this.D);
		nbttagcompound.setLong("BorderSizeLerpTime", this.E);
		nbttagcompound.setDouble("BorderSafeZone", this.G);
		nbttagcompound.setDouble("BorderDamagePerBlock", this.H);
		nbttagcompound.setDouble("BorderSizeLerpTarget", this.F);
		nbttagcompound.setDouble("BorderWarningBlocks", this.I);
		nbttagcompound.setDouble("BorderWarningTime", this.J);
		if (this.z != null) {
			nbttagcompound.setByte("Difficulty", (byte) this.z.a());
		}

		nbttagcompound.setBoolean("DifficultyLocked", this.A);
		nbttagcompound.set("GameRules", this.K.a());
		if (nbttagcompound1 != null) {
			nbttagcompound.set("Player", nbttagcompound1);
		}

	}

	public long getSeed() {
		return this.b;
	}

	public int c() {
		return this.e;
	}

	public int d() {
		return this.f;
	}

	public int e() {
		return this.g;
	}

	public long getTime() {
		return this.h;
	}

	public long getDayTime() {
		return this.i;
	}

	public NBTTagCompound i() {
		return this.l;
	}

	public void setTime(long i) {
		this.h = i;
	}

	public void setDayTime(long i) {
		this.i = i;
	}

	public void setSpawn(BlockPosition blockposition) {
		this.e = blockposition.getX();
		this.f = blockposition.getY();
		this.g = blockposition.getZ();
	}

	public String getName() {
		return this.n;
	}

	public void a(String s) {
		this.n = s;
	}

	public int l() {
		return this.o;
	}

	public void e(int i) {
		this.o = i;
	}

	public int A() {
		return this.p;
	}

	public void i(int i) {
		this.p = i;
	}

	public boolean isThundering() {
		return this.s;
	}

	public void setThundering(boolean flag) {
		// CraftBukkit start
		org.bukkit.World world = Bukkit.getWorld(getName());
		if (world != null) {
			ThunderChangeEvent thunder = new ThunderChangeEvent(world, flag);
			Bukkit.getServer().getPluginManager().callEvent(thunder);
			if (thunder.isCancelled()) {
				return;
			}

			setThunderDuration(0); // Will force a time reset
		}
		// CraftBukkit end
		this.s = flag;
	}

	public int getThunderDuration() {
		return this.t;
	}

	public void setThunderDuration(int i) {
		this.t = i;
	}

	public boolean hasStorm() {
		return this.q;
	}

	public void setStorm(boolean flag) {
		// CraftBukkit start
		org.bukkit.World world = Bukkit.getWorld(getName());
		if (world != null) {
			
			// SpaceSpigot start - toggleable weather change
			if (!SpaceSpigotConfig.weatherChange) {
				setThundering(false);
				setWeatherDuration(0);
				return;
			}
			// SpaceSpigot end
			
			WeatherChangeEvent weather = new WeatherChangeEvent(world, flag);
			Bukkit.getServer().getPluginManager().callEvent(weather);
			if (weather.isCancelled()) {
				return;
			}

			setWeatherDuration(0); // Will force a time reset
		}
		// CraftBukkit end
		this.q = flag;
	}

	public int getWeatherDuration() {
		return this.r;
	}

	public void setWeatherDuration(int i) {
		this.r = i;
	}

	public WorldSettings.EnumGamemode getGameType() {
		return this.u;
	}

	public boolean shouldGenerateMapFeatures() {
		return this.v;
	}

	public void f(boolean flag) {
		this.v = flag;
	}

	public void setGameType(WorldSettings.EnumGamemode worldsettings_enumgamemode) {
		this.u = worldsettings_enumgamemode;
	}

	public boolean isHardcore() {
		return this.w;
	}

	public void g(boolean flag) {
		this.w = flag;
	}

	public WorldType getType() {
		return this.c;
	}

	public void a(WorldType worldtype) {
		this.c = worldtype;
	}

	public String getGeneratorOptions() {
		return this.d;
	}

	public boolean v() {
		return this.x;
	}

	public void c(boolean flag) {
		this.x = flag;
	}

	public boolean w() {
		return this.y;
	}

	public void d(boolean flag) {
		this.y = flag;
	}

	public GameRules x() {
		return this.K;
	}

	public double C() {
		return this.B;
	}

	public double D() {
		return this.C;
	}

	public double E() {
		return this.D;
	}

	public void a(double d0) {
		this.D = d0;
	}

	public long F() {
		return this.E;
	}

	public void e(long i) {
		this.E = i;
	}

	public double G() {
		return this.F;
	}

	public void b(double d0) {
		this.F = d0;
	}

	public void c(double d0) {
		this.C = d0;
	}

	public void d(double d0) {
		this.B = d0;
	}

	public double H() {
		return this.G;
	}

	public void e(double d0) {
		this.G = d0;
	}

	public double I() {
		return this.H;
	}

	public void f(double d0) {
		this.H = d0;
	}

	public int J() {
		return this.I;
	}

	public int K() {
		return this.J;
	}

	public void j(int i) {
		this.I = i;
	}

	public void k(int i) {
		this.J = i;
	}

	public EnumDifficulty getDifficulty() {
		return this.z;
	}

	public void setDifficulty(EnumDifficulty enumdifficulty) {
		this.z = enumdifficulty;
		// CraftBukkit start
		PacketPlayOutServerDifficulty packet = new PacketPlayOutServerDifficulty(this.getDifficulty(),
				this.isDifficultyLocked());
		for (EntityPlayer player : (java.util.List<EntityPlayer>) (java.util.List) world.players) {
			player.playerConnection.sendPacket(packet);
		}
		// CraftBukkit end
	}

	public boolean isDifficultyLocked() {
		return this.A;
	}

	public void e(boolean flag) {
		this.A = flag;
	}

	public void a(CrashReportSystemDetails crashreportsystemdetails) {
		crashreportsystemdetails.a("Level seed", new Callable() {
			public String a() throws Exception {
				return String.valueOf(WorldData.this.getSeed());
			}

			@Override
			public Object call() throws Exception {
				return this.a();
			}
		});
		crashreportsystemdetails.a("Level generator", new Callable() {
			public String a() throws Exception {
				return String.format("ID %02d - %s, ver %d. Features enabled: %b",
						new Object[] { Integer.valueOf(WorldData.this.c.g()), WorldData.this.c.name(),
								Integer.valueOf(WorldData.this.c.getVersion()), WorldData.this.v });
			}

			@Override
			public Object call() throws Exception {
				return this.a();
			}
		});
		crashreportsystemdetails.a("Level generator options", new Callable() {
			public String a() throws Exception {
				return WorldData.this.d;
			}

			@Override
			public Object call() throws Exception {
				return this.a();
			}
		});
		crashreportsystemdetails.a("Level spawn location", new Callable() {
			public String a() throws Exception {
				return CrashReportSystemDetails.a(WorldData.this.e, WorldData.this.f, WorldData.this.g);
			}

			@Override
			public Object call() throws Exception {
				return this.a();
			}
		});
		crashreportsystemdetails.a("Level time", new Callable() {
			public String a() throws Exception {
				return String.format("%d game time, %d day time",
						new Object[] { Long.valueOf(WorldData.this.h), Long.valueOf(WorldData.this.i) });
			}

			@Override
			public Object call() throws Exception {
				return this.a();
			}
		});
		crashreportsystemdetails.a("Level dimension", new Callable() {
			public String a() throws Exception {
				return String.valueOf(WorldData.this.m);
			}

			@Override
			public Object call() throws Exception {
				return this.a();
			}
		});
		crashreportsystemdetails.a("Level storage version", new Callable() {
			public String a() throws Exception {
				String s = "Unknown?";

				try {
					switch (WorldData.this.o) {
					case 19132:
						s = "McRegion";
						break;

					case 19133:
						s = "Anvil";
					}
				} catch (Throwable throwable) {
					;
				}

				return String.format("0x%05X - %s", new Object[] { Integer.valueOf(WorldData.this.o), s });
			}

			@Override
			public Object call() throws Exception {
				return this.a();
			}
		});
		crashreportsystemdetails.a("Level weather", new Callable() {
			public String a() throws Exception {
				return String.format("Rain time: %d (now: %b), thunder time: %d (now: %b)",
						new Object[] { Integer.valueOf(WorldData.this.r), Boolean.valueOf(WorldData.this.q),
								Integer.valueOf(WorldData.this.t), Boolean.valueOf(WorldData.this.s) });
			}

			@Override
			public Object call() throws Exception {
				return this.a();
			}
		});
		crashreportsystemdetails.a("Level game mode", new Callable() {
			public String a() throws Exception {
				return String.format("Game mode: %s (ID %d). Hardcore: %b. Cheats: %b",
						new Object[] { WorldData.this.u.b(), Integer.valueOf(WorldData.this.u.getId()),
								Boolean.valueOf(WorldData.this.w), Boolean.valueOf(WorldData.this.x) });
			}

			@Override
			public Object call() throws Exception {
				return this.a();
			}
		});
	}

	// CraftBukkit start - Check if the name stored in NBT is the correct one
	public void checkName(String name) {
		if (!this.n.equals(name)) {
			this.n = name;
		}
	}
	// CraftBukkit end
}
