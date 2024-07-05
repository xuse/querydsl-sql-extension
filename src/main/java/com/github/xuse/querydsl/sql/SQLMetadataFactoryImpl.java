package com.github.xuse.querydsl.sql;

import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
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
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RoutingStrategy;
import com.querydsl.sql.SchemaAndTable;

public class SQLMetadataFactoryImpl implements SQLMetadataQueryFactory{
	protected final Supplier<Connection> connection;

	protected final ConfigurationEx configuration;
    
    protected final MetadataQuerySupport metadataQuery;
	
    SQLMetadataFactoryImpl(Supplier<Connection> connection,ConfigurationEx configuration){
    	this.connection=connection;
    	this.configuration=configuration;
    	this.metadataQuery=new MetadataQuerySupport(){
			@Override
			protected ConfigurationEx getConfiguration() {
				return configuration;
			}

			@Override
			public Connection getConnection() {
				return connection.get();
			}
    	};
    }
	
	@Override
	public <T> CreateTableQuery createTable(RelationalPath<T> path) {
		return new CreateTableQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> DropTableQuery dropTable(RelationalPath<T> path) {
		return new DropTableQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> AlterTableQuery refreshTable(RelationalPath<T> path) {
		return new AlterTableQuery(metadataQuery, configuration, (RelationalPathEx<?>)path);
	}

	@Override
	public <T> DropConstraintQuery dropConstraintOrIndex(RelationalPath<T> path) {
		return new DropConstraintQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> TruncateTableQuery truncate(RelationalPath<T> path) {
		return new TruncateTableQuery(metadataQuery,configuration,path);
	}

	@Override
	public Collection<String> getCatalogs() {
		return metadataQuery.getCatalogs();
	}

	@Override
	public Collection<String> getSchemas(String catalogy) {
		return metadataQuery.getSchemas(catalogy);
	}

	@Override
	public List<TableInfo> getTables(String catalog, String schema) {
		return metadataQuery.getTables(catalog, schema);
	}

	@Override
	public List<String> getNames(String catalog, String schema,ObjectType... types) {
		return metadataQuery.getNames(catalog, schema, types);
	}

	@Override
	public boolean existsTable(SchemaAndTable table, RoutingStrategy routing) {
		table=metadataQuery.asInCurrentSchema(table);
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
	public Collection<Constraint> getIndecies(SchemaAndTable table) {
		table = metadataQuery.asInCurrentSchema(table);
		return metadataQuery.getIndexes(table, MetadataQuerySupport.INDEX_POLICY_INDEX_ONLY);
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
	
	public <T> PartitionSizeAdjustQuery adjustPartitionSize(RelationalPathEx<T> table) {
		return new PartitionSizeAdjustQuery(metadataQuery, configuration,table);
	}

	@Override
	public  <T> AddPartitionQuery addParition(RelationalPathEx<T> table) {
		return new AddPartitionQuery(metadataQuery, configuration,table);
	}

	@Override
	public  <T> DropPartitionQuery dropPartition(RelationalPathEx<T> table) {
		return new DropPartitionQuery(metadataQuery, configuration,table);
	}

	@Override
	public  <T> RemovePartitioningQuery removePartitioning(RelationalPathEx<T> table) {
		return new RemovePartitioningQuery(metadataQuery,configuration,table);
	}

	@Override
	public <T> CreatePartitioningQuery createPartitioning(RelationalPathEx<T> path) {
		return new CreatePartitioningQuery(metadataQuery, configuration, path);
	}

	/**
	 * 指定一个SQL脚本文件运行
	 * 
	 * @param url the script file.
	 * @throws SQLException
	 */
	public int executeScriptFile(URL url,Charset charset, boolean ignoreErrors, Map<String,RuntimeException> exceptionCollector) {
		return metadataQuery.executeScriptFile(url, charset == null ? Charset.defaultCharset() : charset, ";/",ignoreErrors,exceptionCollector);
	}
}
