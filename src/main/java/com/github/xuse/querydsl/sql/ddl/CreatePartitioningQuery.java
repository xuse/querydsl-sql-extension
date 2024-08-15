package com.github.xuse.querydsl.sql.ddl;

import java.util.Collections;
import java.util.List;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTablePartitionOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionDefineOps;
import com.github.xuse.querydsl.sql.partitions.PartitionBy;
import com.querydsl.core.types.Operator;
import com.querydsl.sql.RelationalPath;

public class CreatePartitioningQuery extends AbstractDDLClause<CreatePartitioningQuery> {

	private PartitionBy partitionBy;

	private boolean checkField = true;

	public CreatePartitioningQuery(MetadataQuerySupport connection, ConfigurationEx configuration,
			RelationalPath<?> path) {
		super(connection, configuration, RelationalPathExImpl.toRelationPathEx(path));
		if (path != null) {
			partitionBy = table.getPartitionBy();
		}
	}

	@Override
	protected List<String> generateSQLs() {
		DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, routing);
		builder.serializePartitionBy(partitionBy, checkField);
		return builder.getSqls();
	}

	public CreatePartitioningQuery partitionBy(PartitionBy partitionBy) {
		this.partitionBy = partitionBy;
		this.checkField = false;
		return this;
	}

	@Override
	protected String generateSQL() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected List<Operator> checkSupports() {
		return Collections.singletonList(AlterTablePartitionOps.ADD_PARTITIONING);
	}
	
	
}
