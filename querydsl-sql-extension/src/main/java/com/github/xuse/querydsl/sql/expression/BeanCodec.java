package com.github.xuse.querydsl.sql.expression;

import java.lang.reflect.Field;

/**
 * 使用ASM动态字节码技术生成的类序列化反序列化器
 * @author Joey
 */
public abstract class BeanCodec {

	/**
	 * 根据字段值（按元数据顺序排列）拼装成对象
	 * @param fields fields
	 * @return Object
	 */
	public abstract Object newInstance(Object[] fields);

	/**
	 * 根据元数据字段序得到所有字段值（按元数据顺序排列）
	 * @param bean bean
	 * @return 所有字段值（按元数据顺序排列）
	 */
	public abstract Object[] values(Object bean);
	
	/**
	 * 相同类型间字段浅拷贝。
	 * @param from soruce
	 * @param target target
	 */
	public abstract void copy(Object from, Object target);

	private Field[] fields;
	
	private Class<?> type;

	public Field[] getFields() {
		return fields;
	}

	public void setFields(Field[] fields) {
		this.fields = fields;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}
}
