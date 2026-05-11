package com.github.xuse.querydsl.sql.ddl;

import com.github.xuse.querydsl.sql.column.AbstractColumnMetadataEx;
import com.querydsl.sql.ColumnMetadata;

/**
 * Extended column metadata implementation with 10 properties:
 * name, index, 1~4: jdbcType / size / decimalDigits / unsigned,
 * 5: nullable, 6: defaultExpression, 7: features, 8: comment.
 * <p>共有10个属性：
 * name, index, 1~4: jdbcType / size / decimalDigits / unsigned,
 * 5: nullable, 6: defaultExpression, 7: features, 8: comment
 */
final class DDLColumnMetadata extends AbstractColumnMetadataEx {

	public DDLColumnMetadata(ColumnMetadata metadata) {
		super(metadata);
	}

	@Override
	protected Class<?> getType() {
		return Object.class;
	}

}
