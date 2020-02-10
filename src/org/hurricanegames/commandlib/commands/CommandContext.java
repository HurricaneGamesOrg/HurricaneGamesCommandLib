package org.hurricanegames.commandlib.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

public class CommandContext {

	protected final CommandMessages messagesprovider;

	protected final CommandSender sender;
	protected final String command;
	protected final String[] args;

	public CommandContext(CommandMessages messagesprovider, CommandSender sender, String command, String[] args) {
		this.messagesprovider = messagesprovider;
		this.sender = sender;
		this.command = command;
		this.args = args.clone();
	}

	public String getCommand() {
		return command;
	}

	public String[] getArgs() {
		return args.clone();
	}

	public boolean hasArg(int argIndex) {
		return (argIndex >= 0) && (argIndex < args.length);
	}

	public String getArg(int argIndex) {
		if (argIndex < 0) {
			throw new CommandResponseException(messagesprovider.getArgIndexErrorNegativeMessage());
		}
		if (argIndex >= args.length) {
			throw new CommandResponseException(messagesprovider.getArgIndexErrorOOBMessage(argIndex));
		}
		return args[argIndex];
	}

	public CommandSender getSender() {
		return sender;
	}

	public CommandContext getSubContext(int argIndexStart) {
		if (argIndexStart == 0) {
			return this;
		} else {
			return new CommandContext(messagesprovider, sender, command + " " + args[argIndexStart - 1], Arrays.copyOfRange(args, argIndexStart, args.length));
		}
	}

}
