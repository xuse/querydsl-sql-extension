package com.github.xuse.querydsl.sql.expression;

import java.util.List;

/**
 * 基于反射的对象访问器，万一ASM失效后启用
 * 
 * @author jiyi
 *
 */
public class ReflectCodec extends BeanCodec {
	private final Class<?> targetClass;
	private List<FieldProperty> methods;

	public ReflectCodec(Class<?> targetClass, List<FieldProperty> methods) {
		this.targetClass = targetClass;
		this.methods = methods;
	}

	@Override
	public Object newInstance(Object[] fields) {
		try {
			Object o = targetClass.newInstance();
			int len = methods.size();
			for (int i = 0; i < len; i++) {
				methods.get(i).getSetter().invoke(o, fields[i]);
			}
			return o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object[] values(Object bean) {
		int len = methods.size();
		Object[] result = new Object[len];
		try {
			for (int index = 0; index < len; index++) {
				result[index] = methods.get(index).getGetter().invoke(bean);
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
