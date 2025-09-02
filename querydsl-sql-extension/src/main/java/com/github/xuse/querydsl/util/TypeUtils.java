package com.github.xuse.querydsl.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.xuse.querydsl.spring.core.resource.Util;
import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.github.xuse.querydsl.sql.expression.BeanCodecManager;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Templates;
import com.querydsl.sql.SQLTemplates;

import lombok.SneakyThrows;

public class TypeUtils {
	private TypeUtils() {
	}

	private static volatile Class<?> RECORD_CLASS;
	private static volatile Method RECORD_GET_RECORD_COMPONENTS;
	private static volatile Method RECORD_COMPONENT_GET_NAME;
	public static Method TEMPLATE_ADD;
	static {
		try {
			TEMPLATE_ADD=Templates.class.getDeclaredMethod("add", Operator.class,String.class);	
			TEMPLATE_ADD.setAccessible(true);	
		}catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@SneakyThrows
	public static void add(SQLTemplates templates, Operator op, String template) {
		TEMPLATE_ADD.invoke(templates, op, template);
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
	@SuppressWarnings("unchecked")
	public static <T> T tryCast(Object o, Class<T> clz) {
		if (o == null) {
			return null;
		}
		if (clz.isAssignableFrom(o.getClass())) {
			return (T) o;
		}
		return null;
	}
	
	public static boolean isRecord(Class<?> objectClass) {
		Class<?> superclass = objectClass.getSuperclass();
		if (superclass == Object.class || superclass==null) {
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

	public static List<Method> getAllDeclaredMethods(Class<?> clazz) {
		List<Method> methods = new ArrayList<>();
		Class<?> c = clazz;
        while (c != Object.class) {
        	methods.addAll(Arrays.asList(Util.getDeclaredMethods(c)));
            c = c.getSuperclass();
        }
        return methods;
	}
	
    public static List<Field> getAllDeclaredFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> c = clazz;
        while (c != Object.class) {
            fields.addAll(Arrays.asList(Util.getDeclaredFields(c)));
            c = c.getSuperclass();
        }
        return fields;
    }
    
    public static <T> void copyProperties(T a, T b, Class<T> type) {
    	BeanCodec codec=BeanCodecManager.getInstance().getCodec(type);
    	codec.copy(a, b);
    }
}
