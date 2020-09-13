package org.hurricanegames.commandlib.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
			command.handleCommand(new CommandContext(command.getHelper().getMessages(), sender, label, splitEscaped(args)));
		} catch (CommandResponseException e) {
			sender.sendMessage(e.getMessage().split("\n"));
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		try {
			if (permission != null) {
				command.getHelper().validateHasPermission(sender, permission);
			}
			return
				command.getAutoComplete(new CommandContext(command.getHelper().getMessages(), sender, label, args)).stream()
				.map(BukkitCommandExecutor::escape)
				.collect(Collectors.toList());
		} catch (CommandResponseException e) {
			return Collections.emptyList();
		}
	}

	protected static String[] splitEscaped(String[] args) {
		ArrayList<String> arglist = new ArrayList<>();

		StringBuilder argbuilder = new StringBuilder();
		boolean escape = false;
		for (char c : String.join(" ", args).toCharArray()) {
			if (escape) {
				escape = false;
				argbuilder.append(c);
			} else if (c == ' ') {
				arglist.add(argbuilder.toString());
				argbuilder.setLength(0);
			} else if (c == '\\') {
				escape = true;
			} else {
				argbuilder.append(c);
			}
		}
		if (argbuilder.length() > 0) {
			arglist.add(argbuilder.toString());
		}
		return arglist.toArray(new String[0]);
	}

	protected static String escape(String string) {
		StringBuilder result = new StringBuilder();
		for (char c : string.toCharArray()) {
			if ((c == '\\') || (c == ' ')) {
				result.append('\\');
			}
			result.append(c);
		}
		return result.toString();
	}

}
