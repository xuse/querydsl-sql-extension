package com.github.xuse.querydsl.sql.dbmeta;

import java.util.Collections;
import java.util.List;

import com.github.xuse.querydsl.sql.ddl.ConnectionWrapper;

public class JdbcSchemaReader implements SchemaReader {
	public static final JdbcSchemaReader INSTANCE = new JdbcSchemaReader();

	@Override
	public List<Constraint> getConstraints(String catalog, String schema, String table, ConnectionWrapper conn,
			boolean detail) {
		Constraint pk = getPrimaryKey(catalog, schema, table, conn);
		return pk == null ? Collections.emptyList() : Collections.singletonList(pk);
	}

	@Override
	public List<PartitionInfo> getPartitions(String catalog, String schema, String table, ConnectionWrapper conn) {
		return Collections.emptyList();
	}
}
