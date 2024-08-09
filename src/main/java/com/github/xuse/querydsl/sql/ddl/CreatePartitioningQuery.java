package com.github.xuse.querydsl.sql.ddl;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionDefineOps;
import com.github.xuse.querydsl.sql.partitions.PartitionBy;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializerAlter;

public class CreatePartitioningQuery extends AbstractDDLClause<CreatePartitioningQuery> {

	private PartitionBy partitionBy;

	private boolean checkField = true;

	public CreatePartitioningQuery(MetadataQuerySupport connection, ConfigurationEx configuration,
			RelationalPath<?> path) {
		super(connection, configuration,RelationalPathExImpl.toRelationPathEx(path));
		if (!configuration.getTemplates().supports(PartitionDefineOps.PARTITION_BY)) {
			throw new UnsupportedOperationException("Current database do not support PARTITION operation.");
		}
		if (path != null) {
			partitionBy = table.getPartitionBy();
		}
	}

	@Override
	protected String generateSQL() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		serializer.setRouting(routing);
		serializer.serializeAction(table, "ALTER TABLE ");
		serializer.serializePartitionBy(partitionBy, table, checkField);
		//Do not support online execute.
		//serializer.append(", ALGORITHM=INPLACE, LOCK=NONE");
		return serializer.toString();
	}

	public CreatePartitioningQuery partitionBy(PartitionBy partitionBy) {
		this.partitionBy = partitionBy;
		this.checkField = false;
		return this;
	}
}
