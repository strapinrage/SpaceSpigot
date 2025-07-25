package net.strapinrage.spacespigot;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.strapinrage.spacespigot.async.AsyncUtil;
import net.strapinrage.spacespigot.async.pathsearch.SearchHandler;
import net.strapinrage.spacespigot.async.thread.CombatThread;
import net.strapinrage.spacespigot.commands.KnockbackCommand;
import net.strapinrage.spacespigot.commands.MobAICommand;
import net.strapinrage.spacespigot.commands.PingCommand;
import net.strapinrage.spacespigot.commands.SetMaxSlotCommand;
import net.strapinrage.spacespigot.commands.SpawnMobCommand;
import net.strapinrage.spacespigot.config.SpaceSpigotConfig;
import net.strapinrage.spacespigot.hitdetection.LagCompensator;
import net.strapinrage.spacespigot.protocol.MovementListener;
import net.strapinrage.spacespigot.protocol.PacketListener;
import net.strapinrage.spacespigot.statistics.StatisticsClient;
import net.minecraft.server.MinecraftServer;
import xyz.sculas.nacho.anticrash.AntiCrash;
import xyz.sculas.nacho.async.AsyncExplosions;

public class SpaceSpigot {

	private StatisticsClient client;
	
	public static final Logger LOGGER = LogManager.getLogger();
	private static final Logger DEBUG_LOGGER = LogManager.getLogger();
	private static SpaceSpigot INSTANCE;
	
	private CombatThread knockbackThread;
	
	private final Executor statisticsExecutor = Executors
			.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("SpaceSpigot Statistics Thread")
			.build());
	
	private volatile boolean statisticsEnabled = false;
	
	private LagCompensator lagCompensator;
	
	private final Set<PacketListener> packetListeners = Sets.newConcurrentHashSet();
	private final Set<MovementListener> movementListeners = Sets.newConcurrentHashSet();

	public SpaceSpigot() {
		INSTANCE = this;
		this.init();
	}

	public void reload() {
		this.init();
	}

	private void initCmds() {
		
		SimpleCommandMap commandMap = MinecraftServer.getServer().server.getCommandMap();
		
		if (SpaceSpigotConfig.mobAiCmd) {
			MobAICommand mobAiCommand = new MobAICommand("mobai");
			commandMap.register(mobAiCommand.getName(), "", mobAiCommand);
		}
		
		if (SpaceSpigotConfig.pingCmd) {
			PingCommand pingCommand = new PingCommand("ping");
			commandMap.register(pingCommand.getName(), "", pingCommand);
		}

		SetMaxSlotCommand setMaxSlotCommand = new SetMaxSlotCommand("sms");
		commandMap.register(setMaxSlotCommand.getName(), "ns", setMaxSlotCommand);

		SpawnMobCommand spawnMobCommand = new SpawnMobCommand("spawnmob");
		commandMap.register(spawnMobCommand.getName(), "ns", spawnMobCommand);

		KnockbackCommand knockbackCommand = new KnockbackCommand("kb");
		commandMap.register(knockbackCommand.getName(), "ns", knockbackCommand);
	}

	private void initStatistics() {
		if (SpaceSpigotConfig.statistics && !statisticsEnabled) {
			Runnable statisticsRunnable = (() -> {
				client = new StatisticsClient();
				try {
					statisticsEnabled = true;

					if (!client.isConnected) {
						client.start("150.230.35.78", 500);
						client.sendMessage("new server");

						while (true) {
							client.sendMessage("keep alive packet");
							client.sendMessage("player count packet " + Bukkit.getOnlinePlayers().size());

							TimeUnit.SECONDS.sleep(40);
						}

					}
				} catch (Exception ignored) {}
			});
			AsyncUtil.run(statisticsRunnable, statisticsExecutor);
		}
	}

	private void init() {
		initCmds();
		initStatistics();

		if (SpaceSpigotConfig.asyncPathSearches && SearchHandler.getInstance() == null) {
			new SearchHandler();
		}
		
		if (SpaceSpigotConfig.asyncKnockback) {
			knockbackThread = new CombatThread("KnockBack Thread");
		}
		lagCompensator = new LagCompensator();	
		if (SpaceSpigotConfig.asyncTnt) {
			AsyncExplosions.initExecutor(SpaceSpigotConfig.fixedPoolSize);
		}
		if (SpaceSpigotConfig.enableAntiCrash) {
			registerPacketListener(new AntiCrash());
		}
	}

	public StatisticsClient getClient() {
		return this.client;
	}
	
	public CombatThread getKnockbackThread() {
		return knockbackThread;
	}
	
    public LagCompensator getLagCompensator() {
        return lagCompensator;
    }
    
	public static void debug(String msg) {
		if (SpaceSpigotConfig.debugMode)
			DEBUG_LOGGER.info(msg);
	}
	
	public void registerPacketListener(PacketListener packetListener) {
		this.packetListeners.add(packetListener);
	}

	public void unregisterPacketListener(PacketListener packetListener) {
		this.packetListeners.remove(packetListener);
	}

	public Set<PacketListener> getPacketListeners() {
		return this.packetListeners;
	}

	public void registerMovementListener(MovementListener movementListener) {
		this.movementListeners.add(movementListener);
	}

	public void unregisterMovementListener(MovementListener movementListener) {
		this.movementListeners.remove(movementListener);
	}

	public Set<MovementListener> getMovementListeners() {
		return this.movementListeners;
	}
	
	public static SpaceSpigot getInstance() {
		return INSTANCE;
	}
}
