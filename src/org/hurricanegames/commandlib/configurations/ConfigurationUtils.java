package org.hurricanegames.commandlib.configurations;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hurricanegames.commandlib.utils.MiscBukkitUtils;
import org.hurricanegames.commandlib.utils.ReflectionUtils;

public class ConfigurationUtils {

	/**
	 * Returns configuration value or throws an exception if not set
	 * @param <T> configuration value type
	 * @param config configuration
	 * @param path path
	 * @return configuration value
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getOrThrow(ConfigurationSection config, String path) {
		if (!config.isSet(path)) {
			throw new IllegalStateException("Configuration is missing path " + path);
		}
		return (T) config.get(path);
	}

	/**
	 * Safely saves the config to file<br>
	 * Actualy saves config to temp file and them atomically replaces actual target
	 * @param config config to save
	 * @param file target file
	 * @throws UncheckedIOException if saving or atomic move fails
	 */
	public static void safeSave(YamlConfiguration config, File file) {
		try {
			File tmpfile = new File(file.getParentFile(), file.getName() + ".tmp");
			config.save(tmpfile);
			Files.move(tmpfile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Loads configuration
	 * @param <T> configuration object instance type
	 * @param section configuration section
	 * @param configuraitonObject configuration object instance
	 * @param fields configuration fields
	 */
	@SafeVarargs
	public static <T> void load(T configuraitonObject, ConfigurationSection section, BaseConfigurationField<T>... fields) {
		Arrays.stream(fields).forEach(field -> field.load(configuraitonObject, section));
	}

	/**
	 * Saves configuration
	 * @param <T> configuration object instance type
	 * @param configuraitonObject configuration object instance
	 * @param section configuration section
	 * @param fields configuration fields
	 */
	@SafeVarargs
	public static <T> void save(T configuraitonObject, ConfigurationSection section, BaseConfigurationField<T>... fields) {
		Arrays.stream(fields).forEach(field -> field.save(configuraitonObject, section));
	}

	public abstract static class BaseConfigurationField<O> {

		protected final Field field;
		protected final String path;

		public BaseConfigurationField(Field field, String cPath) {
			this.field = field;
			this.path = cPath;
		}

		protected abstract void load(O configurationObject, ConfigurationSection section);

		protected abstract void save(O configurationObject, ConfigurationSection section);

	}

	public static class SimpleConfigurationField<O, T> extends BaseConfigurationField<O> {

		public SimpleConfigurationField(Field field, String path) {
			super(field, path);
		}

		@Override
		protected void load(O configurationObject, ConfigurationSection section) {
			Object object = section.get(path);
			if (object != null) {
				ReflectionUtils.setField(field, configurationObject, deserialize(object));
			}
		}

		@SuppressWarnings("unchecked")
		protected T deserialize(Object object) {
			return (T) object;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void save(O configurationObject, ConfigurationSection section) {
			Object object = ReflectionUtils.getField(field, configurationObject);
			if (object != null) {
				section.set(path, serialize((T) object));
			}
		}

		protected Object serialize(T object) {
			return object;
		}

	}

	public static class SimpleColorizedStringConfiguirationField<O> extends SimpleConfigurationField<O, String> {

		public SimpleColorizedStringConfiguirationField(Field field, String path) {
			super(field, path);
		}

		@Override
		protected String deserialize(Object object) {
			if (object instanceof String) {
				return MiscBukkitUtils.colorize((String) object);
			}
			return "";
		}

	}

}
