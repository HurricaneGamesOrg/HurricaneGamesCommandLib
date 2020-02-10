package org.hurricanegames.commandlib.providers.playerinfo;

import java.util.UUID;

import org.bukkit.OfflinePlayer;

public class BukkitPlayerInfo implements PlayerInfo {

	protected final OfflinePlayer offlineplayer;
	public BukkitPlayerInfo(OfflinePlayer offlineplayer) {
		this.offlineplayer = offlineplayer;
	}

	public OfflinePlayer getPlayer() {
		return offlineplayer;
	}

	@Override
	public UUID getUUID() {
		return offlineplayer.getUniqueId();
	}

	@Override
	public String getName() {
		return offlineplayer.getName();
	}



}