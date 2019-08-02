package com.github.xuse.querydsl.sql.column;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.querydsl.sql.ColumnMetadata;

public abstract class AbstractColumnMetadata implements ColumnMapping {
	private final Field field;
	private final ColumnMetadata column;
	private final Predicate<Object> unsavedValue;
	private boolean notInsert;
	private boolean notUpdate;
	private boolean generated;
	private boolean pk;

	protected AbstractColumnMetadata(Field field, ColumnMetadata column) {
		this.field = field;
		this.column = column;
		this.unsavedValue=UnsavedValuePredicateFactory.create(field.getType(), field.getAnnotation(UnsavedValue.class));
		Column anno=field.getAnnotation(Column.class);
		Id id=field.getAnnotation(Id.class);
		GeneratedValue gv=field.getAnnotation(GeneratedValue.class);
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


}
