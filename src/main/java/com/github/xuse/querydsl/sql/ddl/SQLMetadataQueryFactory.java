package com.github.xuse.querydsl.sql.ddl;

import java.util.Collection;
import java.util.List;

import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.ForeignKeyItem;
import com.github.xuse.querydsl.sql.dbmeta.ObjectType;
import com.github.xuse.querydsl.sql.dbmeta.SequenceInfo;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SchemaAndTable;

/**
 * 用于进行数据库表生成和维护
 * 
 * @author jiyi
 *
 */
public interface SQLMetadataQueryFactory {

	/**
	 * 创建表
	 * 
	 * @param <T>
	 * @param path
	 * @return DropTableQuery
	 */
	<T> CreateTableQuery createTable(RelationalPath<T> path);

	/**
	 * 删除表
	 * 
	 * @param <T>
	 * @param path
	 * @return DropTableQuery
	 */
	<T> DropTableQuery dropTable(RelationalPath<T> path);

	/**
	 * 截断表（删除所有数据）
	 * 
	 * @param <T>
	 * @param path
	 * @return TruncateTableQuery
	 */
	<T> TruncateTableQuery truncate(RelationalPath<T> path);

	/**
	 * 更新表(对比Java模型结构，对数据库表结构进行刷新)
	 * @param <T>
	 * @param path
	 * @return AlterTableQuery
	 */
	<T> AlterTableQuery refreshTable(RelationalPath<T> path);

	/**
	 * 创建一个索引
	 * 
	 * @param path
	 * @return CreateIndexQuery
	 */
	<T> CreateIndexQuery createIndex(RelationalPath<T> path);

	/**
	 * 删除索引
	 * 
	 * @param path
	 * @return DropIndexQuery
	 */
	<T> DropIndexQuery dropIndex(RelationalPath<T> path);

	/**
	 * 创建约束
	 * 
	 * @param path
	 * @return CreateConstraintQuery
	 */
	<T> CreateConstraintQuery createContraint(RelationalPath<T> path);

	/**
	 * 删除约束
	 * 
	 * @param <T>
	 * @param path
	 * @return DropConstraintQuery
	 */
	<T> DropConstraintQuery dropConstraint(RelationalPath<T> path);

	Collection<String> getCatalogs();

	Collection<String> getSchemas(String catalog);

	List<TableInfo> getTables(String catalog, String schema);

	List<String> getNames(String catalog, String schema, ObjectType... types);
	
	List<ColumnDef> getColumns(SchemaAndTable schemaAndTable);
	
	List<SequenceInfo> getSequenceInfo(String schema, String seqName);
	
	Constraint getPrimaryKey(SchemaAndTable table);
	
	List<ForeignKeyItem> getForeignKey(SchemaAndTable st);
	
	/**
	 * @param table
	 * @return all indexes on this table, except UNIQUE (unique is treat as a constraint.)
	 */
	Collection<Constraint> getIndexes(SchemaAndTable table);

	/**
	 * @param table
	 * @return all constraint on this table, CHECK, PRIMARY_KEY, UNIQUE and etc.. except any foreign key.
	 */
	Collection<Constraint> getConstraints(SchemaAndTable table);
}
