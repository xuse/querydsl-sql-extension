package com.querydsl.core.types;

import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.InformationSchemaReader;
import com.github.xuse.querydsl.sql.dbmeta.SchemaReader;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.sql.ddl.DDLOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterColumnOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableConstraintOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.Basic;
import com.github.xuse.querydsl.sql.ddl.DDLOps.CreateStatement;
import com.github.xuse.querydsl.sql.ddl.DDLOps.DropStatement;
import com.github.xuse.querydsl.sql.dialect.PrivilegeDetector;
import com.github.xuse.querydsl.sql.dialect.SchemaPolicy;
import com.github.xuse.querydsl.sql.dialect.SimpleDetector;
import com.github.xuse.querydsl.sql.dialect.SizeParser;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.TypeUtils;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase;

/**
 * 用于扩充方言的对象。主要用于DDL支持。
 * 未实现此类的SQLTemplate也可以使用，但在执行DDL时会使用默认策略
 * @author Joey
 */
public interface SQLTemplatesEx {

	default String getIfExists() {
		return null;
	}
	
	/**
	 * 获得表结构的列定义
	 * @param sqlTypes sqlTypes
	 * @param size size
	 * @param scale scale
	 * @return ColumnDef (仅填充其中的以下字段)
	 * <ul>
	 * <li>{@link ColumnDef#getDataType()}</li>
	 * <li>{@link ColumnDef#getColumnSize()}</li>
	 * <li>{@link ColumnDef#getDecimalDigit()}</li>
	 * <li>{@link ColumnDef#getJdbcType()}</li>
	 * </ul>
	 */
	ColumnDef getColumnDataType(int sqlTypes, int size, int scale);

	/**
	 *  Ask for how to create a index or a constraint in current RDBMS.
	 *  @param type ConstraintType
	 *  @return true if the ConstraintType supported in a create/alter table clause;
	 */
	default boolean supportCreateInTableDefinition(ConstraintType type) {
		return true;
	}

	default void init(SQLTemplates templates) {
		initDefaultDDLTemplate(templates);
	}
	
	default SchemaReader getSchemaAccessor() {
		return InformationSchemaReader.DEFAULT;
	}

	default LetterCase getDefaultLetterCase() {
		return null;
	}
	
	default PrivilegeDetector getPrivilegeDetector() {
		return new SimpleDetector();
	}

	/*
	 * Adding the extension Operator into the default SQLTemplates.
	 */
	static void initDefaultDDLTemplate(SQLTemplates templates) {
		TypeUtils.add(templates, Basic.SELECT_VALUES, "select {0}");
		TypeUtils.add(templates, Basic.TIME_EQ, "{0} = {1}");
		TypeUtils.add(templates, AlterTableOps.ADD_COLUMN, "ADD COLUMN {0}");
		TypeUtils.add(templates, AlterTableOps.DROP_COLUMN, "DROP COLUMN {0} {1}");
		TypeUtils.add(templates, AlterTableOps.ALTER_COLUMN, "ALTER COLUMN {0} {1}");
		TypeUtils.add(templates, AlterTableOps.RENAME_KEY, "RENAME KEY {0} TO {1}");
		TypeUtils.add(templates, AlterTableOps.ALTER_TABLE_ADD, "ADD {0}");
		TypeUtils.add(templates, DDLOps.COLUMN_SPEC, "{0} {1} {2}");
		TypeUtils.add(templates, DDLOps.DATA_TYPE, "{0} {1} {2}");
		TypeUtils.add(templates, DDLOps.UNSIGNED, "{0} UNSIGNED");
		TypeUtils.add(templates, DDLOps.DEFAULT, "DEFAULT {0}");
		TypeUtils.add(templates, DDLOps.COMMENT_ON_COLUMN, "{0}");
		TypeUtils.add(templates, DDLOps.COMMENT_ON_TABLE, "{0}");
		TypeUtils.add(templates, DDLOps.CHARSET, "{0} CHARSET = {1}");
		TypeUtils.add(templates, DDLOps.COLLATE, "{0} COLLATE = {1}");
		TypeUtils.add(templates, DDLOps.COLUMN_ALLOW_NULL, "NULL");
		TypeUtils.add(templates, DDLOps.TABLE_DEFINITIONS, "{0},\n  {1}");
		TypeUtils.add(templates, DDLOps.DEF_LIST, "{0} {1}");
		TypeUtils.add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_CHECK, "DROP CHECK {0}");
		TypeUtils.add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_FOREIGNKEY, "DROP FOREIGN KEY {0}");
		TypeUtils.add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_UNIQUE, "DROP CONSTRAINT {0}");
		TypeUtils.add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_PRIMARYKEY, "DROP PRIMARY KEY");
		TypeUtils.add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_CONSTRAINT, "DROP CONSTRAINT {0}");
		TypeUtils.add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_BITMAP, "DROP INDEX {0}");
		TypeUtils.add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_KEY, "DROP INDEX {0}");
		TypeUtils.add(templates, DropStatement.DROP_TABLE, "DROP TABLE {0}");
		TypeUtils.add(templates, DropStatement.DROP_INDEX, "DROP INDEX {0}");
		// all constraint create def
		TypeUtils.add(templates, CreateStatement.CREATE_INDEX, "CREATE INDEX {1} ON {0} {2}");
		TypeUtils.add(templates, CreateStatement.CREATE_UNIQUE, "CREATE UNIQUE INDEX {1} ON {0} {2}");
		TypeUtils.add(templates, CreateStatement.CREATE_HASH, "CREATE INDEX {1} ON {0} USING HASH");
		TypeUtils.add(templates, CreateStatement.CREATE_SPATIAL, "CREATE SPATIAL INDEX {1} ON {0} {2}");
		TypeUtils.add(templates, CreateStatement.CREATE_BITMAP, "CREATE BITMAP INDEX {1} ON {0} {2}");
		// All Constraint inline def
		TypeUtils.add(templates, ConstraintType.CHECK, "CONSTRAINT {1} CHECK ({2})");
		TypeUtils.add(templates, ConstraintType.UNIQUE, "UNIQUE KEY {1} {2}");
		TypeUtils.add(templates, ConstraintType.FOREIGN_KEY, "CONSTRAINT {1} FOREIGN KEY {2}");
		TypeUtils.add(templates, ConstraintType.KEY, "KEY {1} {2}");
		TypeUtils.add(templates, ConstraintType.HASH, "KEY {1} {2} USING HASH");
		TypeUtils.add(templates, ConstraintType.PRIMARY_KEY, "PRIMARY KEY {2}");
		// all alter table columns
		TypeUtils.add(templates, AlterColumnOps.RESTART_WITH, "RESTART WITH {0}");
		TypeUtils.add(templates, AlterColumnOps.SET_INCREMENT_BY, "SET INCREMENT BY {0}");
		TypeUtils.add(templates, AlterColumnOps.SET_DEFAULT, "SET DEFAULT {0}");
		TypeUtils.add(templates, AlterColumnOps.DROP_DEFAULT, "DROP DEFAULT");
		TypeUtils.add(templates, AlterColumnOps.SET_DATATYPE, "SET DATA TYPE {0}");
		TypeUtils.add(templates, AlterColumnOps.SET_GENERATED, "SET GENERATED {0}");
		TypeUtils.add(templates, AlterColumnOps.SET_NOTNULL, "SET NOT NULL");
		TypeUtils.add(templates, AlterColumnOps.SET_NULL, "SET NULL");
	/*
		 * if you dialect supports FULLTEXT INDEX (such as MySQL), add these statment to you SQLTemplates
		 * add(templates, ConstraintType.FULLTEXT, "FULLTEXT KEY {1} {2}");Just for mysql
		 * add(templates, IndexConstraintOps.CREATE_FULLTEXT, "FULLTEXT INDEX {1} ON {0} {2}"); 
		 */
	}
	
	

	/*
	 * for dialects.
	 */
	default void add(SQLTemplates templates, Operator op, String template) {
		TypeUtils.add(templates, op, template);
	}

	/**
	 * @param op op
	 * @return 是否支持该操作符
	 */
	boolean notSupports(Operator op);

	/**
	 * @param op op
	 * @return 是否支持该操作符
	 */
	default boolean supports(Operator op) {
		return !notSupports(op);
	}

	default SchemaPolicy getSchemaPolicy() {
		return SchemaPolicy.SCHEMA_ONLY;
	}

	SQLTemplates getOriginal();

	/**
	 * 当从数据库DBMetadata中获得Column信息时，返回的size和digits含义未必与java或SQL定义中的长度一致。
	 * 比如MYSQL  timestamp(6)代表秒以下保留6位小数（精度到微秒）。但JDBC驱动返回的column length=26（26个字符）。
	 * 因此需要一个方言将长度还原为一般SQL定义中用到的长度。
	 * @param jdbcType jdbcType
	 * @return SizeParser
	 */
	default SizeParser getColumnSizeParser(int jdbcType) {
		return SizeParser.DEFAULT;
	}

	default String getDummyTable() {
		return getOriginal().getDummyTable();
	}

	default String getAutoIncrement() {
		return getOriginal().getAutoIncrement();
	}

	default boolean isWrapSelectParameters() {
		return getOriginal().isWrapSelectParameters();
	}

	default String getCreateTable() {
		return getOriginal().getCreateTable();
	}

	default boolean checkPermission(SQLQueryFactory factory,String... action) {
		return true;
	}
	
	default boolean isBatchToBulkSupported() {
		return getOriginal().isBatchToBulkSupported();
	};
	
	boolean isBatchToBulkInDefault();
	
	/**
	 * @param columnDef columnDef
	 * @param type type
	 * @param size size
	 * @param digits digits
	 * @return 从resultset中获取column_def字段
	 */
	default String translateDefault(String columnDef, int type, int size, int digits) {
		return SQLTypeUtils.serializeLiteral(columnDef, type);
	}

	/**
	 * @return 获得数据库的DEFAULT关键字。不是用在表结构定义上的那个DEFAULT VALUE.
	 */
	default Expression<?> getDefaultExpr() {
		return DDLExpressions.DEFAULT;
	}

}
