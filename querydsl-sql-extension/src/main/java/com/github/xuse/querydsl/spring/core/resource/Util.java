package com.github.xuse.querydsl.spring.core.resource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.Exceptions;

public final class Util {
	private static final Map<Class<?>, Field[]> declaredFieldsCache = new ConcurrentReferenceHashMap<Class<?>, Field[]>(256);

	private static final Map<Class<?>, Method[]> declaredMethodsCache = new ConcurrentReferenceHashMap<Class<?>, Method[]>(256);
	
	public static final Method[] NO_METHODS = {};

	public static final Field[] NO_FIELDS = {};
	
	public static final Class<?>[] NO_CLASSES = {};

	public static Object getField(Field field, Object target) {
		try {
			return field.get(target);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}

	public static Object invokeMethod(Method method, Object target) {
		return invokeMethod(method, target, new Object[0]);
	}

	public static Object invokeMethod(Method method, Object target, Object... args) {
		try {
			return method.invoke(target, args);
		} catch (Exception ex) {
			throw Exceptions.toRuntime(ex);
		}
	}

	public static Type getMethodType(Class<?> clazz, String methodName) {
		try {
			Method method = clazz.getMethod(methodName);
			return method.getGenericReturnType();
		} catch (Exception ex) {
			return null;
		}
	}

	public static Type getFieldType(Class<?> clazz, String fieldName) {
		Class<?> clz = clazz;
		while (clz != Object.class) {
			try {
				Field field = clazz.getDeclaredField(fieldName);
				return field.getGenericType();
			} catch (Exception ex) {
			}
			clz = clz.getSuperclass();
		}
		return null;
	}
	
	/**
	 * @param clazz class to analysis.
	 * @param name field name
	 * @return null if no field in the name.
	 */
	public static Field findField(Class<?> clazz, String name) {
		return findField(clazz, name, null);
	}

	/**
	 * 根据类型或名称寻找合适的field. 
	 * @param clazz  the class
	 * @param name name of field 
	 * @param type type of field 
	 * @return field found.
	 */
	public static Field findField(Class<?> clazz, String name, Class<?> type) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.isTrue(name != null || type != null, "Either name or type of the field must be specified");
		Class<?> searchType = clazz;
		while (Object.class != searchType && searchType != null) {
			Field[] fields = getDeclaredFields(searchType);
			for (Field field : fields) {
				if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
					return field;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}

	public static Field[] getDeclaredFields(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		Field[] result = declaredFieldsCache.computeIfAbsent(clazz, (clz)->{
			Field[] fields= clz.getDeclaredFields();
			return fields.length == 0 ? NO_FIELDS : fields;
		});
		return result;
	}

	public static Method findMethod(Class<?> clazz, String name) {
		return findMethod(clazz, name, NO_CLASSES);
	}

	public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(name, "Method name must not be null");
		Class<?> searchType = clazz;
		while (searchType != null) {
			Method[] methods = (searchType.isInterface() ? searchType.getMethods() : getDeclaredMethods(searchType));
			for (Method method : methods) {
				if (name.equals(method.getName()) && (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}

	public static Method[] getDeclaredMethods(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		Method[] result = declaredMethodsCache.get(clazz);
		if (result == null) {
			Method[] declaredMethods = clazz.getDeclaredMethods();
			List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
			if (defaultMethods != null) {
				Method[] array2 = defaultMethods.toArray(new Method[0]);
				int len1 = declaredMethods.length;
				int len2 = array2.length;
				result = new Method[len1 + len2];
				System.arraycopy(declaredMethods, 0, result, 0, len1);
				System.arraycopy(array2, 0, result, len1, len2);
			} else {
				result = declaredMethods;
			}
			declaredMethodsCache.put(clazz, (result.length == 0 ? NO_METHODS : result));
		}
		return result;
	}

	private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
		List<Method> result = null;
		for (Class<?> ifc : clazz.getInterfaces()) {
			for (Method ifcMethod : ifc.getMethods()) {
				if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
					if (result == null) {
						result = new ArrayList<Method>();
					}
					result.add(ifcMethod);
				}
			}
		}
		return result;
	}
	
	public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		if (clazz.isInterface()) {
			return Collections.singleton(clazz);
		}
		Set<Class<?>> interfaces = new LinkedHashSet<>();
		while (clazz != null) {
			Class<?>[] ifcs = clazz.getInterfaces();
			for (Class<?> ifc : ifcs) {
				interfaces.addAll(getAllInterfacesForClassAsSet(ifc));
			}
			clazz = clazz.getSuperclass();
		}
		return interfaces;
	}
}
