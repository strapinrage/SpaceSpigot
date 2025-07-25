package net.strapinrage.spacespigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldServer;

public class MobAICommand extends Command {
	
	private boolean globalAI = true;

	public MobAICommand(String name) {
		super(name);
		this.description = "Toggles Mob AI";
		this.usageMessage = "/mobai";
		this.setPermission("spacespigot.command.mobai");
	}

	@Override
	public boolean execute(CommandSender sender, String currentAlias, String[] args) {
		if (!testPermission(sender)) {
			return true;
		}

		globalAI = !globalAI;
		
		for (WorldServer world : MinecraftServer.getServer().worlds) {
			world.nachoSpigotConfig.enableMobAI = globalAI;
		}
		
		String status = globalAI ? "wlaczony" : "wylaczony";
		sender.sendMessage("§8» §aMob AI jest teraz " + status + " na wszystkich swiatach.");

		return true;
	}

}
