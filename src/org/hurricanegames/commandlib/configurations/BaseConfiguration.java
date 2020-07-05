package org.hurricanegames.commandlib.configurations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.hurricanegames.commandlib.configurations.BaseConfiguration.ConfigurationFieldDefinition.DefaultConfigurationField;
import org.hurricanegames.commandlib.configurations.ConfigurationUtils.BaseConfigurationField;
import org.hurricanegames.commandlib.configurations.ConfigurationUtils.ConfigurationField;
import org.hurricanegames.commandlib.configurations.ConfigurationUtils.SimpleConfigurationField;
import org.hurricanegames.commandlib.utils.ReflectionUtils;

public class BaseConfiguration {

	@SuppressWarnings("rawtypes")
	protected final ConfigurationField[] fields;

	@SuppressWarnings("rawtypes")
	public BaseConfiguration() {
		List<ConfigurationField> fieldsList = new ArrayList<>();
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
						boolean found = false;
						for (Constructor<?> construstor : definition.fieldType().getConstructors()) {
							Parameter[] parameters = construstor.getParameters();
							if (parameters.length == 3 && parameters[1].getType().isAssignableFrom(Field.class) && parameters[2].getType().isAssignableFrom(String.class)) {
								fieldsList.add(ReflectionUtils.newInstance(ReflectionUtils.setAccessible(construstor), this, field, fieldName));
								found = true;
								break;
							}
						}
						if (!found) {
							throw new IllegalArgumentException("Can't find suitable constructor");
						}
					} catch (Exception e) {
						throw new RuntimeException("Unable to instantiate custom configuration field", e);
					}
				} else {
					Class<?> fieldType = field.getType();
					if (BaseConfiguration.class.isAssignableFrom(fieldType)) {
						fieldsList.add(new BaseConfigurationField<>(this, field, fieldName));
					} else {
						fieldsList.add(new SimpleConfigurationField<>(this, field, fieldName));
					}
				}
			});
		} while ((clazz = clazz.getSuperclass()) != null);
		this.fields = fieldsList.toArray(new ConfigurationField[0]);
	}

	@SuppressWarnings("unchecked")
	protected void load(ConfigurationSection section) {
		ConfigurationUtils.load(section, fields);
	}

	@SuppressWarnings("unchecked")
	protected void save(ConfigurationSection section) {
		ConfigurationUtils.save(section, fields);
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ConfigurationFieldDefinition {

		String fieldName() default "";

		@SuppressWarnings("rawtypes")
		Class<? extends ConfigurationField> fieldType() default DefaultConfigurationField.class;

		/**
		 * A marker class for {@link ConfigurationFieldDefinition#fieldType()}<br>
		 * This marker class means that creating configuration field from definition should be done by the configuration itself
		 */
		@SuppressWarnings("rawtypes")
		public static final class DefaultConfigurationField extends ConfigurationField {

			@SuppressWarnings("unchecked")
			public DefaultConfigurationField(BaseConfiguration configuration, Field field, String cPath) {
				super(configuration, field, cPath);
				throw new UnsupportedOperationException("Marker class");
			}

			@Override
			protected void load(ConfigurationSection section) {
				throw new UnsupportedOperationException("Marker class");
			}

			@Override
			protected void save(ConfigurationSection section) {
				throw new UnsupportedOperationException("Marker class");
			}

		}

	}

}
