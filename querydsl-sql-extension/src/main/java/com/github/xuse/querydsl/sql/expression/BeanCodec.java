package com.github.xuse.querydsl.sql.expression;

import java.util.Map;

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
	
	/**
	 * 将字段赋值到指定对象中
	 */
	public abstract void sets(Object[] values, Object bean);

	private Property[] fields;
	
	private Class<?> type;
	
	private Map<String,Integer> randomAccessIndex;

	public Property[] getFields() {
		return fields;
	}

	public void setFields(FieldProperty[] fields) {
		this.fields = fields;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public Map<String, Integer> getRandomAccessIndex() {
		return randomAccessIndex;
	}

	public void setRandomAccessIndex(Map<String, Integer> randomAccessIndex) {
		this.randomAccessIndex = randomAccessIndex;
	}
}
