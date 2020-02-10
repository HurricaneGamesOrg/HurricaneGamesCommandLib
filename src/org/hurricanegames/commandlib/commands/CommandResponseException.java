package org.hurricanegames.commandlib.commands;

import java.text.MessageFormat;

public class CommandResponseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CommandResponseException(String message) {
		super(message);
	}

	public CommandResponseException(String format, Object... args) {
		this(MessageFormat.format(format, args));
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}

}