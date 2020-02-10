package org.hurricanegames.commandlib.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRouter<H extends CommandHelper<?, ?, ?>> implements Command<H> {

	protected final H helper;
	public CommandRouter(H helper) {
		this.helper = helper;
	}

	private final Map<String, Command<H>> commands = new LinkedHashMap<>();

	protected void addCommand(String name, Command<H> command) {
		this.commands.put(name, command);
	}

	@Override
	public H getHelper() {
		return helper;
	}

	@Override
	public void handleCommand(CommandContext context) {
		if (context.hasArg(0)) {
			String commandName = context.getArg(0);
			Command<H> command = commands.get(commandName);
			if (command == null) {
				throw new CommandResponseException(helper.getMessages().getSubCommandNotFoundMessage(commandName));
			}
			command.handleCommand(context.getSubContext(1));
		} else {
			CommandSender sender = context.getSender();
			String commandName = context.getCommand();
			if (sender instanceof Player) {
				commandName = "/" + commandName;
			}
			getHelpMessages(commandName).forEach(sender::sendMessage);
		}
	}

	@Override
	public List<String> getHelpMessages(String commandLabel) {
		String color = helper.getMessages().getSubCommandLabelColor();
		return
			commands.entrySet().stream()
			.map(entry -> entry.getValue().getHelpMessages(entry.getKey()))
			.flatMap(List::stream)
			.map(s -> color + commandLabel + " " + s)
			.collect(Collectors.toList());
	}

	@Override
	public List<String> getAutoComplete(CommandContext context) {
		if (!context.hasArg(0)) {
			return new ArrayList<>(commands.keySet());
		}
		String commandName = context.getArg(0);
		if (!context.hasArg(1)) {
			return commands.keySet().stream().filter(c -> c.startsWith(commandName)).collect(Collectors.toList());
		}
		Command<H> command = commands.get(commandName);
		if (command == null) {
			return Collections.emptyList();
		}
		return command.getAutoComplete(context.getSubContext(1));
	}

}
