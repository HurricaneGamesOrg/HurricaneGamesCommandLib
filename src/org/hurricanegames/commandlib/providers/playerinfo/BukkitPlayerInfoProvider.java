package org.hurricanegames.commandlib.providers.playerinfo;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class BukkitPlayerInfoProvider implements PlayerInfoProvider<BukkitPlayerInfo> {

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
		return new BukkitPlayerInfo(new OfflinePlayer() {
			@Override
			public UUID getUniqueId() {
				return uuid;
			}
			@Override
			public String getName() {
				return uuid.toString();
			}
			@Override
			public Map<String, Object> serialize() {
				throw new UnsupportedOperationException("Unkown player");
			}
			@Override
			public void setOp(boolean arg0) {
				throw new UnsupportedOperationException("Unkown player");
			}
			@Override
			public boolean isOp() {
				throw new UnsupportedOperationException("Unkown player");
			}
			@Override
			public void setWhitelisted(boolean arg0) {
				throw new UnsupportedOperationException("Unkown player");
			}
			@Override
			public boolean isWhitelisted() {
				throw new UnsupportedOperationException("Unkown player");
			}
			@Override
			public boolean isOnline() {
				throw new UnsupportedOperationException("Unkown player");
			}
			@Override
			public boolean isBanned() {
				throw new UnsupportedOperationException("Unkown player");
			}
			@Override
			public boolean hasPlayedBefore() {
				throw new UnsupportedOperationException("Unkown player");
			}
			@Override
			public Player getPlayer() {
				throw new UnsupportedOperationException("Unkown player");
			}
			@Override
			public long getLastPlayed() {
				throw new UnsupportedOperationException("Unkown player");
			}
			@Override
			public long getFirstPlayed() {
				throw new UnsupportedOperationException("Unkown player");
			}
			@Override
			public long getLastLogin() {
				throw new UnsupportedOperationException("Unkown player");
			}
			@Override
			public long getLastSeen() {
				throw new UnsupportedOperationException("Unkown player");
			}
			@Override
			public Location getBedSpawnLocation() {
				throw new UnsupportedOperationException("Unkown player");
			}
		});
	}

	@Override
	public BukkitPlayerInfo createFromPlayer(Player player) {
		return new BukkitPlayerInfo(player);
	}

}
