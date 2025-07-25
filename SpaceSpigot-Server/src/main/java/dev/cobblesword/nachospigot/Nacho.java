package dev.cobblesword.nachospigot;

import java.util.Set;

import net.strapinrage.spacespigot.SpaceSpigot;
import net.strapinrage.spacespigot.protocol.MovementListener;
import net.strapinrage.spacespigot.protocol.PacketListener;

@Deprecated
public class Nacho {

	private static Nacho INSTANCE;

	public Nacho() {
		INSTANCE = this;
	}

	public static Nacho get() {
		return INSTANCE == null ? new Nacho() : INSTANCE;
	}

	public void registerCommands() {

	}

	public void registerPacketListener(PacketListener packetListener) {
		SpaceSpigot.getInstance().registerPacketListener(packetListener);
	}

	public void unregisterPacketListener(PacketListener packetListener) {
		SpaceSpigot.getInstance().unregisterPacketListener(packetListener);
	}

	public Set<PacketListener> getPacketListeners() {
		return SpaceSpigot.getInstance().getPacketListeners();
	}

	public void registerMovementListener(MovementListener movementListener) {
		SpaceSpigot.getInstance().registerMovementListener(movementListener);
	}

	public void unregisterMovementListener(MovementListener movementListener) {
		SpaceSpigot.getInstance().unregisterMovementListener(movementListener);
	}

	public Set<MovementListener> getMovementListeners() {
		return SpaceSpigot.getInstance().getMovementListeners();
	}

}
