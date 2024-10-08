package com.github.xuse.querydsl.sql.dialect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.InfomationSchemaReader;
import com.github.xuse.querydsl.sql.dbmeta.ObjectType;
import com.github.xuse.querydsl.sql.dbmeta.SchemaReader;
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
	
	private final boolean batchToBulk;
	
	protected final Set<Operator> unsupports=new HashSet<>();
	
	private SchemaReader schemaReader=new InfomationSchemaReader(0) {
		@Override
		public List<TableInfo> fetchTables(ConnectionWrapper e, String catalog, String schema, String qMatchName,
				ObjectType type) {
			// &useInformationSchema=true获得的数据不一样，比现有驱动的要好。但是测试发现，正常情况下无法获得table info的comment等信息，所以直接访问MySQL系统表更好
			List<Object> params = new ArrayList<>();
			params.add(qMatchName);
			String sql = "SELECT TABLE_CATALOG AS TABLE_CAT,TABLE_SCHEMA AS TABLE_SCHEM,TABLE_NAME,CASE WHEN TABLE_TYPE = 'BASE TABLE' THEN CASE WHEN TABLE_SCHEMA = 'mysql' OR TABLE_SCHEMA = 'performance_schema' THEN 'SYSTEM TABLE' ELSE 'TABLE' END WHEN TABLE_TYPE = 'TEMPORARY' THEN 'LOCAL_TEMPORARY' ELSE TABLE_TYPE END AS TABLE_TYPE, TABLE_COMMENT AS REMARKS,"
					+ "ENGINE, VERSION, ROW_FORMAT, AUTO_INCREMENT, CREATE_TIME, UPDATE_TIME, TABLE_COLLATION "
					+ "FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE ?";
			if (StringUtils.isNotEmpty(catalog) && !"%".equals(catalog)) {
				sql += " AND TABLE_SCHEMA LIKE ?";
				params.add(catalog);
			}
			if (type != null) {
				sql += " HAVING TABLE_TYPE = ?";
				params.add(type.name());
			}
			SQLBindings sb = new SQLBindings(sql, params);
			return e.query(sb, this::fromRsEx);
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
	};
	
	
    public static MySQLTemplateBuilderEx builder() {
        return new MySQLTemplateBuilderEx();
    }
    
    
    public static class MySQLTemplateBuilderEx extends Builder{
    	private boolean supportsCheck;
    	private boolean useBulk = true;
    	
    	public Builder supportsCheck() {
    		supportsCheck=true;
    		return this;
    	}
    	
    	public Builder usingBatchToBulkInDefault(boolean flag) {
			useBulk = flag;
    		return this;
    	}
    	
        @Override
        protected SQLTemplates build(char escape, boolean quote) {
			return new MySQLWithJSONTemplates(escape, quote, supportsCheck,useBulk);
        }
    }
    
	
	@Override
	public boolean checkPermission(SQLQueryFactory factory, String... action) {
		// TODO Auto-generated method stub
		return SQLTemplatesEx.super.checkPermission(factory, action);
	}

	public MySQLWithJSONTemplates() {
		this('\\', false, false, true);
	}
	
	public MySQLWithJSONTemplates(char escape, boolean quote,boolean supportsCheckConstraint, boolean batchToBulk) {
		super(escape, quote);
		super.setPrintSchema(false);
		this.batchToBulk = batchToBulk;
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
		add(SpecialFeature.MULTI_COLUMNS_IN_ALTER_TABLE, "");
		add(SpecialFeature.PARTITION_KEY_MUST_IN_PRIMARY,"");
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
		add(PartitionDefineOps.VALUES_IN_LIST," PARTITION {0} VALUES IN ({2})");
		add(PartitionDefineOps.VALUES_LESS_THAN,"PARTITION {0} VALUES LESS THAN ({2})");
		
		add(AlterTablePartitionOps.ADD_PARTITION,"ALTER TABLE {1} ADD PARTITION ({0})");
		add(AlterTablePartitionOps.DROP_PARTITION,"ALTER TABLE {1} DROP PARTITION {0}");
		add(AlterTablePartitionOps.REORGANIZE_PARTITION,"ALTER TABLE {2} REORGANIZE PARTITION {0} INTO {1}");
		
		add(AlterTablePartitionOps.REMOVE_PARTITIONING,"ALTER TABLE {0} REMOVE PARTITIONING");
		add(AlterTablePartitionOps.ADD_PARTITIONING,"PARTITION BY {0}");
		
		add(AlterTablePartitionOps.COALESCE_PARTITION,"ALTER TABLE {0} COALESCE PARTITION {1}");
		add(AlterTablePartitionOps.ADD_PARTITION_COUNT,"ALTER TABLE {0} ADD PARTITION PARTITIONS {1}");
		
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
	public SchemaReader getSchemaAccessor() {
		return schemaReader;
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
	public boolean isBatchToBulkInDefault() {
		return batchToBulk;
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
	public PrivilegeDetector getPrivilegeDetector() {
		return new MySQLPrivilegeDetector();
	}
}
