package com.github.xuse.querydsl.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.ddl.ConstraintTypeDef;
import com.github.xuse.querydsl.sql.ddl.DDLOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.CreateStatement;
import com.querydsl.sql.HSQLDBTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.SQLiteTemplates;
import com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase;

/**
 * Integration tests for SQLite and HSQLDB dialects using embedded databases.
 * These tests verify both the dialect configuration and actual database connectivity.
 */
class EmbeddedDialectIntegrationTest {

	// ==================== SQLite Tests ====================

	@Test
	void testSQLiteInit() {
		SQLiteTemplatesEx sqlite = new SQLiteTemplatesEx(SQLiteTemplates.DEFAULT);
		assertNotNull(sqlite.getOriginal());
		assertNotNull(sqlite.getSchemaAccessor());
		assertEquals(SchemaPolicy.CATALOG_ONLY, sqlite.getSchemaPolicy());
		assertEquals(LetterCase.LOWER, sqlite.getDefaultLetterCase());
	}

	@Test
	void testSQLiteTypeMapping() {
		SQLiteTemplatesEx sqlite = new SQLiteTemplatesEx(SQLiteTemplates.DEFAULT);

		// All integer types map to "integer"
		ColumnDef intDef = sqlite.getColumnDataType(Types.INTEGER, 0, 0);
		assertEquals("integer", intDef.getDataType());

		ColumnDef bigintDef = sqlite.getColumnDataType(Types.BIGINT, 0, 0);
		assertEquals("integer", bigintDef.getDataType());

		ColumnDef boolDef = sqlite.getColumnDataType(Types.BOOLEAN, 0, 0);
		assertEquals("integer", boolDef.getDataType());

		// Text types
		ColumnDef varcharDef = sqlite.getColumnDataType(Types.VARCHAR, 255, 0);
		assertEquals("text", varcharDef.getDataType());

		ColumnDef charDef = sqlite.getColumnDataType(Types.CHAR, 10, 0);
		assertEquals("text", charDef.getDataType());

		// Binary types
		ColumnDef blobDef = sqlite.getColumnDataType(Types.BLOB, 0, 0);
		assertEquals("blob", blobDef.getDataType());

		ColumnDef binaryDef = sqlite.getColumnDataType(Types.BINARY, 100, 0);
		assertEquals("blob", binaryDef.getDataType());

		// Real types
		ColumnDef doubleDef = sqlite.getColumnDataType(Types.DOUBLE, 0, 0);
		assertEquals("real", doubleDef.getDataType());

		// Timestamp stored as text
		ColumnDef tsDef = sqlite.getColumnDataType(Types.TIMESTAMP, 0, 0);
		assertEquals("text", tsDef.getDataType());
	}

	@Test
	void testSQLiteUnsupportedOps() {
		SQLiteTemplatesEx sqlite = new SQLiteTemplatesEx(SQLiteTemplates.DEFAULT);
		assertTrue(sqlite.notSupports(DDLOps.COMMENT_ON_COLUMN));
		assertTrue(sqlite.notSupports(DDLOps.COMMENT_ON_TABLE));
		assertTrue(sqlite.notSupports(DDLOps.UNSIGNED));
		assertTrue(sqlite.notSupports(AlterTableOps.ALTER_COLUMN));
		assertTrue(sqlite.notSupports(AlterTableOps.DROP_COLUMN));
		assertTrue(sqlite.notSupports(CreateStatement.CREATE_BITMAP));
		assertTrue(sqlite.notSupports(CreateStatement.CREATE_FULLTEXT));
	}

	@Test
	void testSQLiteConstraintSupport() {
		SQLiteTemplatesEx sqlite = new SQLiteTemplatesEx(SQLiteTemplates.DEFAULT);
		assertTrue(sqlite.supportCreateInTableDefinition(ConstraintTypeDef.PRIMARY_KEY));
		assertTrue(sqlite.supportCreateInTableDefinition(ConstraintTypeDef.UNIQUE));
		assertTrue(sqlite.supportCreateInTableDefinition(ConstraintTypeDef.CHECK));
		assertFalse(sqlite.supportCreateInTableDefinition(ConstraintTypeDef.KEY));
		assertFalse(sqlite.supportCreateInTableDefinition(ConstraintTypeDef.FULLTEXT));
	}

	@Test
	void testSQLiteTranslateDefault() {
		SQLiteTemplatesEx sqlite = new SQLiteTemplatesEx(SQLiteTemplates.DEFAULT);
		assertEquals("0", sqlite.translateDefault("0", Types.INTEGER, 0, 0));
		assertEquals("'hello'", sqlite.translateDefault("'hello'", Types.VARCHAR, 100, 0));
		assertEquals(null, sqlite.translateDefault("", Types.VARCHAR, 100, 0));
		assertEquals(null, sqlite.translateDefault(null, Types.VARCHAR, 100, 0));
	}

	@Test
	void testSQLiteEmbeddedConnection() throws SQLException {
		// Test actual SQLite embedded database connectivity
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
			assertNotNull(conn);
			try (Statement stmt = conn.createStatement()) {
				stmt.execute("CREATE TABLE test_table (id INTEGER PRIMARY KEY, name TEXT NOT NULL, value REAL)");
				stmt.execute("INSERT INTO test_table VALUES (1, 'hello', 3.14)");
				java.sql.ResultSet rs = stmt.executeQuery("SELECT * FROM test_table");
				assertTrue(rs.next());
				assertEquals(1, rs.getInt("id"));
				assertEquals("hello", rs.getString("name"));
				assertEquals(3.14, rs.getDouble("value"), 0.001);
			}
		}
	}

	// ==================== HSQLDB Tests ====================

	@Test
	void testHSQLDBInit() {
		HSQLDBTemplatesEx hsqldb = new HSQLDBTemplatesEx(HSQLDBTemplates.DEFAULT);
		assertNotNull(hsqldb.getOriginal());
		assertNotNull(hsqldb.getSchemaAccessor());
		assertEquals(SchemaPolicy.SCHEMA_ONLY, hsqldb.getSchemaPolicy());
		assertEquals(LetterCase.UPPER, hsqldb.getDefaultLetterCase());
	}

	@Test
	void testHSQLDBTypeMapping() {
		HSQLDBTemplatesEx hsqldb = new HSQLDBTemplatesEx(HSQLDBTemplates.DEFAULT);

		// Boolean
		ColumnDef boolDef = hsqldb.getColumnDataType(Types.BOOLEAN, 0, 0);
		assertEquals("boolean", boolDef.getDataType());

		ColumnDef bitDef = hsqldb.getColumnDataType(Types.BIT, 0, 0);
		assertEquals("boolean", bitDef.getDataType());
		assertEquals(Types.BOOLEAN, bitDef.getJdbcType());

		// Integer types
		ColumnDef intDef = hsqldb.getColumnDataType(Types.INTEGER, 0, 0);
		assertNotNull(intDef);

		// CLOB/BLOB
		ColumnDef clobDef = hsqldb.getColumnDataType(Types.CLOB, 0, 0);
		assertEquals("clob", clobDef.getDataType());

		ColumnDef blobDef = hsqldb.getColumnDataType(Types.BLOB, 0, 0);
		assertEquals("blob", blobDef.getDataType());

		// Timestamp precision
		ColumnDef ts0 = hsqldb.getColumnDataType(Types.TIMESTAMP, 0, 0);
		assertEquals("timestamp(0)", ts0.getDataType());

		ColumnDef ts3 = hsqldb.getColumnDataType(Types.TIMESTAMP, 3, 0);
		assertEquals("timestamp(3)", ts3.getDataType());

		ColumnDef ts6 = hsqldb.getColumnDataType(Types.TIMESTAMP, 6, 0);
		assertEquals("timestamp(6)", ts6.getDataType());
	}

	@Test
	void testHSQLDBUnsupportedOps() {
		HSQLDBTemplatesEx hsqldb = new HSQLDBTemplatesEx(HSQLDBTemplates.DEFAULT);
		assertTrue(hsqldb.notSupports(DDLOps.UNSIGNED));
		assertTrue(hsqldb.notSupports(DDLOps.COLLATE));
		assertTrue(hsqldb.notSupports(CreateStatement.CREATE_BITMAP));
		assertTrue(hsqldb.notSupports(CreateStatement.CREATE_FULLTEXT));
		assertTrue(hsqldb.notSupports(CreateStatement.CREATE_SPATIAL));
	}

	@Test
	void testHSQLDBTranslateDefault() {
		HSQLDBTemplatesEx hsqldb = new HSQLDBTemplatesEx(HSQLDBTemplates.DEFAULT);
		assertEquals("'hello'", hsqldb.translateDefault("'hello'", Types.VARCHAR, 100, 0));
		assertEquals("0", hsqldb.translateDefault("0", Types.INTEGER, 0, 0));
		assertEquals(null, hsqldb.translateDefault(null, Types.VARCHAR, 100, 0));
		assertEquals(null, hsqldb.translateDefault("", Types.VARCHAR, 100, 0));
		// GENERATED identity should return null
		assertEquals(null, hsqldb.translateDefault("GENERATED BY DEFAULT AS IDENTITY", Types.INTEGER, 0, 0));
	}

	@Test
	void testHSQLDBSizeParser() {
		HSQLDBTemplatesEx hsqldb = new HSQLDBTemplatesEx(HSQLDBTemplates.DEFAULT);
		SizeParser tsParser = hsqldb.getColumnSizeParser(Types.TIMESTAMP);
		assertEquals(SizeParser.TIME_DIGIT_AS_SIZE, tsParser);
		SizeParser timeParser = hsqldb.getColumnSizeParser(Types.TIME);
		assertEquals(SizeParser.TIME_DIGIT_AS_SIZE, timeParser);
		SizeParser defaultParser = hsqldb.getColumnSizeParser(Types.VARCHAR);
		assertEquals(SizeParser.DEFAULT, defaultParser);
	}

	@Test
	void testHSQLDBEmbeddedConnection() throws SQLException {
		// Test actual HSQLDB embedded database connectivity
		try (Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "SA", "")) {
			assertNotNull(conn);
			try (Statement stmt = conn.createStatement()) {
				stmt.execute("CREATE TABLE test_table (id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, name VARCHAR(100) NOT NULL, created TIMESTAMP(3))");
				stmt.execute("INSERT INTO test_table (name, created) VALUES ('hello', CURRENT_TIMESTAMP)");
				java.sql.ResultSet rs = stmt.executeQuery("SELECT * FROM test_table");
				assertTrue(rs.next());
				assertEquals("hello", rs.getString("NAME"));
				assertNotNull(rs.getTimestamp("CREATED"));
				stmt.execute("DROP TABLE test_table");
			}
			// Shutdown HSQLDB
			try (Statement stmt = conn.createStatement()) {
				stmt.execute("SHUTDOWN");
			}
		}
	}

	// ==================== Oracle Tests (unit only, no embedded DB) ====================

	@Test
	void testOracleInit() {
		OracleTemplatesEx oracle = new OracleTemplatesEx(com.querydsl.sql.OracleTemplates.DEFAULT);
		assertNotNull(oracle.getOriginal());
		assertEquals(LetterCase.UPPER, oracle.getDefaultLetterCase());
		assertEquals(SchemaPolicy.SCHEMA_ONLY, oracle.getSchemaPolicy());
	}

	@Test
	void testOracleTypeMapping() {
		OracleTemplatesEx oracle = new OracleTemplatesEx(com.querydsl.sql.OracleTemplates.DEFAULT);

		// NUMBER types
		ColumnDef intDef = oracle.getColumnDataType(Types.INTEGER, 0, 0);
		assertTrue(intDef.getDataType().contains("number"));

		ColumnDef boolDef = oracle.getColumnDataType(Types.BOOLEAN, 0, 0);
		assertTrue(boolDef.getDataType().contains("number"));

		// VARCHAR2
		ColumnDef varcharDef = oracle.getColumnDataType(Types.VARCHAR, 100, 0);
		assertTrue(varcharDef.getDataType().contains("varchar2"));

		// CLOB
		ColumnDef clobDef = oracle.getColumnDataType(Types.CLOB, 0, 0);
		assertEquals("clob", clobDef.getDataType());

		// BLOB
		ColumnDef blobDef = oracle.getColumnDataType(Types.BLOB, 0, 0);
		assertEquals("blob", blobDef.getDataType());

		// TIMESTAMP
		ColumnDef ts0 = oracle.getColumnDataType(Types.TIMESTAMP, 0, 0);
		assertEquals("timestamp(0)", ts0.getDataType());

		ColumnDef ts6 = oracle.getColumnDataType(Types.TIMESTAMP, 6, 0);
		assertEquals("timestamp(6)", ts6.getDataType());

		// DATE (Oracle DATE includes time)
		ColumnDef dateDef = oracle.getColumnDataType(Types.DATE, 0, 0);
		assertEquals("date", dateDef.getDataType());
	}

	@Test
	void testOracleTranslateDefault() {
		OracleTemplatesEx oracle = new OracleTemplatesEx(com.querydsl.sql.OracleTemplates.DEFAULT);
		assertEquals("0", oracle.translateDefault("0", Types.INTEGER, 0, 0));
		assertEquals("SYSDATE", oracle.translateDefault("SYSDATE ", Types.TIMESTAMP, 0, 0));
		assertEquals(null, oracle.translateDefault(null, Types.VARCHAR, 100, 0));
		assertEquals(null, oracle.translateDefault("", Types.VARCHAR, 100, 0));
		assertEquals(null, oracle.translateDefault("   ", Types.VARCHAR, 100, 0));
	}

	// ==================== SQL Server Tests (unit only, no embedded DB) ====================

	@Test
	void testSQLServer2012Init() {
		SQLServer2012TemplatesEx ss = new SQLServer2012TemplatesEx(
				com.querydsl.sql.SQLServerTemplates.DEFAULT);
		assertNotNull(ss.getOriginal());
		assertEquals(LetterCase.UPPER, ss.getDefaultLetterCase());
		assertEquals(SchemaPolicy.SCHEMA_ONLY, ss.getSchemaPolicy());
	}

	@Test
	void testSQLServer2012TypeMapping() {
		SQLServer2012TemplatesEx ss = new SQLServer2012TemplatesEx(
				com.querydsl.sql.SQLServerTemplates.DEFAULT);

		// BIT
		ColumnDef bitDef = ss.getColumnDataType(Types.BIT, 0, 0);
		assertEquals("bit", bitDef.getDataType());

		// VARCHAR
		ColumnDef varcharDef = ss.getColumnDataType(Types.VARCHAR, 100, 0);
		assertTrue(varcharDef.getDataType().contains("varchar"));

		// VARCHAR(MAX)
		ColumnDef clobDef = ss.getColumnDataType(Types.CLOB, 0, 0);
		assertTrue(clobDef.getDataType().contains("varchar(max)"));

		// VARBINARY(MAX)
		ColumnDef blobDef = ss.getColumnDataType(Types.BLOB, 0, 0);
		assertTrue(blobDef.getDataType().contains("varbinary(max)"));

		// DATETIME2
		ColumnDef ts0 = ss.getColumnDataType(Types.TIMESTAMP, 0, 0);
		assertEquals("datetime2(0)", ts0.getDataType());

		ColumnDef ts7 = ss.getColumnDataType(Types.TIMESTAMP, 7, 0);
		assertEquals("datetime2(7)", ts7.getDataType());
	}

	@Test
	void testSQLServerTranslateDefault() {
		SQLServer2012TemplatesEx ss = new SQLServer2012TemplatesEx(
				com.querydsl.sql.SQLServerTemplates.DEFAULT);
		// SQL Server wraps defaults in parens
		assertEquals("0", ss.translateDefault("((0))", Types.INTEGER, 0, 0));
		assertEquals("'hello'", ss.translateDefault("(('hello'))", Types.VARCHAR, 100, 0));
		assertEquals("getdate()", ss.translateDefault("(getdate())", Types.TIMESTAMP, 0, 0));
		assertEquals(null, ss.translateDefault(null, Types.VARCHAR, 100, 0));
		assertEquals(null, ss.translateDefault("", Types.VARCHAR, 100, 0));
	}

	@Test
	void testSQLServerUnsupportedOps() {
		SQLServer2012TemplatesEx ss = new SQLServer2012TemplatesEx(
				com.querydsl.sql.SQLServerTemplates.DEFAULT);
		assertTrue(ss.notSupports(DDLOps.COMMENT_ON_COLUMN));
		assertTrue(ss.notSupports(DDLOps.COMMENT_ON_TABLE));
		assertTrue(ss.notSupports(DDLOps.UNSIGNED));
		assertTrue(ss.notSupports(CreateStatement.CREATE_BITMAP));
	}

	@Test
	void testSQLServer2005TypeMapping() {
		SQLServer2005TemplatesEx ss2005 = new SQLServer2005TemplatesEx(
				com.querydsl.sql.SQLServer2005Templates.DEFAULT);
		// 2005 uses DATETIME instead of DATETIME2
		ColumnDef tsDef = ss2005.getColumnDataType(Types.TIMESTAMP, 0, 0);
		assertEquals("datetime", tsDef.getDataType());

		// DATE maps to DATETIME in 2005
		ColumnDef dateDef = ss2005.getColumnDataType(Types.DATE, 0, 0);
		assertEquals("datetime", dateDef.getDataType());
	}

	@Test
	void testSQLServer2008Inherits2012() {
		SQLServer2008TemplatesEx ss2008 = new SQLServer2008TemplatesEx(
				com.querydsl.sql.SQLServerTemplates.DEFAULT);
		// 2008 has same type mappings as 2012
		ColumnDef tsDef = ss2008.getColumnDataType(Types.TIMESTAMP, 0, 0);
		assertEquals("datetime2(0)", tsDef.getDataType());
	}

	// ==================== DB2 Tests ====================

	@Test
	void testDB2Init() {
		DB2TemplatesEx db2 = new DB2TemplatesEx(com.querydsl.sql.DB2Templates.DEFAULT);
		assertNotNull(db2.getOriginal());
		assertEquals(LetterCase.UPPER, db2.getDefaultLetterCase());
		assertEquals(SchemaPolicy.SCHEMA_ONLY, db2.getSchemaPolicy());
	}

	@Test
	void testDB2TypeMapping() {
		DB2TemplatesEx db2 = new DB2TemplatesEx(com.querydsl.sql.DB2Templates.DEFAULT);

		// BOOLEAN -> smallint
		ColumnDef boolDef = db2.getColumnDataType(Types.BOOLEAN, 0, 0);
		assertEquals("smallint", boolDef.getDataType());
		assertEquals(Types.SMALLINT, boolDef.getJdbcType());

		// VARCHAR
		ColumnDef varcharDef = db2.getColumnDataType(Types.VARCHAR, 200, 0);
		assertTrue(varcharDef.getDataType().contains("varchar"));

		// CLOB
		ColumnDef clobDef = db2.getColumnDataType(Types.CLOB, 0, 0);
		assertEquals("clob", clobDef.getDataType());

		// BLOB
		ColumnDef blobDef = db2.getColumnDataType(Types.BLOB, 0, 0);
		assertEquals("blob", blobDef.getDataType());

		// TIMESTAMP precision
		ColumnDef ts0 = db2.getColumnDataType(Types.TIMESTAMP, 0, 0);
		assertEquals("timestamp(0)", ts0.getDataType());

		ColumnDef ts6 = db2.getColumnDataType(Types.TIMESTAMP, 6, 0);
		assertEquals("timestamp(6)", ts6.getDataType());

		// DOUBLE
		ColumnDef doubleDef = db2.getColumnDataType(Types.DOUBLE, 0, 0);
		assertEquals("double", doubleDef.getDataType());
	}

	@Test
	void testDB2UnsupportedOps() {
		DB2TemplatesEx db2 = new DB2TemplatesEx(com.querydsl.sql.DB2Templates.DEFAULT);
		assertTrue(db2.notSupports(DDLOps.UNSIGNED));
		assertTrue(db2.notSupports(CreateStatement.CREATE_BITMAP));
		assertTrue(db2.notSupports(CreateStatement.CREATE_FULLTEXT));
		assertTrue(db2.notSupports(CreateStatement.CREATE_SPATIAL));
	}

	@Test
	void testDB2TranslateDefault() {
		DB2TemplatesEx db2 = new DB2TemplatesEx(com.querydsl.sql.DB2Templates.DEFAULT);
		assertEquals("0", db2.translateDefault("0", Types.INTEGER, 0, 0));
		assertEquals(null, db2.translateDefault(null, Types.VARCHAR, 100, 0));
		assertEquals(null, db2.translateDefault("", Types.VARCHAR, 100, 0));
		assertEquals(null, db2.translateDefault("GENERATED ALWAYS AS IDENTITY", Types.INTEGER, 0, 0));
	}

	// ==================== CUBRID Tests ====================

	@Test
	void testCUBRIDInit() {
		CUBRIDTemplatesEx cubrid = new CUBRIDTemplatesEx(com.querydsl.sql.CUBRIDTemplates.DEFAULT);
		assertNotNull(cubrid.getOriginal());
		assertEquals(LetterCase.LOWER, cubrid.getDefaultLetterCase());
		assertEquals(SchemaPolicy.SCHEMA_ONLY, cubrid.getSchemaPolicy());
		assertTrue(cubrid.isBatchToBulkInDefault());
	}

	@Test
	void testCUBRIDTypeMapping() {
		CUBRIDTemplatesEx cubrid = new CUBRIDTemplatesEx(com.querydsl.sql.CUBRIDTemplates.DEFAULT);

		// SHORT for small integers
		ColumnDef tinyDef = cubrid.getColumnDataType(Types.TINYINT, 0, 0);
		assertEquals("short", tinyDef.getDataType());

		// VARCHAR
		ColumnDef varcharDef = cubrid.getColumnDataType(Types.VARCHAR, 100, 0);
		assertEquals("varchar(100)", varcharDef.getDataType());

		// DATETIME for timestamp
		ColumnDef tsDef = cubrid.getColumnDataType(Types.TIMESTAMP, 0, 0);
		assertEquals("datetime", tsDef.getDataType());

		// BLOB
		ColumnDef blobDef = cubrid.getColumnDataType(Types.BLOB, 0, 0);
		assertEquals("blob", blobDef.getDataType());

		// BIT(1) for boolean
		ColumnDef boolDef = cubrid.getColumnDataType(Types.BOOLEAN, 0, 0);
		assertEquals("bit(1)", boolDef.getDataType());
	}

	@Test
	void testCUBRIDUnsupportedOps() {
		CUBRIDTemplatesEx cubrid = new CUBRIDTemplatesEx(com.querydsl.sql.CUBRIDTemplates.DEFAULT);
		assertTrue(cubrid.notSupports(DDLOps.UNSIGNED));
		assertTrue(cubrid.notSupports(CreateStatement.CREATE_BITMAP));
		assertTrue(cubrid.notSupports(CreateStatement.CREATE_SPATIAL));
	}

	@Test
	void testCUBRIDTranslateDefault() {
		CUBRIDTemplatesEx cubrid = new CUBRIDTemplatesEx(com.querydsl.sql.CUBRIDTemplates.DEFAULT);
		assertEquals("0", cubrid.translateDefault("0", Types.INTEGER, 0, 0));
		assertEquals(null, cubrid.translateDefault(null, Types.VARCHAR, 100, 0));
		assertEquals(null, cubrid.translateDefault("", Types.VARCHAR, 100, 0));
		assertEquals(null, cubrid.translateDefault("auto_increment", Types.INTEGER, 0, 0));
	}
}
