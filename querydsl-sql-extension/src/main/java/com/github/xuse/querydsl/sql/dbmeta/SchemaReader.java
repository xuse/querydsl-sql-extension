package com.github.xuse.querydsl.sql.dbmeta;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.sql.ddl.ConnectionWrapper;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;

/**
 * Reading database schemas from system tables. The default implementation is reading from JDBC driver.
 * @author Joey
 */
public interface SchemaReader {
	/**
	 * Read tables 
	 * @param connection connection
	 * @param catalog catalog
	 * @param schema schema
	 * @param tableName table name
	 * @param type type
	 * @return TableInfo
	 */
	default List<TableInfo> fetchTables(ConnectionWrapper connection, String catalog, String schema, String tableName,
			ObjectType type){
		return connection.metadataQuery(m -> m.getTables(catalog, schema, tableName, type == null ? null : new String[] { type.name() }), SchemaReader::fromRs);
	};
	
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

	/**
	 * Reading constraints.
	 * The default implementation is read primary key.
	 * @param catalog catalog
	 * @param schema schema
	 * @param table table
	 * @param connection connection
	 * @param detail detail
	 * @return null means do not support to fetch. empty means there is no Constraints.
	 */
	default List<Constraint> getConstraints(String catalog, String schema, String table, ConnectionWrapper connection, boolean detail){
		Constraint pk = getPrimaryKey(catalog, schema, table, connection);
		return pk == null ? Collections.emptyList() : Collections.singletonList(pk);
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
	}

	/**
	 * Reading indexes .
	 * 获得数据库索引。
	 * @implNote
	 * 默认实现：基于JDBC
	 */
	default List<Constraint> getIndexes(String catalog, String schema, String tableName, ConnectionWrapper conn){
		Map<String, List<KeyColumn>> map = conn.metadataQuery( m -> m.getIndexInfo(catalog, schema, tableName, false, false), rs -> {
			KeyColumn k = new KeyColumn();
			k.setTableCat(rs.getString("TABLE_CAT"));
			k.setTableSchema(rs.getString("TABLE_SCHEM"));
			k.setTableName(rs.getString("TABLE_NAME"));
			
			k.setIndexQualifier(rs.getString("INDEX_QUALIFIER"));
			k.setNonUnique(rs.getBoolean("NON_UNIQUE"));
			k.setKeyName(rs.getString("INDEX_NAME"));
			k.setType(rs.getInt("TYPE"));
			k.setSeq(rs.getInt("ORDINAL_POSITION"));
			
			k.setColumnName(rs.getString("COLUMN_NAME"));
			k.setAscDesc(rs.getString("ASC_OR_DESC"));
			k.setCardinality(rs.getLong("CARDINALITY"));
			k.setPages(rs.getLong("PAGES"));
			k.setFilterCondition(rs.getString("FILTER_CONDITION"));
			return k;
		}).stream().collect(Collectors.groupingBy(KeyColumn::getKeyName));
		List<Constraint> result =new ArrayList<>(map.size());
		for (Map.Entry<String, List<KeyColumn>> entry : map.entrySet()) {
			Constraint index = new Constraint();
			List<KeyColumn> columns = entry.getValue();
			columns.sort(Comparator.comparingInt(a -> a.seq));
			index.setColumnNames(columns.stream().map(KeyColumn::getColumnName).collect(Collectors.toList()));
			KeyColumn kc = entry.getValue().get(0);
			index.setCatalog(kc.getTableCat());
			index.setSchema(kc.getTableSchema());
			index.setTableName(kc.getTableName());
			index.setCheckClause(DDLExpressions.wrapCheckExpression(kc.getFilterCondition()));
			index.setName(kc.getKeyName());
			
			boolean isUnique = !kc.isNonUnique();
			switch(kc.getType()) {
				case DatabaseMetaData.tableIndexStatistic:
					index.setConstraintType(ConstraintType.CHECK);
					break;
				case DatabaseMetaData.tableIndexClustered:
				case DatabaseMetaData.tableIndexOther:
					index.setConstraintType(isUnique ? ConstraintType.UNIQUE : ConstraintType.KEY);
					break;
				case DatabaseMetaData.tableIndexHashed:
					index.setConstraintType(ConstraintType.HASH);
					break;
			}
			result.add(index);
		}
		return result;
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
