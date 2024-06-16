package com.github.xuse.querydsl.sql;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.ForeignKeyItem;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dbmeta.ObjectType;
import com.github.xuse.querydsl.sql.dbmeta.SequenceInfo;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.github.xuse.querydsl.sql.ddl.AlterTableQuery;
import com.github.xuse.querydsl.sql.ddl.CreateConstraintQuery;
import com.github.xuse.querydsl.sql.ddl.CreateIndexQuery;
import com.github.xuse.querydsl.sql.ddl.CreateTableQuery;
import com.github.xuse.querydsl.sql.ddl.DropConstraintQuery;
import com.github.xuse.querydsl.sql.ddl.DropIndexQuery;
import com.github.xuse.querydsl.sql.ddl.DropTableQuery;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.ddl.TruncateTableQuery;
import com.querydsl.sql.RelationalPath;
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
	public <T> CreateIndexQuery createIndex(RelationalPath<T> path) {
		return new CreateIndexQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> DropIndexQuery dropIndex(RelationalPath<T> path) {
		return new DropIndexQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> CreateConstraintQuery createContraint(RelationalPath<T> path) {
		return new CreateConstraintQuery(metadataQuery, configuration, path);
	}

	@Override
	public <T> DropConstraintQuery dropConstraint(RelationalPath<T> path) {
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
	public List<ColumnDef> getColumns(SchemaAndTable schemaAndTable) {
		return metadataQuery.getColumns(schemaAndTable);
	}

	@Override
	public List<SequenceInfo> getSequenceInfo(String schema, String seqName) {
		return metadataQuery.getSequenceInfo(schema, seqName);
	}

	@Override
	public Constraint getPrimaryKey(SchemaAndTable table) {
		return metadataQuery.getPrimaryKey(table);
	}

	@Override
	public List<ForeignKeyItem> getForeignKey(SchemaAndTable st) {
		return metadataQuery.getForeignKey(st);
	}

	@Override
	public Collection<Constraint> getIndexes(SchemaAndTable table) {
		return metadataQuery.getIndexes(table, MetadataQuerySupport.INDEX_POLICY_INDEX_ONLY);
	}

	@Override
	public Collection<Constraint> getConstraints(SchemaAndTable table) {
		return metadataQuery.getConstraints(table, true);
	}
}
