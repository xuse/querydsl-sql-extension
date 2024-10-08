package com.github.xuse.querydsl.sql.column;

import com.querydsl.sql.ColumnMetadata;

/**
 * 共有10个属性
 * 
 * name, index
 * 
 * 
 * 1~4 jdbcType \size  \decimalDigits   unsigned
 * 5 nullable
 * 6 defaultExpression
 * 7 features
 * 8 comment
 */
public class ColumnMetadataExImpl extends AbstractColumnMetadataEx {

	public ColumnMetadataExImpl(ColumnMetadata metadata) {
		super(metadata);
	}

	@Override
	protected Class<?> getType() {
		return Object.class;
	}

}
