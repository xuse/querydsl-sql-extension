package com.github.xuse.querydsl.sql.dbmeta;

import java.sql.ResultSet;
import java.sql.SQLException;
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
	
	@Override
	public List<TableInfo> fetchTables(ConnectionWrapper e, String catalog, String schema, String qMatchName, ObjectType type) {
		return e.metadataQuery(m -> m.getTables(catalog, schema, qMatchName, type == null ? null : new String[] { type.name() }), JdbcSchemaReader::fromRs);
	}

	static TableInfo fromRs(ResultSet rs) throws SQLException {
		TableInfo info = new TableInfo();
		info.setCatalog(rs.getString("TABLE_CAT"));
		info.setSchema(rs.getString("TABLE_SCHEM"));
		info.setName(rs.getString("TABLE_NAME"));
		info.setType(rs.getString("TABLE_TYPE"));
		info.setRemarks(rs.getString("REMARKS"));
		info.setTypeCat(rs.getString("TYPE_CAT"));
		info.setTypeName(rs.getString("TYPE_NAME"));
		info.setSchema(rs.getString("TYPE_SCHEM"));
		return info;
	}
}
