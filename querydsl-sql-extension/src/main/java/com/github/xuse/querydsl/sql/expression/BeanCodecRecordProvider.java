package com.github.xuse.querydsl.sql.expression;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.sql.expression.BeanCodecManager.CacheKey;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.JDKEnvironment;
import com.github.xuse.querydsl.util.TypeUtils;

/*
 * Support record classes after JDK 16
 */
public class BeanCodecRecordProvider implements BeanCodecProvider{
	
	public static final BeanCodecRecordProvider INSTANCE=new BeanCodecRecordProvider();

	@Override
	public BeanCodec generateAccessor(CacheKey key, BindingProvider bindings, ClassLoaderAccessor cl) {
		String clzName = key.getClassName();
		List<FieldProperty> properties = initMethods(key, bindings);
		// For Android and graalvm
		if (JDKEnvironment.ANDROID || JDKEnvironment.GRAAL_NATIVE|| JDKEnvironment.DISABLE_ASM) {
			return inputFields(new ReflectCodecRecord(key.targetClass, properties), key.targetClass,properties);
		}
		// Normal JDK
		CodecClassGenerator g = new CodecClassGenerator(cl);
		Class<?> clz = g.generate(key.targetClass, properties, clzName,true);
		BeanCodec bc;
		if (clz == null) {
			bc = new ReflectCodecRecord(key.targetClass, properties);
		} else {
			bc = (BeanCodec) TypeUtils.newInstance(clz);
		}
		return inputFields(bc,key.targetClass,properties);
	}

	private List<FieldProperty> initMethods(CacheKey key, BindingProvider bindings) {
		Class<?> targetClz = key.targetClass;
		int count = 0;
		int len = bindings.size();
		
		String[] fields = TypeUtils.getRecordFieldNames(targetClz);
		
		Map<String,FieldProperty> map=new HashMap<>();
		for(String name:fields) {
			try {
				Field field=targetClz.getDeclaredField(name);
				Method getter=targetClz.getDeclaredMethod(name);
				FieldProperty f=new FieldProperty(getter, null, field);
				map.put(name, f);
			} catch (NoSuchFieldException |NoSuchMethodException | SecurityException e) {
				throw Exceptions.toRuntime(e);
			}
		}
		List<FieldProperty> properties = new ArrayList<>(len);
		for (String property : bindings.names(map)) {
			FieldProperty prop = map.get(property);
			if (prop != null) {
				count++;
			} else {
				// propertyNotFound(property);
				continue;
			}
			prop.setBindingType(bindings.getType(property,prop));
			properties.add(prop);
		}
		if (count == 0) {
			throw new IllegalArgumentException("There is no property match between the bean ["
					+ key.targetClass.getName() + "] and select expression :" + key.fieldNames);
		}
		return properties;
	}

}
