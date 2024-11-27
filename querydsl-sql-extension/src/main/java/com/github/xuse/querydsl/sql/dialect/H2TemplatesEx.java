package com.github.xuse.querydsl.sql.dialect;

import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.InformationSchemaReader;
import com.github.xuse.querydsl.sql.dbmeta.KeyColumn;
import com.github.xuse.querydsl.sql.dbmeta.SchemaReader;
import com.github.xuse.querydsl.sql.ddl.ConnectionWrapper;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.sql.ddl.DDLOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableConstraintOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.Basic;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLTemplates;

public class H2TemplatesEx extends DefaultSQLTemplatesEx {

	public H2TemplatesEx(SQLTemplates templates) {
		super(templates);

		// 默认的时间精度是毫秒，即6。故要生成秒级时间戳需要显式指定0
		typeNames.put(Types.TIME, "time(0)").size(0);
		typeNames.put(Types.TIMESTAMP, "timestamp(0)").size(0);

		typeNames.put(Types.TIMESTAMP, 6, "timestamp($l)");
		typeNames.put(Types.TIMESTAMP, 1024, "timestamp($l)").size(6);

		typeNames.put(Types.TIME, 6, "time($l)");
		typeNames.put(Types.TIME, 1024, "time($l)").size(6);
		//BIT用Boolean模拟
		typeNames.put(Types.BIT, "boolean").type(Types.BOOLEAN).noSize();
	}

	private H2SchemaReader schemaReader = new H2SchemaReader();

	@Override
	public SchemaReader getSchemaAccessor() {
		return schemaReader;
	}

	@Override
	public void init(SQLTemplates templates) {
		SQLTemplatesEx.initDefaultDDLTemplate(templates);
		add(templates, DDLOps.COLUMN_ALLOW_NULL, "");
		add(templates, DDLOps.CHARSET, "{0}");
		add(templates, DDLOps.COLLATE, "{0}");
		add(templates, DDLOps.UNSIGNED, "{0}");
		add(templates, Basic.SELECT_VALUES, "values {0}");

		add(templates, DDLOps.COMMENT_ON_COLUMN, "COMMENT ON COLUMN {0} IS {1}");
		add(templates, DDLOps.COMMENT_ON_TABLE, "COMMENT ON TABLE {0} IS {1}");
		add(templates, ConstraintType.UNIQUE, "CONSTRAINT {1} UNIQUE {2}");
		add(templates, AlterTableOps.RENAME_COLUMN, "ALTER COLUMN {0} RENAME TO {1}");
		
		add(templates, SpecialFeature.INDEPENDENT_COMMENT_STATEMENT, "");
		addUnsupports(DDLOps.UNSIGNED, DDLOps.COLLATE);
		addUnsupports(DDLOps.COLLATE, AlterTableConstraintOps.ALTER_TABLE_DROP_BITMAP,
				AlterTableConstraintOps.ALTER_TABLE_DROP_KEY);
	}

	@Override
	public String translateDefault(String columnDef, int type, int size, int digits) {
		return columnDef;
	}

	@Override
	public SizeParser getColumnSizeParser(int jdbcType) {
		switch (jdbcType) {
		case Types.TIMESTAMP:
		case Types.TIME:
			return SizeParser.TIME_DIGIT_AS_SIZE;
		default:
			return SizeParser.DEFAULT;
		}
	}

	static class H2SchemaReader extends InformationSchemaReader {
		public H2SchemaReader() {
			super(HAS_CHECK_CONSTRAINTS);
		}
		
		@Override
		protected String processCheckClause(String check) {
			check=StringUtils.removeChars(check,'"');
			return check;
		}

		@Override
		public List<Constraint> getIndexes(String catalog, String schema, String tableName, ConnectionWrapper conn) {
			String sqlStr = "SELECT\r\n"
					+ "	i.TABLE_CATALOG AS TABLE_CAT,i.TABLE_SCHEMA  AS TABLE_SCHEM,i.TABLE_NAME,c.IS_UNIQUE,i.INDEX_NAME ,i.INDEX_TYPE_NAME AS TYPE,c.ORDINAL_POSITION,\r\n"
					+ "	c.COLUMN_NAME,c.ORDERING_SPECIFICATION,i.REMARKS\r\n"
					+ "FROM	information_schema.indexes i JOIN information_schema.INDEX_COLUMNS c ON	i.INDEX_CATALOG = c.INDEX_CATALOG AND i.INDEX_SCHEMA = c.INDEX_SCHEMA AND i.INDEX_NAME = c.INDEX_NAME\r\n"
					+ "WHERE i.TABLE_NAME = ? AND i.TABLE_SCHEMA = ? AND i.IS_GENERATED = FALSE";
			SQLBindings sql = new SQLBindings(sqlStr, Arrays.asList(tableName, schema));

			Map<String, List<KeyColumn>> map = conn.query(sql, rs -> {
				KeyColumn c = new KeyColumn();
				c.setTableCat(rs.getString("TABLE_CAT"));
				c.setTableSchema(rs.getString("TABLE_SCHEM"));
				c.setTableName(rs.getString("TABLE_NAME"));
				c.setKeyName(rs.getString("INDEX_NAME"));
				c.setSeq(rs.getInt("ORDINAL_POSITION"));
				c.setColumnName(rs.getString("COLUMN_NAME"));
				c.setAscDesc(rs.getString("ORDERING_SPECIFICATION"));
//				c.setFilterCondition(sqlStr)
				c.setIndexQualifier(rs.getString("REMARKS"));
				c.setNonUnique(!rs.getBoolean("IS_UNIQUE"));
				c.setFilterCondition(rs.getString("INDEX_TYPE_NAME"));
				c.setType(DatabaseMetaData.tableIndexClustered);
				return c;
			}).stream().collect(Collectors.groupingBy(KeyColumn::getKeyName));

			List<Constraint> result = new ArrayList<>(map.size());
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
				switch (kc.getType()) {
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
	}

}
