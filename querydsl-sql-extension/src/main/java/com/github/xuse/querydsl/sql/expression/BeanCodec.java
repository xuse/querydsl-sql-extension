package com.github.xuse.querydsl.sql.expression;

import java.util.Map;

/**
 * <h2>English:</h2>
 * A serializer/deserializer generated using ASM dynamic bytecode technology.
 * <h2>Chinese:</h2>
 * 使用ASM动态字节码技术生成的类序列化反序列化器
 * @author Joey
 */
public abstract class BeanCodec extends ValueExtractor {
	/**
	 * <h2>English:</h2> Assemble an object from field values (ordered by metadata).
	 * <h2>Chinese:</h2> 根据字段值（按元数据顺序排列）拼装成对象
	 * @param fields fields
	 * @return Object
	 */
	public abstract Object newInstance(Object[] fields);

	/**
	 * <h2>English:</h2> Get all field values ordered by metadata field sequence.
	 * <h2>Chinese:</h2> 根据元数据字段序得到所有字段值（按元数据顺序排列）
	 * @param bean bean
	 * @return 所有字段值（按元数据顺序排列）
	 */
	public abstract Object[] values(Object bean);
	
	/**
	 * <h2>English:</h2> Shallow copy fields between objects of the same type.
	 * <h2>Chinese:</h2> 相同类型间字段浅拷贝。
	 * @param from source
	 * @param target target
	 */
	public abstract void copy(Object from, Object target);
	
	/**
	 * <h2>English:</h2> Assign field values to the specified object.
	 * <h2>Chinese:</h2> 将字段赋值到指定对象中
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
