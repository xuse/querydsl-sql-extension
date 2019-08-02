package com.github.xuse.querydsl.sql.column;

import java.lang.reflect.Field;

import javax.persistence.Column;

import org.springframework.util.StringUtils;

import com.querydsl.core.types.Path;
import com.querydsl.sql.ColumnMetadata;

public class MetadataBuilder {
	protected Field field;
	protected Path<?> expr;

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
		return new ColumnMetadataExt(field, ColumnMetadata.named(columnName));
	}

	public MetadataBuilder(Field field, Path<?> expr, Field metadataField) {
		this.field = field;
		this.expr = expr;
	}
}