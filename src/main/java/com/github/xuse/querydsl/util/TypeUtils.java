package com.github.xuse.querydsl.util;

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

}
