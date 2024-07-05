package com.github.xuse.querydsl.sql.ddl;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.ForeignKeyItem;
import com.github.xuse.querydsl.sql.dbmeta.ObjectType;
import com.github.xuse.querydsl.sql.dbmeta.PartitionInfo;
import com.github.xuse.querydsl.sql.dbmeta.SequenceInfo;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.github.xuse.querydsl.sql.partitions.Partitions;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RoutingStrategy;
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
	 * Alter table.更新表(对比Java模型结构，对数据库表结构进行刷新)
	 * 支持以下功能
	 * <ul>
	 * <li>从Java模型刷新到数据库表结构</li>
	 * <li>为表增加索引</li>
	 * <li>为表增加约束（不支持外键）</li>
	 * <li>修改表的主键</li>
	 * </ul>
	 * @implSpec
	 * 本方法不会对比更新表的分区。
	 * @param <T>
	 * @param path
	 * @return AlterTableQuery
	 */
	<T> AlterTableQuery refreshTable(RelationalPath<T> path);

	/**
	 * 删除约束
	 * 
	 * @param <T>
	 * @param path
	 * @return DropConstraintQuery
	 */
	<T> DropConstraintQuery dropConstraintOrIndex(RelationalPath<T> path);
	
	
	/**
	 * 删除分区，<strong>连同分区内的数据一同删除</strong>
	 */
	<T> DropPartitionQuery dropPartition(RelationalPathEx<T> path);
	
	/**
	 * 移除表上所有的分区设置，但不改变表的数据
	 */
	<T> RemovePartitioningQuery removePartitioning(RelationalPathEx<T> path);
	
	/**
	 * 对于尚未分区的表，进行分区设置。 新增分区必须遵守数据库规则——分区字段必须是主键字段之一。如果不是将会失败，如有新增主键字段的诉求需先执行Alter
	 * table功能。
	 * 
	 * @implNote 使用这个方法，可以为实体定义中不含分区策略的表添加分区，唯一要注意的就是分区字段需要位于主键的要求。
	 *           可以使用工具类{@link Partitions}灵活地创建分区策略。
	 * Eg. <pre><code>metadata.createPartitioning(t1).partitionBy(
	 *     Partitions.byHash(HashType.HASH, "TO_DAYS(created)", 4))
	 *   .execute();</code></pre>
	 */
	<T> CreatePartitioningQuery createPartitioning(RelationalPathEx<T> path);
	
	
	/**
	 * 在表已经分区的情况下添加分区，适用LIST/RANGE方式组织的分区。不支持HASH/KEY分区。
	 * 该操作会自动计算添加的分区是否对已有分区产生影响，如果发现新增分区与原有分区存在数据重叠，会使用REORNANIZE方式对数据进行重新组织。
	 */
	<T> AddPartitionQuery addParition(RelationalPathEx<T> path);
	
	/**
	 * 在表已经分区的情况下添加/减少分区。仅针对Hash/key类型分区，调整分区数量（增加或减少）
	 * 减少分区会引起数据重新分布，不会导致数据删除。
	 */
	<T> PartitionSizeAdjustQuery adjustPartitionSize(RelationalPathEx<T> path);

	Collection<String> getCatalogs();

	Collection<String> getSchemas(String catalog);

	List<TableInfo> getTables(String catalog, String schema);
	
	boolean existsTable(SchemaAndTable table,RoutingStrategy routing);

	/**
	 * 在指定catalog和schema下查询指定类型的表名称。用于快速遍历数据库中的表和视图。
	 * @param catalog
	 * @param schema
	 * @param types
	 * @return 名称
	 */
	List<String> getNames(String catalog, String schema, ObjectType... types);
	
	List<ColumnDef> getColumns(SchemaAndTable schemaAndTable);
	
	List<SequenceInfo> getSequenceInfo(String schema, String seqName);
	
	Constraint getPrimaryKey(SchemaAndTable table);
	
	List<ForeignKeyItem> getForeignKey(SchemaAndTable st);
	
	/**
	 * @param table
	 * @return all indexes on this table, except UNIQUE (unique is treat as a constraint.)
	 */
	Collection<Constraint> getIndecies(SchemaAndTable table);

	/**
	 * @param table
	 * @return all constraint on this table, CHECK, PRIMARY_KEY, UNIQUE and etc.. except any foreign key.
	 */
	Collection<Constraint> getConstraints(SchemaAndTable table);
	
	/**
	 * 对于支持数据分区的表，获得分区信息
	 * @param table
	 * @return
	 */
	List<PartitionInfo> getPartitions(SchemaAndTable table);

	int executeScriptFile(URL url, Charset charset,boolean ignoreErrors, Map<String,RuntimeException> exceptionCollector);
}
