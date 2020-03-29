package org.hurricanegames.commandlib.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {

	public static <T extends Annotation> T getAnnotationByType(Class<T> type, Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (type.equals(annotation.annotationType())) {
				return type.cast(annotation);
			}
		}
		return null;
	}

	public static <T extends AccessibleObject> T setAccessible(T object) {
		object.setAccessible(true);
		return object;
	}

	public static void setField(Field field, Object obj, Object value) {
		try {
			field.set(obj, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			sneakyThrow(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getField(Field field, Object obj) {
		try {
			return (T) field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			sneakyThrow(e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T invoke(Method method, Object obj, Object... args) {
		try {
			return (T) method.invoke(obj, args);
		} catch (InvocationTargetException e) {
			sneakyThrow(e.getCause());
		} catch (IllegalArgumentException | IllegalAccessException e) {
			sneakyThrow(e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Constructor<?> constr, Object... args) {
		try {
			return (T) constr.newInstance(args);
		} catch (InvocationTargetException e) {
			sneakyThrow(e.getCause());
		} catch (IllegalArgumentException | InstantiationException | IllegalAccessException e) {
			sneakyThrow(e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
	    throw (E) e;
	}

}
