package org.hurricanegames.commandlib.configurations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hurricanegames.commandlib.configurations.BaseConfiguration.ConfigurationFieldDefinition.DefaultConfigurationField;
import org.hurricanegames.commandlib.configurations.ConfigurationUtils.BaseConfigurationField;
import org.hurricanegames.commandlib.configurations.ConfigurationUtils.SimpleConfigurationField;
import org.hurricanegames.commandlib.utils.ReflectionUtils;

public class BaseConfiguration {

	@SuppressWarnings("rawtypes")
	protected final BaseConfigurationField[] fields;

	@SuppressWarnings("rawtypes")
	public BaseConfiguration() {
		List<BaseConfigurationField> fieldsList = new ArrayList<>();
		Class<?> clazz = getClass();
		do {
			Arrays.stream(clazz.getDeclaredFields())
			.filter(field -> {
				return !Modifier.isStatic(field.getModifiers());
			})
			.map(f -> {
				f.setAccessible(true);
				return f;
			})
			.forEach(field -> {
				ConfigurationFieldDefinition definition = field.getAnnotation(ConfigurationFieldDefinition.class);

				if (definition == null) {
					return;
				}

				String fieldName =
					!definition.fieldName().isEmpty() ?
					definition.fieldName() :
					field.getName().toLowerCase().replace("_", ".");

				if (definition.fieldType() != DefaultConfigurationField.class) {
					try {
						fieldsList.add(definition.fieldType().getConstructor(Field.class, String.class).newInstance(field, fieldName));
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new RuntimeException("Unable to instantiate custom configuration field", e);
					}
				} else {
					Class<?> fieldType = field.getType();
					if (BaseConfiguration.class.isAssignableFrom(fieldType)) {
						fieldsList.add(new BaseConfigurationField<BaseConfiguration>(field, fieldName) {
							@Override
							protected void load(BaseConfiguration configurationObject, ConfigurationSection section) {
								ConfigurationSection objectSection = section.getConfigurationSection(path);
								if (objectSection == null) {
									objectSection = new YamlConfiguration();
								}
								ReflectionUtils.<BaseConfiguration>getField(field, configurationObject).load(objectSection);
							}
							@Override
							protected void save(BaseConfiguration configurationObject, ConfigurationSection section) {
								ReflectionUtils.<BaseConfiguration>getField(field, configurationObject).save(section.createSection(path));
							}
						});
					} else {
						fieldsList.add(new SimpleConfigurationField(field, fieldName));
					}
				}
			});
		} while ((clazz = clazz.getSuperclass()) != null);
		this.fields = fieldsList.toArray(new BaseConfigurationField[0]);
	}

	@SuppressWarnings("unchecked")
	protected void load(ConfigurationSection section) {
		ConfigurationUtils.load(this, section, fields);
	}

	@SuppressWarnings("unchecked")
	protected void save(ConfigurationSection section) {
		ConfigurationUtils.save(this, section, fields);
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConfigurationFieldDefinition {

		String fieldName() default "";

		@SuppressWarnings("rawtypes")
		Class<? extends BaseConfigurationField> fieldType() default DefaultConfigurationField.class;

		/**
		 * A marker class for {@link ConfigurationFieldDefinition#fieldType()}<br>
		 * This marker class means that creating configuration field from definition should be done by the configuration itself
		 */
		@SuppressWarnings("rawtypes")
		public static final class DefaultConfigurationField extends BaseConfigurationField {

			public DefaultConfigurationField(Field field, String cPath) {
				super(field, cPath);
				throw new UnsupportedOperationException("Marker class");
			}

			@Override
			protected void load(Object configurationObject, ConfigurationSection section) {
				throw new UnsupportedOperationException("Marker class");
			}

			@Override
			protected void save(Object configurationObject, ConfigurationSection section) {
				throw new UnsupportedOperationException("Marker class");
			}

		}

	}

}
