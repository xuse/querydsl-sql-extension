package com.github.xuse.querydsl.sql.ddl;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.SchemaAndTable;

public class RemovePartitioningQuery extends AbstractDDLClause<RemovePartitioningQuery> {

	private boolean ignore;
	
	
	public RemovePartitioningQuery ignore() {
		ignore = true;
		return this;
	}
	
	
	public RemovePartitioningQuery(MetadataQuerySupport connection, ConfigurationEx configuration,
			RelationalPathEx<?> path) {
		super(connection, configuration, path);
	}

	@Override
	protected String generateSQL() {
		if(ignore) {
			SchemaAndTable acutalTable = connection.asInCurrentSchema(table.getSchemaAndTable());
			if(routing!=null) {
				acutalTable=routing.getOverride(acutalTable, configuration);
			}
			if(connection.getPartitions(acutalTable).isEmpty()) {
				return null;
			}
		}
		
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		serializer.setRouting(routing);
		//", ALGORITHM=INPLACE, LOCK=NONE" not support
		serializer.serializeAction("ALTER TABLE ", table, " REMOVE PARTITIONING");
		return serializer.toString();
	}
}
