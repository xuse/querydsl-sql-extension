package com.github.xuse.querydsl.sql.dialect;

import java.sql.Types;

import com.github.xuse.querydsl.sql.dbmeta.InformationSchemaReader;
import com.github.xuse.querydsl.sql.dbmeta.SchemaReader;
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
 * HSQLDB (HyperSQL) dialect extension.
 * <p>
 * HSQLDB is a SQL:2016 compliant database. Key characteristics:
 * <ul>
 *   <li>Supports IDENTITY columns for auto-increment</li>
 *   <li>Supports information_schema for metadata queries</li>
 *   <li>Default identifier case is UPPER</li>
 *   <li>Uses SCHEMA_ONLY namespace policy</li>
 *   <li>Supports CHECK constraints and independent COMMENT statements (via ALTER)</li>
 *   <li>No UNSIGNED, CHARSET, COLLATE support</li>
 *   <li>No FULLTEXT, SPATIAL, BITMAP index types</li>
 * </ul>
 * 
 * @author Joey
 */
public class HSQLDBTemplatesEx extends DefaultSQLTemplatesEx {

	private final SchemaReader schemaReader = new InformationSchemaReader(
			InformationSchemaReader.HAS_CHECK_CONSTRAINTS | InformationSchemaReader.FILTER_NOT_NULL_CHECK);

	public HSQLDBTemplatesEx(SQLTemplates template) {
		super(template);

		// HSQLDB type mappings
		// Boolean is native
		typeNames.put(Types.BOOLEAN, "boolean").noSize();
		typeNames.put(Types.BIT, "boolean").type(Types.BOOLEAN).noSize();

		// No TINYINT in older versions, use SMALLINT
		typeNames.put(Types.TINYINT, "tinyint").noSize();
		typeNames.put(Types.FLOAT, "double").type(Types.DOUBLE).noSize();

		// HSQLDB supports CLOB and BLOB natively
		typeNames.put(Types.CLOB, "clob").noSize();
		typeNames.put(Types.BLOB, "blob").noSize();
		typeNames.put(Types.LONGVARCHAR, "clob").type(Types.CLOB).noSize();
		typeNames.put(Types.LONGVARBINARY, "blob").type(Types.BLOB).noSize();

		// HSQLDB timestamp precision: default is 6 (microseconds)
		typeNames.put(Types.TIMESTAMP, "timestamp(0)").size(0);
		typeNames.put(Types.TIMESTAMP, 6, "timestamp($l)");
		typeNames.put(Types.TIMESTAMP, 1024, "timestamp($l)").size(6);

		typeNames.put(Types.TIME, "time(0)").size(0);
		typeNames.put(Types.TIME, 6, "time($l)");
		typeNames.put(Types.TIME, 1024, "time($l)").size(6);
	}

	@Override
	public void init(SQLTemplates templates) {
		SQLTemplatesEx.initDefaultDDLTemplate(templates);

		add(templates, DDLOps.COLUMN_ALLOW_NULL, "");
		add(templates, DDLOps.CHARSET, "{0}");
		add(templates, DDLOps.COLLATE, "{0}");
		add(templates, DDLOps.UNSIGNED, "{0}");
		add(templates, Basic.SELECT_VALUES, "values {0}");

		// HSQLDB uses standard SQL COMMENT syntax (not inline)
		add(templates, DDLOps.COMMENT_ON_COLUMN, "COMMENT ON COLUMN {0} IS {1}");
		add(templates, DDLOps.COMMENT_ON_TABLE, "COMMENT ON TABLE {0} IS {1}");

		add(templates, AlterTableOps.RENAME_COLUMN, "ALTER COLUMN {0} RENAME TO {1}");
		add(templates, ConstraintTypeDef.UNIQUE, "CONSTRAINT {1} UNIQUE {2}");
		add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_UNIQUE, "DROP CONSTRAINT {0}");

		add(templates, SpecialFeature.INDEPENDENT_COMMENT_STATEMENT, "");

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
	public SchemaReader getSchemaAccessor() {
		return schemaReader;
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
		// HSQLDB identity columns
		if (columnDef.startsWith("GENERATED")) {
			return null;
		}
		return columnDef;
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
