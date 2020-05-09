package org.hurricanegames.commandlib.configurations;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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
				ReflectionUtils.setField(field, configurationObject, deserialize(configurationObject, object));
			}
		}

		@SuppressWarnings("unchecked")
		protected T deserialize(O configurationObject, Object object) {
			return (T) object;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void save(O configurationObject, ConfigurationSection section) {
			Object object = ReflectionUtils.getField(field, configurationObject);
			if (object != null) {
				section.set(path, serialize(configurationObject, (T) object));
			}
		}

		protected Object serialize(O configurationObject, T object) {
			return object;
		}

	}

	public abstract static class SimpleCollectionConfigurationField<O, C extends Collection<T>, T> extends SimpleConfigurationField<O, C> {

		public SimpleCollectionConfigurationField(Field field, String path) {
			super(field, path);
		}

		@Override
		protected C deserialize(O configurationObject, Object object) {
			C collection = createCollection(configurationObject);
			if (object instanceof Collection) {
				for (Object element : (Collection<?>) object) {
					collection.add(deserializeElement(configurationObject, element));
				}
			}
			return collection;
		}

		protected abstract C createCollection(O configurationObject);

		@SuppressWarnings("unchecked")
		protected T deserializeElement(O configurationObject, Object element) {
			return (T) element;
		}

		@Override
		protected Object serialize(O configurationObject, C object) {
			List<Object> list = new ArrayList<>();
			for (T element : object) {
				list.add(serializeElement(configurationObject, element));
			}
			return list;
		}

		protected Object serializeElement(O configurationObject, T element) {
			return element;
		}

	}

	public static class SimpleListConfigurationField<O, T> extends SimpleCollectionConfigurationField<O, List<T>, T> {

		public SimpleListConfigurationField(Field field, String path) {
			super(field, path);
		}

		@Override
		protected List<T> createCollection(O configurationObject) {
			return new ArrayList<>();
		}

	}

	public static class SimpleSetConfigurationField<O, T> extends SimpleCollectionConfigurationField<O, Set<T>, T> {

		public SimpleSetConfigurationField(Field field, String path) {
			super(field, path);
		}

		@Override
		protected Set<T> createCollection(O configurationObject) {
			return Collections.newSetFromMap(new LinkedHashMap<>());
		}

	}

	public static class SimpleColorizedStringConfigurationField<O> extends SimpleConfigurationField<O, String> {

		public SimpleColorizedStringConfigurationField(Field field, String path) {
			super(field, path);
		}

		@Override
		protected String deserialize(O configurationObject, Object object) {
			if (object instanceof String) {
				return MiscBukkitUtils.colorize((String) object);
			}
			return "";
		}

	}

	public static class SimpleColorizedStringListConfigurationField<O> extends SimpleListConfigurationField<O, String> {

		public SimpleColorizedStringListConfigurationField(Field field, String path) {
			super(field, path);
		}

		@Override
		protected String deserializeElement(O configurationObject, Object element) {
			return MiscBukkitUtils.colorize((String) element);
		}

	}

}
