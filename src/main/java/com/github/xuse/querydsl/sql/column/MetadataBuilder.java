package com.github.xuse.querydsl.sql.column;

import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.util.StringUtils;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.ColumnMetadata;

public class MetadataBuilder<T> {
	protected Field field;
	protected Path<T> expr;
	
	/**
	 * Java数据类型（不一定等于field.getType()，可能受注解@Type影响）
	 */
	private Class<?> javaType;
	/**
	 * 注解中的类型
	 * 
	 * Eg. varchar(100)中的varchar
	 */
	private String def;
	/**
	 * 注解中的类型参数
	 * 
	 * Eg. varchar(100)中的100
	 */
	private String[] typeArgs;
	// ///////////完全解析后的数据/////////////

	/**
	 * 是否为null
	 */
	private boolean nullable;
	/**
	 * 是否唯一
	 */
	private boolean unique;
	/**
	 * 长度
	 */
	private int length;
	/**
	 * 精度
	 */
	private int precision;
	/**
	 * 小数位数
	 */
	private int scale;
	/**
	 * 是否为版本字段
	 */
	private boolean version;
	/**
	 * 是否为LOB字段
	 */
	private boolean lob;
	// ////////////////////////////////////
	/**
	 * 缺省值：仅当指定了default关键字后才有值
	 */
	private SimpleExpression<T> defaultExpression = null;
	
	private boolean qsPk;
	
	/**
	 * 根据类上的注解，自动生成列名，数据类型大小，映射等信息 兼容部分JPA（注解同名不同包）
	 * 
	 * @return
	 */
	public ColumnMetadataExt build() {
		String columnName = expr.getMetadata().getName().toUpperCase();
		com.querydsl.sql.Column c = field.getAnnotation(com.querydsl.sql.Column.class);
		if (c != null) {
			columnName = c.value();
		}
		Column jpaAnnotaion = field.getAnnotation(Column.class);
		if (jpaAnnotaion != null && !StringUtils.isEmpty(jpaAnnotaion.name())) {
			columnName = jpaAnnotaion.name();
		}
		Column anno=field.getAnnotation(Column.class);
		Id id=field.getAnnotation(Id.class);
		GeneratedValue gv=field.getAnnotation(GeneratedValue.class);
		ColumnMetadataExt columnMapping = new ColumnMetadataExt(field, ColumnMetadata.named(columnName),qsPk || id!=null);
		return columnMapping;
	}

	public MetadataBuilder(Field field, Path<T> expr, Field metadataField) {
		this.field = field;
		this.expr = expr;
	}
	


	public void hasQueryDSLPk(boolean pk) {
		this.qsPk=pk;
	}
}