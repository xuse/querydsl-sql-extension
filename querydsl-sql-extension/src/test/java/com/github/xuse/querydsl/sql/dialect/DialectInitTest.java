package com.github.xuse.querydsl.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

	@Test
	void testSizeParserMySQLTimestamp() {
		// MySQL TIMESTAMP: size=26 means 6 fractional digits (26 - 20 = 6)
		SizeParser parser = SizeParser.MYSQL_TIMESTAMP;
		assertEquals(6, parser.size(26, 0));
		// size=19 means 0 fractional digits (19 - 19 = 0)
		assertEquals(0, parser.size(19, 0));
		// size=22 means 2 fractional digits (22 - 20 = 2)
		assertEquals(2, parser.size(22, 0));
	}

	@Test
	void testSizeParserMySQLTime() {
		// MySQL TIME: size=15 means 6 fractional digits (15 - 9 = 6)
		SizeParser parser = SizeParser.MYSQL_TIME;
		assertEquals(6, parser.size(15, 0));
		// size=8 means 0 fractional digits (8 - 8 = 0)
		assertEquals(0, parser.size(8, 0));
	}

	@Test
	void testSizeParserTimeDigitAsSize() {
		// TIME_DIGIT_AS_SIZE: returns digits as size
		SizeParser parser = SizeParser.TIME_DIGIT_AS_SIZE;
		assertEquals(6, parser.size(0, 6));
		assertEquals(3, parser.size(100, 3));
	}

	// --- PrivilegeDetector instances ---

	@Test
	void testMySQLPrivilegeDetectorInstance() {
		MySQLWithJSONTemplates mysql = new MySQLWithJSONTemplates();
		PrivilegeDetector detector = mysql.getPrivilegeDetector();
		assertNotNull(detector);
		assertTrue(detector instanceof MySQLPrivilegeDetector);
	}

	@Test
	void testPostgreSQLPrivilegeDetectorInstance() {
		PostgreSQLTemplatesEx pg = new PostgreSQLTemplatesEx(PostgreSQLTemplates.DEFAULT);
		PrivilegeDetector detector = pg.getPrivilegeDetector();
		assertNotNull(detector);
		assertTrue(detector instanceof PostgreSQLPrivilegeDetector);
	}

	@Test
	void testH2PrivilegeDetectorInstance() {
		H2TemplatesEx h2 = new H2TemplatesEx(H2Templates.DEFAULT);
		PrivilegeDetector detector = h2.getPrivilegeDetector();
		assertNotNull(detector);
		assertTrue(detector instanceof H2PrivilegeDetector);
	}

	@Test
	void testDerbyPrivilegeDetectorInstance() {
		DerbySQLTemplatesEx derby = new DerbySQLTemplatesEx(DerbyTemplates.DEFAULT);
		PrivilegeDetector detector = derby.getPrivilegeDetector();
		assertNotNull(detector);
		assertTrue(detector instanceof DerbyPrivilegeDetector);
	}

	@Test
	void testDefaultPrivilegeDetectorIsFallback() {
		DefaultSQLTemplatesEx def = new DefaultSQLTemplatesEx(SQLTemplates.DEFAULT);
		PrivilegeDetector detector = def.getPrivilegeDetector();
		assertNotNull(detector);
		// Default uses SimpleDetector as fallback
		assertTrue(detector instanceof SimpleDetector);
	}

	// --- SchemaPolicy edge cases ---

	@Test
	void testSchemaPolicyCatalogOnly() {
		SchemaPolicy policy = SchemaPolicy.CATALOG_ONLY;
		assertEquals("mydb", policy.asCatalog("mydb"));
		assertNull(policy.asSchema("mydb"));
		assertEquals("mydb", policy.toNamespace("mydb", null));
	}

	@Test
	void testSchemaPolicySchemaOnly() {
		SchemaPolicy policy = SchemaPolicy.SCHEMA_ONLY;
		assertNull(policy.asCatalog("public"));
		assertEquals("public", policy.asSchema("public"));
		assertEquals("public", policy.toNamespace(null, "public"));
	}

	@Test
	void testSchemaPolicyCatalogAndSchema() {
		SchemaPolicy policy = SchemaPolicy.CATALOG_AND_SCHEMA;
		assertEquals("cat", policy.asCatalog("cat.schema"));
		assertEquals("schema", policy.asSchema("cat.schema"));
		assertEquals("cat.schema", policy.toNamespace("cat", "schema"));
	}

	@Test
	void testSchemaPolicyNullHandling() {
		// CATALOG_ONLY with null
		assertNull(SchemaPolicy.CATALOG_ONLY.asSchema(null));
		// SCHEMA_ONLY with null
		assertNull(SchemaPolicy.SCHEMA_ONLY.asCatalog(null));
		// CATALOG_AND_SCHEMA with null
		assertNull(SchemaPolicy.CATALOG_AND_SCHEMA.asCatalog(null));
		assertNull(SchemaPolicy.CATALOG_AND_SCHEMA.asSchema(null));
	}

	// --- MySQL Builder ---

	@Test
	void testMySQLBuilder() {
		MySQLWithJSONTemplates mysql = (MySQLWithJSONTemplates) MySQLWithJSONTemplates.builder()
				.supportsCheck()
				.build();
		assertNotNull(mysql);
		// With supportsCheck, CHECK constraint should be supported
		assertFalse(mysql.notSupports(ConstraintTypeDef.CHECK));
	}

	@Test
	void testMySQLBuilderDefaultBehavior() {
		MySQLWithJSONTemplates mysql = (MySQLWithJSONTemplates) MySQLWithJSONTemplates.builder().build();
		assertNotNull(mysql);
		assertTrue(mysql.isBatchToBulkInDefault());
		// Without supportsCheck, CHECK constraint should NOT be supported
		assertTrue(mysql.notSupports(ConstraintTypeDef.CHECK));
	}

	// --- MySQL specific features ---

	@Test
	void testMySQLGetIfExists() {
		MySQLWithJSONTemplates mysql = new MySQLWithJSONTemplates();
		assertEquals("IF EXISTS ", mysql.getIfExists());
	}

	@Test
	void testMySQLSchemaPolicy() {
		MySQLWithJSONTemplates mysql = new MySQLWithJSONTemplates();
		assertEquals(SchemaPolicy.CATALOG_ONLY, mysql.getSchemaPolicy());
	}

	@Test
	void testMySQLLetterCase() {
		MySQLWithJSONTemplates mysql = new MySQLWithJSONTemplates();
		assertEquals(com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase.LOWER, mysql.getDefaultLetterCase());
	}

	// --- Derby specific features ---

	@Test
	void testDerbyTranslateDefault() {
		DerbySQLTemplatesEx derby = new DerbySQLTemplatesEx(DerbyTemplates.DEFAULT);
		// Normal default value
		assertEquals("'hello'", derby.translateDefault("'hello'", Types.VARCHAR, 100, 0));
		// AUTOINCREMENT should return null
		assertNull(derby.translateDefault("AUTOINCREMENT: start 1 increment 1", Types.INTEGER, 0, 0));
		// GENERATED_BY_DEFAULT should return null
		assertNull(derby.translateDefault("GENERATED_BY_DEFAULT", Types.INTEGER, 0, 0));
		// Empty string should return null
		assertNull(derby.translateDefault("", Types.VARCHAR, 100, 0));
		// Null should return null
		assertNull(derby.translateDefault(null, Types.VARCHAR, 100, 0));
	}

	// --- PostgreSQL specific features ---

	@Test
	void testPostgreSQLSchemaPolicy() {
		PostgreSQLTemplatesEx pg = new PostgreSQLTemplatesEx(PostgreSQLTemplates.DEFAULT);
		// PostgreSQL uses SCHEMA_ONLY by default
		assertEquals(SchemaPolicy.SCHEMA_ONLY, pg.getSchemaPolicy());
	}

	@Test
	void testPostgreSQLSizeParser() {
		PostgreSQLTemplatesEx pg = new PostgreSQLTemplatesEx(PostgreSQLTemplates.DEFAULT);
		// TIMESTAMP uses TIME_DIGIT_AS_SIZE
		SizeParser tsParser = pg.getColumnSizeParser(Types.TIMESTAMP);
		assertNotNull(tsParser);
		assertEquals(6, tsParser.size(0, 6));
		// TIME uses TIME_DIGIT_AS_SIZE
		SizeParser timeParser = pg.getColumnSizeParser(Types.TIME);
		assertNotNull(timeParser);
		assertEquals(3, timeParser.size(0, 3));
		// Other types use DEFAULT
		SizeParser defaultParser = pg.getColumnSizeParser(Types.VARCHAR);
		assertEquals(SizeParser.DEFAULT, defaultParser);
	}

	// --- H2 specific features ---

	@Test
	void testH2SizeParser() {
		H2TemplatesEx h2 = new H2TemplatesEx(H2Templates.DEFAULT);
		// TIMESTAMP uses TIME_DIGIT_AS_SIZE
		SizeParser tsParser = h2.getColumnSizeParser(Types.TIMESTAMP);
		assertNotNull(tsParser);
		assertEquals(6, tsParser.size(0, 6));
		// TIME uses TIME_DIGIT_AS_SIZE
		SizeParser timeParser = h2.getColumnSizeParser(Types.TIME);
		assertNotNull(timeParser);
		// Other types use DEFAULT
		SizeParser defaultParser = h2.getColumnSizeParser(Types.VARCHAR);
		assertEquals(SizeParser.DEFAULT, defaultParser);
	}

	@Test
	void testH2TranslateDefault() {
		H2TemplatesEx h2 = new H2TemplatesEx(H2Templates.DEFAULT);
		// H2 returns columnDef as-is
		assertEquals("'hello'", h2.translateDefault("'hello'", Types.VARCHAR, 255, 0));
		assertEquals("0", h2.translateDefault("0", Types.INTEGER, 0, 0));
	}

	// --- SpecialFeature enum ---

	@Test
	void testSpecialFeatureEnum() {
		// Verify all enum values exist and getType returns null
		for (SpecialFeature feature : SpecialFeature.values()) {
			assertNotNull(feature.name());
			assertNull(feature.getType());
		}
		// Verify specific features
		assertNotNull(SpecialFeature.MULTI_COLUMNS_IN_ALTER_TABLE);
		assertNotNull(SpecialFeature.INDEPENDENT_COMMENT_STATEMENT);
		assertNotNull(SpecialFeature.INDEPENDENT_PARTITION_CREATION);
		assertNotNull(SpecialFeature.PARTITION_KEY_MUST_IN_PRIMARY);
		assertNotNull(SpecialFeature.NO_KEYS_ON_PARTITION_TABLE);
		assertNotNull(SpecialFeature.PREFER_AUTOGENERATED_KEYS);
	}

	// --- Privilege enum ---

	@Test
	void testPrivilegeEnum() {
		Privilege[] values = Privilege.values();
		assertTrue(values.length > 0);
		assertNotNull(Privilege.CREATE);
		assertNotNull(Privilege.DROP);
		assertNotNull(Privilege.ALTER);
		assertNotNull(Privilege.INDEX);
		assertNotNull(Privilege.REFERENCES);
	}

	// --- DbType ---

	@Test
	void testDbTypeFind() {
		assertEquals(DbType.mysql, DbType.find("mysql"));
		assertEquals(DbType.postgresql, DbType.find("postgresql"));
		assertEquals(DbType.h2, DbType.find("h2"));
		assertEquals(DbType.derby, DbType.find("derby"));
		assertEquals(DbType.oracle, DbType.find("oracle"));
		assertEquals(DbType.other, DbType.find("unknown_db_xyz"));
	}

	@Test
	void testDbTypeOfAlias() {
		// gbase8s has aliases "gbasedbt", "gbasedbt_sqli"
		assertEquals(DbType.gbase8s, DbType.ofAlias("gbasedbt"));
		assertEquals(DbType.gbase8s, DbType.ofAlias("gbasedbt_sqli"));
		// Non-existent alias
		assertNull(DbType.ofAlias("nonexistent"));
	}

	@Test
	void testDbTypeExtractDbNameFromURL() {
		assertEquals("mysql", DbType.extractDbNameFromURL("jdbc:mysql://localhost:3306/mydb"));
		assertEquals("postgresql", DbType.extractDbNameFromURL("jdbc:postgresql://localhost/test"));
		assertEquals("h2", DbType.extractDbNameFromURL("jdbc:h2:mem:test"));
		assertEquals("derby", DbType.extractDbNameFromURL("jdbc:derby:memory:testdb;create=true"));
		// Non-JDBC URL
		assertEquals("something", DbType.extractDbNameFromURL("something"));
	}

	@Test
	void testDbTypeTemplates() {
		// Verify supported databases can create templates
		assertNotNull(DbType.mysql.templates());
		assertNotNull(DbType.postgresql.templates());
		assertNotNull(DbType.h2.templates());
		assertNotNull(DbType.derby.templates());
		assertNotNull(DbType.oracle.templates());
	}
}
