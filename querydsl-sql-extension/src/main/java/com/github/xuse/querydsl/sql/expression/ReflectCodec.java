package com.github.xuse.querydsl.sql.expression;

import java.util.List;

import com.github.xuse.querydsl.util.TypeUtils;

import lombok.SneakyThrows;

/**
 * 基于反射的对象访问器，万一ASM失效后启用
 * 
 * @author Joey
 *
 */
public class ReflectCodec extends BeanCodec {
	private final Class<?> targetClass;
	private final List<FieldProperty> methods;

	public ReflectCodec(Class<?> targetClass, List<FieldProperty> methods) {
		this.targetClass = targetClass;
		this.methods = methods;
	}

	@Override
	public Object newInstance(Object[] fields) {
		try {
			Object o = TypeUtils.newInstance(targetClass);
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

	@Override
	@SneakyThrows
	public void copy(Object from, Object target) {
		for(FieldProperty p:methods) {
			if(p.getGetter()!=null && p.getSetter()!=null) {
				Object value=p.getGetter().invoke(from);
				p.getSetter().invoke(target, value);
			}
		}
	}
}
