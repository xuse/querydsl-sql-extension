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
 * CUBRID dialect extension.
 * <p>
 * CUBRID is an open-source relational database optimized for web applications.
 * Key characteristics:
 * <ul>
 *   <li>Uses AUTO_INCREMENT for auto-increment columns</li>
 *   <li>VARCHAR max 1,073,741,823 bytes</li>
 *   <li>Supports ENUM, SET, MULTISET, SEQUENCE collection types</li>
 *   <li>DATETIME type with millisecond precision (not TIMESTAMP)</li>
 *   <li>BIT/BIT VARYING for binary data</li>
 *   <li>Supports COMMENT on table/column (inline in DDL)</li>
 *   <li>Default identifier case is LOWER</li>
 *   <li>Uses SCHEMA_ONLY namespace policy</li>
 *   <li>Supports multiple columns in ALTER TABLE</li>
 *   <li>No UNSIGNED keyword (but supports unsigned integer types natively)</li>
 *   <li>No SPATIAL, BITMAP index types</li>
 * </ul>
 *
 * @author Joey
 */
public class CUBRIDTemplatesEx extends DefaultSQLTemplatesEx {

	public CUBRIDTemplatesEx(SQLTemplates template) {
		super(template);
		this.batchToBulk = true;

		// CUBRID numeric types
		typeNames.put(Types.BIT, "bit(1)").noSize();
		typeNames.put(Types.BOOLEAN, "bit(1)").type(Types.BIT).noSize();
		typeNames.put(Types.TINYINT, "short").type(Types.SMALLINT).noSize();
		typeNames.put(Types.SMALLINT, "short").noSize();
		typeNames.put(Types.FLOAT, "float").noSize();
		typeNames.put(Types.DOUBLE, "double").noSize();
		typeNames.put(Types.DECIMAL, "numeric($p,$s)");
		typeNames.put(Types.NUMERIC, "numeric($p,$s)");

		// CUBRID character types
		typeNames.put(Types.CHAR, "char($l)");
		typeNames.put(Types.VARCHAR, "varchar($l)");
		typeNames.put(Types.LONGVARCHAR, "string").type(Types.VARCHAR).noSize();
		typeNames.put(Types.CLOB, "clob").noSize();

		// CUBRID binary types — uses BIT VARYING
		typeNames.put(Types.BINARY, "bit varying($l)").type(Types.VARBINARY);
		typeNames.put(Types.VARBINARY, "bit varying($l)");
		typeNames.put(Types.LONGVARBINARY, "blob").type(Types.BLOB).noSize();
		typeNames.put(Types.BLOB, "blob").noSize();

		// CUBRID date/time types
		typeNames.put(Types.DATE, "date").noSize();
		typeNames.put(Types.TIME, "time").noSize();
		// CUBRID uses DATETIME (millisecond precision) rather than TIMESTAMP
		typeNames.put(Types.TIMESTAMP, "datetime").noSize();
	}

	@Override
	public void init(SQLTemplates templates) {
		SQLTemplatesEx.initDefaultDDLTemplate(templates);

		add(templates, DDLOps.COLUMN_ALLOW_NULL, "");
		add(templates, DDLOps.CHARSET, "{0}");
		add(templates, DDLOps.COLLATE, "{0}");
		add(templates, DDLOps.UNSIGNED, "{0}");
		add(templates, Basic.SELECT_VALUES, "select {0}");

		// CUBRID supports inline COMMENT
		add(templates, DDLOps.COMMENT_ON_COLUMN, "{0} COMMENT {1}");
		add(templates, DDLOps.COMMENT_ON_TABLE, "{0} COMMENT {1}");

		// CUBRID supports multiple columns in ALTER TABLE
		add(templates, SpecialFeature.MULTI_COLUMNS_IN_ALTER_TABLE, "");

		// CUBRID ALTER COLUMN syntax uses MODIFY
		add(templates, AlterTableOps.CHANGE_COLUMN, "CHANGE {0} {1}");
		add(templates, AlterTableOps.RENAME_COLUMN, "RENAME COLUMN {0} AS {1}");

		add(templates, ConstraintTypeDef.UNIQUE, "CONSTRAINT {1} UNIQUE {2}");
		add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_UNIQUE, "DROP UNIQUE INDEX {0}");
		add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_KEY, "DROP INDEX {0}");

		addUnsupports(
				DDLOps.UNSIGNED,
				DDLOps.CHARSET,
				DDLOps.COLLATE,
				AlterTableConstraintOps.ALTER_TABLE_DROP_BITMAP,
				CreateStatement.CREATE_BITMAP,
				CreateStatement.CREATE_SPATIAL
		);
	}

	@Override
	public LetterCase getDefaultLetterCase() {
		return LetterCase.LOWER;
	}

	@Override
	public SchemaPolicy getSchemaPolicy() {
		return SchemaPolicy.SCHEMA_ONLY;
	}

	@Override
	public String translateDefault(String columnDef, int type, int size, int digits) {
		if (columnDef == null || columnDef.isEmpty()) {
			return null;
		}
		// CUBRID auto_increment
		if (columnDef.contains("auto_increment") || columnDef.contains("AUTO_INCREMENT")) {
			return null;
		}
		return columnDef;
	}
}
