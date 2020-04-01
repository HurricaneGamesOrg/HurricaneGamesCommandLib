package org.hurricanegames.commandlib.providers.messages;

import org.hurricanegames.commandlib.commands.CommandMessages;
import org.hurricanegames.commandlib.configurations.SimpleConfiguration;

public abstract class DefaultMessagesProxy extends SimpleConfiguration implements CommandMessages {

	protected final CommandMessages parent;
	public DefaultMessagesProxy(CommandMessages parent) {
		this.parent = parent;
	}

	@Override
	public String getSubCommandLabelColor() {
		return parent.getSubCommandLabelColor();
	}

	@Override
	public String getHelpArgsColor() {
		return parent.getHelpArgsColor();
	}

	@Override
	public String getHelpExplainColor() {
		return parent.getHelpExplainColor();
	}

	@Override
	public String getArgIndexErrorNegativeMessage() {
		return parent.getArgIndexErrorNegativeMessage();
	}

	@Override
	public String getArgIndexErrorOOBMessage(int argIndex) {
		return parent.getArgIndexErrorOOBMessage(argIndex);
	}

	@Override
	public String getSubCommandNotFoundMessage(String commandName) {
		return parent.getSubCommandNotFoundMessage(commandName);
	}

	@Override
	public String getArgSenderPlayerErrorNotPlayerMessage() {
		return parent.getArgSenderPlayerErrorNotPlayerMessage();
	}

	@Override
	public String getArgOnlinePlayerHelp() {
		return parent.getArgOnlinePlayerHelp();
	}

	@Override
	public String getArgOnlinePlayerErrorNotOnlineMessage(String playerName) {
		return parent.getArgOnlinePlayerErrorNotOnlineMessage(playerName);
	}

	@Override
	public String getArgOfflinePlayerHelp() {
		return parent.getArgOfflinePlayerHelp();
	}

	@Override
	public String getArgOfflinePlayerErrorNeverPlayedMessage(String playerName) {
		return parent.getArgOfflinePlayerErrorNeverPlayedMessage(playerName);
	}

	@Override
	public String getArgIntegerErrorNotIntegerMessage(String string) {
		return parent.getArgIntegerErrorNotIntegerMessage(string);
	}

	@Override
	public String getArgDoubleErrorNotDoubleMessage(String string) {
		return parent.getArgDoubleErrorNotDoubleMessage(string);
	}

	@Override
	public String getArgBooleanHelp() {
		return parent.getArgBooleanHelp();
	}

	@Override
	public String getArgBooleanValueTrue() {
		return parent.getArgBooleanValueTrue();
	}

	@Override
	public String getArgBooleanValueFalse() {
		return parent.getArgBooleanValueFalse();
	}

	@Override
	public String getArgBooleanErrorNotBooleanMessage(String string) {
		return parent.getArgBooleanErrorNotBooleanMessage(string);
	}

	@Override
	public String getValidateHasPermissionErrorNoPermissionMessage(String permission) {
		return parent.getValidateHasPermissionErrorNoPermissionMessage(permission);
	}

	@Override
	public String getCommandConfigurationReloadHelpMessage(String configuration) {
		return parent.getCommandConfigurationReloadHelpMessage(configuration);
	}

	@Override
	public String getCommandConfigurationReloadSuccessMessage(String configuration) {
		return parent.getCommandConfigurationReloadSuccessMessage(configuration);
	}

	@Override
	public String getCommandConfigurationReloadFailMessage(String configuration, String error) {
		return parent.getCommandConfigurationReloadFailMessage(configuration, error);
	}

}
