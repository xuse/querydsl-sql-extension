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
 * IBM DB2 dialect extension.
 * <p>
 * DB2-specific characteristics:
 * <ul>
 *   <li>Uses GENERATED ALWAYS/BY DEFAULT AS IDENTITY for auto-increment</li>
 *   <li>VARCHAR max 32672 bytes; beyond that use CLOB</li>
 *   <li>VARBINARY max 32672 bytes; beyond that use BLOB</li>
 *   <li>DECIMAL(p,s) for precise numerics (max precision 31)</li>
 *   <li>DOUBLE instead of FLOAT for double-precision</li>
 *   <li>TIMESTAMP with fractional seconds precision 0-12 (default 6)</li>
 *   <li>Supports COMMENT ON TABLE/COLUMN as independent statements</li>
 *   <li>Default identifier case is UPPER</li>
 *   <li>Uses SCHEMA_ONLY namespace policy</li>
 *   <li>ALTER TABLE supports only one operation per statement</li>
 *   <li>No UNSIGNED, CHARSET, COLLATE in DDL</li>
 *   <li>No FULLTEXT, SPATIAL, BITMAP, HASH index types</li>
 * </ul>
 *
 * @author Joey
 */
public class DB2TemplatesEx extends DefaultSQLTemplatesEx {

	public DB2TemplatesEx(SQLTemplates template) {
		super(template);

		// DB2 numeric types
		typeNames.put(Types.BIT, "smallint").type(Types.SMALLINT).noSize();
		typeNames.put(Types.BOOLEAN, "smallint").type(Types.SMALLINT).noSize();
		typeNames.put(Types.TINYINT, "smallint").type(Types.SMALLINT).noSize();
		typeNames.put(Types.FLOAT, "double").type(Types.DOUBLE).noSize();
		typeNames.put(Types.DOUBLE, "double").noSize();
		typeNames.put(Types.DECIMAL, "decimal($p,$s)");
		typeNames.put(Types.NUMERIC, "decimal($p,$s)").type(Types.DECIMAL);

		// DB2 character types
		typeNames.put(Types.CHAR, "char($l)");
		typeNames.put(Types.CHAR, 254, "char($l)");
		typeNames.put(Types.VARCHAR, "varchar($l)");
		typeNames.put(Types.VARCHAR, 32672, "varchar($l)");
		typeNames.put(Types.VARCHAR, Integer.MAX_VALUE, "clob").type(Types.CLOB).noSize();
		typeNames.put(Types.LONGVARCHAR, "clob").type(Types.CLOB).noSize();

		// DB2 binary types
		typeNames.put(Types.BINARY, "char($l) for bit data");
		typeNames.put(Types.BINARY, 254, "char($l) for bit data");
		typeNames.put(Types.VARBINARY, "varchar($l) for bit data");
		typeNames.put(Types.VARBINARY, 32672, "varchar($l) for bit data");
		typeNames.put(Types.VARBINARY, Integer.MAX_VALUE, "blob").type(Types.BLOB).noSize();
		typeNames.put(Types.LONGVARBINARY, "blob").type(Types.BLOB).noSize();

		// DB2 LOB types
		typeNames.put(Types.CLOB, "clob").noSize();
		typeNames.put(Types.BLOB, "blob").noSize();

		// DB2 date/time types
		typeNames.put(Types.DATE, "date").noSize();
		typeNames.put(Types.TIME, "time").noSize();
		// DB2 TIMESTAMP default precision is 6
		typeNames.put(Types.TIMESTAMP, "timestamp(0)").size(0);
		typeNames.put(Types.TIMESTAMP, 6, "timestamp($l)");
		typeNames.put(Types.TIMESTAMP, 12, "timestamp($l)");
		typeNames.put(Types.TIMESTAMP, 1024, "timestamp($l)").size(12);
	}

	@Override
	public void init(SQLTemplates templates) {
		SQLTemplatesEx.initDefaultDDLTemplate(templates);

		add(templates, DDLOps.COLUMN_ALLOW_NULL, "");
		add(templates, DDLOps.CHARSET, "{0}");
		add(templates, DDLOps.COLLATE, "{0}");
		add(templates, DDLOps.UNSIGNED, "{0}");
		add(templates, Basic.SELECT_VALUES, "values {0}");

		// DB2 uses independent COMMENT statements
		add(templates, DDLOps.COMMENT_ON_COLUMN, "COMMENT ON COLUMN {0} IS {1}");
		add(templates, DDLOps.COMMENT_ON_TABLE, "COMMENT ON TABLE {0} IS {1}");

		add(templates, SpecialFeature.INDEPENDENT_COMMENT_STATEMENT, "");

		// DB2 ALTER COLUMN syntax
		add(templates, AlterTableOps.ALTER_COLUMN, "ALTER COLUMN {0} {1}");
		add(templates, AlterTableOps.RENAME_COLUMN, "RENAME COLUMN {0} TO {1}");

		add(templates, ConstraintTypeDef.UNIQUE, "CONSTRAINT {1} UNIQUE {2}");
		add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_UNIQUE, "DROP UNIQUE {0}");

		addUnsupports(
				DDLOps.UNSIGNED,
				DDLOps.CHARSET,
				DDLOps.COLLATE,
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
		// DB2 identity columns
		if (columnDef.contains("IDENTITY") || columnDef.contains("GENERATED")) {
			return null;
		}
		return columnDef.trim();
	}

	@Override
	public SizeParser getColumnSizeParser(int jdbcType) {
		switch (jdbcType) {
		case Types.TIMESTAMP:
			return SizeParser.TIME_DIGIT_AS_SIZE;
		default:
			return SizeParser.DEFAULT;
		}
	}
}
