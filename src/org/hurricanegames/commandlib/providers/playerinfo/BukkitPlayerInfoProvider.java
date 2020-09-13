package org.hurricanegames.commandlib.providers.playerinfo;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hurricanegames.commandlib.utils.UnknownOfflinePlayer;

public class BukkitPlayerInfoProvider implements PlayerInfoProvider<BukkitPlayerInfo> {

	public static final BukkitPlayerInfoProvider INSTANCE = new BukkitPlayerInfoProvider();

	@Override
	public BukkitPlayerInfo getByUUID(UUID uuid) {
		return new BukkitPlayerInfo(Bukkit.getOfflinePlayer(uuid));
	}

	@SuppressWarnings("deprecation")
	@Override
	public BukkitPlayerInfo getByName(String name) {
		return new BukkitPlayerInfo(Bukkit.getOfflinePlayer(name));
	}

	@Override
	public BukkitPlayerInfo createUnknown(UUID uuid) {
		return new BukkitPlayerInfo(new UnknownOfflinePlayer(uuid));
	}

	@Override
	public BukkitPlayerInfo createFromPlayer(Player player) {
		return new BukkitPlayerInfo(player);
	}

}
