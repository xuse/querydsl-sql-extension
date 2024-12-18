package com.github.xuse.querydsl.sql.dialect;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.SchemaReader;
import com.github.xuse.querydsl.sql.ddl.ConnectionWrapper;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.sql.ddl.DDLOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableConstraintOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.Basic;
import com.github.xuse.querydsl.sql.ddl.DDLOps.OtherStatement;
import com.github.xuse.querydsl.util.ArrayUtils;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.TemplatesAccessor;
import com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase;

public class DerbySQLTemplatesEx extends DefaultSQLTemplatesEx{

	public DerbySQLTemplatesEx(SQLTemplates templates) {
		super(templates);
		typeNames.put(Types.TINYINT, "smallint").type(Types.SMALLINT).noSize();
		typeNames.put(Types.BIT, "boolean").type(Types.BOOLEAN).noSize();
		typeNames.put(Types.FLOAT, "float").type(Types.DOUBLE).noSize();
		
		typeNames.put(Types.BINARY, 254,"CHAR($l) FOR BIT DATA").type(Types.BINARY);
		typeNames.put(Types.BINARY, 1024 * 1024 * 16, "VARCHAR($l) FOR BIT DATA").type(Types.VARBINARY);
		typeNames.put(Types.VARBINARY, "VARCHAR($l) FOR BIT DATA").type(Types.VARBINARY);
		typeNames.put(Types.LONGVARBINARY, "VARCHAR($l) FOR BIT DATA").type(Types.VARBINARY);
		
		//Derby不支持time类型精度调整，固定为8
		typeNames.put(Types.TIME, "time").size(8);
		//Derby不支持timestamp类型精度调整，固定为29(到纳秒)
		typeNames.put(Types.TIMESTAMP, "timestamp").size(29);
		
	}

	@Override
	public void init(SQLTemplates templates) {
		SQLTemplatesEx.initDefaultDDLTemplate(templates);
		
		TemplatesAccessor.setAutoIncrement(templates, " generated by default as identity");
		
		add(templates, DDLOps.COLUMN_ALLOW_NULL, "");
		add(templates, DDLOps.CHARSET, "{0}");
		add(templates, DDLOps.COLLATE, "{0}");
		add(templates, DDLOps.UNSIGNED, "{0}");
		add(templates, Basic.SELECT_VALUES, "values {0}");
		
		add(templates, OtherStatement.RENAME_COLUMN, "RENAME COLUMN {2}.{0} TO {1}");
		add(templates, OtherStatement.RENAME_INDEX, "RENAME INDEX {0} TO {1}");
		add(templates, OtherStatement.RENAME_TABLE, "RENAME TABLE {0} TO {1}");

		add(templates, ConstraintType.UNIQUE, "CONSTRAINT {1} UNIQUE {2}");
		add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_UNIQUE, "DROP UNIQUE {0}");
		
		addUnsupports(DDLOps.COMMENT_ON_COLUMN, DDLOps.COMMENT_ON_TABLE, DDLOps.UNSIGNED,DDLOps.COLLATE, AlterTableConstraintOps.ALTER_TABLE_DROP_BITMAP,
				AlterTableConstraintOps.ALTER_TABLE_DROP_KEY);
		
		add(templates, SpecialFeature.PERFER_AUTOGENERATED_KEYS, "");
	}
	
	@Override
	public SchemaReader getSchemaAccessor() {
		return schemaReader;
	}
	
	private SchemaReader schemaReader=new DerbySchemaReader();
	
	static class DerbySchemaReader implements SchemaReader{
		@Override
		public List<Constraint> getConstraints(String catalog,String schema, String table, ConnectionWrapper conn, boolean detail) {
			List<ColumnDef> columns = Collections.emptyList();
			// 先计算出列顺序
			if (detail) {
				String sql2 = "select t.* from sys.syscolumns t LEFT JOIN sys.systables tab ON t.referenceid = tab.tableid where tab.tablename=?";
				List<Object> params2 = Collections.singletonList(StringUtils.isEmpty(table) ? "%" : table);
				columns = conn.query(new SQLBindings(sql2, params2), rs -> {
					ColumnDef c = new ColumnDef();
					c.setColumnName(rs.getString("COLUMNNAME"));
					c.setColumnDef(rs.getString("COLUMNDEFAULT"));// TODO 引用COLUMNDEFAULTID
					c.setDataType(rs.getString("COLUMNDATATYPE"));
					c.setOrdinal(rs.getInt("COLUMNNUMBER"));
					return c;
				});
			}
			// 开始计算约束
			String sql = "SELECT con.*, keys.*, cong.conglomeratename as INDEXQUALIFIER, cong.isindex, cong.descriptor, tab.tablename, scm.schemaname, ck.checkdefinition AS CHECK_CLAUSE, ck.referencedcolumns FROM sys.sysconstraints con LEFT JOIN sys.systables tab ON con.TABLEID = tab.tableid LEFT JOIN sys.sysschemas scm ON con.schemaid = scm.schemaid LEFT JOIN sys.syschecks ck ON con.constraintid = ck.constraintid LEFT JOIN sys.syskeys keys ON con.constraintid = keys.constraintid LEFT JOIN sys.sysconglomerates cong ON cong.conglomerateid = keys.conglomerateid WHERE tab.tablename =? AND scm.schemaname like ?";
			List<Object> params = Arrays.asList(StringUtils.isEmpty(table) ? "%" : table,
					StringUtils.isEmpty(schema) ? "%" : schema);
			final List<ColumnDef> finalColumns = columns;
			List<Constraint> result = conn.query(new SQLBindings(sql, params), rs -> {
				Constraint c = new Constraint();
				c.setCatalog("");
				c.setSchema(rs.getString("SCHEMANAME"));
				c.setTableName(rs.getString("TABLENAME"));
				c.setName(rs.getString("CONSTRAINTNAME"));
				c.setConstraintType(ConstraintType.parseName(rs.getString("TYPE")));
				c.setDeferrable(!"E".equals(rs.getString("STATE")));
				c.setInitiallyDeferred("e".equals(rs.getString("STATE")));
				String check=rs.getString("CHECK_CLAUSE");
				check = StringUtils.removeBucket(check);
				
				c.setCheckClause(DDLExpressions.wrapCheckExpression(check));
				c.setIndexQualifier(rs.getString("INDEXQUALIFIER"));
				Object obj = rs.getObject("DESCRIPTOR");
				if (obj != null) {
					c.setComment(obj.toString());
					try {
						Field field = obj.getClass().getDeclaredField("baseColumnPositions");
						field.setAccessible(true);
						int[] columnPosition = (int[]) field.get(obj);
						if (finalColumns.isEmpty()) {
							c.setColumnNames(
									ArrayUtils.stream(columnPosition).map(String::valueOf).collect(Collectors.toList()));
						} else {
							c.setColumnNames(ArrayUtils.stream(columnPosition).map(e -> {
								for (ColumnDef column : finalColumns) {
									if (column.getOrdinal() == e.intValue()) {
										return column.getColumnName();
									}
								}
								throw Exceptions.illegalArgument("Table {} columnIndex= {} not exist", table, e);
							}).collect(Collectors.toList()));
						}
					} catch (Exception e) {
						throw Exceptions.toRuntime(e);
					}
				}
				return c;
			});
			return result;
		}
	}

	@Override
	public String translateDefault(String columnDef, int type, int size, int digits){
		if(columnDef==null || columnDef.length()==0) {
			return null;
		}
		//AUTOINCREMENT: start 1 increment 1
		if(columnDef.startsWith("AUTOINCREMENT:")||columnDef.startsWith("GENERATED_BY_DEFAULT")) {
			return null;
		}
		return columnDef;
	}

	@Override
	public LetterCase getDefaultLetterCase() {
		return LetterCase.UPPER;
	}
}
