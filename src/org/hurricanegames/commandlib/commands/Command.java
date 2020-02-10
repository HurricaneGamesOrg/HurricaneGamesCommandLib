package org.hurricanegames.commandlib.commands;

import java.util.List;

public interface Command<H extends CommandHelper<?, ?, ?>> {

	public H getHelper();

	public void handleCommand(CommandContext context);

	public List<String> getAutoComplete(CommandContext context);

	public List<String> getHelpMessages(String commandLabel);

}
