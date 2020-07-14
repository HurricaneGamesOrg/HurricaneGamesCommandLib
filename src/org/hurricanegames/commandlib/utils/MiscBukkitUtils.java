package org.hurricanegames.commandlib.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

public class MiscBukkitUtils {

	public static String colorize(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static List<String> colorize(List<String> list) {
		return list.stream().map(MiscBukkitUtils::colorize).collect(Collectors.toCollection(ArrayList::new));
	}

	public static ConfigurationSection createSection(Map<?, ?> map) {
		MemoryConfiguration configuration = new MemoryConfiguration();
		return configuration.createSection("root", map);
	}

}
