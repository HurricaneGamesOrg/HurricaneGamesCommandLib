package org.hurricanegames.commandlib.providers.commands;

import java.util.function.Supplier;

import org.hurricanegames.commandlib.commands.CommandBasic;
import org.hurricanegames.commandlib.commands.CommandHelper;
import org.hurricanegames.commandlib.commands.CommandResponseException;
import org.hurricanegames.commandlib.configurations.SimpleConfiguration;

public class SimpleConfigurationReloadCommand<H extends CommandHelper<?,?,?>> extends CommandBasic<H> {

	protected final SimpleConfiguration configuration;
	protected final Supplier<String> configurationName;

	public SimpleConfigurationReloadCommand(H helper, SimpleConfiguration configuration, Supplier<String> configurationName) {
		super(helper);
		this.configuration = configuration;
		this.configurationName = configurationName;
	}

	@CommandHandler
	private void handleCommand() {
		try {
			configuration.reload();
			throw new CommandResponseException(helper.getMessages().getCommandConfigurationReloadSuccessMessage(configurationName.get()));
		} catch (CommandResponseException e) {
			throw e;
		} catch (Exception e) {
			handleReloadFailException(e);
			throw new CommandResponseException(helper.getMessages().getCommandConfigurationReloadFailMessage(configurationName.get(), e.getMessage()));
		}
	}

	@Override
	protected String getHelpExplainMessage() {
		return helper.getMessages().getCommandConfigurationReloadHelpMessage(configurationName.get());
	}

	protected void handleReloadFailException(Throwable t) {
		System.err.println("Error while reloading configuration " + configurationName);
		t.printStackTrace(System.err);
	}

}
