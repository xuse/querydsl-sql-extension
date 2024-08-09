package com.github.xuse.querydsl.sql.column;

import com.github.xuse.querydsl.util.ArrayUtils;
import com.querydsl.core.types.Expression;
import com.querydsl.sql.ColumnMetadata;

import lombok.Getter;
import lombok.Setter;

/**
 * 列的元数据。 在QueryDSL的ColumnMetadata六个描述的基础上，补充了四个列描述。
 * 
 * @implNote the member value of this class mean metadata within the database
 *           side, not java side.
 * @author Joey
 */
@Getter
@Setter
public abstract class AbstractColumnMetadataEx implements ColumnMetadataEx{
	/**
	 * QueryDSL的元数据对象
	 */
	private final ColumnMetadata column;

	/**
	 * 无符号数修饰
	 */
	protected boolean unsigned;

	/**
	 * 列默认值（表达式）
	 */
	protected Expression<?> defaultExpression;

	/**
	 * 其他修饰，如自增等，今后可能考虑支持unique、key、Check等实际为约束或索引的特性。
	 */
	protected ColumnFeature[] features;

	/**
	 * 列注释
	 */
	protected String comment;

	protected abstract Class<?> getType();

	public AbstractColumnMetadataEx(ColumnMetadata metadata) {
		this.column = metadata;
	}
	
	public boolean isAutoIncreament() {
		return features!=null && ArrayUtils.contains(features, ColumnFeature.AUTO_INCREMENT);
	}
}
