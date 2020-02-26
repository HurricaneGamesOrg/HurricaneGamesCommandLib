package org.hurricanegames.commandlib.utils;

import org.bukkit.ChatColor;

public class MiscBukkitUtils {

	public static String colorize(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

}
