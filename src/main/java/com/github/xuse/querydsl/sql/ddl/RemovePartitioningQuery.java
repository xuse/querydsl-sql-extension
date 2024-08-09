package com.github.xuse.querydsl.sql.ddl;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.SchemaAndTable;

public class RemovePartitioningQuery extends AbstractDDLClause<RemovePartitioningQuery> {

	private boolean ignore;
	
	/**
	 * set to true, if there is no Partitioning on the table. do nothing.
	 * <p>
	 * 设置为true时，如果表上没有分区信息，那么就什么也不做。
	 * @return thr current object.
	 */
	public RemovePartitioningQuery ignore() {
		ignore = true;
		return this;
	}
	
	
	public RemovePartitioningQuery(MetadataQuerySupport connection, ConfigurationEx configuration,
			RelationalPath<?> path) {
		super(connection, configuration, RelationalPathExImpl.toRelationPathEx(path));
	}

	@Override
	protected String generateSQL() {
		if(ignore) {
			SchemaAndTable actualTable = connection.asInCurrentSchema(table.getSchemaAndTable());
			if(routing!=null) {
				actualTable=routing.getOverride(actualTable, configuration);
			}
			if(connection.getPartitions(actualTable).isEmpty()) {
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
