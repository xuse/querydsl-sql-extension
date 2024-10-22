package com.github.xuse.querydsl.sql.dbmeta;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.sql.ddl.ConnectionWrapper;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.util.StringUtils;
import com.github.xuse.querydsl.util.collection.CollectionUtils;
import com.querydsl.sql.SQLBindings;

/**
 * MySQL和PG等逐步遵循infomation_schema，提供表结构数据查询。此类为该种实现
 *
 * 常用SQL语句包括 -- 寻找合适的系统视图 select * from information_schema."tables" where
 * table_schema ='information_schema' and table_name like '%column%'
 * 
 * -- 这个仅列出约束的字段 select * from information_schema."key_column_usage" where
 * table_schema ='public' and table_name like 'aaa'
 * 
 * -- 这个会列出约束(含check）中用到的字段，关注check表达式即可。 select * from
 * information_schema."constraint_column_usage" where table_schema ='public' and
 * table_name like 'aaa'
 * 
 * -- 列出约束 SELECT a.*,b.check_clause FROM information_schema.table_constraints a
 * left join information_schema.check_constraints b on a.constraint_catalog
 * =b.constraint_catalog and a.constraint_schema =b.constraint_schema and
 * a.constraint_name =b.constraint_name WHERE table_name='aaa' AND table_schema
 * = 'public'
 * 
 * @author Joey
 *
 */
public class InformationSchemaReader implements SchemaReader {
	/**
	 * 数据库支持CHECK的情况下，有check_clause这一列存储该表达式。
	 */
	public static final int HAS_CHECK_CONSTRAINTS = 1;
	/**
	 * 数据库列NOT NULL也作为约束存储在系统表中，指定该特性可以过滤掉这部分数据
	 */
	public static final int FILTER_NOT_NULL_CHECK = 2;
	/**
	 * CHECK表达式两边的括号需要移除掉
	 */
	public static final int REMOVE_BUCKET_FOR_CHECK = 4;
	
	public static final SchemaReader DEFAULT = new InformationSchemaReader(0);
	
	private final int features;
	
	public InformationSchemaReader(int features){
		this.features=features;
	}

	@Override
	public List<Constraint> getConstraints(String catalog, String schema, String tableName, ConnectionWrapper conn,
			boolean detail) {
		schema = mergeSchema(catalog,schema);
		
		if (StringUtils.isEmpty(schema)) {
			schema = "%";
		}
		// 只会得到UNIQUE，普通的KEY不会在这个表返回（纯索引）
		String sqlStr;
		boolean hasCheck = has(HAS_CHECK_CONSTRAINTS); 
		if(hasCheck) {
			sqlStr="SELECT a.*,b.check_clause FROM information_schema.table_constraints a "
					+ "left join information_schema.check_constraints b on a.constraint_catalog =b.constraint_catalog and a.constraint_schema =b.constraint_schema and a.constraint_name =b.constraint_name "
					+ "WHERE table_name LIKE ? AND table_schema LIKE ?";
		}else {
			sqlStr="SELECT * FROM information_schema.table_constraints WHERE table_name=? AND table_schema LIKE ?";
		}
		SQLBindings sql = new SQLBindings(
				sqlStr,
				Arrays.asList(tableName, schema));
		List<Constraint> constraints = conn.query(sql, rs -> {
			Constraint c = new Constraint();
			c.setName(rs.getString("CONSTRAINT_NAME"));
			c.setTableName(rs.getString("TABLE_NAME"));
			c.setTableSchema(rs.getString("TABLE_SCHEMA"));
			ConstraintType type = ConstraintType.valueOf(rs.getString("CONSTRAINT_TYPE").replace(' ', '_'));
			c.setConstraintType(type);
			if(hasCheck) {
				String check=rs.getString("check_clause");
				if(check!=null) {
					c.setCheckClause(DDLExpressions.wrapCheckExpression(processCheckClause(check)));	
				}
			}
			return c;
		});
		if (!detail) {
			return constraints;
		}
		// 尝试获取字段填入
		sql = new SQLBindings(
				"SELECT * FROM information_schema.key_column_usage WHERE table_name=? AND constraint_schema LIKE ?",
				Arrays.asList(tableName, schema));
		List<KeyColumn> keyColumns = conn.query(sql, rs -> {
			KeyColumn c = new KeyColumn();
			c.setKeyName(rs.getString("CONSTRAINT_NAME"));
			c.setTableCat(rs.getString("TABLE_CATALOG"));
			c.setTableSchema(rs.getString("TABLE_SCHEMA"));
			c.setTableName(rs.getString("TABLE_NAME"));
			c.setColumnName(rs.getString("COLUMN_NAME"));
			c.setSeq(rs.getInt("ORDINAL_POSITION"));
			return c;
		});
		Map<String, List<KeyColumn>> map = CollectionUtils.bucket(keyColumns, KeyColumn::getKeyName);
		for (Constraint c : constraints) {
			String name = c.getName();
			List<KeyColumn> columns = map.get(name);
			if (columns == null) {
				continue;
			}
			columns.sort(Comparator.comparingInt(a -> a.seq));
			c.setColumnNames(columns.stream().map(KeyColumn::getColumnName).collect(Collectors.toList()));
		}
		return constraints;
	}

	protected String processCheckClause(String check) {
		boolean filterNotNull=has(FILTER_NOT_NULL_CHECK);
		if(filterNotNull && check.endsWith("IS NOT NULL")) {
			return null;
		}
		check=StringUtils.removeBucket(check);
		if(has(REMOVE_BUCKET_FOR_CHECK)) {
			check=StringUtils.removeBucket(check);	
		}
		return check;
	}

	protected String mergeSchema(String catalog, String schema) {
		if(StringUtils.isNotEmpty(catalog)) {
			return catalog;
		}
		return schema;
	}
	
	protected boolean has(int check) {
		return (this.features & check) == check; 
	}
	
	protected boolean hasAll(int check) {
		return (this.features & check) == check; 
	}
	
	protected boolean hasAny(int check) {
		return (this.features & check) > 0; 
	}
}
