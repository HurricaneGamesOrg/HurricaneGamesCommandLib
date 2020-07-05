package org.hurricanegames.commandlib.configurations;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
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
	 * Actually saves config to temp file and them atomically replaces actual target
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
	 * @param fields configuration fields
	 */
	@SafeVarargs
	public static <T> void load(ConfigurationSection section, ConfigurationField<T>... fields) {
		Arrays.stream(fields).forEach(field -> field.load(section));
	}

	/**
	 * Saves configuration
	 * @param <T> configuration object instance type
	 * @param section configuration section
	 * @param fields configuration fields
	 */
	@SafeVarargs
	public static <T> void save(ConfigurationSection section, ConfigurationField<T>... fields) {
		Arrays.stream(fields).forEach(field -> field.save(section));
	}

	public static interface TypeSerializer<T> {

		public T deserialize(Object object);

		public Object serialize(T type);

	}

	public static class IdentityTypeSerializer<T> implements TypeSerializer<T> {

		protected final Class<?> clazz;

		public IdentityTypeSerializer(Class<?> clazz) {
			this.clazz = clazz;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T deserialize(Object object) {
			if (clazz.isInstance(object)) {
				return (T) object;
			}
			return null;
		}

		@Override
		public Object serialize(T type) {
			return type;
		}

	}

	public static class BaseConfigurationTypeSerializer implements TypeSerializer<BaseConfiguration> {

		protected final Supplier<BaseConfiguration> configurationSupplier;

		public BaseConfigurationTypeSerializer(Supplier<BaseConfiguration> configurationSupplier) {
			this.configurationSupplier = configurationSupplier;
		}

		@Override
		public BaseConfiguration deserialize(Object object) {
			if (object instanceof ConfigurationSection) {
				BaseConfiguration configuration = configurationSupplier.get();
				configuration.load((ConfigurationSection) object);
				return configuration;
			}
			return null;
		}

		@Override
		public Object serialize(BaseConfiguration type) {
			ConfigurationSection section = new MemoryConfiguration();
			type.save(section);
			return section;
		}

	}

	public static class ColorizedStringTypeSerializer implements TypeSerializer<String> {

		public static final ColorizedStringTypeSerializer INSTANCE = new ColorizedStringTypeSerializer();

		@Override
		public String deserialize(Object object) {
			if (object instanceof String) {
				return MiscBukkitUtils.colorize((String) object);
			}
			return null;
		}

		@Override
		public Object serialize(String type) {
			return type;
		}

	}

	public static class CollectionTypeSerializer<C extends Collection<T>, T> implements TypeSerializer<C> {

		protected final Supplier<C> collectionSupplier;
		protected final TypeSerializer<T> elementSerializer;

		public CollectionTypeSerializer(Supplier<C> collectionSupplier, TypeSerializer<T> elementSerializer) {
			this.collectionSupplier = collectionSupplier;
			this.elementSerializer = elementSerializer;
		}

		@Override
		public C deserialize(Object object) {
			if (object instanceof Collection) {
				C collection = collectionSupplier.get();
				for (Object element : (Collection<?>) object) {
					T t = elementSerializer.deserialize(element);
					if (t != null) {
						collection.add(t);
					}
				}
				return collection;
			}
			return null;
		}

		@Override
		public Object serialize(C type) {
			List<Object> list = new ArrayList<>();
			for (T element : type) {
				list.add(elementSerializer.serialize(element));
			}
			return list;
		}

	}

	public static class ListTypeSerializer<T> extends CollectionTypeSerializer<List<T>, T> {

		public ListTypeSerializer(TypeSerializer<T> elementSerializer) {
			super(ArrayList::new, elementSerializer);
		}

	}

	public static class SetTypeSerializer<T> extends CollectionTypeSerializer<Set<T>, T> {

		public SetTypeSerializer(TypeSerializer<T> elementSerializer) {
			super(() -> Collections.newSetFromMap(new LinkedHashMap<>()), elementSerializer);
		}

	}

	public static class MapTypeSerializer<C extends Map<K, V>, K, V> implements TypeSerializer<C> {

		protected final Supplier<C> mapSupplier;
		protected final TypeSerializer<K> keySerializer;
		protected final TypeSerializer<V> valueSerializer;

		public MapTypeSerializer(Supplier<C> mapSupplier, TypeSerializer<K> keySerializer, TypeSerializer<V> valueSerializer) {
			this.mapSupplier = mapSupplier;
			this.keySerializer = keySerializer;
			this.valueSerializer = valueSerializer;
		}

		@Override
		public C deserialize(Object object) {
			if (object instanceof ConfigurationSection) {
				ConfigurationSection section = (ConfigurationSection) object;
				C map = mapSupplier.get();
				for (String keyString : section.getKeys(false)) {
					K key = keySerializer.deserialize(keyString);
					V value = valueSerializer.deserialize(section.get(keyString));
					if ((key != null) && (value != null)) {
						map.put(key, value);
					}
				}
				return map;
			}
			return null;
		}

		@Override
		public Object serialize(C type) {
			ConfigurationSection section = new MemoryConfiguration();
			for (Map.Entry<K, V> entry : type.entrySet()) {
				section.set((String) keySerializer.serialize(entry.getKey()), valueSerializer.serialize(entry.getValue()));
			}
			return section;
		}

	}

	public abstract static class ConfigurationField<O> {

		protected final O configuration;
		protected final Field configurationField;
		protected final String path;

		public ConfigurationField(O configuration, Field field, String path) {
			this.configurationField = field;
			this.configuration = configuration;
			this.path = path;
		}

		protected abstract void load(ConfigurationSection section);

		protected abstract void save(ConfigurationSection section);

	}

	public static class SimpleConfigurationField<O, T> extends ConfigurationField<O> {

		protected final TypeSerializer<T> elementSerializer;

		public SimpleConfigurationField(O configuration, Field field, String path) {
			this(configuration, field, path, new IdentityTypeSerializer<>(field.getType()));
		}

		public SimpleConfigurationField(O configuration, Field field, String path, TypeSerializer<T> elementSerializer) {
			super(configuration, field, path);
			this.elementSerializer = elementSerializer;
		}

		@Override
		protected void load(ConfigurationSection section) {
			Object object = section.get(path);
			if (object != null) {
				T t = elementSerializer.deserialize(object);
				if (t != null) {
					ReflectionUtils.setField(configurationField, configuration, t);
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void save(ConfigurationSection section) {
			Object object = ReflectionUtils.getField(configurationField, configuration);
			if (object != null) {
				section.set(path, elementSerializer.serialize((T) object));
			}
		}

	}

	public static class SimpleColorizedStringConfigurationField<O> extends SimpleConfigurationField<O, String> {

		public SimpleColorizedStringConfigurationField(O configuration, Field field, String path) {
			super(configuration, field, path, ColorizedStringTypeSerializer.INSTANCE);
		}

	}

	public static class BaseConfigurationField<O> extends SimpleConfigurationField<O, BaseConfiguration> {

		protected static BaseConfigurationTypeSerializer createSerializer(Object object, Field field) {
			return new BaseConfigurationTypeSerializer(() -> {
				try {
					return (BaseConfiguration) field.get(object);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			});
		}

		public BaseConfigurationField(O configuration, Field field, String path) {
			super(configuration, field, path, createSerializer(configuration, field));
		}

	}

	public static class SimpleCollectionConfigurationField<O, T> extends SimpleConfigurationField<O, Collection<T>> {

		@SuppressWarnings("unchecked")
		protected static <T> TypeSerializer<T> createCollectionElementSerializer(Field field) {
			Type type = field.getGenericType();
			if (type instanceof ParameterizedType) {
				Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
				if (actualTypeArguments.length == 1) {
					Type actualTypeArgument = actualTypeArguments[0];
					if (actualTypeArgument instanceof Class) {
						return new IdentityTypeSerializer<>((Class<T>) actualTypeArgument);
					}
				}
			}
			System.err.println("Unable to get element type from collection generic type " + type.getClass().getName() + "(" + type + ")");
			return new IdentityTypeSerializer<>(Object.class);
		}

		@SuppressWarnings("unchecked")
		protected static <T> CollectionTypeSerializer<Collection<T>, T> createCollectionSerializer(Field field, TypeSerializer<T> elementSerializer) {
			Class<?> fieldType = field.getType();
			if (!fieldType.isInterface() && !Modifier.isAbstract(fieldType.getModifiers())) {
				return new CollectionTypeSerializer<>(() -> {
					try {
						return (Collection<T>) fieldType.getConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new RuntimeException("Unable to create collection instance", e);
					}
				}, elementSerializer);
			} else if (Queue.class.isAssignableFrom(fieldType)) {
				return new CollectionTypeSerializer<>(ArrayDeque::new, elementSerializer);
			} else if (Set.class.isAssignableFrom(fieldType)) {
				return new CollectionTypeSerializer<>(HashSet::new, elementSerializer);
			} else {
				return new CollectionTypeSerializer<>(ArrayList::new, elementSerializer);
			}
		}

		public SimpleCollectionConfigurationField(O configuration, Field field, String path) {
			this(configuration, field, path, createCollectionElementSerializer(field));
		}

		public SimpleCollectionConfigurationField(O configuration, Field field, String path, TypeSerializer<T> elementSerializer) {
			this(configuration, field, path, createCollectionSerializer(field, elementSerializer));
		}

		public SimpleCollectionConfigurationField(O configuration, Field field, String path, CollectionTypeSerializer<Collection<T>, T> serializer) {
			super(configuration, field, path, serializer);
		}

	}

	public static class SimpleListConfigurationField<O, T> extends SimpleCollectionConfigurationField<O, List<T>> {

		public SimpleListConfigurationField(O configuration, Field field, String path) {
			super(configuration, field, path);
		}

		public SimpleListConfigurationField(O configuration, Field field, String path, TypeSerializer<T> elementSerializer) {
			super(configuration, field, path, new ListTypeSerializer<>(elementSerializer));
		}

	}

	public static class SimpleSetConfigurationField<O, T> extends SimpleCollectionConfigurationField<O, Set<T>> {

		public SimpleSetConfigurationField(O configuration, Field field, String path) {
			super(configuration, field, path);
		}

		public SimpleSetConfigurationField(O configuration, Field field, String path, TypeSerializer<T> elementSerializer) {
			super(configuration, field, path, new SetTypeSerializer<>(elementSerializer));
		}

	}

	public static class SimpleMapConfigurationField<O, K, V> extends SimpleConfigurationField<O, Map<K, V>> {

		protected static <K, V> Map.Entry<TypeSerializer<K>, TypeSerializer<V>> createMapKVSerializers(Field field) {
			Type type = field.getGenericType();
			if (type instanceof ParameterizedType) {
				Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
				if (actualTypeArguments.length == 2) {
					Type keyType = actualTypeArguments[0];
					Type valueType = actualTypeArguments[1];
					if ((keyType instanceof Class) && (valueType instanceof Class)) {
						return new AbstractMap.SimpleImmutableEntry<>(
							new IdentityTypeSerializer<>((Class<?>) keyType), new IdentityTypeSerializer<>((Class<?>) valueType)
						);
					}
				}
			}
			System.err.println("Unable to get element type from map generic type " + type.getClass().getName() + "(" + type + ")");
			return new AbstractMap.SimpleImmutableEntry<>(new IdentityTypeSerializer<>(Object.class), new IdentityTypeSerializer<>(Object.class));
		}

		@SuppressWarnings("unchecked")
		protected static <K, V> MapTypeSerializer<Map<K, V>, K, V> createMapSerializer(Field field, Map.Entry<TypeSerializer<K>, TypeSerializer<V>> serializers) {
			Class<?> fieldType = field.getType();
			if (!fieldType.isInterface() && !Modifier.isAbstract(fieldType.getModifiers())) {
				return new MapTypeSerializer<>(() -> {
					try {
						return (Map<K, V>) fieldType.getConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new RuntimeException("Unable to create collection instance", e);
					}
				}, serializers.getKey(), serializers.getValue());
			} else if (NavigableMap.class.isAssignableFrom(fieldType)) {
				return new MapTypeSerializer<>(TreeMap::new, serializers.getKey(), serializers.getValue());
			} else {
				return new MapTypeSerializer<>(LinkedHashMap::new, serializers.getKey(), serializers.getValue());
			}
		}

		public SimpleMapConfigurationField(O configuration, Field field, String path) {
			super(configuration, field, path, createMapSerializer(field, createMapKVSerializers(field)));
		}

		public SimpleMapConfigurationField(O configuration, Field field, String path, TypeSerializer<K> keySerializer, TypeSerializer<V> valueSerializer) {
			super(configuration, field, path, createMapSerializer(field, new AbstractMap.SimpleImmutableEntry<>(keySerializer, valueSerializer)));
		}

		public SimpleMapConfigurationField(O configuration, Field field, String path, MapTypeSerializer<Map<K,V>, K, V> mapSerializer) {
			super(configuration, field, path, mapSerializer);
		}

	}

}
