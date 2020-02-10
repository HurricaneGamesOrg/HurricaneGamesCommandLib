package org.hurricanegames.commandlib.providers.playerinfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.StampedLock;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public abstract class AbstractCachedPlayerInfoProvider<T extends PlayerInfo> implements Listener, PlayerInfoProvider<T> {

	protected final Plugin plugin;
	public AbstractCachedPlayerInfoProvider(Plugin plugin) {
		this.plugin = plugin;
	}

	protected final Map<UUID, T> byUUID = new HashMap<>();
	protected final Map<String, T> byName = new HashMap<>();

	protected final StampedLock lock = new StampedLock();

	protected boolean init = false;
	public AbstractCachedPlayerInfoProvider<T> init() {
		if (init) {
			throw new IllegalArgumentException("Already initialized");
		}
		init = true;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		Arrays.stream(Bukkit.getOfflinePlayers()).forEach(this::add);
		Bukkit.getOnlinePlayers().forEach(this::add);
		return this;
	}

	@Override
	public T getByUUID(UUID uuid) {
		long stamp = lock.readLock();
		try {
			return byUUID.get(uuid);
		} finally {
			lock.unlockRead(stamp);
		}
	}

	@Override
	public T getByName(String name) {
		long stamp = lock.readLock();
		try {
			return byName.get(name);
		} finally {
			lock.unlockRead(stamp);
		}
	}

	@Override
	public T createFromPlayer(Player player) {
		return createPlayerInfo(player);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		add(event.getPlayer());
	}

	protected void add(OfflinePlayer player) {
		long stamp = lock.writeLock();
		try {
			Optional.ofNullable(byUUID.remove(player.getUniqueId()))
			.ifPresent(cplayer -> byName.remove(cplayer.getName()));
			T newplayer = createPlayerInfo(player);
			byUUID.put(newplayer.getUUID(), newplayer);
			byName.put(newplayer.getName(), newplayer);
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	protected void add(Player player) {
		long stamp = lock.writeLock();
		try {
			Optional.ofNullable(byUUID.remove(player.getUniqueId()))
			.ifPresent(cplayer -> byName.remove(cplayer.getName()));
			T newplayer = createPlayerInfo(player);
			byUUID.put(newplayer.getUUID(), newplayer);
			byName.put(newplayer.getName(), newplayer);
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	protected abstract T createPlayerInfo(Player player);

	protected abstract T createPlayerInfo(OfflinePlayer player);

}
