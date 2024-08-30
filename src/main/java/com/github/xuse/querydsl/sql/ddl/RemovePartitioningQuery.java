package com.github.xuse.querydsl.sql.ddl;

import java.util.Collections;
import java.util.List;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTablePartitionOps;
import com.querydsl.core.types.Operator;
import com.querydsl.sql.RelationalPath;
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
		DDLMetadataBuilder builder=new DDLMetadataBuilder(configuration, table, routing);
		//", ALGORITHM=INPLACE, LOCK=NONE" not support on mysql
		builder.serilizeSimple(AlterTablePartitionOps.REMOVE_PARTITIONING, table);
		return builder.getSql();
	}

	@Override
	protected List<Operator> checkSupports() {
		return Collections.singletonList(AlterTablePartitionOps.REMOVE_PARTITIONING);
	}
}



