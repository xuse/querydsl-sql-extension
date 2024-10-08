package com.github.xuse.querydsl.sql.column;

import com.querydsl.core.types.Expression;
import com.querydsl.sql.ColumnMetadata;

public interface ColumnMetadataEx {
	/**
	 * 得到QueryDSL原生的ColumnMetadata.
	 * 原生的列定义内容较少，只有列名、序号、jdbcType，nullable，size，decimalDigits六个核心数据。
	 * @return ColumnMetadata
	 */
	ColumnMetadata getColumn();
	

	/**
	 * @return true if 无符号数
	 */
	default boolean isUnsigned() {
		return false;
	};
	
	/**
	 * 获得缺省值定义
	 * @return expression of default value
	 */
	default Expression<?> getDefaultExpression(){
		return null;
	};
	
	/**
	 * @return 获得其他修饰
	 */
	default ColumnFeature[] getFeatures() {
		return new ColumnFeature[0];
	};
	
	/**
	 * @return 获得字段注释
	 */
	default String getComment() {
		return null;
	};
	
	default int getIndex() {
		return getColumn().getIndex();
	}

	default int getJdbcType() {
		return getColumn().getJdbcType();
	}
	
	default boolean isNullable(){
		return getColumn().isNullable();
	}
	
	default int getSize() {
		return getColumn().getSize();
	}
	default boolean hasSize() {
		return getColumn().hasSize();
	}
	default int getDigits() {
		return getColumn().getDigits();
	}
	default boolean hasDigits() {
		return getColumn().hasDigits();
	}
	
	default String getName() {
		return getColumn().getName();
	}
}
