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
		for (ConfigurationField<T> field : fields) {
			try {
				field.load(section);
			} catch (Throwable t) {
				throw new RuntimeException("Unable to load field " + field.configurationField, t);
			}
		}
	}

	/**
	 * Saves configuration
	 * @param <T> configuration object instance type
	 * @param section configuration section
	 * @param fields configuration fields
	 */
	@SafeVarargs
	public static <T> void save(ConfigurationSection section, ConfigurationField<T>... fields) {
		for (ConfigurationField<T> field : fields) {
			try {
				field.save(section);
			} catch (Throwable t) {
				throw new RuntimeException("Unable to save field " + field.configurationField, t);
			}
		}
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

	public static class IntegerTypeSerializer implements TypeSerializer<Integer> {

		public static final IntegerTypeSerializer INSTANCE = new IntegerTypeSerializer();

		@Override
		public Integer deserialize(Object object) {
			if (object instanceof Integer) {
				return (Integer) object;
			} else if (object instanceof Number) {
				return Integer.valueOf(((Number) object).intValue());
			} else {
				return null;
			}
		}

		@Override
		public Object serialize(Integer type) {
			return type;
		}

	}

	public static class LongTypeSerializer implements TypeSerializer<Long> {

		public static final LongTypeSerializer INSTANCE = new LongTypeSerializer();

		@Override
		public Long deserialize(Object object) {
			if (object instanceof Long) {
				return (Long) object;
			} else if (object instanceof Number) {
				return Long.valueOf(((Number) object).longValue());
			} else {
				return null;
			}
		}

		@Override
		public Object serialize(Long type) {
			return type;
		}

	}

	public static class BaseConfigurationTypeSerializer<T extends BaseConfiguration> implements TypeSerializer<T> {

		protected final Supplier<T> configurationSupplier;

		public BaseConfigurationTypeSerializer(Supplier<T> configurationSupplier) {
			this.configurationSupplier = configurationSupplier;
		}

		@Override
		public T deserialize(Object object) {
			if (object instanceof ConfigurationSection) {
				T configuration = configurationSupplier.get();
				configuration.load((ConfigurationSection) object);
				return configuration;
			}
			return null;
		}

		@Override
		public Object serialize(T type) {
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
					if (element instanceof Map) {
						element = MiscBukkitUtils.createSection((Map<?, ?>) element);
					}
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
		protected final MapKVTypeSerializer<K, V> entrySerializer;

		public MapTypeSerializer(Supplier<C> mapSupplier, TypeSerializer<K> keySerializer, TypeSerializer<V> valueSerializer) {
			this.mapSupplier = mapSupplier;
			this.entrySerializer = new MapKVTypeSerializer<K,V>() {
				@Override
				protected Map.Entry<K, V> deserializeKV(String key, Object value) {
					return new AbstractMap.SimpleEntry<>(keySerializer.deserialize(key), valueSerializer.deserialize(value));
				}
				@Override
				protected Map.Entry<String, Object> serializeKV(K key, V value) {
					return new AbstractMap.SimpleEntry<>(keySerializer.serialize(key).toString(), valueSerializer.serialize(value));
				}
			};
		}

		public MapTypeSerializer(Supplier<C> mapSupplier, MapKVTypeSerializer<K, V> entrySerializer) {
			this.mapSupplier = mapSupplier;
			this.entrySerializer = entrySerializer;
		}

		public abstract static class MapKVTypeSerializer<K, V> implements TypeSerializer<Map.Entry<K, V>> {

			@SuppressWarnings("unchecked")
			@Override
			public Map.Entry<K, V> deserialize(Object object) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) object;
				return deserializeKV(entry.getKey(), entry.getValue());
			}

			@Override
			public Object serialize(Map.Entry<K, V> type) {
				return serializeKV(type.getKey(), type.getValue());
			}

			protected abstract Map.Entry<K, V> deserializeKV(String key, Object value);

			protected abstract Map.Entry<String, Object> serializeKV(K key, V value);

		}

		@Override
		public C deserialize(Object object) {
			if (object instanceof ConfigurationSection) {
				ConfigurationSection section = (ConfigurationSection) object;
				C map = mapSupplier.get();
				for (String keyString : section.getKeys(false)) {
					Map.Entry<K, V> entry = entrySerializer.deserialize(new AbstractMap.SimpleEntry<>(keyString, section.get(keyString)));
					if (entry != null) {
						map.put(entry.getKey(), entry.getValue());
					}
				}
				return map;
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object serialize(C type) {
			ConfigurationSection section = new MemoryConfiguration();
			for (Map.Entry<K, V> entry : type.entrySet()) {
				Map.Entry<String, Object> serialized = (Map.Entry<String, Object>) entrySerializer.serialize(entry);
				section.set(serialized.getKey(), serialized.getValue());
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

	public static class IntegerConfigurationField<O> extends SimpleConfigurationField<O, Integer> {

		public IntegerConfigurationField(O configuration, Field field, String path) {
			super(configuration, field, path, IntegerTypeSerializer.INSTANCE);
		}

	}

	public static class LongConfigurationField<O> extends SimpleConfigurationField<O, Long> {

		public LongConfigurationField(O configuration, Field field, String path) {
			super(configuration, field, path, LongTypeSerializer.INSTANCE);
		}

	}

	public static class SimpleColorizedStringConfigurationField<O> extends SimpleConfigurationField<O, String> {

		public SimpleColorizedStringConfigurationField(O configuration, Field field, String path) {
			super(configuration, field, path, ColorizedStringTypeSerializer.INSTANCE);
		}

	}

	public static class BaseConfigurationField<O, T extends BaseConfiguration> extends SimpleConfigurationField<O, T> {

		@SuppressWarnings("unchecked")
		protected static <T extends BaseConfiguration> BaseConfigurationTypeSerializer<T> createSerializer(Object object, Field field) {
			return new BaseConfigurationTypeSerializer<>(() -> {
				try {
					return (T) field.get(object);
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

		@SuppressWarnings("unchecked")
		public SimpleCollectionConfigurationField(O configuration, Field field, String path, CollectionTypeSerializer<? extends Collection<T>, T> serializer) {
			super(configuration, field, path, (TypeSerializer<Collection<T>>) serializer);
		}

	}

	public static class SimpleListConfigurationField<O, T> extends SimpleCollectionConfigurationField<O, T> {

		public SimpleListConfigurationField(O configuration, Field field, String path) {
			super(configuration, field, path);
		}

		public SimpleListConfigurationField(O configuration, Field field, String path, TypeSerializer<T> elementSerializer) {
			super(configuration, field, path, new ListTypeSerializer<>(elementSerializer));
		}

	}

	public static class SimpleSetConfigurationField<O, T> extends SimpleCollectionConfigurationField<O, T> {

		public SimpleSetConfigurationField(O configuration, Field field, String path) {
			super(configuration, field, path);
		}

		public SimpleSetConfigurationField(O configuration, Field field, String path, TypeSerializer<T> elementSerializer) {
			super(configuration, field, path, new SetTypeSerializer<>(elementSerializer));
		}

	}

	public static class SimpleColorizedStringListConfigurationField<O> extends SimpleListConfigurationField<O, String> {

		public SimpleColorizedStringListConfigurationField(O configuration, Field field, String path) {
			super(configuration, field, path, ColorizedStringTypeSerializer.INSTANCE);
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
