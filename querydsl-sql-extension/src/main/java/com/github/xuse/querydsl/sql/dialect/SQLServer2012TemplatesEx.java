package com.github.xuse.querydsl.sql.dialect;

import java.sql.Types;

import com.github.xuse.querydsl.sql.ddl.ConstraintTypeDef;
import com.github.xuse.querydsl.sql.ddl.DDLOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableConstraintOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.Basic;
import com.github.xuse.querydsl.sql.ddl.DDLOps.CreateStatement;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase;

/**
 * SQL Server 2012+ dialect extension.
 * <p>
 * SQL Server characteristics:
 * <ul>
 *   <li>Uses IDENTITY for auto-increment columns</li>
 *   <li>Uses NVARCHAR/NCHAR for Unicode strings</li>
 *   <li>VARCHAR(MAX) / NVARCHAR(MAX) for large text (replaces TEXT/NTEXT)</li>
 *   <li>VARBINARY(MAX) for large binary (replaces IMAGE)</li>
 *   <li>DATETIME2 for high-precision timestamps (replaces DATETIME)</li>
 *   <li>Supports OFFSET/FETCH for pagination (2012+)</li>
 *   <li>Uses [brackets] for identifier quoting</li>
 *   <li>Default identifier case is UPPER</li>
 *   <li>Uses SCHEMA_ONLY namespace policy (dbo is default schema)</li>
 *   <li>No UNSIGNED, CHARSET support</li>
 *   <li>Supports sp_rename for renaming objects</li>
 * </ul>
 * 
 * @author Joey
 */
public class SQLServer2012TemplatesEx extends DefaultSQLTemplatesEx {

	public SQLServer2012TemplatesEx(SQLTemplates template) {
		super(template);
		initTypeNames();
	}

	protected void initTypeNames() {
		// SQL Server numeric types
		typeNames.put(Types.BIT, "bit").noSize();
		typeNames.put(Types.BOOLEAN, "bit").type(Types.BIT).noSize();
		typeNames.put(Types.TINYINT, "tinyint").noSize();
		typeNames.put(Types.FLOAT, "float").type(Types.DOUBLE).noSize();

		// SQL Server character types
		typeNames.put(Types.CHAR, "char($l)");
		typeNames.put(Types.CHAR, 8000, "char($l)");
		typeNames.put(Types.VARCHAR, "varchar($l)");
		typeNames.put(Types.VARCHAR, 8000, "varchar($l)");
		typeNames.put(Types.VARCHAR, Integer.MAX_VALUE, "varchar(max)").type(Types.LONGVARCHAR).noSize();
		typeNames.put(Types.LONGVARCHAR, "varchar(max)").noSize();

		// SQL Server binary types
		typeNames.put(Types.BINARY, "binary($l)");
		typeNames.put(Types.BINARY, 8000, "binary($l)");
		typeNames.put(Types.VARBINARY, "varbinary($l)");
		typeNames.put(Types.VARBINARY, 8000, "varbinary($l)");
		typeNames.put(Types.VARBINARY, Integer.MAX_VALUE, "varbinary(max)").type(Types.LONGVARBINARY).noSize();
		typeNames.put(Types.LONGVARBINARY, "varbinary(max)").noSize();

		// SQL Server LOB types
		typeNames.put(Types.CLOB, "varchar(max)").type(Types.LONGVARCHAR).noSize();
		typeNames.put(Types.BLOB, "varbinary(max)").type(Types.LONGVARBINARY).noSize();

		// SQL Server date/time types
		typeNames.put(Types.DATE, "date").noSize();
		typeNames.put(Types.TIME, "time").noSize();
		typeNames.put(Types.TIME, 7, "time($l)");
		// DATETIME2 supports 0-7 fractional seconds (default 7)
		typeNames.put(Types.TIMESTAMP, "datetime2(0)").size(0);
		typeNames.put(Types.TIMESTAMP, 7, "datetime2($l)");
		typeNames.put(Types.TIMESTAMP, 1024, "datetime2($l)").size(7);
	}

	@Override
	public void init(SQLTemplates templates) {
		SQLTemplatesEx.initDefaultDDLTemplate(templates);

		add(templates, DDLOps.COLUMN_ALLOW_NULL, "");
		add(templates, DDLOps.CHARSET, "{0}");
		add(templates, DDLOps.COLLATE, "{0}");
		add(templates, DDLOps.UNSIGNED, "{0}");
		add(templates, Basic.SELECT_VALUES, "select {0}");

		// SQL Server does not support inline COMMENT in DDL
		// Comments are managed via sp_addextendedproperty
		add(templates, AlterTableOps.ALTER_COLUMN, "ALTER COLUMN {0} {1}");
		add(templates, ConstraintTypeDef.UNIQUE, "CONSTRAINT {1} UNIQUE {2}");
		add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_UNIQUE, "DROP CONSTRAINT {0}");
		add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_CHECK, "DROP CONSTRAINT {0}");

		addUnsupports(
				DDLOps.COMMENT_ON_COLUMN,
				DDLOps.COMMENT_ON_TABLE,
				DDLOps.UNSIGNED,
				DDLOps.CHARSET,
				AlterTableConstraintOps.ALTER_TABLE_DROP_BITMAP,
				AlterTableConstraintOps.ALTER_TABLE_DROP_KEY,
				CreateStatement.CREATE_BITMAP,
				CreateStatement.CREATE_FULLTEXT,
				CreateStatement.CREATE_SPATIAL,
				CreateStatement.CREATE_HASH
		);
	}

	@Override
	public LetterCase getDefaultLetterCase() {
		return LetterCase.UPPER;
	}

	@Override
	public String translateDefault(String columnDef, int type, int size, int digits) {
		if (columnDef == null || columnDef.isEmpty()) {
			return null;
		}
		// SQL Server wraps defaults in parentheses: ((0)), (('value')), (getdate())
		String result = columnDef;
		while (result.startsWith("(") && result.endsWith(")")) {
			result = result.substring(1, result.length() - 1);
		}
		if (result.isEmpty()) {
			return null;
		}
		return result;
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
}
