package com.github.xuse.querydsl.sql.expression;

import java.lang.reflect.Field;
import java.util.List;

import com.github.xuse.querydsl.sql.expression.BeanCodecManager.CacheKey;

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
}
