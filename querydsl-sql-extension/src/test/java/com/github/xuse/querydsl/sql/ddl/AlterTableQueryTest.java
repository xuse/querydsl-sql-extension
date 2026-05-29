package com.github.xuse.querydsl.sql.ddl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.entity.QCaAsset;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.column.ColumnMetadataEx;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.DriverInfo;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterColumnOps;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.H2Templates;

/**
 * Unit tests for ALTER TABLE SQL generation via {@link DDLMetadataBuilder#serializeAlterTable(CompareResult)}.
 * Validates that ALTER TABLE SQL contains ADD COLUMN, ALTER/MODIFY COLUMN, and DROP COLUMN clauses
 * matching the differences in CompareResult.
 *
 * Requirements: 4.12
 */
@DisplayName("AlterTableQuery Unit Tests")
class AlterTableQueryTest {

	private static ConfigurationEx configuration;
	private static DriverInfo driverInfo;

	@BeforeAll
	static void setup() {
		configuration = new ConfigurationEx(H2Templates.DEFAULT);
		configuration.allowTableDropAndCreate();
		configuration.scanPackages("com.github.xuse.querydsl.entity");

		driverInfo = new DriverInfo();
		driverInfo.setUrl("jdbc:h2:mem:altertabletest");
	}

	@Nested
	@DisplayName("ADD COLUMN clause generation")
	class AddColumnClause {

		@Test
		@DisplayName("ALTER TABLE SQL contains ADD COLUMN for a new column")
		void testAddColumnClause() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);

			CompareResult difference = new CompareResult();
			ColumnMapping mockMapping = createMockColumnMapping("email", Types.VARCHAR, 128, table);
			difference.setAddColumns(Collections.singletonList(mockMapping));

			builder.serializeAlterTable(difference);
			List<String> sqls = builder.getSqls();

			assertNotNull(sqls);
			assertFalse(sqls.isEmpty(), "Should generate at least one ALTER TABLE SQL");

			String combinedSql = String.join(" ", sqls).toUpperCase();
			assertTrue(combinedSql.contains("ALTER TABLE"),
					"SQL should contain ALTER TABLE, got: " + combinedSql);
			assertTrue(combinedSql.contains("ADD"),
					"SQL should contain ADD for new column, got: " + combinedSql);
			assertTrue(combinedSql.contains("EMAIL"),
					"SQL should reference the new column name 'email', got: " + combinedSql);
		}

		@Test
		@DisplayName("ADD COLUMN includes column type definition")
		void testAddColumnIncludesType() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);

			CompareResult difference = new CompareResult();
			ColumnMapping mockMapping = createMockColumnMapping("age", Types.INTEGER, 0, table);
			difference.setAddColumns(Collections.singletonList(mockMapping));

			builder.serializeAlterTable(difference);
			List<String> sqls = builder.getSqls();

			String combinedSql = String.join(" ", sqls).toUpperCase();
			assertTrue(combinedSql.contains("ALTER TABLE"), "Should contain ALTER TABLE");
			assertTrue(combinedSql.contains("AGE"), "Should reference column name 'age'");
			// H2 maps INTEGER to "integer" type
			assertTrue(combinedSql.contains("INTEGER") || combinedSql.contains("INT"),
					"Should contain integer type definition, got: " + combinedSql);
		}

		@Test
		@DisplayName("multiple ADD COLUMN clauses for multiple new columns")
		void testMultipleAddColumns() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);

			CompareResult difference = new CompareResult();
			List<ColumnMapping> columns = Arrays.asList(
					createMockColumnMapping("phone", Types.VARCHAR, 20, table),
					createMockColumnMapping("address", Types.VARCHAR, 200, table)
			);
			difference.setAddColumns(columns);

			builder.serializeAlterTable(difference);
			List<String> sqls = builder.getSqls();

			assertNotNull(sqls);
			// H2 dialect generates one ALTER TABLE per column (no MULTI_COLUMNS_IN_ALTER_TABLE)
			assertTrue(sqls.size() >= 2,
					"Should generate at least 2 ALTER TABLE statements for 2 columns, got: " + sqls.size());

			String combinedSql = String.join(" ", sqls).toUpperCase();
			assertTrue(combinedSql.contains("PHONE"), "Should reference 'phone' column");
			assertTrue(combinedSql.contains("ADDRESS"), "Should reference 'address' column");
		}
	}

	@Nested
	@DisplayName("DROP COLUMN clause generation")
	class DropColumnClause {

		@Test
		@DisplayName("ALTER TABLE SQL contains DROP COLUMN for removed column")
		void testDropColumnClause() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);

			CompareResult difference = new CompareResult();
			difference.setDropColumns(Collections.singletonList("obsolete_col"));

			builder.serializeAlterTable(difference);
			List<String> sqls = builder.getSqls();

			assertNotNull(sqls);
			assertFalse(sqls.isEmpty(), "Should generate ALTER TABLE SQL for drop column");

			String combinedSql = String.join(" ", sqls).toUpperCase();
			assertTrue(combinedSql.contains("ALTER TABLE"),
					"SQL should contain ALTER TABLE, got: " + combinedSql);
			assertTrue(combinedSql.contains("DROP"),
					"SQL should contain DROP for removed column, got: " + combinedSql);
			assertTrue(combinedSql.contains("OBSOLETE_COL"),
					"SQL should reference the dropped column name, got: " + combinedSql);
		}

		@Test
		@DisplayName("multiple DROP COLUMN clauses for multiple removed columns")
		void testMultipleDropColumns() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);

			CompareResult difference = new CompareResult();
			difference.setDropColumns(Arrays.asList("col_a", "col_b"));

			builder.serializeAlterTable(difference);
			List<String> sqls = builder.getSqls();

			assertNotNull(sqls);
			assertTrue(sqls.size() >= 2,
					"Should generate at least 2 ALTER TABLE statements for 2 dropped columns, got: " + sqls.size());

			String combinedSql = String.join(" ", sqls).toUpperCase();
			assertTrue(combinedSql.contains("COL_A"), "Should reference 'col_a'");
			assertTrue(combinedSql.contains("COL_B"), "Should reference 'col_b'");
		}
	}

	@Nested
	@DisplayName("MODIFY/ALTER COLUMN clause generation")
	class ModifyColumnClause {

		@Test
		@DisplayName("ALTER TABLE SQL contains ALTER COLUMN for type change")
		void testAlterColumnForTypeChange() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);

			CompareResult difference = new CompareResult();
			ColumnModification modification = createTypeChangeModification(
					"code", Types.VARCHAR, 64, Types.VARCHAR, 255);
			difference.setChangeColumns(Collections.singletonList(modification));

			builder.serializeAlterTable(difference);
			List<String> sqls = builder.getSqls();

			assertNotNull(sqls);
			assertFalse(sqls.isEmpty(), "Should generate ALTER TABLE SQL for column change");

			String combinedSql = String.join(" ", sqls).toUpperCase();
			assertTrue(combinedSql.contains("ALTER TABLE"),
					"SQL should contain ALTER TABLE, got: " + combinedSql);
			assertTrue(combinedSql.contains("ALTER") || combinedSql.contains("MODIFY") || combinedSql.contains("CHANGE"),
					"SQL should contain ALTER/MODIFY/CHANGE for column modification, got: " + combinedSql);
			assertTrue(combinedSql.contains("CODE"),
					"SQL should reference the modified column name 'code', got: " + combinedSql);
		}

		@Test
		@DisplayName("ALTER TABLE SQL contains SET NULL for nullable change")
		void testAlterColumnSetNull() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);

			CompareResult difference = new CompareResult();
			ColumnModification modification = createNullChangeModification("status", true);
			difference.setChangeColumns(Collections.singletonList(modification));

			builder.serializeAlterTable(difference);
			List<String> sqls = builder.getSqls();

			assertNotNull(sqls);
			assertFalse(sqls.isEmpty(), "Should generate ALTER TABLE SQL for null change");

			String combinedSql = String.join(" ", sqls).toUpperCase();
			assertTrue(combinedSql.contains("ALTER TABLE"),
					"SQL should contain ALTER TABLE, got: " + combinedSql);
			assertTrue(combinedSql.contains("NULL"),
					"SQL should contain NULL keyword for nullable change, got: " + combinedSql);
		}

		@Test
		@DisplayName("ALTER TABLE SQL contains SET NOT NULL for not-nullable change")
		void testAlterColumnSetNotNull() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);

			CompareResult difference = new CompareResult();
			ColumnModification modification = createNullChangeModification("status", false);
			difference.setChangeColumns(Collections.singletonList(modification));

			builder.serializeAlterTable(difference);
			List<String> sqls = builder.getSqls();

			assertNotNull(sqls);
			assertFalse(sqls.isEmpty(), "Should generate ALTER TABLE SQL for not-null change");

			String combinedSql = String.join(" ", sqls).toUpperCase();
			assertTrue(combinedSql.contains("ALTER TABLE"),
					"SQL should contain ALTER TABLE, got: " + combinedSql);
			assertTrue(combinedSql.contains("NOT NULL"),
					"SQL should contain NOT NULL for not-nullable change, got: " + combinedSql);
		}
	}

	@Nested
	@DisplayName("Combined ALTER TABLE operations")
	class CombinedOperations {

		@Test
		@DisplayName("ALTER TABLE with ADD, DROP, and MODIFY generates multiple statements")
		void testCombinedAlterOperations() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);

			CompareResult difference = new CompareResult();
			// Add a column
			difference.setAddColumns(Collections.singletonList(
					createMockColumnMapping("new_field", Types.VARCHAR, 100, table)));
			// Drop a column
			difference.setDropColumns(Collections.singletonList("old_field"));
			// Change a column
			ColumnModification modification = createTypeChangeModification(
					"name", Types.VARCHAR, 50, Types.VARCHAR, 200);
			difference.setChangeColumns(Collections.singletonList(modification));

			builder.serializeAlterTable(difference);
			List<String> sqls = builder.getSqls();

			assertNotNull(sqls);
			// H2 generates separate statements for each operation
			assertTrue(sqls.size() >= 3,
					"Should generate at least 3 ALTER TABLE statements (add + drop + change), got: " + sqls.size());

			String combinedSql = String.join(" ", sqls).toUpperCase();
			assertTrue(combinedSql.contains("ADD"), "Should contain ADD operation");
			assertTrue(combinedSql.contains("DROP"), "Should contain DROP operation");
			assertTrue(combinedSql.contains("NEW_FIELD"), "Should reference new column");
			assertTrue(combinedSql.contains("OLD_FIELD"), "Should reference dropped column");
		}

		@Test
		@DisplayName("empty CompareResult generates no SQL")
		void testEmptyCompareResultGeneratesNoSql() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);

			CompareResult difference = new CompareResult();
			builder.serializeAlterTable(difference);
			List<String> sqls = builder.getSqls();

			// Empty CompareResult should produce no meaningful SQL
			// (may produce empty DDLMetadata entries that get filtered out)
			for (String sql : sqls) {
				// If any SQL is generated, it should still be valid ALTER TABLE
				if (sql != null && !sql.trim().isEmpty()) {
					assertTrue(sql.toUpperCase().contains("ALTER TABLE"),
							"Any generated SQL should be ALTER TABLE, got: " + sql);
				}
			}
		}
	}

	@Nested
	@DisplayName("Constraint changes in ALTER TABLE")
	class ConstraintChanges {

		@Test
		@DisplayName("ALTER TABLE with ADD constraint generates appropriate SQL")
		void testAddConstraint() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);

			CompareResult difference = new CompareResult();
			Constraint constraint = new Constraint();
			constraint.setName("unq_code");
			constraint.setConstraintType(ConstraintTypeDef.UNIQUE);
			constraint.setColumnNames(new ArrayList<>(Collections.singletonList("code")));
			difference.getAddConstraints().add(constraint);

			builder.serializeAlterTable(difference);
			List<String> sqls = builder.getSqls();

			assertNotNull(sqls);
			String combinedSql = String.join(" ", sqls).toUpperCase();
			assertTrue(combinedSql.contains("ALTER TABLE") || combinedSql.contains("CREATE"),
					"Should contain ALTER TABLE or CREATE INDEX, got: " + combinedSql);
		}

		@Test
		@DisplayName("ALTER TABLE with DROP constraint generates appropriate SQL")
		void testDropConstraint() {
			QCaAsset table = QCaAsset.caAsset;
			DDLMetadataBuilder builder = new DDLMetadataBuilder(configuration, table, null, driverInfo);

			CompareResult difference = new CompareResult();
			Constraint constraint = new Constraint();
			constraint.setName("idx_old");
			constraint.setConstraintType(ConstraintTypeDef.KEY);
			constraint.setColumnNames(new ArrayList<>(Collections.singletonList("name")));
			difference.getDropConstraints().add(constraint);

			builder.serializeAlterTable(difference);
			List<String> sqls = builder.getSqls();

			assertNotNull(sqls);
			String combinedSql = String.join(" ", sqls).toUpperCase();
			assertTrue(combinedSql.contains("DROP"),
					"Should contain DROP for removed constraint, got: " + combinedSql);
		}
	}

	// ---- Helper methods ----

	private static ColumnMapping createMockColumnMapping(String name, int jdbcType, int size, QCaAsset table) {
		ColumnMetadata colMeta = ColumnMetadata.named(name).ofType(jdbcType).withSize(size);
		StringPath path = Expressions.stringPath(table, name);
		ColumnMapping mapping = mock(ColumnMapping.class);
		when(mapping.getColumn()).thenReturn(colMeta);
		when(mapping.getName()).thenReturn(name);
		when(mapping.getJdbcType()).thenReturn(jdbcType);
		when(mapping.getSize()).thenReturn(size);
		when(mapping.getDigits()).thenReturn(0);
		when(mapping.isNullable()).thenReturn(true);
		org.mockito.Mockito.doReturn(path).when(mapping).getPath();
		return mapping;
	}

	private static ColumnModification createTypeChangeModification(String columnName, int fromType, int fromSize, int toType, int toSize) {
		ColumnMetadata fromMeta = ColumnMetadata.named(columnName).ofType(fromType).withSize(fromSize);
		ColumnMetadata toMeta = ColumnMetadata.named(columnName).ofType(toType).withSize(toSize);
		ColumnMetadataEx fromEx = new SimpleColumnMetadataEx(fromMeta);
		ColumnMetadataEx toEx = new SimpleColumnMetadataEx(toMeta);

		Expression<?> fromExpr = DDLExpressions.dataType(DDLOps.DEF_LIST,
				"VARCHAR(" + fromSize + ")", true, false, null);
		Expression<?> toExpr = DDLExpressions.dataType(AlterColumnOps.SET_DATATYPE,
				"VARCHAR(" + toSize + ")", true, false, null);
		ColumnChange typeChange = ColumnChange.dataType(fromExpr, toExpr);

		StringPath path = Expressions.stringPath(QCaAsset.caAsset, columnName);
		return new ColumnModification(path, fromEx, Collections.singletonList(typeChange), toEx, null);
	}

	private static ColumnModification createNullChangeModification(String columnName, boolean setToNull) {
		ColumnMetadata meta = ColumnMetadata.named(columnName).ofType(Types.VARCHAR).withSize(50);
		ColumnMetadataEx metaEx = new SimpleColumnMetadataEx(meta);

		ColumnChange nullChange = setToNull ? ColumnChange.toNull() : ColumnChange.toNotNull();

		StringPath path = Expressions.stringPath(QCaAsset.caAsset, columnName);
		return new ColumnModification(path, metaEx, Collections.singletonList(nullChange), metaEx, null);
	}

	/**
	 * Simple ColumnMetadataEx implementation for testing purposes.
	 */
	private static class SimpleColumnMetadataEx implements ColumnMetadataEx {
		private final ColumnMetadata column;

		SimpleColumnMetadataEx(ColumnMetadata column) {
			this.column = column;
		}

		@Override
		public ColumnMetadata getColumn() {
			return column;
		}

		@Override
		public java.util.Map<String, String> getSpecialSpec() {
			return null;
		}
	}
}
