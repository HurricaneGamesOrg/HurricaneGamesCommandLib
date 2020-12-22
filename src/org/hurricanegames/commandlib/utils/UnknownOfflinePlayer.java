package org.hurricanegames.commandlib.utils;

import java.util.Map;
import java.util.UUID;

import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class UnknownOfflinePlayer implements OfflinePlayer {

	protected final UUID uuid;
	protected final String name;

	public UnknownOfflinePlayer(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}

	public UnknownOfflinePlayer(UUID uuid) {
		this(uuid, uuid.toString());
	}

	@Override
	public UUID getUniqueId() {
		return uuid;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}

	@Override
	public boolean isOnline() {
		return getPlayer() != null;
	}

	@Override
	public boolean hasPlayedBefore() {
		return false;
	}

	@Override
	public boolean isBanned() {
		return Bukkit.getBanList(Type.NAME).isBanned(uuid.toString());
	}

	@Override
	public boolean isOp() {
		return false;
	}

	@Override
	public boolean isWhitelisted() {
		return false;
	}

	@Override
	public void setOp(boolean op) {
	}

	@Override
	public void setWhitelisted(boolean whitelisted) {
	}

	@Override
	public long getLastPlayed() {
		return 0;
	}

	@Override
	public long getFirstPlayed() {
		return 0;
	}

	@Override
	public long getLastLogin() {
		return 0;
	}

	@Override
	public long getLastSeen() {
		return 0;
	}

	@Override
	public Location getBedSpawnLocation() {
		return null;
	}

	@Override
	public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {
	}

	@Override
	public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {
	}

	@Override
	public void incrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
	}

	@Override
	public void decrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
	}

	@Override
	public void setStatistic(Statistic statistic, int newValue) throws IllegalArgumentException {
	}

	@Override
	public int getStatistic(Statistic statistic) throws IllegalArgumentException {
		return 0;
	}

	@Override
	public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
	}

	@Override
	public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
	}

	@Override
	public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
		return 0;
	}

	@Override
	public void incrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
	}

	@Override
	public void decrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
	}

	@Override
	public void setStatistic(Statistic statistic, Material material, int newValue) throws IllegalArgumentException {
	}

	@Override
	public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
	}

	@Override
	public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
	}

	@Override
	public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
		return 0;
	}

	@Override
	public void incrementStatistic(Statistic statistic, EntityType entityType, int amount) throws IllegalArgumentException {
	}

	@Override
	public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
	}

	@Override
	public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
	}

	@Override
	public Map<String, Object> serialize() {
		throw new UnsupportedOperationException("Unkown player");
	}

}
