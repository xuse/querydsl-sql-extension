package com.github.xuse.querydsl.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Types;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.ddl.DDLOps;
import com.querydsl.sql.PostgreSQLTemplates;

/**
 * Unit tests for {@link PostgreSQLTemplatesEx} verifying type mappings,
 * schema policy, unsupported operations, and SizeParser behavior.
 *
 * Requirements: 8.2
 */
@DisplayName("PostgreSQLTemplatesEx - dialect verification")
class PostgreSQLTemplatesExTest {

	private static PostgreSQLTemplatesEx pg;

	@BeforeAll
	static void setUp() {
		pg = new PostgreSQLTemplatesEx(PostgreSQLTemplates.DEFAULT);
	}

	@Nested
	@DisplayName("Type Mappings")
	class TypeMappings {

		@Test
		@DisplayName("INTEGER maps to 'int' with no size")
		void testIntegerMapping() {
			ColumnDef def = pg.getColumnDataType(Types.INTEGER, 0, 0);
			assertNotNull(def);
			assertEquals("int", def.getDataType());
			assertEquals(Types.INTEGER, def.getJdbcType());
		}

		@Test
		@DisplayName("VARCHAR maps to 'varchar' with size placeholder")
		void testVarcharMapping() {
			ColumnDef def = pg.getColumnDataType(Types.VARCHAR, 255, 0);
			assertNotNull(def);
			assertEquals("varchar(255)", def.getDataType());
			assertEquals(Types.VARCHAR, def.getJdbcType());
		}

		@Test
		@DisplayName("CLOB maps to 'text' with no size")
		void testClobMapping() {
			ColumnDef def = pg.getColumnDataType(Types.CLOB, 0, 0);
			assertNotNull(def);
			assertEquals("text", def.getDataType());
		}

		@Test
		@DisplayName("TIMESTAMP without fractional seconds maps to 'timestamp(0)'")
		void testTimestampMapping() {
			ColumnDef def = pg.getColumnDataType(Types.TIMESTAMP, 0, 0);
			assertNotNull(def);
			assertEquals("timestamp(0)", def.getDataType());
		}

		@Test
		@DisplayName("TIMESTAMP with fractional seconds maps to 'timestamp(n)'")
		void testTimestampWithPrecision() {
			ColumnDef def = pg.getColumnDataType(Types.TIMESTAMP, 6, 0);
			assertNotNull(def);
			assertEquals("timestamp(6)", def.getDataType());
		}

		@Test
		@DisplayName("BINARY maps to 'bytea' with BINARY jdbc type")
		void testBinaryMapping() {
			ColumnDef def = pg.getColumnDataType(Types.BINARY, 0, 0);
			assertNotNull(def);
			assertEquals("bytea", def.getDataType());
			assertEquals(Types.BINARY, def.getJdbcType());
		}

		@Test
		@DisplayName("BLOB maps to 'bytea'")
		void testBlobMapping() {
			ColumnDef def = pg.getColumnDataType(Types.BLOB, 0, 0);
			assertNotNull(def);
			assertEquals("bytea", def.getDataType());
			assertEquals(Types.BINARY, def.getJdbcType());
		}

		@Test
		@DisplayName("BIT maps to 'boolean'")
		void testBitMapping() {
			ColumnDef def = pg.getColumnDataType(Types.BIT, 0, 0);
			assertNotNull(def);
			assertEquals("boolean", def.getDataType());
			assertEquals(Types.BIT, def.getJdbcType());
		}

		@Test
		@DisplayName("TINYINT maps to 'smallint'")
		void testTinyintMapping() {
			ColumnDef def = pg.getColumnDataType(Types.TINYINT, 0, 0);
			assertNotNull(def);
			assertEquals("smallint", def.getDataType());
			assertEquals(Types.SMALLINT, def.getJdbcType());
		}

		@Test
		@DisplayName("DOUBLE maps to 'double precision'")
		void testDoubleMapping() {
			ColumnDef def = pg.getColumnDataType(Types.DOUBLE, 0, 0);
			assertNotNull(def);
			assertEquals("double precision", def.getDataType());
		}

		@Test
		@DisplayName("NUMERIC maps to 'numeric(p,s)' with precision and scale")
		void testNumericMapping() {
			ColumnDef def = pg.getColumnDataType(Types.NUMERIC, 10, 2);
			assertNotNull(def);
			assertEquals("numeric(10, 2)", def.getDataType());
			assertEquals(Types.NUMERIC, def.getJdbcType());
		}
	}

	@Nested
	@DisplayName("Schema Policy")
	class SchemaPolicyTests {

		@Test
		@DisplayName("Schema policy returns SCHEMA_ONLY")
		void testSchemaPolicyIsSchemaOnly() {
			assertEquals(SchemaPolicy.SCHEMA_ONLY, pg.getSchemaPolicy());
		}
	}

	@Nested
	@DisplayName("Unsupported Operations")
	class UnsupportedOperations {

		@Test
		@DisplayName("UNSIGNED operation is not supported")
		void testUnsignedNotSupported() {
			assertTrue(pg.notSupports(DDLOps.UNSIGNED));
		}

		@Test
		@DisplayName("COLLATE operation is not supported")
		void testCollateNotSupported() {
			assertTrue(pg.notSupports(DDLOps.COLLATE));
		}
	}

	@Nested
	@DisplayName("SizeParser")
	class SizeParserTests {

		@Test
		@DisplayName("TIMESTAMP SizeParser returns digits as size")
		void testTimestampSizeParserReturnsDigitsAsSize() {
			SizeParser parser = pg.getColumnSizeParser(Types.TIMESTAMP);
			assertNotNull(parser);
			// digits=6 should be returned as size
			assertEquals(6, parser.size(0, 6));
			assertEquals(3, parser.size(100, 3));
			assertEquals(0, parser.size(19, 0));
		}

		@Test
		@DisplayName("TIME SizeParser returns digits as size")
		void testTimeSizeParserReturnsDigitsAsSize() {
			SizeParser parser = pg.getColumnSizeParser(Types.TIME);
			assertNotNull(parser);
			assertEquals(6, parser.size(0, 6));
			assertEquals(0, parser.size(8, 0));
		}

		@Test
		@DisplayName("Other types use DEFAULT SizeParser")
		void testOtherTypesUseDefaultParser() {
			SizeParser parser = pg.getColumnSizeParser(Types.VARCHAR);
			assertEquals(SizeParser.DEFAULT, parser);
		}
	}
}
