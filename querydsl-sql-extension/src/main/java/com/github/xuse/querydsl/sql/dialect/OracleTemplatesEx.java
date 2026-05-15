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
 * Oracle dialect extension.
 * <p>
 * Oracle-specific characteristics:
 * <ul>
 *   <li>Uses NUMBER(p,s) for all numeric types</li>
 *   <li>VARCHAR2 instead of VARCHAR (max 4000 bytes)</li>
 *   <li>CLOB/BLOB for large objects</li>
 *   <li>DATE includes time component (no separate TIME type)</li>
 *   <li>TIMESTAMP with fractional seconds precision (0-9, default 6)</li>
 *   <li>Supports COMMENT ON TABLE/COLUMN as independent statements</li>
 *   <li>Supports multiple columns in single ALTER TABLE</li>
 *   <li>Uses sequences for auto-increment (12c+ supports IDENTITY)</li>
 *   <li>Default identifier case is UPPER</li>
 *   <li>Uses SCHEMA_ONLY namespace policy</li>
 *   <li>Supports BITMAP indexes</li>
 *   <li>Default value expressions may contain CRLF</li>
 * </ul>
 * 
 * @author Joey
 */
public class OracleTemplatesEx extends DefaultSQLTemplatesEx {

	public OracleTemplatesEx(SQLTemplates template) {
		super(template);

		// Oracle numeric types: all map to NUMBER(p,s)
		typeNames.put(Types.BIT, "number(1)").type(Types.DECIMAL).size(1).digits(0);
		typeNames.put(Types.BOOLEAN, "number(1)").type(Types.DECIMAL).size(1).digits(0);
		typeNames.put(Types.TINYINT, "number(3)").type(Types.DECIMAL).size(3).digits(0);
		typeNames.put(Types.SMALLINT, "number(5)").type(Types.DECIMAL).size(5).digits(0);
		typeNames.put(Types.INTEGER, "number(10)").type(Types.DECIMAL).size(10).digits(0);
		typeNames.put(Types.BIGINT, "number(19)").type(Types.DECIMAL).size(19).digits(0);
		typeNames.put(Types.FLOAT, "float").type(Types.DOUBLE).noSize();
		typeNames.put(Types.DOUBLE, "float").noSize();
		typeNames.put(Types.DECIMAL, "number($p,$s)");
		typeNames.put(Types.NUMERIC, "number($p,$s)");

		// Oracle character types
		typeNames.put(Types.CHAR, "char($l)");
		typeNames.put(Types.CHAR, 2000, "char($l)");
		typeNames.put(Types.VARCHAR, "varchar2($l)");
		typeNames.put(Types.VARCHAR, 4000, "varchar2($l)");
		typeNames.put(Types.VARCHAR, Integer.MAX_VALUE, "clob").type(Types.CLOB).noSize();
		typeNames.put(Types.LONGVARCHAR, "clob").type(Types.CLOB).noSize();

		// Oracle binary types
		typeNames.put(Types.BINARY, "raw($l)");
		typeNames.put(Types.BINARY, 2000, "raw($l)");
		typeNames.put(Types.VARBINARY, "raw($l)");
		typeNames.put(Types.VARBINARY, 2000, "raw($l)");
		typeNames.put(Types.VARBINARY, Integer.MAX_VALUE, "blob").type(Types.BLOB).noSize();
		typeNames.put(Types.LONGVARBINARY, "blob").type(Types.BLOB).noSize();

		// Oracle LOB types
		typeNames.put(Types.CLOB, "clob").noSize();
		typeNames.put(Types.BLOB, "blob").noSize();

		// Oracle date/time types
		// Oracle DATE includes time (equivalent to TIMESTAMP(0))
		typeNames.put(Types.DATE, "date").noSize();
		// Oracle has no TIME type, use DATE
		typeNames.put(Types.TIME, "date").type(Types.DATE).noSize();
		// Oracle TIMESTAMP default precision is 6
		typeNames.put(Types.TIMESTAMP, "timestamp(0)").size(0);
		typeNames.put(Types.TIMESTAMP, 6, "timestamp($l)");
		typeNames.put(Types.TIMESTAMP, 1024, "timestamp($l)").size(6);
	}

	@Override
	public void init(SQLTemplates templates) {
		SQLTemplatesEx.initDefaultDDLTemplate(templates);

		add(templates, DDLOps.COLUMN_ALLOW_NULL, "");
		add(templates, DDLOps.CHARSET, "{0}");
		add(templates, DDLOps.COLLATE, "{0}");
		add(templates, DDLOps.UNSIGNED, "{0}");
		add(templates, Basic.SELECT_VALUES, "select {0} from dual");

		// Oracle uses independent COMMENT statements
		add(templates, DDLOps.COMMENT_ON_COLUMN, "COMMENT ON COLUMN {0} IS {1}");
		add(templates, DDLOps.COMMENT_ON_TABLE, "COMMENT ON TABLE {0} IS {1}");

		// Oracle ALTER TABLE supports multiple columns
		add(templates, SpecialFeature.MULTI_COLUMNS_IN_ALTER_TABLE, "");
		add(templates, SpecialFeature.INDEPENDENT_COMMENT_STATEMENT, "");
		add(templates, SpecialFeature.HAS_CRLF_IN_DEFAULT_VALUE_EXPRESSION, "");

		// Oracle uses MODIFY instead of ALTER COLUMN
		add(templates, AlterTableOps.ALTER_COLUMN, "MODIFY {0} {1}");
		add(templates, AlterTableOps.RENAME_COLUMN, "RENAME COLUMN {0} TO {1}");

		add(templates, ConstraintTypeDef.UNIQUE, "CONSTRAINT {1} UNIQUE {2}");
		add(templates, AlterTableConstraintOps.ALTER_TABLE_DROP_UNIQUE, "DROP CONSTRAINT {0}");

		addUnsupports(
				DDLOps.UNSIGNED,
				DDLOps.CHARSET,
				DDLOps.COLLATE,
				AlterTableConstraintOps.ALTER_TABLE_DROP_KEY,
				CreateStatement.CREATE_FULLTEXT,
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
		// Oracle wraps default values, trim trailing whitespace/newlines
		String trimmed = columnDef.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		return trimmed;
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
