package org.hurricanegames.commandlib.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hurricanegames.commandlib.providers.playerinfo.PlayerInfo;
import org.hurricanegames.commandlib.utils.MiscBukkitUtils;
import org.hurricanegames.commandlib.utils.ReflectionUtils;
import org.hurricanegames.commandlib.utils.Tuple;

public abstract class CommandBasic<H extends CommandHelper<?, ?, ?>> implements Command<H> {

	protected final H helper;

	private final Map<String, Object> parsedValuesStorage = new HashMap<>();
	private final Method handleMethod;
	private final Tuple<String, CommandArgument<Object>>[] handleMethodArguments;

	private String createArgumentIdentififer(CommandArgumentDefinition definition) {
		Class<?> clazz = definition.value();
		CommandArgumentDefinition overrideDefiniton = clazz.getAnnotation(CommandArgumentDefinition.class);
		if ((overrideDefiniton != null) && (overrideDefiniton.value() != clazz)) {
			return createArgumentIdentififer(overrideDefiniton);
		} else {
			String identifier = definition.identifier();
			if (identifier.isEmpty()) {
				return clazz.getName();
			} else {
				return clazz.getName() + "_" + identifier;
			}
		}
	}

	private CommandArgument<Object> tryInitArgumentFromConstructor(
		CommandBasic<H> command, Set<String> handleMethodArgumentsInitialized, Map<String, Object> parsedValuesStorage,
		Constructor<?> argumentConstructor
	) {
		Parameter[] argumentConstructorParameters = argumentConstructor.getParameters();
		Object[] argumentConstructorParameterArray = new Object[argumentConstructorParameters.length];
		if (!argumentConstructorParameters[0].getType().isInstance(command)) {
			throw new IllegalArgumentException(MessageFormat.format(
				"Constuctor first parameter {0} has wrong type (should be {1} (or one of it''s superclasses))",
				argumentConstructorParameters[0], command.getClass()
			));
		}
		argumentConstructorParameterArray[0] = command;
		if (argumentConstructorParameterArray.length > 1) {
			for (int argumentParameterIndex = 1; argumentParameterIndex < argumentConstructorParameters.length; argumentParameterIndex++) {
				Parameter argumentParameter = argumentConstructorParameters[argumentParameterIndex];
				CommandArgumentDefinition argumentParameterDefinition = argumentParameter.getAnnotation(CommandArgumentDefinition.class);
				if (argumentParameterDefinition == null) {
					throw new IllegalArgumentException(MessageFormat.format(
						"Constuctor parameter {0} is missing {1} annotation",
						argumentParameter, CommandArgumentDefinition.class.getName()
					));
				}
				if (argumentParameter.getType() != Supplier.class) {
					throw new IllegalArgumentException(MessageFormat.format(
						"Constructor parameter {0} has invalid type {1} (should be {2})",
						argumentParameter, argumentParameter.getType(), Supplier.class.getName()
					));
				}
				String argumentParameterIdentifier = createArgumentIdentififer(argumentParameterDefinition);
				if (!handleMethodArgumentsInitialized.contains(argumentParameterIdentifier)) {
					throw new IllegalArgumentException(MessageFormat.format(
						"Cosntructor parameter {0} uses command argument {1} which doesn''t exist (currenty existing arguments: {2})",
						argumentParameter, argumentParameterIdentifier, handleMethodArgumentsInitialized
					));
				}
				argumentConstructorParameterArray[argumentParameterIndex] = (Supplier<?>) () -> parsedValuesStorage.get(argumentParameterIdentifier);
			}
		}
		return ReflectionUtils.newInstance(argumentConstructor, argumentConstructorParameterArray);
	}

	private CommandArgument<Object> tryInitArgument(
		CommandBasic<H> command, Set<String> handleMethodArgumentsInitialized, Map<String, Object> parsedValuesStorage,
		Class<?> argumentClass
	) {
		Map<Constructor<?>, Throwable> constructorsInitFailCause = new HashMap<>();
		for (Constructor<?> argumentConstructor : argumentClass.getDeclaredConstructors()) {
			argumentConstructor = ReflectionUtils.setAccessible(argumentConstructor);
			try {
				return tryInitArgumentFromConstructor(
					command, handleMethodArgumentsInitialized, parsedValuesStorage,
					argumentConstructor
				);
			} catch (Throwable t) {
				constructorsInitFailCause.put(argumentConstructor, t);
			}
		}
		throw new IllegalArgumentException(MessageFormat.format(
			"No suitable constructor found for initializing command argument {0}. Tried constructors: {1}",
			argumentClass,
			constructorsInitFailCause.entrySet().stream()
			.map(entry -> entry.getKey().toString() + " - " + entry.getValue().getMessage())
			.collect(Collectors.joining(", "))
		));
	}

	@SuppressWarnings("unchecked")
	public CommandBasic(H helper) {
		this.helper = helper;

		this.handleMethod =
			ReflectionUtils.setAccessible(
				Arrays.stream(getClass().getDeclaredMethods())
				.filter(cmethod -> cmethod.getAnnotation(CommandHandler.class) != null)
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("Missing method annotated with " + CommandHandler.class.getName()))
			);


		Parameter[] handleMethodParameters = handleMethod.getParameters();
		Set<String> handleMethodArgumentsParsedIdetifiers = new HashSet<>();
		this.handleMethodArguments = new Tuple[handleMethodParameters.length];
		for (int handleMethodParameterIndex = 0; handleMethodParameterIndex < handleMethodParameters.length; handleMethodParameterIndex++) {
			Parameter handleMethodParameter = handleMethodParameters[handleMethodParameterIndex];

			CommandArgumentDefinition handleMethodParameterDefinition = handleMethodParameter.getAnnotation(CommandArgumentDefinition.class);

			if (handleMethodParameterDefinition == null) {
				throw new IllegalArgumentException(MessageFormat.format(
					"Command handle method {0} parameter {1} is missing {2} annotation",
					handleMethod, handleMethodParameter, CommandArgumentDefinition.class.getName()
				));
			}

			String argumentIdentifier = createArgumentIdentififer(handleMethodParameterDefinition);
			if (handleMethodArgumentsParsedIdetifiers.contains(argumentIdentifier)) {
				throw new IllegalArgumentException(MessageFormat.format(
					"Command handle method {0} parameter {1} uses identififer {2} which is already taken",
					handleMethod, handleMethodParameter, argumentIdentifier
				));
			}
			this.handleMethodArguments[handleMethodParameterIndex] = new Tuple<>(argumentIdentifier, tryInitArgument(
				this, handleMethodArgumentsParsedIdetifiers, parsedValuesStorage, handleMethodParameterDefinition.value()
			));
			handleMethodArgumentsParsedIdetifiers.add(argumentIdentifier);
		}
	}

	@Override
	public H getHelper() {
		return helper;
	}

	@Override
	public void handleCommand(CommandContext context) {
		try {
			int argIndex = 0;
			Object[] handleMethodValues = new Object[handleMethodArguments.length];
			for (int i = 0; i < handleMethodArguments.length; i++) {
				Tuple<String, CommandArgument<Object>> tuple = handleMethodArguments[i];
				CommandArgument<Object> argument = tuple.getObject2();
				Object value = argument.parse(context, argIndex);
				handleMethodValues[i] = value;
				parsedValuesStorage.put(tuple.getObject1(), value);
				if (argument.isPositional()) {
					argIndex++;
				}
			}
			ReflectionUtils.invoke(handleMethod, this, handleMethodValues);
		} finally {
			parsedValuesStorage.clear();
		}
	}

	@Override
	public List<String> getAutoComplete(CommandContext context) {
		try {
			int argIndex = 0;
			for (int i = 0; i < handleMethodArguments.length; i++) {
				Tuple<String, CommandArgument<Object>> tuple = handleMethodArguments[i];
				CommandArgument<Object> argument = tuple.getObject2();
				if (argument.isPositional() && !context.hasArg(argIndex + 1)) {
					return argument.complete(context, argIndex);
				}
				parsedValuesStorage.put(tuple.getObject1(), argument.parse(context, argIndex));
				if (argument.isPositional()) {
					argIndex++;
				}
			}
		} finally {
			parsedValuesStorage.clear();
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getHelpMessages(String commandLabel) {
		StringBuilder help = new StringBuilder(100);
		help.append(commandLabel);
		help.append(helper.getMessages().getHelpArgsColor());
		Arrays.stream(handleMethodArguments)
		.map(tuple -> tuple.getObject2().getHelpMessage())
		.filter(Objects::nonNull)
		.forEach(s -> {
			help.append(" ");
			help.append(s);
		});
		help.append(helper.getMessages().getHelpExplainColor());
		help.append(" - ");
		help.append(getHelpExplainMessage());
		return Collections.singletonList(help.toString());
	}

	protected abstract String getHelpExplainMessage();

	/**
	 * Annotate a method which is responsible for handling command after all arguments have been handled
	 */
	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	protected static @interface CommandHandler {
	}

	/**
	 * Annotate an argument in method annotated by {@link CommandHandler} to automatically inject argument with specified type <br>
	 * <br>
	 * Annotate an argument in constructor of {@link CommandArgument} instance to inject {@link Supplier} that will supply the argument when parsing<br>
	 * Only arguments that are present in method annotated by {@link CommandHandler} can be supplied
	 * <br>
	 * Annotate an argument type to override another argument type
	 */
	@Target({ElementType.PARAMETER, ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	protected static @interface CommandArgumentDefinition {

		@SuppressWarnings("rawtypes")
		Class<? extends CommandBasic.CommandArgument> value();

		String identifier() default "";

	}

	protected abstract class CommandArgument<V> {

		/**
		 * Returns if argIndex is actually used for this argument, and should be incremented for use with next argument
		 * @return true if argIndex should be incremented after this argument
		 */
		protected abstract boolean isPositional();

		/**
		 * Returns parsed value <br>
		 * Can also throw {@link CommandResponseException} to interrupt command handling and send player a message
		 * @param context command context
		 * @param argIndex current argument index
		 * @return parsed value
		 */
		protected abstract V parse(CommandContext context, int argIndex);

		/**
		 * Returns list of strings for tab-complete <br>
		 * Can also throw {@link CommandResponseException} to cancel tab-complete
		 * @param context command context
		 * @param argIndex current argument index
		 * @return parsed value
		 */
		protected abstract List<String> complete(CommandContext context, int argIndex);

		protected abstract String getHelpMessage();

	}

	protected abstract class CommandArgumentSender<V> extends CommandArgument<V> {

		/*
		 * Not positional
		 */
		@Override
		protected boolean isPositional() {
			return false;
		}

		/*
		 * Just return result from {@link #parseValue(CommandSender)}
		 */
		@Override
		protected V parse(CommandContext context, int argIndex) {
			return parseSender(context.getSender());
		}

		/*
		 * Don't handle tab-complete
		 */
		@Override
		protected List<String> complete(CommandContext context, int argIndex) {
			throw new UnsupportedOperationException("Command sender argument doesn't handle tab-complete");
		}

		/*
		 * Don't have help message (Tho that may be worth changing to indicate who can actually use the command)
		 */
		@Override
		protected String getHelpMessage() {
			return null;
		}

		/**
		 * Return parsed value from command sender <br>
		 * Can also throw {@link CommandResponseException} to interrupt command handling and send player a message
		 * @param sender command sender
		 * @return value
		 */
		protected abstract V parseSender(CommandSender sender);

	}

	protected abstract class CommandArgumentContextual<V> extends CommandArgument<V> {

		/*
		 * Not positional
		 */
		@Override
		protected boolean isPositional() {
			return false;
		}

		/*
		 * Just return result from {@link #parseValue()}
		 */
		@Override
		protected V parse(CommandContext context, int argIndex) {
			return parseValue();
		}

		/*
		 * Don't handle tab-complete
		 */
		@Override
		protected List<String> complete(CommandContext context, int argIndex) {
			throw new UnsupportedOperationException("Contextual argument doesn't handle tab-complete");
		}

		/*
		 * Don't have help message
		 */
		@Override
		protected String getHelpMessage() {
			return null;
		}

		/**
		 * Returns parsed value from other already parsed values <br>
		 * Can also throw {@link CommandResponseException} to interrupt command handling and send player a message
		 * @return parsed value
		 */
		protected abstract V parseValue();

	}

	protected abstract class CommandArgumentPositional<V> extends CommandArgument<V> {

		/*
		 * Is positional
		 */
		@Override
		protected boolean isPositional() {
			return true;
		}

		/*
		 * If is optional and argument raw value doesn't exist, return result from {@link #parseValue(String)} using null
		 * Otherwise, return result from {@link #parseValue(String)} using raw argument value from provided argIndex
		 */
		@Override
		protected V parse(CommandContext context, int argIndex) {
			if (isOptional() && !context.hasArg(argIndex)) {
				return parseValue(null);
			} else {
				return parseValue(context.getArg(argIndex));
			}
		}

		/*
		 * If argument raw value of this argIndex doesn't exist, return result from {@link #parseValue(String)} using empty string
		 * Otherwise, return result from {@link #parseValue(String)} using raw argument value from provided argIndex
		 */
		@Override
		protected List<String> complete(CommandContext context, int argIndex) {
			if (!context.hasArg(argIndex)) {
				return complete("");
			} else {
				return complete(context.getArg(argIndex));
			}
		}

		/**
		 * Returns parsed value from provided raw value <br>
		 * Can also throw {@link CommandResponseException} to interrupt command handling and send player a message
		 * @param arg raw value
		 * @return parsed value
		 */
		protected abstract V parseValue(String arg);

		/**
		 * Returns list of strings for tab-complete for provided raw value <br>
		 * Can also throw {@link CommandResponseException} to cancel tab-complete
		 * @param arg  raw value
		 * @return list of strings for tab-complete response
		 */
		protected abstract List<String> complete(String arg);

		/**
		 * Returns if value is optional and the parse value logic should decide on default value if player didn't input one
		 * @return true if value is optional
		 */
		protected boolean isOptional() {
			return false;
		}

	}



	@CommandArgumentDefinition(CommandBasic.CommandArgumentSenderRaw.class)
	protected class CommandArgumentSenderRaw extends CommandArgumentSender<CommandSender> {

		@Override
		public CommandSender parseSender(CommandSender sender) {
			return sender;
		}

	}

	@CommandArgumentDefinition(CommandBasic.CommandArgumentSenderPlayer.class)
	protected class CommandArgumentSenderPlayer extends CommandArgumentSender<Player> {

		@Override
		public Player parseSender(CommandSender sender) {
			Player player = helper.getSenderAsPlayer(sender, helper.getMessages().getArgSenderPlayerErrorNotPlayerMessage());
			validate(player);
			return player;
		}

		/**
		 * Override to additionally validate player
		 * @param player
		 */
		protected void validate(Player player) {
		}

	}

	protected class CommandArgumentBoolean extends CommandArgumentPositional<Boolean> {

		@Override
		public Boolean parseValue(String arg) {
			return helper.parseBoolean(arg);
		}

		@Override
		public String getHelpMessage() {
			return helper.getMessages().getArgBooleanHelp();
		}

		@Override
		public List<String> complete(String arg) {
			return
				Arrays.asList(helper.getMessages().getArgBooleanValueTrue(), helper.getMessages().getArgBooleanValueFalse()).stream()
				.filter(b -> b.startsWith(arg))
				.collect(Collectors.toList());
		}

	}

	protected class CommandArgumentOnlinePlayer extends CommandArgumentPositional<Player> {

		@Override
		public Player parseValue(String arg) {
			Player player = helper.parseOnlinePlayer(arg);
			validate(player);
			return player;
		}

		@Override
		public List<String> complete(String arg) {
			return
				Bukkit.getOnlinePlayers().stream()
				.filter(player -> player.getName().startsWith(arg))
				.filter(helper.createPredicate(this::validate))
				.map(Player::getName)
				.collect(Collectors.toList());
		}

		@Override
		public String getHelpMessage() {
			return helper.getMessages().getArgOnlinePlayerHelp();
		}

		/**
		 * Override to additionally validate player
		 * @param player
		 */
		protected void validate(Player player) {
		}

	}

	protected class CommandArgumentOfflinePlayer<V extends PlayerInfo> extends CommandArgumentPositional<V> {

		@SuppressWarnings("unchecked")
		@Override
		public V parseValue(String arg) {
			V player = (V) helper.parseOfflinePlayer(arg);
			validate(player);
			return player;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<String> complete(String arg) {
			return
				Bukkit.getOnlinePlayers().stream()
				.filter(player -> player.getName().startsWith(arg))
				.filter(helper.createPredicate(player -> validate((V) helper.getPlayersInfoProvider().createFromPlayer(player))))
				.map(Player::getName)
				.collect(Collectors.toList());
		}

		@Override
		public String getHelpMessage() {
			return helper.getMessages().getArgOnlinePlayerHelp();
		}

		/**
		 * Override to additionally validate player
		 * @param player
		 */
		protected void validate(V player) {
		}

	}

	protected abstract class CommandArgumentColorizedString extends CommandArgumentPositional<String> {

		@Override
		protected String parseValue(String arg) {
			return MiscBukkitUtils.colorize(arg);
		}

		@Override
		protected List<String> complete(String arg) {
			return Collections.emptyList();
		}

	}

	protected abstract class CommandArgumentInteger extends CommandArgumentPositional<Integer> {

		@Override
		public Integer parseValue(String arg) {
			int value = helper.parseInteger(arg);
			validate(value);
			return value;
		}

		@Override
		public List<String> complete(String arg) {
			return Collections.emptyList();
		}

		/**
		 * Override to additionally validate integer
		 * @param value
		 */
		protected void validate(int value) {
		}

	}

	protected abstract class CommandArgumentDouble extends CommandArgumentPositional<Double> {

		@Override
		protected Double parseValue(String arg) {
			double value = helper.parseDouble(arg);
			validate(value);
			return value;
		}

		@Override
		protected List<String> complete(String arg) {
			return Collections.emptyList();
		}

		/**
		 * Override to additionally validate double
		 * @param value
		 */
		protected void validate(double value) {
		}

	}

}
