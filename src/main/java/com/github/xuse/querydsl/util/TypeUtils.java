package com.github.xuse.querydsl.util;

import java.lang.reflect.InvocationTargetException;

public class TypeUtils {
	private TypeUtils() {}
	
	/**
	 * 如果可以转换为clz的类型，就转换，否则返回null。
	 * @param o
	 * @param clz
	 * @return object of type T; 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T tryCast(Object o,Class<T> clz) {
		if(o==null) {
			return null;
		}
		if(clz.isAssignableFrom(o.getClass())) {
			return (T)o;
		}
		return null;
	}

	/**
	 * 在java9， Class.newInstance()被标记为@Deprecated， 用这个函数替代。
	 */
	public static <T> T newInstance(Class<T> clz) throws InstantiationException, IllegalAccessException {
		try {
			return clz.getConstructor().newInstance();
		}catch(InvocationTargetException e) {
			InstantiationException ex= new InstantiationException(clz.getName());
			ex.initCause(e.getTargetException());
			 throw ex;
		}catch(NoSuchMethodException e) {
			InstantiationException ex= new InstantiationException(clz.getName());
			ex.initCause(e);
			throw ex;
		}
	}
}
