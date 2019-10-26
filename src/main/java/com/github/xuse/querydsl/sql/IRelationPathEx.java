package com.github.xuse.querydsl.sql;

import java.util.List;

import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.querydsl.core.types.Path;

/**
 * 扩展后的元数据存储类，可以存储一些优化后的反射对象
 * 
 * @author jiyi
 *
 */
public interface IRelationPathEx {

	/**
	 * 获得Bean转换器。
	 * 
	 * @return
	 */
	BeanCodec getBeanCodec();

	/**
	 * 获得所有的列（字段）
	 * @return
	 */
	List<Path<?>> getColumns();
	
	/**
	 * 获得指定列（字段）的元数据描述
	 * @param path
	 * @return
	 */
	ColumnMapping getColumnMetadata(Path<?> path);
	
	String getTableName();
}
