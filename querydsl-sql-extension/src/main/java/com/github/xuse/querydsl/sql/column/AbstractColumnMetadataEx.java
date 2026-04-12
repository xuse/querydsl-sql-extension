package com.github.xuse.querydsl.sql.column;

import java.util.Map;

import com.github.xuse.querydsl.util.ArrayUtils;
import com.querydsl.core.types.Expression;
import com.querydsl.sql.ColumnMetadata;

import lombok.Getter;
import lombok.Setter;

/**
 * <h2>English:</h2>
 * Column metadata. Extends QueryDSL's ColumnMetadata with four additional column descriptors.
 * <h2>Chinese:</h2>
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
	 * <h2>English:</h2> QueryDSL metadata object.
	 * <h2>Chinese:</h2> QueryDSL的元数据对象
	 */
	private final ColumnMetadata column;

	/**
	 * <h2>English:</h2> Unsigned number modifier.
	 * <h2>Chinese:</h2> 无符号数修饰
	 */
	protected boolean unsigned;

	/**
	 * <h2>English:</h2> Column default value (expression).
	 * <h2>Chinese:</h2> 列默认值（表达式）
	 */
	protected Expression<?> defaultExpression;

	/**
	 * <h2>English:</h2> Other modifiers such as auto-increment. May support unique, key, check in the future.
	 * <h2>Chinese:</h2> 其他修饰，如自增等，今后可能考虑支持unique、key、Check等实际为约束或索引的特性。
	 */
	protected ColumnFeature[] features;

	/**
	 * <h2>English:</h2> Column comment.
	 * <h2>Chinese:</h2> 列注释
	 */
	protected String comment;
	
	/**
	 * <h2>English:</h2> Additional special specifications.
	 * <h2>Chinese:</h2> 附加SpecialSpec
	 */
	protected Map<String,String> specialSpec;

	protected abstract Class<?> getType();

	public AbstractColumnMetadataEx(ColumnMetadata metadata) {
		this.column = metadata;
	}
	
	public boolean isAutoIncrement() {
		return features!=null && ArrayUtils.contains(features, ColumnFeature.AUTO_INCREMENT);
	}
}
