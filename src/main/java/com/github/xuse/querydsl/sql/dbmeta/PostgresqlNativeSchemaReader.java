package com.github.xuse.querydsl.sql.dbmeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.github.xuse.querydsl.sql.ddl.ConnectionWrapper;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.util.StringUtils;

public class PostgresqlNativeSchemaReader implements SchemaReader {
	public List<Constraint> getConstraints(String catalog, String schema, String tablename, ConnectionWrapper conn,
			boolean detail) {
		List<Constraint> constraints = InfomationSchemaReader.DEFAULT.getConstraints(catalog, schema, tablename, conn,
				detail);

//			String sql="select\r\n"
//					+ "		current_database() as constraint_catalog,"
//					+ "		cns.nspname as constraint_schema,"
//					+ "		con.conname as constraint_name,"
//					+ "		current_database() as table_catalog,"
//					+ "		tns.nspname as table_schema,"
//					+ "		r.relname as table_name,"
//					+ "		con.contype as constraint_type,"
//					+ "		con.condeferrable as is_deferrable,"
//					+ "		con.condeferred as initially_deferred,"
//					+ "		con.conkey as keys,"
//					+ "		(case when con.contype = 'c' then pg_get_constraintdef (con.oid) else '' end ) as check_clause,"
//					+ "		(case con.confmatchtype when 'f' then 'FULL' when 'p' then 'PARTIAL' when 's' then 'NONE' else null end) as match_option,"
//					+ "		(case con.confupdtype when 'c' then 'CASCADE' when 'n' then 'SET NULL' when 'd' then 'SET DEFAULT' when 'r' then 'RESTRICT' when 'a' then 'NO ACTION' else null end) as update_rule,"
//					+ "		(case con.confdeltype when 'c' then 'CASCADE' when 'n' then 'SET NULL' when 'd' then 'SET DEFAULT' when 'r' then 'RESTRICT' when 'a' then 'NO ACTION' else null end) as delete_rule"
//					+ "from\r\n"
//					+ "		pg_constraint con left join pg_namespace cns on con.connamespace = cns.oid left join pg_class r on con.conrelid = r.oid left join pg_namespace tns on r.relnamespace = tns.oid"
//					+ "where\r\n"
//					+ "		r.relname like ? and tns.nspname like ?";
//				tablename = StringUtils.isBlank(tablename) ? "%" : tablename.toLowerCase();
//				schema = StringUtils.isBlank(schema) ? "%" : schema.toLowerCase();
//				List<Constraint> constraints= conn.query(new SQLBindings(sql,Arrays.asList(tablename,schema)), this::convert);
//				if(detail) {
//					// 先计算出列顺序
//					if (detail) {
//						String sql2 = "select t.* from sys.syscolumns t LEFT JOIN sys.systables tab ON t.referenceid = tab.tableid where tab.tablename=?";
//						List<Object> params2 = Collections.singletonList(StringUtils.isEmpty(table) ? "%" : table);
//						columns = conn.query(new SQLBindings(sql2, params2), rs -> {
//							ColumnDef c = new ColumnDef();
//							c.setColumnName(rs.getString("COLUMNNAME"));
//							c.setColumnDef(rs.getString("COLUMNDEFAULT"));// TODO 引用COLUMNDEFAULTID
//							c.setDataType(rs.getString("COLUMNDATATYPE"));
//							c.setOrdinal(rs.getInt("COLUMNNUMBER"));
//							return c;
//						});
//					}
//					
//				}
		return constraints;
	}

	private Constraint convert(ResultSet rs) throws SQLException {
		Constraint c = new Constraint();
		c.setCatalog(rs.getString("constraint_catalog"));
		c.setSchema(rs.getString("constraint_schema"));
		c.setName(rs.getString("constraint_name"));

		c.setTableCatalog(rs.getString("table_catalog"));
		c.setTableSchema(rs.getString("table_schema"));
		c.setTableName(rs.getString("table_name"));

		c.setConstraintType(ConstraintType.parseName(rs.getString("constraint_type")));
		c.setDeferrable(rs.getBoolean("is_deferrable"));
		c.setInitiallyDeferred(rs.getBoolean("initially_deferred"));

		String check = rs.getString("check_clause");
		check = removeBucket(check);
		c.setCheckClause(DDLExpressions.wrapCheckExpression(check));

		c.setMatchType(ForeignKeyMatchType.parseName(rs.getString("match_option")));
		c.setUpdateRule(ForeignKeyAction.parseName(rs.getString("update_rule")));
		c.setDeleteRule(ForeignKeyAction.parseName(rs.getString("delete_rule")));
		return c;
	}

	private String removeBucket(String check) {
		if (StringUtils.isEmpty(check)) {
			return check;
		}
		if (check.startsWith("CHECK (") && check.charAt(check.length() - 1) == ')') {
			return check.substring(7, check.length() - 1);
		}
		return check;
	}
}
