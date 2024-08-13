package com.github.xuse.querydsl.sql.dialect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.KeyColumn;
import com.github.xuse.querydsl.sql.dbmeta.ObjectType;
import com.github.xuse.querydsl.sql.dbmeta.PartitionInfo;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.github.xuse.querydsl.sql.ddl.ConnectionWrapper;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;
import com.github.xuse.querydsl.sql.ddl.DDLOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableConstraintOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTablePartitionOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.Basic;
import com.github.xuse.querydsl.sql.ddl.DDLOps.CreateStatement;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionDefineOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;
import com.github.xuse.querydsl.sql.expression.JsonOps;
import com.github.xuse.querydsl.util.StringUtils;
import com.github.xuse.querydsl.util.collection.CollectionUtils;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase;

/**
 * 扩展的MySQL方言，支持了MySQL的JSON操作
 * 
 * @author Joey
 *
 */
public class MySQLWithJSONTemplates extends MySQLTemplates implements SQLTemplatesEx {
	private final TypeNames typeNames = TypeNames.generateDefault();

	private final boolean usingInfoSchema;
	
	protected final Set<Operator> unsupports=new HashSet<>();
	
    public static MySQLTemplateBuilderEx builder() {
        return new MySQLTemplateBuilderEx();
    }
    
    
    public static class MySQLTemplateBuilderEx extends Builder{
    	private boolean supportsCheck;
    	
    	public Builder supportsCheck() {
    		supportsCheck=true;
    		return this;
    	}
    	
        @Override
        protected SQLTemplates build(char escape, boolean quote) {
            return new MySQLWithJSONTemplates(escape, quote,false,supportsCheck);
        }
    }
    
	
	@Override
	public boolean checkPermission(SQLQueryFactory factory, String... action) {
		// TODO Auto-generated method stub
		return SQLTemplatesEx.super.checkPermission(factory, action);
	}

	public MySQLWithJSONTemplates() {
		this('\\',false,false,false);
	}
	
	public MySQLWithJSONTemplates(char escape, boolean quote,boolean usingInfoSchema,boolean supportsCheckConstraint) {
		super(escape, quote);
		super.setPrintSchema(false);
		this.usingInfoSchema = usingInfoSchema;
		
		SQLTemplatesEx.initDefaultDDLTemplate(this);
		initJsonFunctions();
		initPartitionOps();
		
		add(ConstraintType.FULLTEXT, "FULLTEXT KEY {1} {2}");
		
		
		
		add(Basic.TIME_EQ, "UNIX_TIMESTAMP({0}) = UNIX_TIMESTAMP({1})");
		add(DDLOps.COMMENT_ON_COLUMN, "{0} COMMENT {1}");
		add(DDLOps.COMMENT_ON_TABLE, "{0} COMMENT {1}");
		add(AlterTableOps.CHANGE_COLUMN, "CHANGE {0} {1},ALGORITHM=INPLACE, LOCK=NONE");
		//add(AlterTableOps.COMMENT_ON_TABLE, "COMMENT = {0}");
		
		add(AlterTableConstraintOps.ALTER_TABLE_DROP_KEY, "DROP KEY {0},ALGORITHM=INPLACE, LOCK=NONE");
		add(AlterTableConstraintOps.ALTER_TABLE_DROP_UNIQUE, "DROP KEY {0},ALGORITHM=INPLACE, LOCK=NONE");
		
		add(AlterTableOps.ALTER_TABLE_ADD, "ADD {0}, ALGORITHM=INPLACE, LOCK=SHARED");
		add(CreateStatement.CREATE_INDEX, "CREATE INDEX {1} ON {0} {2}, ALGORITHM=INPLACE, LOCK=NONE");
		add(CreateStatement.CREATE_FULLTEXT, "CREATE FULLTEXT INDEX {1} ON {0} {2}, ALGORITHM=INPLACE, LOCK=SHARED");
		add(CreateStatement.CREATE_UNIQUE, "CREATE UNIQUE INDEX {1} ON {0} {2}, ALGORITHM=INPLACE, LOCK=NONE");
		add(CreateStatement.CREATE_HASH, "CREATE INDEX {1} ON {0} USING HASH, ALGORITHM=INPLACE, LOCK=NONE");
		add(CreateStatement.CREATE_SPATIAL, "CREATE SPATIAL INDEX {1} ON {0} {2}, ALGORITHM=INPLACE, LOCK=NONE");
		add(SpecialFeature.PARTITION_SUPPORT,"");
		add(SpecialFeature.MULTI_COLUMNS_IN_ALTER_TABLE, "");
		//MySQ:L 8.0.16之后的版本才支持 CONSTRAINT {1} CHECK {2} [ENFORCED]语法
		if(!supportsCheckConstraint) {
			unsupports.add(ConstraintType.CHECK);
		}
		unsupports.add(CreateStatement.CREATE_BITMAP);
		
		typeNames.put(Types.BOOLEAN, "bit(1)").type(Types.BIT);
		typeNames.put(Types.FLOAT, "float").type(Types.REAL);

		// MySQL最大的秒以下时间精度只能保留到6位。
		typeNames.put(Types.TIMESTAMP, "datetime").size(0);
		typeNames.put(Types.TIMESTAMP, 6, "datetime($l)");
		typeNames.put(Types.TIMESTAMP, 1024, "datetime($l)").size(6);

		typeNames.put(Types.TIME, 6, "time($l)");
		typeNames.put(Types.TIME, 1024, "time($l)").size(6);

		typeNames.put(Types.CHAR, 255, "char($l)");
		typeNames.put(Types.BINARY, 255, "binary($l)");
		typeNames.put(Types.CHAR, 65535, "text").type(Types.CLOB).noSize();
		typeNames.put(Types.BINARY, 65535, "blob").type(Types.LONGVARBINARY).noSize();

		typeNames.put(Types.VARCHAR, 16383, "varchar($l)");
		typeNames.put(Types.VARBINARY, 16383, "varbinary($l)");

		typeNames.put(Types.VARCHAR, 65535, "text").type(Types.CLOB).noSize();
		typeNames.put(Types.VARBINARY, 65535, "blob").type(Types.BLOB).noSize();

		typeNames.put(Types.LONGVARCHAR, 65535, "text").type(Types.CLOB).noSize();
		typeNames.put(Types.LONGVARBINARY, 65535, "blob").type(Types.LONGVARBINARY).noSize();
		
		typeNames.put(Types.VARCHAR, 1024 * 1024 * 16, "mediumtext").type(Types.CLOB).noSize();
		typeNames.put(Types.VARBINARY, 1024 * 1024 * 16, "mediumblob").type(Types.LONGVARBINARY).noSize();
		
		typeNames.put(Types.LONGVARCHAR, 1024 * 1024 * 16, "mediumtext").type(Types.CLOB).noSize();
		typeNames.put(Types.LONGVARBINARY, 1024 * 1024 * 16, "mediumblob").type(Types.LONGVARBINARY).noSize();
		// LOBS
		typeNames.put(Types.CLOB, "mediumtext").noSize();
		typeNames.put(Types.BLOB, "mediumblob").noSize();
		typeNames.put(Types.BLOB, 255, "tinyblob").noSize();
		typeNames.put(Types.CLOB, 255, "tinytext").noSize();
		typeNames.put(Types.BLOB, 65535, "blob").noSize();
		typeNames.put(Types.CLOB, 65535, "text").noSize();
		typeNames.put(Types.BLOB, 1024 * 1024 * 16, "mediumblob").noSize();
		typeNames.put(Types.CLOB, 1024 * 1024 * 16, "mediumtext").noSize();
		typeNames.put(Types.BLOB, 1024 * 1024 * 1024, "longblob").noSize();
		typeNames.put(Types.CLOB, 1024 * 1024 * 1024, "longtext").noSize();
	}

	private void initPartitionOps() {
		add(PartitionDefineOps.PARTITION_BY,"PARTITION BY {0}");
		
		add(PartitionMethod.KEY,"KEY({0}) PARTITIONS {1}");
		add(PartitionMethod.HASH,"HASH({0}) PARTITIONS {1}");
		add(PartitionMethod.LINEAR_HASH," LINEAR HASH({0}) PARTITIONS {1}");
		add(PartitionMethod.RANGE,"RANGE ({0}) {1}");
		add(PartitionMethod.RANGE_COLUMNS,"RANGE COLUMNS({0}) {1}");
		add(PartitionMethod.LIST,"LIST ({0}) {1}");
		add(PartitionMethod.LIST_COLUMNS,"LIST COLUMNS({0}) {1}");
		add(PartitionDefineOps.PARTITION_IN_LIST," PARTITION {0} VALUES IN ({1})");
		add(PartitionDefineOps.PARTITION_LESS_THAN,"PARTITION {0} VALUES LESS THAN ({1})");
		
		add(AlterTablePartitionOps.ADD_PARTITION," ADD PARTITION ({0})");
		add(AlterTablePartitionOps.REORGANIZE_PARTITION," REORGANIZE PARTITION {0} INTO {1}");
		
	}

	private void initJsonFunctions() {
		// 入参必须是集合，当传入为集合时，集合自带一对小括号，所以函数小括号可以省去
		add(JsonOps.JSON_ARRAY, "JSON_ARRAY{0}");
		add(JsonOps.JSON_OBJECT, "JSON_OBJECT{0}");
		add(JsonOps.JSON_QUOTE, "JSON_QUOTE({0})");

		add(JsonOps.JSON_CONTAINS, "JSON_CONTAINS({0},{1})");
		add(JsonOps.JSON_CONTAINS_UNDER_PATH, "JSON_CONTAINS({0},{1},{2})");
		//
		add(JsonOps.JSON_CONTAINS_PATH, "JSON_CONTAINS_PATH({0},{1},{2})");
		add(JsonOps.JSON_EXTRACT, "JSON_EXTRACT({0},{1})");
		add(JsonOps.JSON_KEYS, "JSON_KEYS({0},{1})");

		add(JsonOps.JSON_OVERLAPS, "JSON_OVERLAPS({0},{1})");

		// JSON_SEARCH(json_doc, one_or_all, search_str[, escape_char[, path] ...])
		add(JsonOps.JSON_SEARCH, "JSON_SEARCH({0},{1},{2},{3})");
		add(JsonOps.JSON_SEARCH_WITH_PATH, "JSON_SEARCH({0},{1},{2},{3},{4}})");
		add(JsonOps.JSON_VALUE, "JSON_VALUE({0},{1})");

		add(JsonOps.JSON_ARRAY_APPEND, "JSON_ARRAY_APPEND({0},{1})");
		add(JsonOps.JSON_ARRAY_INSERT, "JSON_ARRAY_INSERT({0},{1})");
		add(JsonOps.JSON_INSERT, "JSON_INSERT({0},{1})");
		add(JsonOps.JSON_MERGE, "JSON_MERGE({0},{1})");
		add(JsonOps.JSON_REMOVE, "JSON_REMOVE({0},{1})");
		add(JsonOps.JSON_REPLACE, "JSON_REPLACE({0},{1})");
		add(JsonOps.JSON_SET, "JSON_SET({0},{1})");

		add(JsonOps.JSON_UNQUOTE, "JSON_UNQUOTE({0})");
		add(JsonOps.JSON_DEPTH, "JSON_DEPTH({0})");
		add(JsonOps.JSON_LENGTH, "JSON_LENGTH({0},{1})");

		add(JsonOps.JSON_TYPE, "JSON_TYPE({0})");
		add(JsonOps.JSON_VALID, "JSON_VALID({0})");
		// 这个函数太复杂了，先不具体包装
		add(JsonOps.JSON_TABLE, "JSON_TABLE({0})");
		add(JsonOps.JSON_SCHEMA_VALID, "JSON_SCHEMA_VALID({0},{1})");
		add(JsonOps.JSON_SCHEMA_VALIDATION_REPORT, "JSON_SCHEMA_VALIDATION_REPORT({0},{1})");

		add(JsonOps.JSON_PRETTY, "JSON_PRETTY({0})");
		add(JsonOps.MEMBER_OF, "{0} MEMBER OF({1})");
	}

	@Override
	public String getIfExists() {
		return "IF EXISTS ";
	}

	@Override
	public ColumnDef getColumnDataType(int sqlTypes, int size, int scale) {
		return typeNames.get(sqlTypes, size, scale);
	}

	@Override
	public void init(SQLTemplates template) {
		SQLTemplatesEx.initDefaultDDLTemplate(template);
		// add(template, DDLOps.ALTER_COLUMN,"CHANGE {0} {0} {1}");
	}

	
	
	@Override
	public List<PartitionInfo> getPartitions(String schema, String tableName, ConnectionWrapper w) {
		if (StringUtils.isEmpty(schema)) {
			schema = "%";
		}
		SQLBindings sql = new SQLBindings(
				"SELECT * FROM information_schema.partitions WHERE table_name=? AND TABLE_SCHEMA LIKE ? ORDER BY PARTITION_ORDINAL_POSITION ASC",
				Arrays.asList(tableName, schema));
		List<PartitionInfo> partitions = w.query(sql, rs -> {
			PartitionInfo c = new PartitionInfo();
			c.setTableCat(rs.getString("TABLE_CATALOG"));
			c.setTableSchema(rs.getString("TABLE_SCHEMA"));
			c.setTableName(rs.getString("TABLE_NAME"));
			c.setName(rs.getString("PARTITION_NAME"));
			c.setMethod(PartitionMethod.parse(rs.getString("PARTITION_METHOD")));
			c.setCreateTime(rs.getTimestamp("CREATE_TIME"));
			c.setPartitionExpression(rs.getString("PARTITION_EXPRESSION"));
			c.setPartitionOrdinal(rs.getInt("PARTITION_ORDINAL_POSITION"));
			c.setPartitionDescription(rs.getString("PARTITION_DESCRIPTION"));
			return c;
		});
		return partitions;
	}
	
	@Override
	public List<Constraint> getConstraints(String schema, String tableName, ConnectionWrapper w, boolean detail) {
		if (StringUtils.isEmpty(schema)) {
			schema = "%";
		}
		// 只会得到UNIQUE，普通的KEY不会在这个表返回（纯索引）
		SQLBindings sql = new SQLBindings(
				"SELECT * FROM information_schema.table_constraints WHERE  table_name=? AND constraint_schema LIKE ?",
				Arrays.asList(tableName, schema));
		List<Constraint> constraints = w.query(sql, rs -> {
			Constraint c = new Constraint();
			c.setName(rs.getString("CONSTRAINT_NAME"));
			c.setTableName(rs.getString("TABLE_NAME"));
			c.setTableSchema(rs.getString("TABLE_SCHEMA"));
			ConstraintType type = ConstraintType.valueOf(rs.getString("CONSTRAINT_TYPE").replace(' ', '_'));
			c.setConstraintType(type);
			return c;
		});
		if (!detail) {
			return constraints;
		}
		// 尝试获取字段填入
		sql = new SQLBindings(
				"SELECT * FROM information_schema.key_column_usage WHERE  table_name=? AND constraint_schema LIKE ?",
				Arrays.asList(tableName, schema));
		List<KeyColumn> keyColumns = w.query(sql, rs -> {
			KeyColumn c = new KeyColumn();
			c.setKeyName(rs.getString("CONSTRAINT_NAME"));
			c.setTableCat(rs.getString("TABLE_CATALOG"));
			c.setTableSchema(rs.getString("TABLE_SCHEMA"));
			c.setTableName(rs.getString("TABLE_NAME"));
			c.setColumnName(rs.getString("COLUMN_NAME"));
			c.setSeq(rs.getInt("ORDINAL_POSITION"));
			return c;
		});
		Map<String, List<KeyColumn>> map = CollectionUtils.bucket(keyColumns, KeyColumn::getKeyName, e -> e);
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

	@Override
	public LetterCase getDefaultLetterCase() {
		return LetterCase.LOWER;
	}

	@Override
	public SchemaPolicy getSchemaPolicy() {
		return SchemaPolicy.CATALOG_ONLY;
	}

	@Override
	public boolean notSupports(Operator op) {
		return getTemplate(op)==null || unsupports.contains(op);
	}

	@Override
	public SQLTemplates getOriginal() {
		return this;
	}

	@Override
	public SizeParser getColumnSizeParser(int jdbcType) {
		switch (jdbcType) {
		case Types.TIMESTAMP:
			return SizeParser.MYSQL_TIMESTAMP;
		case Types.TIME:
			return SizeParser.MYSQL_TIME;
		default:
			return SizeParser.DEFAULT;
		}
	}

	@Override
	public List<TableInfo> fetchTables(ConnectionWrapper e, String catalog, String schema, String qMatchName,
			ObjectType type) {
		// 测试发现，正常情况下无法获得table info的comment等信息
		// &useInformationSchema=true
		if(usingInfoSchema) {
			return SQLTemplatesEx.super.fetchTables(e, catalog, schema, qMatchName, type);	
		}else {
			List<Object> params=new ArrayList<>();
			params.add(qMatchName);
			String sql="SELECT TABLE_CATALOG AS TABLE_CAT,TABLE_SCHEMA AS TABLE_SCHEM,TABLE_NAME,CASE WHEN TABLE_TYPE = 'BASE TABLE' THEN CASE WHEN TABLE_SCHEMA = 'mysql' OR TABLE_SCHEMA = 'performance_schema' THEN 'SYSTEM TABLE' ELSE 'TABLE' END WHEN TABLE_TYPE = 'TEMPORARY' THEN 'LOCAL_TEMPORARY' ELSE TABLE_TYPE END AS TABLE_TYPE, TABLE_COMMENT AS REMARKS,"
					+"ENGINE, VERSION, ROW_FORMAT, AUTO_INCREMENT, CREATE_TIME, UPDATE_TIME, TABLE_COLLATION "
					+ "FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE ?";
			if(StringUtils.isNotEmpty(catalog)&& !"%".equals(catalog)) {
				sql+=" AND TABLE_SCHEMA LIKE ?";
				params.add(catalog);
			}
			if(type!=null) {
				sql+=" HAVING TABLE_TYPE = ?";
				params.add(type.name());
			}
			SQLBindings sb=new SQLBindings(sql, params);
			return e.query(sb, this::fromRsEx);
		}
	}
	

	private TableInfo fromRsEx(ResultSet rs) throws SQLException {
		TableInfo info = new TableInfo();
		info.setCatalog(rs.getString("TABLE_CAT"));
		info.setSchema(rs.getString("TABLE_SCHEM"));
		info.setName(rs.getString("TABLE_NAME"));
		info.setType(rs.getString("TABLE_TYPE"));
		info.setRemarks(rs.getString("REMARKS"));
		info.setAttribute("ENGINE", rs.getString("ENGINE"));
		info.setAttribute("VERSION", rs.getInt("VERSION"));
		info.setAttribute("ROW_FORMAT", rs.getString("ROW_FORMAT"));
		info.setAttribute("AUTO_INCREMENT", rs.getLong("AUTO_INCREMENT"));
		info.setAttribute("CREATE_TIME", rs.getTimestamp("CREATE_TIME"));
		info.setAttribute("UPDATE_TIME", rs.getTimestamp("UPDATE_TIME"));
		info.setAttribute("COLLATE", rs.getString("TABLE_COLLATION"));
		return info;
	}

	@Override
	public PrivilegeDetector getPrivilegeDetector() {
		return new MySQLPrivilegeDetector();
	}
}
