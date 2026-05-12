package com.github.xuse.querydsl.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Types;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.ddl.ConstraintTypeDef;
import com.github.xuse.querydsl.sql.ddl.DDLOps;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.DerbyTemplates;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLTemplates;

/**
 * Tests for dialect initialization and basic operations across all supported databases.
 * Covers DerbySQLTemplatesEx, PostgreSQLTemplatesEx, H2TemplatesEx, MySQLWithJSONTemplates,
 * and DefaultSQLTemplatesEx.
 */
class DialectInitTest {

	// --- Derby ---

	@Test
	void testDerbyInit() {
		SQLTemplates base = DerbyTemplates.DEFAULT;
		DerbySQLTemplatesEx derby = new DerbySQLTemplatesEx(base);
		assertNotNull(derby.getOriginal());
		assertNotNull(derby.getSchemaAccessor());
		assertNotNull(derby.getSchemaPolicy());
	}

	@Test
	void testDerbyColumnDataType() {
		DerbySQLTemplatesEx derby = new DerbySQLTemplatesEx(DerbyTemplates.DEFAULT);
		// INTEGER
		ColumnDef intDef = derby.getColumnDataType(Types.INTEGER, 0, 0);
		assertNotNull(intDef);
		// VARCHAR
		ColumnDef varcharDef = derby.getColumnDataType(Types.VARCHAR, 128, 0);
		assertNotNull(varcharDef);
		assertTrue(varcharDef.getDataType().toLowerCase().contains("varchar"));
		// TIMESTAMP (Derby fixed size 29)
		ColumnDef tsDef = derby.getColumnDataType(Types.TIMESTAMP, 0, 0);
		assertNotNull(tsDef);
		// BINARY
		ColumnDef binDef = derby.getColumnDataType(Types.BINARY, 100, 0);
		assertNotNull(binDef);
		// TINYINT -> smallint
		ColumnDef tinyDef = derby.getColumnDataType(Types.TINYINT, 0, 0);
		assertNotNull(tinyDef);
	}

	@Test
	void testDerbyNotSupports() {
		DerbySQLTemplatesEx derby = new DerbySQLTemplatesEx(DerbyTemplates.DEFAULT);
		// Derby doesn't support some operations
		assertTrue(derby.notSupports(DDLOps.COMMENT_ON_COLUMN));
		assertTrue(derby.notSupports(DDLOps.COMMENT_ON_TABLE));
	}

	@Test
	void testDerbyLetterCase() {
		DerbySQLTemplatesEx derby = new DerbySQLTemplatesEx(DerbyTemplates.DEFAULT);
		assertNotNull(derby.getDefaultLetterCase());
	}

	@Test
	void testDerbyBatchToBulk() {
		DerbySQLTemplatesEx derby = new DerbySQLTemplatesEx(DerbyTemplates.DEFAULT);
		assertFalse(derby.isBatchToBulkInDefault());
	}

	// --- PostgreSQL ---

	@Test
	void testPostgreSQLInit() {
		SQLTemplates base = PostgreSQLTemplates.DEFAULT;
		PostgreSQLTemplatesEx pg = new PostgreSQLTemplatesEx(base);
		assertNotNull(pg.getOriginal());
		assertNotNull(pg.getSchemaAccessor());
		assertNotNull(pg.getSchemaPolicy());
	}

	@Test
	void testPostgreSQLColumnDataType() {
		PostgreSQLTemplatesEx pg = new PostgreSQLTemplatesEx(PostgreSQLTemplates.DEFAULT);
		// INTEGER
		ColumnDef intDef = pg.getColumnDataType(Types.INTEGER, 0, 0);
		assertNotNull(intDef);
		// TEXT
		ColumnDef textDef = pg.getColumnDataType(Types.CLOB, 0, 0);
		assertNotNull(textDef);
		// TIMESTAMP
		ColumnDef tsDef = pg.getColumnDataType(Types.TIMESTAMP, 6, 0);
		assertNotNull(tsDef);
		// BYTEA (default mapping for BINARY)
		ColumnDef binDef = pg.getColumnDataType(Types.BINARY, 0, 0);
		assertNotNull(binDef);
		// BOOLEAN
		ColumnDef boolDef = pg.getColumnDataType(Types.BIT, 0, 0);
		assertNotNull(boolDef);
	}

	@Test
	void testPostgreSQLWithBytea() {
		// Test alternate constructor with jdbcTypeOfBytea
		PostgreSQLTemplatesEx pg = new PostgreSQLTemplatesEx(PostgreSQLTemplates.DEFAULT, Types.LONGVARBINARY);
		assertNotNull(pg.getColumnDataType(Types.LONGVARBINARY, 0, 0));
	}

	@Test
	void testPostgreSQLNotSupports() {
		PostgreSQLTemplatesEx pg = new PostgreSQLTemplatesEx(PostgreSQLTemplates.DEFAULT);
		// PostgreSQL doesn't support UNSIGNED
		assertTrue(pg.notSupports(DDLOps.UNSIGNED));
	}

	@Test
	void testPostgreSQLLetterCase() {
		PostgreSQLTemplatesEx pg = new PostgreSQLTemplatesEx(PostgreSQLTemplates.DEFAULT);
		assertNotNull(pg.getDefaultLetterCase());
	}

	@Test
	void testPostgreSQLBatchToBulk() {
		PostgreSQLTemplatesEx pg = new PostgreSQLTemplatesEx(PostgreSQLTemplates.DEFAULT);
		assertFalse(pg.isBatchToBulkInDefault());
	}

	@Test
	void testPostgreSQLConstraintSupport() {
		PostgreSQLTemplatesEx pg = new PostgreSQLTemplatesEx(PostgreSQLTemplates.DEFAULT);
		// PostgreSQL supports PRIMARY_KEY in table definition
		assertTrue(pg.supportCreateInTableDefinition(ConstraintTypeDef.PRIMARY_KEY));
		// KEY is MySQL-specific, PostgreSQL may not support it inline
		// Just verify the method doesn't throw
		pg.supportCreateInTableDefinition(ConstraintTypeDef.KEY);
	}

	// --- H2 ---

	@Test
	void testH2Init() {
		H2TemplatesEx h2 = new H2TemplatesEx(H2Templates.DEFAULT);
		assertNotNull(h2.getOriginal());
		assertNotNull(h2.getSchemaAccessor());
	}

	@Test
	void testH2ColumnDataType() {
		H2TemplatesEx h2 = new H2TemplatesEx(H2Templates.DEFAULT);
		ColumnDef intDef = h2.getColumnDataType(Types.INTEGER, 0, 0);
		assertNotNull(intDef);
		ColumnDef varcharDef = h2.getColumnDataType(Types.VARCHAR, 255, 0);
		assertNotNull(varcharDef);
	}

	// --- MySQL ---

	@Test
	void testMySQLInit() {
		MySQLWithJSONTemplates mysql = new MySQLWithJSONTemplates();
		assertNotNull(mysql.getOriginal());
		assertNotNull(mysql.getSchemaAccessor());
		assertNotNull(mysql.getPrivilegeDetector());
	}

	@Test
	void testMySQLColumnDataType_allTypes() {
		MySQLWithJSONTemplates mysql = new MySQLWithJSONTemplates();
		// Cover various JDBC types
		int[] types = {
			Types.BIT, Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT,
			Types.FLOAT, Types.DOUBLE, Types.DECIMAL, Types.NUMERIC,
			Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR,
			Types.DATE, Types.TIME, Types.TIMESTAMP,
			Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY,
			Types.BLOB, Types.CLOB, Types.BOOLEAN
		};
		for (int type : types) {
			ColumnDef def = mysql.getColumnDataType(type, 0, 0);
			assertNotNull(def, "Should have mapping for JDBC type: " + type);
		}
	}

	@Test
	void testMySQLTimestampPrecision() {
		MySQLWithJSONTemplates mysql = new MySQLWithJSONTemplates();
		// datetime without precision
		ColumnDef ts0 = mysql.getColumnDataType(Types.TIMESTAMP, 0, 0);
		assertTrue(ts0.getDataType().equals("datetime"));
		// datetime(3)
		ColumnDef ts3 = mysql.getColumnDataType(Types.TIMESTAMP, 3, 0);
		assertTrue(ts3.getDataType().equals("datetime(3)"));
		// datetime(6) - max precision
		ColumnDef ts6 = mysql.getColumnDataType(Types.TIMESTAMP, 6, 0);
		assertTrue(ts6.getDataType().equals("datetime(6)"));
	}

	@Test
	void testMySQLBatchToBulk() {
		MySQLWithJSONTemplates mysql = new MySQLWithJSONTemplates();
		assertTrue(mysql.isBatchToBulkInDefault());
		assertTrue(mysql.isBatchToBulkSupported());
	}

	// --- DefaultSQLTemplatesEx (fallback for unknown databases) ---

	@Test
	void testDefaultTemplatesEx() {
		DefaultSQLTemplatesEx def = new DefaultSQLTemplatesEx(SQLTemplates.DEFAULT);
		assertNotNull(def.getOriginal());
		assertNotNull(def.getSchemaAccessor());
		// Default should not support batch-to-bulk
		assertFalse(def.isBatchToBulkInDefault());
	}

	@Test
	void testDefaultColumnDataType() {
		DefaultSQLTemplatesEx def = new DefaultSQLTemplatesEx(SQLTemplates.DEFAULT);
		ColumnDef intDef = def.getColumnDataType(Types.INTEGER, 0, 0);
		assertNotNull(intDef);
		ColumnDef varcharDef = def.getColumnDataType(Types.VARCHAR, 100, 0);
		assertNotNull(varcharDef);
	}

	// --- SchemaPolicy ---

	@Test
	void testSchemaPolicy() {
		// Test all enum values
		SchemaPolicy[] policies = SchemaPolicy.values();
		assertTrue(policies.length > 0);
		for (SchemaPolicy policy : policies) {
			assertNotNull(policy.name());
		}
	}

	// --- SizeParser ---

	@Test
	void testSizeParserDefault() {
		SizeParser parser = SizeParser.DEFAULT;
		assertNotNull(parser);
	}
}
