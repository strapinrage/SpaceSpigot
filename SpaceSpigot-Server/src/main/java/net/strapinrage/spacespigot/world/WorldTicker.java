package net.strapinrage.spacespigot.world;

import java.util.List;

import net.strapinrage.spacespigot.async.AsyncUtil;
import net.strapinrage.spacespigot.async.ResettableLatch;
import net.strapinrage.spacespigot.async.entitytracker.AsyncEntityTracker;
import net.strapinrage.spacespigot.config.SpaceSpigotConfig;
import net.minecraft.server.CrashReport;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.PlayerConnection;
import net.minecraft.server.ReportedException;
import net.minecraft.server.WorldServer;

public class WorldTicker implements Runnable {

	public final WorldServer worldserver;
	private final ResettableLatch latch = new ResettableLatch(SpaceSpigotConfig.trackingThreads);
	private final Runnable cachedUpdateTrackerTask;
	protected volatile boolean hasTracked = false;
	
	public WorldTicker(WorldServer worldServer) {
		this.worldserver = worldServer;
		cachedUpdateTrackerTask = () -> {
			hasTracked = true;
			worldserver.getTracker().updatePlayers();
		};
	}
	
	@Override
	public void run() {
		run((!SpaceSpigotConfig.disableTracking && SpaceSpigotConfig.fullAsyncTracking));
	}

	private void run(boolean handleTrackerAsync) {
		CrashReport crashreport;

		try {
			worldserver.timings.doTick.startTiming();
			worldserver.doTick();
			worldserver.timings.doTick.stopTiming();
		} catch (Throwable throwable) {
			try {
				crashreport = CrashReport.a(throwable, "Exception ticking world");
			} catch (Throwable t) {
				throw new RuntimeException("Error generating crash report", t);
			}
			worldserver.a(crashreport);
			throw new ReportedException(crashreport);
		}

		try {
			worldserver.timings.tickEntities.startTiming();
			worldserver.tickEntities();
			worldserver.timings.tickEntities.stopTiming();
		} catch (Throwable throwable1) {
			try {
				crashreport = CrashReport.a(throwable1, "Exception ticking world entities");
			} catch (Throwable t) {
				throw new RuntimeException("Error generating crash report", t);
			}
			worldserver.a(crashreport);
			throw new ReportedException(crashreport);
		}

        worldserver.timings.tracker.startTiming();
		if (handleTrackerAsync) {
			AsyncUtil.run(cachedUpdateTrackerTask, AsyncEntityTracker.getExecutor());
		} else {
			if (MinecraftServer.getServer().getPlayerList().getPlayerCount() != 0)
			{
				List<NetworkManager> disabledFlushes = new java.util.ArrayList<>(
						MinecraftServer.getServer().getPlayerList().getPlayerCount());
				for (EntityPlayer player : MinecraftServer.getServer().getPlayerList().players) {
					PlayerConnection connection = player.playerConnection;
					if (connection != null) {
						connection.networkManager.disableAutomaticFlush();
						disabledFlushes.add(connection.networkManager);
					}
				}
				try {
					worldserver.getTracker().updatePlayers();
				} finally {
					for (NetworkManager networkManager : disabledFlushes) {
						networkManager.enableAutomaticFlush();
					}
				}
			}
	
			worldserver.timings.tracker.stopTiming();
		}
		worldserver.explosionDensityCache.clear();
		worldserver.movementCache.clear();
	}
	
	public ResettableLatch getLatch() {
		return latch;
	}

}
