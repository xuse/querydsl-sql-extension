package com.github.xuse.querydsl.sql.dbmeta;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.sql.ddl.ConnectionWrapper;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;

public interface SchemaReader {

	/**
	 * @param catalog
	 * @param schema schema
	 * @param table table
	 * @param conn conn
	 * @param detail detail
	 * @return null means do not support to fetch. empty means there is no Constraints.
	 */
	List<Constraint> getConstraints(String catalog, String schema, String table, ConnectionWrapper conn, boolean detail);

	/**
	 * @param schema schema
	 * @param catalog catalog
	 * @param table table
	 * @param conn conn
	 * @return null means do not support partitions.
	 */
	default List<PartitionInfo> getPartitions(String catalog, String schema, String table, ConnectionWrapper conn){
		return null;
	};
	
	
	default Constraint getPrimaryKey(String catalog,String schema, String table, ConnectionWrapper conn) {
		List<KeyColumn> ts = conn.metadataQuery(m -> m.getPrimaryKeys(catalog, schema, table), rs -> {
			KeyColumn k = new KeyColumn();
			k.setColumnName(rs.getString("COLUMN_NAME"));
			k.setKeyName(rs.getString("PK_NAME"));
			k.setSeq(rs.getInt("KEY_SEQ"));
			k.setTableCat(rs.getString("TABLE_CAT"));
			k.setTableName(rs.getString("TABLE_NAME"));
			k.setTableSchema(rs.getString("TABLE_SCHEM"));
			return k;
		});
		if (ts.isEmpty()) {
			return null;
		}
		ts.sort(Comparator.comparingInt(KeyColumn::getSeq));
		Constraint c = new Constraint();
		c.setColumnNames(ts.stream().map(KeyColumn::getColumnName).collect(Collectors.toList()));
		KeyColumn k = ts.get(0);
		c.setCatalog(k.getTableCat());
		c.setSchema(k.getTableSchema());
		c.setTableName(k.getTableName());
		c.setConstraintType(ConstraintType.PRIMARY_KEY);
		c.setEnabled(true);
		c.setName(k.getKeyName());
		return c;
	};
}
