package com.github.xuse.querydsl.sql.ddl;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.DataType;
import com.github.xuse.querydsl.sql.dbmeta.DatabaseInfo;
import com.github.xuse.querydsl.sql.dbmeta.ForeignKeyItem;
import com.github.xuse.querydsl.sql.dbmeta.ObjectType;
import com.github.xuse.querydsl.sql.dbmeta.PartitionInfo;
import com.github.xuse.querydsl.sql.dbmeta.SequenceInfo;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.github.xuse.querydsl.sql.dialect.Privilege;
import com.github.xuse.querydsl.sql.partitions.Partitions;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SchemaAndTable;

/**
 * For generating and maintaining database tables
 * <p>
 * 用于进行数据库表生成和维护
 *
 * @author Joey
 */
public interface SQLMetadataQueryFactory {

	/**
	 *  Create a table creation query.
	 *  <p>
	 *  生成创建表请求
	 *  @param <T> the type of the table model.
	 *  @param path table path.
	 *  @return DropTableQuery
	 */
	<T> CreateTableQuery createTable(RelationalPath<T> path);
	
	/**
	 *  Create a table creation query.
	 *  <p>
	 *  生成创建表请求
	 *  @param <T> the type of the table model.
	 *  @param path table path.
	 *  @return DropTableQuery
	 */
	<T> CreateTableQuery createTable(LambdaTable<T> path);

	/**
	 *  Create a table deletion query.
	 *  <p>
	 *  生成删除表请求
	 *
	 *  @param <T>  the type of the table model.
	 *  @param path  path of the table.
	 *  @return DropTableQuery
	 */
	<T> DropTableQuery dropTable(RelationalPath<T> path);
	
	/**
	 *  Create a table deletion query.
	 *  <p>
	 *  生成删除表请求。需要调用{@link DropTableQuery#execute()}才会实际操作。
	 *
	 *  @param <T>  the type of the table model.
	 *  @param path  path of the table.
	 *  @return DropTableQuery
	 */
	<T> DropTableQuery dropTable(LambdaTable<T> path);

	/**
	 * Create a table truncation query.
	 * <p>
	 * 截断表（删除所有数据）。需要调用{@link TruncateTableQuery#execute()}才会实际操作。
	 * @param <T>  the type of the table model.
	 * @param path path
	 * @return TruncateTableQuery
	 */
	<T> TruncateTableQuery truncate(RelationalPath<T> path);
	
	/**
	 * Create a table truncation query. 需要调用{@link TruncateTableQuery#execute()}才会实际操作。
	 * <p>
	 * 截断表（删除所有数据）
	 * @param <T>  the type of the table model.
	 * @param path path
	 * @return TruncateTableQuery
	 */
	<T> TruncateTableQuery truncate(LambdaTable<T> path);

	/**
	 *  Create an ALTER TABLE query.
	 *  <p>
	 *  Update Table (Refresh the database table structure based on the Java model
	 *  structure) Supports the following features:
	 *  <ul>
	 *  <li>Refresh database table structure from the Java model</li>
	 *  <li>Add indexes to the table</li>
	 *  <li>Add constraints to the table (foreign keys are not supported)</li>
	 *  <li>Modify the primary key of the table</li>
	 *  </ul>
	 *  This method does not compare or update the table's partitions.
	 *  <p>
	 *  更新表(对比Java模型结构，对数据库表结构进行刷新) 支持以下功能
	 *  <ul>
	 *  <li>从Java模型刷新到数据库表结构</li>
	 *  <li>为表增加索引</li>
	 *  <li>为表增加约束（不支持外键）</li>
	 *  <li>修改表的主键</li>
	 *  </ul>
	 *  本方法不会对比更新表的分区。
	 *  @param <T> the type of the model.
	 *  @param path path of the table.
	 *  @return AlterTableQuery
	 */
	<T> AlterTableQuery refreshTable(RelationalPath<T> path);
	

	/**
	 *  Create an ALTER TABLE query.
	 *  <p>
	 *  Update Table (Refresh the database table structure based on the Java model
	 *  structure) Supports the following features:
	 *  <ul>
	 *  <li>Refresh database table structure from the Java model</li>
	 *  <li>Add indexes to the table</li>
	 *  <li>Add constraints to the table (foreign keys are not supported)</li>
	 *  <li>Modify the primary key of the table</li>
	 *  </ul>
	 *  This method does not compare or update the table's partitions.
	 *  <p>
	 *  更新表(对比Java模型结构，对数据库表结构进行刷新) 支持以下功能
	 *  <ul>
	 *  <li>从Java模型刷新到数据库表结构</li>
	 *  <li>为表增加索引</li>
	 *  <li>为表增加约束（不支持外键）</li>
	 *  <li>修改表的主键</li>
	 *  </ul>
	 *  本方法不会对比更新表的分区。
	 *  @param <T> the type of the model.
	 *  @param path path of the table.
	 *  @return AlterTableQuery
	 */
	<T> AlterTableQuery refreshTable(LambdaTable<T> path);
	

	/**
	 *  Create a query to drop database constraints.
	 *  <p>
	 *  删除约束
	 *
	 *  @param <T>  the type of the table model.
	 *  @param path path of the table.
	 *  @return DropConstraintQuery
	 */
	<T> DropConstraintQuery dropConstraintOrIndex(RelationalPath<T> path);

	/**
	 * Create a query to drop table partitions.
	 * <p>
	 * 删除分区，<strong>连同分区内的数据一同删除</strong>
	 * @return DropPartitionQuery
	 * @param <T> The type of target object.
	 * @param path path of the entity
	 */
	<T> DropPartitionQuery dropPartition(RelationalPath<T> path);

	/**
	 * @return 移除表上所有的分区设置，但不改变表的数据
	 * @param <T> The type of target object.
	 * @param path path of the entity
	 */
	<T> RemovePartitioningQuery removePartitioning(RelationalPath<T> path);

	/**
	 * For tables that are not yet partitioned, set up the partitions. New partitions
	 * must comply with database rules—partition fields must be one of the primary
	 * key fields. If not, the operation will fail. If there is a need to add a new
	 * primary key field, the `Alter table` function should be executed first.
	 * <h2>中文</h2>
	 * 对于尚未分区的表，进行分区设置。
	 *           新增分区必须遵守数据库规则——分区字段必须是主键字段之一。如果不是将会失败，如有新增主键字段的诉求需先执行Alter table功能。
	 * @implNote 使用这个方法，可以为实体定义中不含分区策略的表添加分区，唯一要注意的就是分区字段需要位于主键的要求。
	 *           可以使用工具类{@link Partitions}灵活地创建分区策略。 Eg.
	 * <pre>
	 * <code>metadata.createPartitioning(t1).partitionBy(
	 *     Partitions.byHash(HashType.HASH, "TO_DAYS(created)", 4))
	 *   .execute();</code>
	 * </pre>
	 * @return CreatePartitioningQuery
	 * @param <T> The type of target object.
	 * @param path path of the entity
	 */
	<T> CreatePartitioningQuery createPartitioning(RelationalPath<T> path);

	/**
	 * <h2>English:</h2>
	 * Add partitions to an already partitioned table, applicable to partitions
	 * organized by LIST/RANGE methods. HASH/KEY partitions are not supported. This
	 * operation will automatically calculate whether the added partitions affect
	 * existing ones. If it detects data overlap between the new partition and the
	 * original partitions, it will reorganize the data using the REORGANIZE method.
	 * <h2>Chinese:</h2>
	 * 在表已经分区的情况下添加分区，适用LIST/RANGE方式组织的分区。不支持HASH/KEY分区。
	 * 该操作会自动计算添加的分区是否对已有分区产生影响，如果发现新增分区与原有分区存在数据重叠，会使用REORGANIZE方式对数据进行重新组织。
	 * 
	 * @return AddPartitionQuery
	 * @param <T>  The type of target object.
	 * @param path path of the entity
	 */
	<T> AddPartitionQuery addPartition(RelationalPath<T> path);

	/**
	 * <h2>English:</h2> Add or reduce partitions to an already partitioned table.
	 * This is only applicable to HASH/KEY type partitions for adjusting the number
	 * of partitions (increase or decrease). Reducing partitions will cause data
	 * redistribution without data deletion.
	 * <h2>Chinese:</h2> 在表已经分区的情况下添加/减少分区。仅针对Hash/key类型分区，调整分区数量（增加或减少）
	 * 减少分区会引起数据重新分布，不会导致数据删除。
	 * 
	 * @return PartitionSizeAdjustQuery
	 * @param <T>  The type of target object.
	 * @param path path of the entity
	 */
	<T> PartitionSizeAdjustQuery adjustPartitionSize(RelationalPath<T> path);

	/**
	 * 判断表是否存在。
	 * @param table  the table
	 * @param routing RoutingStrategy
	 * @return true if the table exists.
	 */
	boolean existsTable(SchemaAndTable table, RoutingStrategy routing);

	/**
	 * <h2>English:</h2> 
	 * To query the names of specific types of tables within a specified catalog and schema
	 * <h2>Chinese:</h2>
	 * 在指定catalog和schema下查询指定类型的表名称。用于快速遍历数据库中的表和视图。
	 * @param catalog catalog
	 * @param schema schema
	 * @param types types
	 * @return 名称 name of the exist database objects.
	 */
	List<String> getNames(String catalog, String schema, ObjectType... types);

	/**
	 * 得到表的主键信息.
	 * <p>
	 * fetch the information of primary key on table.
	 * @param table the table
	 * @return information of primary keys.
	 */
	Constraint getPrimaryKey(SchemaAndTable table);

	/**
	 * <h2>English:</h2>
	 * Fetch the information of indices on the assigned table.
	 * <h2>Chinese:</h2>
	 * 获得指定表的索引信息。
	 *  @param table SchemaAndTable
	 *  @return all indexes on this table, except UNIQUE (unique is treat as a constraint.)
	 */
	Collection<Constraint> getIndices(SchemaAndTable table);

	/**
	 *  fetch constraints of table.
	 *  <p>
	 *  得到表的约束信息
	 *  
	 *  @param table  SchemaAndTable
	 *  @return all constraint on this table, CHECK, PRIMARY_KEY, UNIQUE etc. except any foreign key.
	 */
	Collection<Constraint> getConstraints(SchemaAndTable table);
	
	
	/**
	 *  fetch constraints and indices of table.
	 * @param table
	 * @return Indices and constraints.
	 */
	Collection<Constraint> getAllIndexAndConstraints(SchemaAndTable table);

	/**
	 * Fetch partition information of the table. will return null if the database do not support partition feature.
	 * <p>
	 * 对于支持数据分区的表，获得分区信息.
	 * 
	 * @param table table
	 * @return List&lt;partitionInfo&gt;
	 */
	List<PartitionInfo> getPartitions(SchemaAndTable table);
	

	/**
	 * Get all catalogs in current database instance.
	 * <p>
	 * 获得数据库实例内所有catalog.
	 * @return catalog names.
	 */
	Collection<String> getCatalogs();

	/**
	 * Get all schemas in the catalog.
	 * <p>
	 * 获得catalog下所有的schema.
	 * @param catalog catalog
	 * @return schema names.
	 */
	Collection<String> getSchemas(String catalog);

	/**
	 * @param catalog catalog 
	 * @param schema schema. Note that MySQL and postgresql do not have schemas, and Oracle has.  
	 * @return List of Table information.
	 */
	@Deprecated
	List<TableInfo> getTables(String catalog, String schema);
	
	/**
     * Fetch information of all tables in schema.
     * <p>
     * 得到表的信息
     * @param namespace catalog or schema. null as the current namespace. if you want to fetch tables from all catalogs/schemas, input '%'.  
     * @param tableNamePattern table name. null as '%'
     * @return List of Table information.
     */
	List<TableInfo> listTables(String namespace, String tableNamePattern);
	
	/**
	 *  Fetch information of the table
	 * @param schemaAndTable
	 * @return  Table information.
	 */
	TableInfo getTable(SchemaAndTable schemaAndTable);

	/**
	 * Get the database product name.
	 * <p>
	 * 得到数据库产品名称
	 * @return database product name. such as 'MySQL'/'Oracle'..
	 */
	String getDatabaseProduct();

	/**
	 * get the version number of current database.
	 * <p>
	 * 得到数据库版本号.
	 * @return version number of database. / 数据库版本。 
	 */
	String getDatabaseVersion();
	
	DatabaseInfo getDatabaseInfo();
	
	List<DataType> getDataTypes();
	
	int executeScriptFile(URL url, Charset charset, boolean ignoreErrors, Map<String, RuntimeException> exceptionCollector);

	boolean hasPrivilege(Privilege... create);
	
	
	Date getCurrentDateTime();

	List<ColumnDef> getColumns(SchemaAndTable schemaAndTable);

	List<SequenceInfo> getSequenceInfo(String namespace, String seqName);

	List<ForeignKeyItem> getForeignKey(SchemaAndTable st);
}
