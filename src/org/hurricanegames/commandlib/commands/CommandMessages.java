package org.hurricanegames.commandlib.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface CommandMessages {

	public String getSubCommandLabelColor();

	public String getHelpArgsColor();

	public String getHelpExplainColor();

	public String getArgIndexErrorNegativeMessage();

	public String getArgIndexErrorOOBMessage(int argIndex);

	public String getSubCommandNotFoundMessage(String commandName);

	/**
	 * Error message for sender player command argument<br>
	 * Describes that {@link CommandSender} is not {@link Player}
	 * @return message
	 */
	public String getArgSenderPlayerErrorNotPlayerMessage();

	/**
	 * Help message for online player command argument
	 * @return message
	 */
	public String getArgOnlinePlayerHelp();

	/**
	 * Error message for online player command argument
	 * @param playerName name of player that is not online
	 * @return message
	 */
	public String getArgOnlinePlayerErrorNotOnlineMessage(String playerName);

	/**
	 * Help message for offline player command argument
	 * @return message
	 */
	public String getArgOfflinePlayerHelp();

	/**
	 * Error message for offline player command argument
	 * @param playerName name of player that never played on the server (info unavailable)
	 * @return message
	 */
	public String getArgOfflinePlayerErrorNeverPlayedMessage(String playerName);

	/**
	 * Error message for integer command argument
	 * @param string string that can't be parsed as int
	 * @return message
	 */
	public String getArgIntegerErrorNotIntegerMessage(String string);

	/**
	 * Error message for double command argume
	 * @param string string that can't be parsed as double
	 * @return message
	 */
	public String getArgDoubleErrorNotDoubleMessage(String string);

	/**
	 * Help message for boolean command argument
	 * @return message
	 */
	public String getArgBooleanHelp();

	/**
	 * True string value for boolean command argument
	 * @return true string value
	 */
	public String getArgBooleanValueTrue();

	/**
	 * False string value for boolean command argument
	 * @return false string value
	 */
	public String getArgBooleanValueFalse();

	/**
	 * Error message for boolean command argument
	 * @param string string that can't be parsed as boolean
	 * @return message
	 */
	public String getArgBooleanErrorNotBooleanMessage(String string);

	/**
	 * Error message for permissible has permission validator
	 * @param permission permission that permissible doesn't have
	 * @return message
	 */
	public String getValidateHasPermissionErrorNoPermissionMessage(String permission);

	/**
	 * Success message for configuration reload command
	 * @param configuration configuration name
	 * @return message
	 */
	public String getCommandConfigurationReloadSuccessMessage(String configuration);

	/**
	 * Fail message for configuration reload command
	 * @param configuration configuration name
	 * @param error configuration reload error
	 * @return message
	 */
	public String getCommandConfigurationReloadFailMessage(String configuration, String error);

	/**
	 * Help message for configuration reload command
	 * @param configuration configuration name
	 * @return message
	 */
	public String getCommandConfigurationReloadHelpMessage(String configuration);

}
