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

	List<Path<?>> getColumns();
	
	ColumnMapping getColumnMetadata(Path<?> path);
}
