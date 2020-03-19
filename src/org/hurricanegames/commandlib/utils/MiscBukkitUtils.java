package org.hurricanegames.commandlib.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;

public class MiscBukkitUtils {

	public static String colorize(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static List<String> colorize(List<String> list) {
		return list.stream().map(MiscBukkitUtils::colorize).collect(Collectors.toCollection(ArrayList::new));
	}

}
