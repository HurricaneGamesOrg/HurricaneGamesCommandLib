package org.hurricanegames.commandlib.providers.playerinfo;

import java.util.UUID;

import org.bukkit.entity.Player;

public interface PlayerInfoProvider<T extends PlayerInfo> {

	public T getByUUID(UUID uuid);

	public T getByName(String name);

	public T createUnknown(UUID uuid);

	public T createFromPlayer(Player player);

}
