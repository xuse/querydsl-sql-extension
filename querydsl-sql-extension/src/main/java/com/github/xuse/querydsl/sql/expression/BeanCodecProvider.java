package com.github.xuse.querydsl.sql.expression;

import java.util.List;

import com.github.xuse.querydsl.sql.expression.BeanCodecManager.CacheKey;
import com.querydsl.core.types.Expression;

public interface BeanCodecProvider {

	BeanCodec generateAccessor(CacheKey key, BindingProvider bindings,ClassLoaderAccessor cl);

	default BeanCodec inputFields(BeanCodec bc, Class<?> target,List<FieldProperty> properties) {
		bc.setFields(properties.toArray(new FieldProperty[0]));
		bc.setType(target);
		return bc;
	}
	

	default void typeMismatch(Class<?> type, Expression<?> expr) {
		final String msg = expr.getType().getName() + " is not compatible with " + type.getName();
		throw new IllegalArgumentException(msg);
	}
}
