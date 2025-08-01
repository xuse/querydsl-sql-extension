package com.github.xuse.querydsl.sql.expression;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.spring.core.resource.Util;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.Primitives;
import com.github.xuse.querydsl.util.TypeUtils;
import com.mysema.commons.lang.Pair;

import lombok.SneakyThrows;

public class ReflectCodecRecord extends BeanCodec {
	private final List<FieldProperty> methods;
	private final Pair<Integer,FieldProperty>[] exprs;
	private final Constructor<?> constructor;
	
	public ReflectCodecRecord(Class<?> targetClass, List<FieldProperty> methods) {
		this.methods = methods;
		this.exprs=initFieldIndex(targetClass,methods);
		List<Class<?>> constructorTypes = Arrays.stream(exprs).map(e -> e.getSecond().getField().getType())
				.collect(Collectors.toList());
		try {
			this.constructor=targetClass.getDeclaredConstructor(constructorTypes.toArray(Util.NO_CLASSES));
		} catch (NoSuchMethodException e) {
			throw Exceptions.toRuntime(e);
		}
	}

	@SuppressWarnings("unchecked")
	@SneakyThrows
	private Pair<Integer,FieldProperty>[] initFieldIndex(Class<?> targetClass, List<FieldProperty> methods) {
		String[] names = TypeUtils.getRecordFieldNames(targetClass);
		Map<String,Pair<Integer,FieldProperty>> map=new HashMap<>();
		for(int i=0;i<methods.size();i++) {
			FieldProperty f=methods.get(i);
			Pair<Integer,FieldProperty> entry=new Pair<>(i,f);
			map.put(f.getName(),entry);
		}
		List<Pair<Integer,FieldProperty>> exprs=new ArrayList<>();
		for (String name:names) {
			Pair<Integer,FieldProperty> entry=map.get(name);
			if(entry==null) {
				Field field=targetClass.getDeclaredField(name);
				exprs.add(new Pair<>(-1, new FieldProperty(null, null, field)));
			}else {
				exprs.add(entry);
			}
		}
		return exprs.toArray(new Pair[0]);
	}

	@Override
	public Object newInstance(Object[] values) {
		Pair<Integer,FieldProperty>[] exprs = this.exprs;
		int len=exprs.length;
		Object[] params=new Object[len];
		for (int i = 0; i < len; i++) {
			Pair<Integer,FieldProperty> expr=exprs[i];
			int index=expr.getFirst();
			if(index<0) {
				Class<?> clz=expr.getSecond().getField().getType();
				if(clz.isPrimitive()) {
					params[i]=Primitives.defaultValueOfPrimitive(clz);
				}
			}else {
				params[i] = values[index];
			}
		}
		try {
			Object o =	constructor.newInstance(params);
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
	public void copy(Object from, Object target) {
		//All fields in Record in final value. unable to be updated.
	}
}
