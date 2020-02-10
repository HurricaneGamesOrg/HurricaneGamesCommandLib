package org.hurricanegames.commandlib.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class BukkitCommandExecutor implements CommandExecutor, TabCompleter {

	protected final String permission;
	protected final Command<? extends CommandHelper<?, ?, ?>> command;

	public BukkitCommandExecutor(Command<? extends CommandHelper<?, ?, ?>> command) {
		this(command, null);
	}

	public BukkitCommandExecutor(Command<? extends CommandHelper<?, ?, ?>> command, String permission) {
		this.command = command;
		this.permission = permission;
	}

	public Command<? extends CommandHelper<?, ?, ?>> getCommand() {
		return command;
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		try {
			if (permission != null) {
				command.getHelper().validateHasPermission(sender, permission);
			}
			command.handleCommand(new CommandContext(command.getHelper().getMessages(), sender, label, args));
		} catch (CommandResponseException e) {
			sender.sendMessage(e.getMessage());
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		try {
			if (permission != null) {
				command.getHelper().validateHasPermission(sender, permission);
			}
			return command.getAutoComplete(new CommandContext(command.getHelper().getMessages(), sender, label, args));
		} catch (CommandResponseException e) {
			return Collections.emptyList();
		}
	}

}
