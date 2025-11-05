package com.github.xuse.querydsl.sql;

import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.DataType;
import com.github.xuse.querydsl.sql.dbmeta.DatabaseInfo;
import com.github.xuse.querydsl.sql.dbmeta.ForeignKeyItem;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dbmeta.ObjectType;
import com.github.xuse.querydsl.sql.dbmeta.PartitionInfo;
import com.github.xuse.querydsl.sql.dbmeta.SequenceInfo;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.github.xuse.querydsl.sql.ddl.AddPartitionQuery;
import com.github.xuse.querydsl.sql.ddl.AlterTableQuery;
import com.github.xuse.querydsl.sql.ddl.CreatePartitioningQuery;
import com.github.xuse.querydsl.sql.ddl.CreateTableQuery;
import com.github.xuse.querydsl.sql.ddl.DropConstraintQuery;
import com.github.xuse.querydsl.sql.ddl.DropPartitionQuery;
import com.github.xuse.querydsl.sql.ddl.DropTableQuery;
import com.github.xuse.querydsl.sql.ddl.PartitionSizeAdjustQuery;
import com.github.xuse.querydsl.sql.ddl.RemovePartitioningQuery;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.ddl.TruncateTableQuery;
import com.github.xuse.querydsl.sql.dialect.Privilege;
import com.github.xuse.querydsl.sql.dialect.PrivilegeDetector;
import com.github.xuse.querydsl.sql.dialect.SchemaPolicy;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.sql.support.DbDistributedLockProvider;
import com.github.xuse.querydsl.sql.support.DistributedLock;
import com.github.xuse.querydsl.sql.support.DistributedLockProvider;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SchemaAndTable;

public class SQLMetadataFactoryImpl implements SQLMetadataQueryFactory {

	protected final SQLQueryFactory connection;

	protected final ConfigurationEx configuration;

	protected final MetadataQuerySupport metadataQuery;

	SQLMetadataFactoryImpl(SQLQueryFactory factory) {
		this.connection = factory;
		this.configuration = factory.getConfiguration();
		this.metadataQuery = new MetadataQuerySupport() {
			@Override
			protected ConfigurationEx getConfiguration() {
				return configuration;
			}

			@Override
			public Connection getConnection() {
				return connection.getConnection();
			}

			@Override
			public DistributedLock getLock(String lockName) {
				DistributedLockProvider provider = configuration
						.computeLockProvider(() -> DbDistributedLockProvider.create(factory));
				if(provider==null) {
					throw new IllegalStateException("There is no distributed-lock provider available.");
				}
				return provider.getLock(lockName, 3);
			}
		};
	}
	
	

	@Override
	public <T> CreateTableQuery createTable(RelationalPath<T> path) {
		return new CreateTableQuery(metadataQuery, configuration, path);
	}
	

	@Override
	public <T> CreateTableQuery createTable(LambdaTable<T> path) {
		return new CreateTableQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> DropTableQuery dropTable(RelationalPath<T> path) {
		return new DropTableQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> DropTableQuery dropTable(LambdaTable<T> path) {
		return new DropTableQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> AlterTableQuery refreshTable(RelationalPath<T> path) {
		return new AlterTableQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> AlterTableQuery refreshTable(LambdaTable<T> path) {
		return new AlterTableQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> DropConstraintQuery dropConstraintOrIndex(RelationalPath<T> path) {
		return new DropConstraintQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> TruncateTableQuery truncate(RelationalPath<T> path) {
		return new TruncateTableQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> TruncateTableQuery truncate(LambdaTable<T> path) {
		return new TruncateTableQuery(metadataQuery, configuration, path);
	}

	@Override
	public Collection<String> getCatalogs() {
		return metadataQuery.getCatalogs();
	}

	@Override
	public Collection<String> getSchemas(String catalog) {
		return metadataQuery.getSchemas(catalog);
	}

	@Override
	public List<TableInfo> getTables(String catalog, String schema) {
	    SchemaPolicy policy= configuration.getTemplates().getSchemaPolicy();
		return listTables(policy.toNamespace(catalog, schema),"%");
	}
	
    @Override
    public List<TableInfo> listTables(String namespace, String tableName) {
        if(StringUtils.isEmpty(namespace)) {
            namespace = metadataQuery.getDriverInfo().getNamespace(); 
        }
        return metadataQuery.listTables(namespace, tableName);
    }

    @Override
    public TableInfo getTable(SchemaAndTable schemaAndTable) {
        return metadataQuery.getTable(schemaAndTable);
    }

	@Override
	public List<String> getNames(String catalog, String schema, ObjectType... types) {
		return metadataQuery.getNames(catalog, schema, types);
	}

	@Override
	public boolean existsTable(SchemaAndTable table, RoutingStrategy routing) {
		table = metadataQuery.asInCurrentSchema(table);
		return metadataQuery.existsTable(table, routing == null ? RoutingStrategy.DEFAULT : routing);
	}

	@Override
	public List<ColumnDef> getColumns(SchemaAndTable table) {
		table = metadataQuery.asInCurrentSchema(table);
		return metadataQuery.getColumns(table);
	}

	@Override
	public List<SequenceInfo> getSequenceInfo(String schema, String seqName) {
		SchemaAndTable table = metadataQuery.asInCurrentSchema(new SchemaAndTable(schema, seqName));
		return metadataQuery.getSequenceInfo(table.getSchema(), table.getTable());
	}

	@Override
	public Constraint getPrimaryKey(SchemaAndTable table) {
		table = metadataQuery.asInCurrentSchema(table);
		return metadataQuery.getPrimaryKey(table);
	}

	@Override
	public List<ForeignKeyItem> getForeignKey(SchemaAndTable table) {
		table = metadataQuery.asInCurrentSchema(table);
		return metadataQuery.getForeignKey(table);
	}

	@Override
	public Collection<Constraint> getIndices(SchemaAndTable table) {
		table = metadataQuery.asInCurrentSchema(table);
		return metadataQuery.getIndexes(table, MetadataQuerySupport.INDEX_POLICY_INDEX_ONLY);
	}

    @Override
    public Collection<Constraint> getAllIndexAndConstraints(SchemaAndTable table) {
        table = metadataQuery.asInCurrentSchema(table);
        return metadataQuery.getIndexes(table, MetadataQuerySupport.INDEX_POLICY_MERGE_CONSTRAINTS);
    }

	@Override
	public Collection<Constraint> getConstraints(SchemaAndTable table) {
		table = metadataQuery.asInCurrentSchema(table);
		return metadataQuery.getConstraints(table, true);
	}

	@Override
	public List<PartitionInfo> getPartitions(SchemaAndTable table) {
		table = metadataQuery.asInCurrentSchema(table);
		return metadataQuery.getPartitions(table);
	}

	@Override
	public <T> PartitionSizeAdjustQuery adjustPartitionSize(RelationalPath<T> path) {
		return new PartitionSizeAdjustQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> AddPartitionQuery addPartition(RelationalPath<T> table) {
		return new AddPartitionQuery(metadataQuery, configuration, table);
	}

	@Override
	public <T> DropPartitionQuery dropPartition(RelationalPath<T> table) {
		return new DropPartitionQuery(metadataQuery, configuration, table);
	}

	@Override
	public <T> RemovePartitioningQuery removePartitioning(RelationalPath<T> table) {
		return new RemovePartitioningQuery(metadataQuery, configuration, table);
	}

	@Override
	public <T> CreatePartitioningQuery createPartitioning(RelationalPath<T> path) {
		return new CreatePartitioningQuery(metadataQuery, configuration, path);
	}

	/**
	 * 指定一个SQL脚本文件运行
	 * @param url the script file.
	 * @return int
	 * @param charset Charset
	 * @param ignoreErrors boolean
	 * @param exceptionCollector Map&lt;String,RuntimeException&gt;
	 */
	public int executeScriptFile(URL url, Charset charset, boolean ignoreErrors, Map<String, RuntimeException> exceptionCollector) {
		return metadataQuery.executeScriptFile(url, charset == null ? Charset.defaultCharset() : charset, ";/", ignoreErrors, exceptionCollector);
	}

	@Override
	public boolean hasPrivilege(Privilege... p) {
		PrivilegeDetector pd = configuration.getTemplates().getPrivilegeDetector();
		return pd.check(connection, p);
	}

	@Override
	public String getDatabaseProduct() {
		return metadataQuery.getDriverInfo().getDatabaseProductName();
	}
	
	@Override
	public DatabaseInfo getDatabaseInfo() {
		return metadataQuery.getDriverInfo();
	}

	@Override
	public String getDatabaseVersion() {
		return metadataQuery.getDriverInfo().getDataProductVersion();
	}

	@Override
	public Date getCurrentDateTime() {
		return metadataQuery.getDatabaseTime();
	}

	@Override
	public List<DataType> getDataTypes() {
		return metadataQuery.getDataTypes();
	}
}
