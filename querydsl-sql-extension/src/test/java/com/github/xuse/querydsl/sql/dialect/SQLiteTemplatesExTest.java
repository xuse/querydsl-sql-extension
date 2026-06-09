package com.github.xuse.querydsl.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Types;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.ddl.ConstraintTypeDef;
import com.github.xuse.querydsl.sql.ddl.DDLOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableConstraintOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.CreateStatement;
import com.querydsl.sql.SQLiteTemplates;
import com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase;

/**
 * Unit tests for {@link SQLiteTemplatesEx} verifying JDBC type mappings
 * to SQLite storage classes and unsupported DDL operations.
 *
 * Requirements: 8.4
 */
@DisplayName("SQLiteTemplatesEx - SQLite dialect extension")
class SQLiteTemplatesExTest {

	private static SQLiteTemplatesEx sqlite;

	@BeforeAll
	static void setUp() {
		sqlite = new SQLiteTemplatesEx(SQLiteTemplates.DEFAULT);
	}

	@Nested
	@DisplayName("Type Mappings - JDBC types to SQLite storage classes")
	class TypeMappings {

		@Test
		@DisplayName("INTEGER maps to 'integer' storage class")
		void testIntegerMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.INTEGER, 0, 0);
			assertNotNull(def);
			assertEquals("integer", def.getDataType());
		}

		@Test
		@DisplayName("BIGINT maps to 'integer' storage class")
		void testBigintMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.BIGINT, 0, 0);
			assertNotNull(def);
			assertEquals("integer", def.getDataType());
		}

		@Test
		@DisplayName("SMALLINT maps to 'integer' storage class")
		void testSmallintMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.SMALLINT, 0, 0);
			assertNotNull(def);
			assertEquals("integer", def.getDataType());
			assertEquals(Types.INTEGER, def.getJdbcType());
		}

		@Test
		@DisplayName("TINYINT maps to 'integer' storage class")
		void testTinyintMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.TINYINT, 0, 0);
			assertNotNull(def);
			assertEquals("integer", def.getDataType());
			assertEquals(Types.INTEGER, def.getJdbcType());
		}

		@Test
		@DisplayName("BOOLEAN maps to 'integer' storage class")
		void testBooleanMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.BOOLEAN, 0, 0);
			assertNotNull(def);
			assertEquals("integer", def.getDataType());
			assertEquals(Types.INTEGER, def.getJdbcType());
		}

		@Test
		@DisplayName("BIT maps to 'integer' storage class")
		void testBitMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.BIT, 0, 0);
			assertNotNull(def);
			assertEquals("integer", def.getDataType());
			assertEquals(Types.INTEGER, def.getJdbcType());
		}

		@Test
		@DisplayName("VARCHAR maps to 'text' storage class")
		void testVarcharMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.VARCHAR, 255, 0);
			assertNotNull(def);
			assertEquals("text", def.getDataType());
		}

		@Test
		@DisplayName("CHAR maps to 'text' storage class")
		void testCharMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.CHAR, 10, 0);
			assertNotNull(def);
			assertEquals("text", def.getDataType());
			assertEquals(Types.VARCHAR, def.getJdbcType());
		}

		@Test
		@DisplayName("CLOB maps to 'text' storage class")
		void testClobMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.CLOB, 0, 0);
			assertNotNull(def);
			assertEquals("text", def.getDataType());
			assertEquals(Types.VARCHAR, def.getJdbcType());
		}

		@Test
		@DisplayName("LONGVARCHAR maps to 'text' storage class")
		void testLongVarcharMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.LONGVARCHAR, 0, 0);
			assertNotNull(def);
			assertEquals("text", def.getDataType());
			assertEquals(Types.VARCHAR, def.getJdbcType());
		}

		@Test
		@DisplayName("BLOB maps to 'blob' storage class")
		void testBlobMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.BLOB, 0, 0);
			assertNotNull(def);
			assertEquals("blob", def.getDataType());
		}

		@Test
		@DisplayName("BINARY maps to 'blob' storage class")
		void testBinaryMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.BINARY, 100, 0);
			assertNotNull(def);
			assertEquals("blob", def.getDataType());
			assertEquals(Types.BLOB, def.getJdbcType());
		}

		@Test
		@DisplayName("VARBINARY maps to 'blob' storage class")
		void testVarbinaryMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.VARBINARY, 200, 0);
			assertNotNull(def);
			assertEquals("blob", def.getDataType());
			assertEquals(Types.BLOB, def.getJdbcType());
		}

		@Test
		@DisplayName("LONGVARBINARY maps to 'blob' storage class")
		void testLongVarbinaryMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.LONGVARBINARY, 0, 0);
			assertNotNull(def);
			assertEquals("blob", def.getDataType());
			assertEquals(Types.BLOB, def.getJdbcType());
		}

		@Test
		@DisplayName("DOUBLE maps to 'real' storage class")
		void testDoubleMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.DOUBLE, 0, 0);
			assertNotNull(def);
			assertEquals("real", def.getDataType());
		}

		@Test
		@DisplayName("FLOAT maps to 'real' storage class")
		void testFloatMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.FLOAT, 0, 0);
			assertNotNull(def);
			assertEquals("real", def.getDataType());
			assertEquals(Types.DOUBLE, def.getJdbcType());
		}

		@Test
		@DisplayName("TIMESTAMP maps to 'text' storage class")
		void testTimestampMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.TIMESTAMP, 0, 0);
			assertNotNull(def);
			assertEquals("text", def.getDataType());
			assertEquals(Types.VARCHAR, def.getJdbcType());
		}

		@Test
		@DisplayName("DATE maps to 'text' storage class")
		void testDateMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.DATE, 0, 0);
			assertNotNull(def);
			assertEquals("text", def.getDataType());
			assertEquals(Types.VARCHAR, def.getJdbcType());
		}

		@Test
		@DisplayName("TIME maps to 'text' storage class")
		void testTimeMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.TIME, 0, 0);
			assertNotNull(def);
			assertEquals("text", def.getDataType());
			assertEquals(Types.VARCHAR, def.getJdbcType());
		}

		@Test
		@DisplayName("DECIMAL maps to 'numeric' with precision and scale")
		void testDecimalMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.DECIMAL, 10, 2);
			assertNotNull(def);
			assertEquals("numeric(10,2)", def.getDataType());
		}

		@Test
		@DisplayName("NUMERIC maps to 'numeric' with precision and scale")
		void testNumericMapping() {
			ColumnDef def = sqlite.getColumnDataType(Types.NUMERIC, 8, 4);
			assertNotNull(def);
			assertEquals("numeric(8,4)", def.getDataType());
		}
	}

	@Nested
	@DisplayName("Unsupported DDL Operations")
	class UnsupportedOperations {

		@Test
		@DisplayName("COMMENT_ON_COLUMN is not supported")
		void testCommentOnColumnNotSupported() {
			assertTrue(sqlite.notSupports(DDLOps.COMMENT_ON_COLUMN));
		}

		@Test
		@DisplayName("COMMENT_ON_TABLE is not supported")
		void testCommentOnTableNotSupported() {
			assertTrue(sqlite.notSupports(DDLOps.COMMENT_ON_TABLE));
		}

		@Test
		@DisplayName("ALTER_COLUMN is not supported")
		void testAlterColumnNotSupported() {
			assertTrue(sqlite.notSupports(AlterTableOps.ALTER_COLUMN));
		}

		@Test
		@DisplayName("DROP_COLUMN is not supported")
		void testDropColumnNotSupported() {
			assertTrue(sqlite.notSupports(AlterTableOps.DROP_COLUMN));
		}

		@Test
		@DisplayName("CHANGE_COLUMN is not supported")
		void testChangeColumnNotSupported() {
			assertTrue(sqlite.notSupports(AlterTableOps.CHANGE_COLUMN));
		}

		@Test
		@DisplayName("UNSIGNED is not supported")
		void testUnsignedNotSupported() {
			assertTrue(sqlite.notSupports(DDLOps.UNSIGNED));
		}

		@Test
		@DisplayName("CREATE_BITMAP is not supported")
		void testCreateBitmapNotSupported() {
			assertTrue(sqlite.notSupports(CreateStatement.CREATE_BITMAP));
		}

		@Test
		@DisplayName("CREATE_FULLTEXT is not supported")
		void testCreateFulltextNotSupported() {
			assertTrue(sqlite.notSupports(CreateStatement.CREATE_FULLTEXT));
		}

		@Test
		@DisplayName("CREATE_SPATIAL is not supported")
		void testCreateSpatialNotSupported() {
			assertTrue(sqlite.notSupports(CreateStatement.CREATE_SPATIAL));
		}

		@Test
		@DisplayName("CREATE_HASH is not supported")
		void testCreateHashNotSupported() {
			assertTrue(sqlite.notSupports(CreateStatement.CREATE_HASH));
		}

		@Test
		@DisplayName("ALTER_TABLE_DROP_KEY is not supported")
		void testDropKeyNotSupported() {
			assertTrue(sqlite.notSupports(AlterTableConstraintOps.ALTER_TABLE_DROP_KEY));
		}

		@Test
		@DisplayName("ALTER_TABLE_DROP_UNIQUE is not supported")
		void testDropUniqueNotSupported() {
			assertTrue(sqlite.notSupports(AlterTableConstraintOps.ALTER_TABLE_DROP_UNIQUE));
		}

		@Test
		@DisplayName("ALTER_TABLE_DROP_PRIMARYKEY is not supported")
		void testDropPrimaryKeyNotSupported() {
			assertTrue(sqlite.notSupports(AlterTableConstraintOps.ALTER_TABLE_DROP_PRIMARYKEY));
		}

		@Test
		@DisplayName("ALTER_TABLE_DROP_BITMAP is not supported")
		void testDropBitmapNotSupported() {
			assertTrue(sqlite.notSupports(AlterTableConstraintOps.ALTER_TABLE_DROP_BITMAP));
		}
	}

	@Nested
	@DisplayName("Dialect Properties")
	class DialectProperties {

		@Test
		@DisplayName("Default letter case is LOWER")
		void testDefaultLetterCaseIsLower() {
			assertEquals(LetterCase.LOWER, sqlite.getDefaultLetterCase());
		}

		@Test
		@DisplayName("Schema policy is CATALOG_ONLY")
		void testSchemaPolicyIsCatalogOnly() {
			assertEquals(SchemaPolicy.CATALOG_ONLY, sqlite.getSchemaPolicy());
		}

		@Test
		@DisplayName("Original template is accessible")
		void testOriginalTemplateAccessible() {
			assertNotNull(sqlite.getOriginal());
		}
	}

	@Nested
	@DisplayName("translateDefault")
	class TranslateDefaultTests {

		@Test
		@DisplayName("Returns value as-is for non-empty input")
		void testReturnsValueAsIs() {
			assertEquals("0", sqlite.translateDefault("0", Types.INTEGER, 0, 0));
			assertEquals("'hello'", sqlite.translateDefault("'hello'", Types.VARCHAR, 100, 0));
			assertEquals("CURRENT_TIMESTAMP", sqlite.translateDefault("CURRENT_TIMESTAMP", Types.TIMESTAMP, 0, 0));
		}

		@Test
		@DisplayName("Returns null for null input")
		void testReturnsNullForNull() {
			assertNull(sqlite.translateDefault(null, Types.VARCHAR, 100, 0));
		}

		@Test
		@DisplayName("Returns null for empty string")
		void testReturnsNullForEmpty() {
			assertNull(sqlite.translateDefault("", Types.VARCHAR, 100, 0));
		}
	}

	@Nested
	@DisplayName("Constraint Support in CREATE TABLE")
	class ConstraintSupport {

		@Test
		@DisplayName("PRIMARY_KEY supported inline in CREATE TABLE")
		void testPrimaryKeySupported() {
			assertTrue(sqlite.supportCreateInTableDefinition(ConstraintTypeDef.PRIMARY_KEY));
		}

		@Test
		@DisplayName("UNIQUE supported inline in CREATE TABLE")
		void testUniqueSupported() {
			assertTrue(sqlite.supportCreateInTableDefinition(ConstraintTypeDef.UNIQUE));
		}

		@Test
		@DisplayName("CHECK supported inline in CREATE TABLE")
		void testCheckSupported() {
			assertTrue(sqlite.supportCreateInTableDefinition(ConstraintTypeDef.CHECK));
		}

		@Test
		@DisplayName("KEY (index) not supported inline in CREATE TABLE")
		void testKeyNotSupported() {
			assertFalse(sqlite.supportCreateInTableDefinition(ConstraintTypeDef.KEY));
		}

		@Test
		@DisplayName("FULLTEXT not supported inline in CREATE TABLE")
		void testFulltextNotSupported() {
			assertFalse(sqlite.supportCreateInTableDefinition(ConstraintTypeDef.FULLTEXT));
		}

		@Test
		@DisplayName("HASH not supported inline in CREATE TABLE")
		void testHashNotSupported() {
			assertFalse(sqlite.supportCreateInTableDefinition(ConstraintTypeDef.HASH));
		}
	}
}
