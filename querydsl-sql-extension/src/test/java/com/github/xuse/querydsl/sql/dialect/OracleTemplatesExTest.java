package com.github.xuse.querydsl.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Types;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase;

/**
 * Unit tests for {@link OracleTemplatesEx} verifying type mappings,
 * letter case, schema policy, and translateDefault behavior.
 *
 * Requirements: 8.3
 */
@DisplayName("OracleTemplatesEx - Oracle dialect extension")
class OracleTemplatesExTest {

	private static OracleTemplatesEx oracle;

	@BeforeAll
	static void setUp() {
		oracle = new OracleTemplatesEx(com.querydsl.sql.OracleTemplates.DEFAULT);
	}

	@Nested
	@DisplayName("Type Mappings")
	class TypeMappings {

		@Test
		@DisplayName("INTEGER maps to number(10)")
		void testIntegerMapping() {
			ColumnDef def = oracle.getColumnDataType(Types.INTEGER, 0, 0);
			assertNotNull(def);
			assertEquals("number(10)", def.getDataType());
			assertEquals(Types.DECIMAL, def.getJdbcType());
		}

		@Test
		@DisplayName("VARCHAR maps to varchar2 with size")
		void testVarcharMapping() {
			ColumnDef def = oracle.getColumnDataType(Types.VARCHAR, 200, 0);
			assertNotNull(def);
			assertTrue(def.getDataType().contains("varchar2"),
					"Expected varchar2, got: " + def.getDataType());
			assertTrue(def.getDataType().contains("200"),
					"Expected size 200 in type, got: " + def.getDataType());
		}

		@Test
		@DisplayName("CLOB maps to clob")
		void testClobMapping() {
			ColumnDef def = oracle.getColumnDataType(Types.CLOB, 0, 0);
			assertNotNull(def);
			assertEquals("clob", def.getDataType());
		}

		@Test
		@DisplayName("BLOB maps to blob")
		void testBlobMapping() {
			ColumnDef def = oracle.getColumnDataType(Types.BLOB, 0, 0);
			assertNotNull(def);
			assertEquals("blob", def.getDataType());
		}

		@Test
		@DisplayName("TIMESTAMP maps to timestamp with precision")
		void testTimestampMapping() {
			ColumnDef ts0 = oracle.getColumnDataType(Types.TIMESTAMP, 0, 0);
			assertNotNull(ts0);
			assertEquals("timestamp(0)", ts0.getDataType());

			ColumnDef ts6 = oracle.getColumnDataType(Types.TIMESTAMP, 6, 0);
			assertNotNull(ts6);
			assertEquals("timestamp(6)", ts6.getDataType());
		}

		@Test
		@DisplayName("BIGINT maps to number(19)")
		void testBigintMapping() {
			ColumnDef def = oracle.getColumnDataType(Types.BIGINT, 0, 0);
			assertNotNull(def);
			assertEquals("number(19)", def.getDataType());
			assertEquals(Types.DECIMAL, def.getJdbcType());
		}

		@Test
		@DisplayName("DATE maps to date")
		void testDateMapping() {
			ColumnDef def = oracle.getColumnDataType(Types.DATE, 0, 0);
			assertNotNull(def);
			assertEquals("date", def.getDataType());
		}
	}

	@Nested
	@DisplayName("Letter Case")
	class LetterCaseTests {

		@Test
		@DisplayName("Default letter case is UPPER")
		void testDefaultLetterCaseIsUpper() {
			assertEquals(LetterCase.UPPER, oracle.getDefaultLetterCase());
		}
	}

	@Nested
	@DisplayName("Schema Policy")
	class SchemaPolicyTests {

		@Test
		@DisplayName("Schema policy is SCHEMA_ONLY")
		void testSchemaPolicyIsSchemaOnly() {
			assertEquals(SchemaPolicy.SCHEMA_ONLY, oracle.getSchemaPolicy());
		}
	}

	@Nested
	@DisplayName("translateDefault")
	class TranslateDefaultTests {

		@Test
		@DisplayName("Trims whitespace from default value")
		void testTrimsWhitespace() {
			assertEquals("SYSDATE", oracle.translateDefault("SYSDATE ", Types.TIMESTAMP, 0, 0));
			assertEquals("0", oracle.translateDefault("  0  ", Types.INTEGER, 0, 0));
			assertEquals("hello", oracle.translateDefault("\thello\n", Types.VARCHAR, 100, 0));
		}

		@Test
		@DisplayName("Returns null for null input")
		void testReturnsNullForNull() {
			assertNull(oracle.translateDefault(null, Types.VARCHAR, 100, 0));
		}

		@Test
		@DisplayName("Returns null for empty string")
		void testReturnsNullForEmpty() {
			assertNull(oracle.translateDefault("", Types.VARCHAR, 100, 0));
		}

		@Test
		@DisplayName("Returns null for whitespace-only string")
		void testReturnsNullForWhitespaceOnly() {
			assertNull(oracle.translateDefault("   ", Types.VARCHAR, 100, 0));
			assertNull(oracle.translateDefault("\t\n", Types.INTEGER, 0, 0));
		}

		@Test
		@DisplayName("Preserves non-whitespace default value")
		void testPreservesValue() {
			assertEquals("0", oracle.translateDefault("0", Types.INTEGER, 0, 0));
			assertEquals("'default_text'", oracle.translateDefault("'default_text'", Types.VARCHAR, 100, 0));
		}
	}

	@Nested
	@DisplayName("Column Size Parser")
	class ColumnSizeParserTests {

		@Test
		@DisplayName("TIMESTAMP uses TIME_DIGIT_AS_SIZE parser (digits as size)")
		void testTimestampSizeParser() {
			SizeParser parser = oracle.getColumnSizeParser(Types.TIMESTAMP);
			assertNotNull(parser);
			// TIME_DIGIT_AS_SIZE returns digits as size
			assertEquals(6, parser.size(0, 6));
			assertEquals(3, parser.size(0, 3));
			assertEquals(0, parser.size(0, 0));
		}

		@Test
		@DisplayName("Non-TIMESTAMP types use DEFAULT parser")
		void testDefaultSizeParser() {
			SizeParser parser = oracle.getColumnSizeParser(Types.VARCHAR);
			assertNotNull(parser);
			// DEFAULT parser returns size as-is
			assertEquals(100, parser.size(100, 0));
		}
	}
}
