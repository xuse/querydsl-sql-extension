package com.github.xuse.querydsl.sql.expression;

import java.lang.reflect.Field;
import java.util.List;

import com.github.xuse.querydsl.sql.expression.BeanCodecManager.CacheKey;
import com.querydsl.core.types.Expression;

public interface BeanCodecProvider {

	BeanCodec generateAccessor(CacheKey key, BindingProvider bindings,ClassLoaderAccessor cl);


	default BeanCodec inputFields(BeanCodec bc, List<FieldProperty> properties) {
		Field[] fields = new Field[properties.size()];
		for (int i = 0; i < properties.size(); i++) {
			fields[i] = properties.get(i).getField();
		}
		bc.setFields(fields);
		return bc;
	}
	

	default void typeMismatch(Class<?> type, Expression<?> expr) {
		final String msg = expr.getType().getName() + " is not compatible with " + type.getName();
		throw new IllegalArgumentException(msg);
	}
}
