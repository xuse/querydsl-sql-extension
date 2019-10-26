package com.github.xuse.querydsl.sql.column;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.function.Predicate;

import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.querydsl.sql.ColumnMetadata;

public abstract class AbstractColumnMetadata implements ColumnMapping {
	
	/**
	 * Java字段（反射）
	 */
	private final Field field;
	
	/**
	 * QueryDSL的元数据对象
	 */
	private final ColumnMetadata column;
	
	/**
	 * 无效值
	 */
	private final Predicate<Object> unsavedValue;
	
	/**
	 * 不插入
	 */
	private boolean notInsert;
	/**
	 * 不更新
	 */
	private boolean notUpdate;
	/**
	 * 自动生成
	 */
	private boolean generated;
	/**
	 * 是否为主键
	 */
	private boolean pk;

	protected AbstractColumnMetadata(Field field, ColumnMetadata column) {
		this.field = field;
		this.column = column;
		this.unsavedValue=UnsavedValuePredicateFactory.create(field.getType(), field.getAnnotation(UnsavedValue.class));
		//根据注解进行初始化

//		ColumnMetadataExt()
//		ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10);

//			addMetadata(id, );
//			addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(64));
//			addMetadata(code, ColumnMetadata.named("CODE").withIndex(2).ofType(Types.VARCHAR).notNull().withSize(64));
		//
//			addMetadata(gender, ColumnMetadata.named("GENDER").withIndex(2).ofType(Types.VARCHAR).withSize(64));
//			addMetadata(created, ColumnMetadata.named("CREATED").withIndex(3).ofType(Types.TIMESTAMP).withSize(29).withDigits(9));
	}
	

	public ColumnMetadata get() {
		return column;
	}
	
	@Override
	public boolean isUnsavedValue(Object value) {
		return unsavedValue.test(value);
	}
	

	@Override
	public boolean isGenerated() {
		return generated;
	}
	
	@Override
	public boolean isNotInsert() {
		return notInsert;
	}

	@Override
	public boolean isNotUpdate() {
		return notUpdate;
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}
	
	@Override
	public String fieldName() {
		return field.getName();
	}

	@Override
	public int getSqlType() {
		return column.getJdbcType();
	}

	@Override
	public boolean isPk() {
		return pk;
	}


	@Override
	public <T extends Annotation> T getAnnotation(Class<T> clz) {
		return field.getAnnotation(clz);
	}


}
