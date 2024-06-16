package com.github.xuse.querydsl.sql.ddl;

import java.util.List;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.SchemaAndTable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TruncateTableQuery  extends AbstractDDLClause<TruncateTableQuery>{

	public TruncateTableQuery(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPath<?> path) {
		super(connection, configuration, path);
	}

	@Override
	protected String generateSQL() {
		SQLSerializerAlter serializer=new SQLSerializerAlter(configuration,true);
		serializer.serializeAction(table,"TRUNCATE TABLE ");
		return serializer.toString();
	}

	@Override
	protected boolean preExecute(MetadataQuerySupport metadata) {
		SchemaAndTable actualTable=metadata.asInCurrentSchema(table.getSchemaAndTable());
		TableInfo tInfo=metadata.getTable(actualTable);
		return tInfo!=null;
	}

	@Override
	protected void finished(List<String> sqls) {
		log.info("Drop table {} finished, {} sqls executed.",table.getSchemaAndTable(),sqls.size());
	}
}
