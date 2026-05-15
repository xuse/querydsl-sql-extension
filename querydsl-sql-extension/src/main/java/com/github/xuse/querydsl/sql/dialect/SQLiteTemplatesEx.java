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
 * SQLite dialect extension.
 * <p>
 * SQLite uses dynamic typing with type affinity rules. Column types are suggestions
 * rather than strict constraints. Key characteristics:
 * <ul>
 *   <li>No ALTER COLUMN support (only ADD COLUMN)</li>
 *   <li>No COMMENT support</li>
 *   <li>No UNSIGNED, CHARSET, COLLATE in DDL</li>
 *   <li>AUTOINCREMENT only on INTEGER PRIMARY KEY</li>
 *   <li>Limited ALTER TABLE (no DROP COLUMN before 3.35.0)</li>
 *   <li>No schema concept — uses attached databases as namespace</li>
 * </ul>
 * 
 * @author Joey
 */
public class SQLiteTemplatesEx extends DefaultSQLTemplatesEx {

	public SQLiteTemplatesEx(SQLTemplates template) {
		super(template);
		// SQLite type affinity: INTEGER, TEXT, BLOB, REAL, NUMERIC
		// Boolean stored as integer 0/1
		typeNames.put(Types.BOOLEAN, "integer").type(Types.INTEGER).noSize();
		typeNames.put(Types.BIT, "integer").type(Types.INTEGER).noSize();
		typeNames.put(Types.TINYINT, "integer").type(Types.INTEGER).noSize();
		typeNames.put(Types.SMALLINT, "integer").type(Types.INTEGER).noSize();
		typeNames.put(Types.INTEGER, "integer").noSize();
		typeNames.put(Types.BIGINT, "integer").noSize();

		// Real types
		typeNames.put(Types.FLOAT, "real").type(Types.DOUBLE).noSize();
		typeNames.put(Types.DOUBLE, "real").noSize();
		typeNames.put(Types.DECIMAL, "numeric($p,$s)");
		typeNames.put(Types.NUMERIC, "numeric($p,$s)");

		// Text types — SQLite has no length limit on TEXT
		typeNames.put(Types.CHAR, "text").type(Types.VARCHAR).noSize();
		typeNames.put(Types.VARCHAR, "text").noSize();
		typeNames.put(Types.LONGVARCHAR, "text").type(Types.VARCHAR).noSize();
		typeNames.put(Types.CLOB, "text").type(Types.VARCHAR).noSize();

		// Binary types
		typeNames.put(Types.BINARY, "blob").type(Types.BLOB).noSize();
		typeNames.put(Types.VARBINARY, "blob").type(Types.BLOB).noSize();
		typeNames.put(Types.LONGVARBINARY, "blob").type(Types.BLOB).noSize();
		typeNames.put(Types.BLOB, "blob").noSize();

		// Date/Time — SQLite stores as TEXT, REAL, or INTEGER
		typeNames.put(Types.DATE, "text").type(Types.VARCHAR).noSize();
		typeNames.put(Types.TIME, "text").type(Types.VARCHAR).noSize();
		typeNames.put(Types.TIMESTAMP, "text").type(Types.VARCHAR).noSize();
	}

	@Override
	public void init(SQLTemplates templates) {
		SQLTemplatesEx.initDefaultDDLTemplate(templates);

		add(templates, DDLOps.COLUMN_ALLOW_NULL, "");
		add(templates, DDLOps.CHARSET, "{0}");
		add(templates, DDLOps.COLLATE, "{0}");
		add(templates, DDLOps.UNSIGNED, "{0}");
		add(templates, Basic.SELECT_VALUES, "select {0}");

		// SQLite does not support COMMENT
		// SQLite does not support ALTER COLUMN or DROP COLUMN (before 3.35)
		// SQLite does not support RENAME COLUMN (before 3.25)
		add(templates, ConstraintTypeDef.UNIQUE, "CONSTRAINT {1} UNIQUE {2}");

		addUnsupports(
				DDLOps.COMMENT_ON_COLUMN,
				DDLOps.COMMENT_ON_TABLE,
				DDLOps.UNSIGNED,
				DDLOps.CHARSET,
				DDLOps.COLLATE,
				AlterTableOps.ALTER_COLUMN,
				AlterTableOps.CHANGE_COLUMN,
				AlterTableOps.DROP_COLUMN,
				AlterTableConstraintOps.ALTER_TABLE_DROP_BITMAP,
				AlterTableConstraintOps.ALTER_TABLE_DROP_KEY,
				AlterTableConstraintOps.ALTER_TABLE_DROP_UNIQUE,
				AlterTableConstraintOps.ALTER_TABLE_DROP_PRIMARYKEY,
				CreateStatement.CREATE_BITMAP,
				CreateStatement.CREATE_FULLTEXT,
				CreateStatement.CREATE_SPATIAL,
				CreateStatement.CREATE_HASH
		);
	}

	@Override
	public LetterCase getDefaultLetterCase() {
		return LetterCase.LOWER;
	}

	@Override
	public SchemaPolicy getSchemaPolicy() {
		// SQLite uses catalog (attached database name) as namespace
		return SchemaPolicy.CATALOG_ONLY;
	}

	@Override
	public String translateDefault(String columnDef, int type, int size, int digits) {
		if (columnDef == null || columnDef.isEmpty()) {
			return null;
		}
		return columnDef;
	}

	@Override
	public boolean supportCreateInTableDefinition(ConstraintTypeDef type) {
		// SQLite supports PRIMARY KEY, UNIQUE, CHECK inline; no separate INDEX in CREATE TABLE
		switch (type) {
		case PRIMARY_KEY:
		case UNIQUE:
		case CHECK:
			return true;
		default:
			return false;
		}
	}
}
