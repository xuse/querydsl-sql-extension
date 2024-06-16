package com.github.xuse.querydsl.sql;

import java.util.Collection;
import java.util.List;

import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.dbmeta.Collate;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.querydsl.core.types.Path;
import com.querydsl.sql.RelationalPath;

/**
 * 扩展后的元数据存储类，可以存储一些优化后的反射对象
 * 
 * @author jiyi
 * @param <T> 
 *
 */
public interface RelationalPathEx<T> extends RelationalPath<T>{

	/**
	 * @return 获得Bean转换器。
	 */
	BeanCodec getBeanCodec();

	/**
	 * 
	 * @return list of path 获得所有的列（字段）
	 */
	List<Path<?>> getColumns();
	
	/**
	 * @param path
	 * @return 获得指定列（字段）的元数据描述
	 */
	ColumnMapping getColumnMetadata(Path<?> path);
	
	
	/**
	 * 根据名称获得path
	 * @param name of the path.(not column name)
	 * @return Path
	 */
	Path<?> getColumn(String name);
	
	
	/**
	 * 获得各类约束(含索引)
	 * @return collection of Constraint 
	 */
	Collection<Constraint> getConstraints();
	
	
	Collate getCollate();
	
	String getComment();
}
