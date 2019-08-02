package com.github.xuse.querydsl.sql.column;

import java.lang.reflect.Field;

import com.querydsl.sql.ColumnMetadata;

/**
 * 列的元数据（增强版）
 * @author Joey
 *
 */
public class ColumnMetadataExt extends AbstractColumnMetadata{
	public ColumnMetadataExt(Field field,ColumnMetadata metadata) {
		super(field,metadata);
	}


}
