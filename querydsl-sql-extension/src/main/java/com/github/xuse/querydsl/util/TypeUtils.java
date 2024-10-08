package com.github.xuse.querydsl.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.Expressions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TypeUtils {
	private final static Map<Class<?>, BiFunction<Class<?>, PathMetadata, Path<?>>> PathCreators = new HashMap<>();

	private static final BiFunction<Class<?>, PathMetadata, Path<?>> StringCreator = (a, b) -> Expressions
			.stringPath(b);
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> NumberCreator = (a, b) -> Expressions
			.numberPath((Class) a, b);
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> PrimitiveNumberCreator = (a, b) -> Expressions
			.numberPath((Class) Primitives.toWrapperClass(a), b);
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> DateCreator = (a, b) -> Expressions
			.datePath(a.asSubclass(Date.class), b);
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> DateTimeCreator = (a, b) -> Expressions
			.dateTimePath(a.asSubclass(Date.class), b);
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> TimeCreator = (a, b) -> Expressions
			.timePath(a.asSubclass(Date.class), b);

	private static final BiFunction<Class<?>, PathMetadata, Path<?>> BooleanCreator = (a, b) -> Expressions
			.booleanPath(b);
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> SimpleCreator = Expressions::simplePath;
	
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> ArrayCreator = (a, b) -> Expressions.arrayPath(a, b);
	
	static {
		PathCreators.put(byte[].class, ArrayCreator);
		PathCreators.put(Byte[].class, ArrayCreator);
		PathCreators.put(long[].class, ArrayCreator);
		PathCreators.put(Long[].class, ArrayCreator);
		PathCreators.put(float[].class, ArrayCreator);
		PathCreators.put(Float[].class, ArrayCreator);
		PathCreators.put(double[].class, ArrayCreator);
		PathCreators.put(Double[].class, ArrayCreator);
		PathCreators.put(short[].class, ArrayCreator);
		PathCreators.put(Short[].class, ArrayCreator);
		PathCreators.put(char[].class, ArrayCreator);
		PathCreators.put(Character[].class, ArrayCreator);
		PathCreators.put(boolean[].class, ArrayCreator);
		PathCreators.put(Boolean[].class, ArrayCreator);
		PathCreators.put(int[].class, ArrayCreator);
		PathCreators.put(Integer[].class, ArrayCreator);
		
		PathCreators.put(String.class, StringCreator);
		PathCreators.put(CharSequence.class, StringCreator);

		PathCreators.put(Long.class, NumberCreator);
		PathCreators.put(Short.class, NumberCreator);
		PathCreators.put(Integer.class, NumberCreator);
		PathCreators.put(Float.class, NumberCreator);
		PathCreators.put(Double.class, NumberCreator);
		
		PathCreators.put(BigInteger.class, NumberCreator);
		PathCreators.put(BigDecimal.class, NumberCreator);

		PathCreators.put(Long.TYPE, PrimitiveNumberCreator);
		PathCreators.put(Short.TYPE, PrimitiveNumberCreator);
		PathCreators.put(Integer.TYPE, PrimitiveNumberCreator);
		PathCreators.put(Float.TYPE, PrimitiveNumberCreator);
		PathCreators.put(Double.TYPE, PrimitiveNumberCreator);

		PathCreators.put(java.sql.Date.class, DateCreator);
		PathCreators.put(LocalDate.class, (a, b) -> Expressions.datePath(a.asSubclass(LocalDate.class), b));

		PathCreators.put(java.sql.Time.class, TimeCreator);
		PathCreators.put(LocalTime.class, (a, b) ->Expressions.timePath(a.asSubclass(LocalTime.class), b));

		
		PathCreators.put(Instant.class, (a, b) -> Expressions.dateTimePath(a.asSubclass(Instant.class), b));
		PathCreators.put(java.util.Date.class, DateTimeCreator);
		PathCreators.put(java.sql.Timestamp.class, DateTimeCreator);
		PathCreators.put(LocalDateTime.class,  (a, b) ->Expressions.dateTimePath(a.asSubclass(LocalDateTime.class), b));

		PathCreators.put(Boolean.class, BooleanCreator);
		PathCreators.put(Boolean.TYPE, BooleanCreator);
	}

	private TypeUtils() {
	}

	/**
	 * If it can be converted to type of class, convert it; otherwise, return
	 * `null`.
	 * <h2>Chinese:</h2> 如果可以转换为class的类型，就转换，否则返回null。
	 * 
	 * @param o   the object
	 * @param clz the class
	 * @return object of type T;
	 * @param <T> The type of target object.
	 */
	public static <T> T tryCast(Object o, Class<T> clz) {
		if (o == null) {
			return null;
		}
		if (clz.isAssignableFrom(o.getClass())) {
			return (T) o;
		}
		return null;
	}

	private static volatile Class RECORD_CLASS;
	private static volatile Method RECORD_GET_RECORD_COMPONENTS;
	private static volatile Method RECORD_COMPONENT_GET_NAME;

	public static boolean isRecord(Class objectClass) {
		Class superclass = objectClass.getSuperclass();
		if (superclass == Object.class) {
			return false;
		}
		if (RECORD_CLASS == null) {
			String superclassName = superclass.getName();
			if ("java.lang.Record".equals(superclassName)) {
				RECORD_CLASS = superclass;
				return true;
			} else {
				return false;
			}
		}

		return superclass == RECORD_CLASS;
	}

	//基于反射方式获得每个Record记录的名称
	public static String[] getRecordFieldNames(Class<?> recordType) {
		if (JDKEnvironment.JVM_VERSION < 14 && JDKEnvironment.ANDROID_SDK_INT < 33) {
			return new String[0];
		}

		try {
			if (RECORD_GET_RECORD_COMPONENTS == null) {
				RECORD_GET_RECORD_COMPONENTS = Class.class.getMethod("getRecordComponents");
			}

			if (RECORD_COMPONENT_GET_NAME == null) {
				Class<?> c = Class.forName("java.lang.reflect.RecordComponent");
				RECORD_COMPONENT_GET_NAME = c.getMethod("getName");
			}

			final Object[] components = (Object[]) RECORD_GET_RECORD_COMPONENTS.invoke(recordType);
			final String[] names = new String[components.length];
			for (int i = 0; i < components.length; i++) {
				names[i] = (String) RECORD_COMPONENT_GET_NAME.invoke(components[i]);
			}

			return names;
		} catch (Exception e) {
			throw new RuntimeException(
					String.format("Failed to access Methods needed to support `java.lang.Record`: (%s) %s",
							e.getClass().getName(), e.getMessage()),
					e);
		}
	}

	/**
	 * 
	 * Instead of class.newInstance() method. since it was marked as 'deprecated'
	 * above JDK 9.
	 * <h2>Chinese:</h2> 在java9， Class.newInstance()被标记为@Deprecated， 用这个函数替代。
	 * 
	 * @return the instance of class.
	 * @param <T> The type of target object.
	 * @param clz Class&lt;T&gt;
	 */
	public static <T> T newInstance(Class<T> clz) {
		try {
			return clz.getConstructor().newInstance();
		} catch (InvocationTargetException e) {
			throw Exceptions.toRuntime(e.getTargetException());
		} catch (NoSuchMethodException|IllegalAccessException|InstantiationException e) {
			throw Exceptions.toRuntime(e);
		}
	}

	public static <T> Constructor<T> getDeclaredConstructor(Class<T> clz) {
		try {
			return clz.getDeclaredConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			return null;
		}
	}
	
    public static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();
        Class<?> c = clazz;
        while (c != Object.class) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
            c = c.getSuperclass();
        }
        return fields;
    }
    

	/*
	 * crate the path object according to the java type.
	 */
	public static Path<?> createPathByType(Class<?> type, String name, Path<?> parent) {
		PathMetadata metadata = PathMetadataFactory.forProperty(parent, name);
		BiFunction<Class<?>, PathMetadata, Path<?>> creator = PathCreators.get(type);
		if (creator != null) {
			return creator.apply(type, metadata);
		}
		if (Enum.class.isAssignableFrom(type)) {
			return Expressions.enumPath((Class<? extends Enum>) type, metadata);
		}
		return SimpleCreator.apply(type, metadata);
	}
}
