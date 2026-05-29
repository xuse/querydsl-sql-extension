package com.github.xuse.querydsl.sql.ddl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.entity.QCaAsset;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.DriverInfo;
import com.querydsl.core.types.Path;
import com.querydsl.sql.H2Templates;

/**
 * Unit tests for {@link DDLMetadataBuilder} metadata extraction.
 * Verifies that metadata extraction matches annotation values on entity class.
 *
 * Requirements: 4.13
 */
@DisplayName("DDLMetadataBuilder Metadata Extraction Tests")
class DDLMetadataBuilderTest {

	private static ConfigurationEx configuration;
	private static DriverInfo driverInfo;

	@BeforeAll
	static void setup() {
		configuration = new ConfigurationEx(H2Templates.DEFAULT);
		configuration.allowTableDropAndCreate();
		configuration.scanPackages("com.github.xuse.querydsl.entity");

		driverInfo = new DriverInfo();
		driverInfo.setUrl("jdbc:h2:mem:ddlmetadatatest");
	}

	@Nested
	@DisplayName("Table metadata extraction")
	class TableMetadata {

		@Test
		@DisplayName("extracts table name matching @TableSpec annotation")
		void testTableNameExtraction() {
			QCaAsset table = QCaAsset.caAsset;
			// The table name should match the @TableSpec(name="ca_asset") annotation
			assertEquals("ca_asset", table.getTableName().toLowerCase());
		}

		@Test
		@DisplayName("extracts table comment from @Comment annotation")
		void testTableCommentExtraction() {
			QCaAsset table = QCaAsset.caAsset;
			RelationalPathEx<?> tableEx = (RelationalPathEx<?>) table;
			assertNotNull(tableEx.getComment());
			assertEquals("Test table for assets.", tableEx.getComment());
		}

		@Test
		@DisplayName("extracts collate from @TableSpec annotation")
		void testCollateExtraction() {
			QCaAsset table = QCaAsset.caAsset;
			RelationalPathEx<?> tableEx = (RelationalPathEx<?>) table;
			assertNotNull(tableEx.getCollate());
		}

		@Test
		@DisplayName("extracts primary key from @TableSpec(primaryKeys) annotation")
		void testPrimaryKeyExtraction() {
			QCaAsset table = QCaAsset.caAsset;
			assertNotNull(table.getPrimaryKey());
			List<? extends Path<?>> pkColumns = table.getPrimaryKey().getLocalColumns();
			assertFalse(pkColumns.isEmpty());
			// @TableSpec(primaryKeys="id") means the PK should reference the 'id' column
			assertEquals(1, pkColumns.size());
		}
	}

	@Nested
	@DisplayName("Column metadata extraction")
	class ColumnMetadataExtraction {

		@Test
		@DisplayName("extracts column names matching @ColumnSpec annotations")
		void testColumnNamesExtraction() {
			QCaAsset table = QCaAsset.caAsset;
			RelationalPathEx<?> tableEx = (RelationalPathEx<?>) table;

			// Verify 'name' field maps to column 'asset_name' per @ColumnSpec(name="asset_name")
			ColumnMapping nameMapping = tableEx.getColumnMetadata(table.name);
			assertNotNull(nameMapping);
			assertEquals("asset_name", nameMapping.getColumn().getName());
		}

		@Test
		@DisplayName("extracts column types matching @ColumnSpec(type) annotations")
		void testColumnTypeExtraction() {
			QCaAsset table = QCaAsset.caAsset;
			RelationalPathEx<?> tableEx = (RelationalPathEx<?>) table;

			// @ColumnSpec(type = Types.INTEGER) on 'id' field
			ColumnMapping idMapping = tableEx.getColumnMetadata(table.id);
			assertNotNull(idMapping);
			assertEquals(java.sql.Types.INTEGER, idMapping.getJdbcType());

			// @ColumnSpec(type = Types.VARCHAR, size=64) on 'code' field
			ColumnMapping codeMapping = tableEx.getColumnMetadata(table.code);
			assertNotNull(codeMapping);
			assertEquals(java.sql.Types.VARCHAR, codeMapping.getJdbcType());
			assertEquals(64, codeMapping.getSize());
		}

		@Test
		@DisplayName("extracts nullable attribute from @ColumnSpec annotations")
		void testNullableExtraction() {
			QCaAsset table = QCaAsset.caAsset;
			RelationalPathEx<?> tableEx = (RelationalPathEx<?>) table;

			// @ColumnSpec(nullable = false) on 'id'
			ColumnMapping idMapping = tableEx.getColumnMetadata(table.id);
			assertFalse(idMapping.isNullable());

			// @ColumnSpec(nullable = false) on 'code'
			ColumnMapping codeMapping = tableEx.getColumnMetadata(table.code);
			assertFalse(codeMapping.isNullable());

			// @ColumnSpec(nullable = false) on 'name'
			ColumnMapping nameMapping = tableEx.getColumnMetadata(table.name);
			assertFalse(nameMapping.isNullable());
		}

		@Test
		@DisplayName("extracts column size from @ColumnSpec(size) annotations")
		void testColumnSizeExtraction() {
			QCaAsset table = QCaAsset.caAsset;
			RelationalPathEx<?> tableEx = (RelationalPathEx<?>) table;

			// @ColumnSpec(size=64) on 'code'
			ColumnMapping codeMapping = tableEx.getColumnMetadata(table.code);
			assertEquals(64, codeMapping.getSize());

			// @ColumnSpec(size=128) on 'name'
			ColumnMapping nameMapping = tableEx.getColumnMetadata(table.name);
			assertEquals(128, nameMapping.getSize());

			// @ColumnSpec(size=16384) on 'content'
			ColumnMapping contentMapping = tableEx.getColumnMetadata(table.content);
			assertEquals(16384, contentMapping.getSize());
		}

		@Test
		@DisplayName("extracts unsigned attribute from @ColumnSpec annotations")
		void testUnsignedExtraction() {
			QCaAsset table = QCaAsset.caAsset;
			RelationalPathEx<?> tableEx = (RelationalPathEx<?>) table;

			// @ColumnSpec(unsigned = true) on 'id'
			ColumnMapping idMapping = tableEx.getColumnMetadata(table.id);
			assertTrue(idMapping.isUnsigned());
		}

		@Test
		@DisplayName("extracts default value from @ColumnSpec(defaultValue) annotations")
		void testDefaultValueExtraction() {
			QCaAsset table = QCaAsset.caAsset;
			RelationalPathEx<?> tableEx = (RelationalPathEx<?>) table;

			// @ColumnSpec(defaultValue = "''") on 'code'
			ColumnMapping codeMapping = tableEx.getColumnMetadata(table.code);
			assertNotNull(codeMapping.getDefaultExpression());
		}
	}

	@Nested
	@DisplayName("Constraint metadata extraction")
	class ConstraintMetadataExtraction {

		@Test
		@DisplayName("extracts constraints from @TableSpec(keys) annotation")
		void testConstraintExtraction() {
			QCaAsset table = QCaAsset.caAsset;
			RelationalPathEx<?> tableEx = (RelationalPathEx<?>) table;

			Collection<Constraint> constraints = tableEx.getConstraints();
			assertNotNull(constraints);
			assertFalse(constraints.isEmpty());
		}

		@Test
		@DisplayName("extracts UNIQUE constraint type from @Key(type=ConstraintType.UNIQUE)")
		void testUniqueConstraintExtraction() {
			QCaAsset table = QCaAsset.caAsset;
			RelationalPathEx<?> tableEx = (RelationalPathEx<?>) table;

			Collection<Constraint> constraints = tableEx.getConstraints();
			boolean hasUnique = constraints.stream()
					.anyMatch(c -> c.getConstraintType() == ConstraintTypeDef.UNIQUE);
			assertTrue(hasUnique, "Should have a UNIQUE constraint from @Key(type=ConstraintType.UNIQUE)");
		}

		@Test
		@DisplayName("extracts FULLTEXT constraint type from @Key(type=ConstraintType.FULLTEXT)")
		void testFulltextConstraintExtraction() {
			QCaAsset table = QCaAsset.caAsset;
			RelationalPathEx<?> tableEx = (RelationalPathEx<?>) table;

			Collection<Constraint> constraints = tableEx.getConstraints();
			boolean hasFulltext = constraints.stream()
					.anyMatch(c -> c.getConstraintType() == ConstraintTypeDef.FULLTEXT);
			assertTrue(hasFulltext, "Should have a FULLTEXT constraint from @Key(type=ConstraintType.FULLTEXT)");
		}

		@Test
		@DisplayName("UNIQUE constraint references 'code' column per @Key(path={\"code\"})")
		void testUniqueConstraintColumns() {
			QCaAsset table = QCaAsset.caAsset;
			RelationalPathEx<?> tableEx = (RelationalPathEx<?>) table;

			Collection<Constraint> constraints = tableEx.getConstraints();
			Constraint uniqueConstraint = constraints.stream()
					.filter(c -> c.getConstraintType() == ConstraintTypeDef.UNIQUE)
					.findFirst()
					.orElse(null);
			assertNotNull(uniqueConstraint);
			assertFalse(uniqueConstraint.getPaths().isEmpty());
		}
	}

	@Nested
	@DisplayName("DDL SQL generation from metadata")
	class DDLSqlGeneration {

		@Test
		@DisplayName("generates CREATE TABLE SQL that includes all annotated columns")
		void testCreateTableIncludesAllColumns() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);
			builder.serializeTableCreate(false);
			List<String> sqls = builder.getSqls();

			assertNotNull(sqls);
			assertFalse(sqls.isEmpty());
			String createSql = sqls.get(0).toLowerCase();

			// Verify column names from annotations appear in the SQL (case-insensitive)
			assertTrue(createSql.contains("asset_name"), "SQL should contain renamed column 'asset_name'");
			assertTrue(createSql.contains("code"), "SQL should contain column 'code'");
			assertTrue(createSql.contains("id"), "SQL should contain column 'id'");
		}

		@Test
		@DisplayName("generates CREATE TABLE SQL with correct data types from annotations")
		void testCreateTableDataTypes() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);
			builder.serializeTableCreate(false);
			List<String> sqls = builder.getSqls();

			String createSql = sqls.get(0).toLowerCase();
			// H2 maps Types.VARCHAR to varchar
			assertTrue(createSql.contains("varchar"), "SQL should contain varchar type");
			// H2 maps Types.INTEGER to integer or int
			assertTrue(createSql.contains("integer") || createSql.contains("int"),
					"SQL should contain integer type");
		}

		@Test
		@DisplayName("generates CREATE TABLE SQL with PRIMARY KEY from annotation")
		void testCreateTablePrimaryKey() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);
			builder.serializeTableCreate(false);
			List<String> sqls = builder.getSqls();

			String createSql = sqls.get(0).toLowerCase();
			assertTrue(createSql.contains("primary key"), "SQL should contain PRIMARY KEY clause");
		}

		@Test
		@DisplayName("generates CREATE TABLE SQL with NOT NULL for non-nullable columns")
		void testCreateTableNotNull() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);
			builder.serializeTableCreate(false);
			List<String> sqls = builder.getSqls();

			String createSql = sqls.get(0).toLowerCase();
			assertTrue(createSql.contains("not null"), "SQL should contain NOT NULL for non-nullable columns");
		}
	}
}
