package com.github.xuse.querydsl.sql.ddl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.entity.QCaAsset;
import com.github.xuse.querydsl.entity.partition.QPartitionFoo1b;
import com.github.xuse.querydsl.sql.dbmeta.DriverInfo;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.github.xuse.querydsl.sql.dialect.H2TemplatesEx;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.SchemaAndTable;

/**
 * Unit tests for {@link CreateTableQuery} and DDL SQL generation.
 * Validates CREATE TABLE SQL generation with H2 dialect including
 * primary keys, indexes, default values, and IF NOT EXISTS behavior.
 */
@DisplayName("CreateTableQuery Unit Tests")
class CreateTableQueryTest {

	private static ConfigurationEx configuration;
	private static DriverInfo driverInfo;

	@BeforeAll
	static void setup() {
		configuration = new ConfigurationEx(H2Templates.DEFAULT);
		configuration.allowTableDropAndCreate();
		configuration.scanPackages("com.github.xuse.querydsl.entity");

		driverInfo = new DriverInfo();
		driverInfo.setUrl("jdbc:h2:mem:createtabletest");
	}

	@Nested
	@DisplayName("Basic CREATE TABLE SQL generation with H2 dialect")
	class BasicCreateTable {

		@Test
		@DisplayName("generates SQL starting with CREATE TABLE and containing column definitions")
		void testBasicCreateTableH2() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);
			builder.serializeTableCreate(false);
			List<String> sqls = builder.getSqls();

			assertNotNull(sqls);
			assertFalse(sqls.isEmpty(), "Should generate at least one SQL statement");

			String createSql = sqls.get(0);
			assertNotNull(createSql);
			assertTrue(createSql.toUpperCase().startsWith("CREATE TABLE"),
					"SQL should start with CREATE TABLE, got: " + createSql);
			// Should contain the table name
			assertTrue(createSql.toLowerCase().contains("ca_asset"),
					"SQL should contain table name 'ca_asset', got: " + createSql);
			// Should contain column definitions with H2-compatible types
			assertTrue(createSql.toLowerCase().contains("integer") || createSql.toLowerCase().contains("int"),
					"SQL should contain integer type for id column");
			assertTrue(createSql.toLowerCase().contains("varchar"),
					"SQL should contain varchar type for string columns");
		}

		@Test
		@DisplayName("generates SQL with multiple column definitions")
		void testMultipleColumns() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);
			builder.serializeTableCreate(false);
			List<String> sqls = builder.getSqls();

			String createSql = sqls.get(0);
			// CaAsset has columns: id, code, name(asset_name), content, created, updated, gender, ext, map
			assertTrue(createSql.toLowerCase().contains("id"),
					"SQL should contain 'id' column");
			assertTrue(createSql.toLowerCase().contains("code"),
					"SQL should contain 'code' column");
		}
	}

	@Nested
	@DisplayName("Table with primary key clause")
	class PrimaryKeyClause {

		@Test
		@DisplayName("generates SQL containing PRIMARY KEY clause")
		void testCreateTableWithPrimaryKey() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);
			builder.serializeTableCreate(false);
			List<String> sqls = builder.getSqls();

			String createSql = sqls.get(0);
			assertTrue(createSql.toUpperCase().contains("PRIMARY KEY"),
					"SQL should contain PRIMARY KEY clause, got: " + createSql);
		}

		@Test
		@DisplayName("PRIMARY KEY clause references the designated key column")
		void testPrimaryKeyReferencesColumn() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);
			builder.serializeTableCreate(false);
			List<String> sqls = builder.getSqls();

			String createSql = sqls.get(0);
			// The primary key is on 'id' column
			// Find the PRIMARY KEY clause and verify it references 'id'
			String upper = createSql.toUpperCase();
			int pkIndex = upper.indexOf("PRIMARY KEY");
			assertTrue(pkIndex > 0, "Should contain PRIMARY KEY");
			// After PRIMARY KEY, there should be a parenthesized list containing 'id'
			String afterPk = createSql.substring(pkIndex);
			assertTrue(afterPk.toLowerCase().contains("id"),
					"PRIMARY KEY clause should reference 'id' column, got: " + afterPk);
		}
	}

	@Nested
	@DisplayName("Table with indexes (CREATE INDEX statements)")
	class IndexStatements {

		@Test
		@DisplayName("generates additional SQL statements for index creation")
		void testCreateTableWithIndexes() {
			// QPartitionFoo1b has @Key(path={"name"}, type=ConstraintType.KEY)
			// KEY type is an index, so H2 generates a separate CREATE INDEX statement
			QPartitionFoo1b table = QPartitionFoo1b.partitionFoo1b;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);
			builder.serializeTableCreate(false);
			List<String> sqls = builder.getSqls();

			assertTrue(sqls.size() > 1,
					"Should generate additional SQL statements for indexes, got " + sqls.size() + " statements: " + sqls);
		}

		@Test
		@DisplayName("index SQL contains CREATE INDEX or equivalent syntax")
		void testIndexSqlContainsCreateIndex() {
			QPartitionFoo1b table = QPartitionFoo1b.partitionFoo1b;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);
			builder.serializeTableCreate(false);
			List<String> sqls = builder.getSqls();

			// Find the index creation statement(s) - they should be after the first CREATE TABLE statement
			boolean hasIndexStatement = false;
			for (int i = 1; i < sqls.size(); i++) {
				String sql = sqls.get(i).toUpperCase();
				if (sql.contains("CREATE") && sql.contains("INDEX")) {
					hasIndexStatement = true;
					break;
				}
			}
			assertTrue(hasIndexStatement,
					"Should have at least one CREATE INDEX statement, got: " + sqls);
		}

		@Test
		@DisplayName("UNIQUE constraint is included inline in CREATE TABLE")
		void testUniqueConstraintInline() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);
			builder.serializeTableCreate(false);
			List<String> sqls = builder.getSqls();

			String createSql = sqls.get(0);
			// UNIQUE constraint on 'code' should be inline in the CREATE TABLE statement
			assertTrue(createSql.toUpperCase().contains("UNIQUE"),
					"CREATE TABLE should contain inline UNIQUE constraint, got: " + createSql);
		}
	}

	@Nested
	@DisplayName("Table with default values")
	class DefaultValues {

		@Test
		@DisplayName("generates SQL containing DEFAULT clause for columns with default values")
		void testCreateTableWithDefaults() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);
			builder.serializeTableCreate(false);
			List<String> sqls = builder.getSqls();

			String createSql = sqls.get(0);
			// CaAsset.code has @ColumnSpec(defaultValue = "''")
			assertTrue(createSql.toUpperCase().contains("DEFAULT"),
					"SQL should contain DEFAULT clause for columns with default values, got: " + createSql);
		}

		@Test
		@DisplayName("DEFAULT clause contains the specified default value")
		void testDefaultValueContent() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);
			builder.serializeTableCreate(false);
			List<String> sqls = builder.getSqls();

			String createSql = sqls.get(0);
			// The code column has defaultValue = "''" (empty string literal)
			// After DEFAULT, the value should appear
			String upper = createSql.toUpperCase();
			int defaultIndex = upper.indexOf("DEFAULT");
			assertTrue(defaultIndex > 0, "Should contain DEFAULT keyword");
			// The default value for code is '' (empty string)
			assertTrue(createSql.contains("''"),
					"SQL should contain the default value '' for code column, got: " + createSql);
		}
	}

	@Nested
	@DisplayName("IF NOT EXISTS clause behavior")
	class IfNotExistsClause {

		@Test
		@DisplayName("ifExists() causes create to be skipped when table already exists")
		void testIfExistsSkipsWhenTableExists() {
			// Mock MetadataQuerySupport to simulate table already existing
			MetadataQuerySupport mockConnection = mock(MetadataQuerySupport.class);
			DriverInfo mockDriverInfo = new DriverInfo();
			mockDriverInfo.setUrl("jdbc:h2:mem:ifexiststest");
			when(mockConnection.getDriverInfo()).thenReturn(mockDriverInfo);

			// Mock that the table exists
			TableInfo existingTable = new TableInfo();
			existingTable.setName("ca_asset");
			when(mockConnection.getTable(any(SchemaAndTable.class))).thenReturn(existingTable);
			when(mockConnection.asInCurrentSchema(any(SchemaAndTable.class)))
					.thenAnswer(inv -> inv.getArgument(0));

			QCaAsset table = QCaAsset.caAsset;
			CreateTableQuery query = new CreateTableQuery(mockConnection, configuration, table);
			query.ifExists();

			// The execute should return 0 because the table exists and ifExists() was called
			int result = query.execute();
			assertEquals(0, result, "Should return 0 when table exists and ifExists() is set");
		}

		@Test
		@DisplayName("without ifExists(), create throws when table already exists")
		void testWithoutIfExistsThrowsWhenTableExists() {
			// Mock MetadataQuerySupport to simulate table already existing
			MetadataQuerySupport mockConnection = mock(MetadataQuerySupport.class);
			DriverInfo mockDriverInfo = new DriverInfo();
			mockDriverInfo.setUrl("jdbc:h2:mem:ifexiststest2");
			when(mockConnection.getDriverInfo()).thenReturn(mockDriverInfo);

			// Mock that the table exists
			TableInfo existingTable = new TableInfo();
			existingTable.setName("ca_asset");
			when(mockConnection.getTable(any(SchemaAndTable.class))).thenReturn(existingTable);
			when(mockConnection.asInCurrentSchema(any(SchemaAndTable.class)))
					.thenAnswer(inv -> inv.getArgument(0));

			QCaAsset table = QCaAsset.caAsset;
			CreateTableQuery query = new CreateTableQuery(mockConnection, configuration, table);

			// Without ifExists(), should throw IllegalStateException when table exists
			org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, query::execute,
					"Should throw IllegalStateException when table exists and ifExists() is not set");
		}

		@Test
		@DisplayName("toSQLs() generates valid SQL regardless of ifExists setting")
		void testToSqlsGeneratesValidSql() {
			// toSQLs() only generates SQL without checking table existence
			MetadataQuerySupport mockConnection = mock(MetadataQuerySupport.class);
			DriverInfo mockDriverInfo = new DriverInfo();
			mockDriverInfo.setUrl("jdbc:h2:mem:ifexiststest3");
			when(mockConnection.getDriverInfo()).thenReturn(mockDriverInfo);

			QCaAsset table = QCaAsset.caAsset;
			CreateTableQuery query = new CreateTableQuery(mockConnection, configuration, table);
			query.ifExists();

			List<String> sqls = query.toSQLs();
			assertNotNull(sqls);
			assertFalse(sqls.isEmpty());
			assertTrue(sqls.get(0).toUpperCase().startsWith("CREATE TABLE"));
		}
	}
}
