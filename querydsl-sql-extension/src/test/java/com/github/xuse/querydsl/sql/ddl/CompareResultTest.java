package com.github.xuse.querydsl.sql.ddl;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.column.ColumnMetadataEx;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterColumnOps;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

/**
 * Unit tests for {@link CompareResult}.
 * Validates schema comparison result behavior including detection of
 * added/removed columns, type changes, index changes, and constraint changes.
 */
@DisplayName("CompareResult Unit Tests")
class CompareResultTest {

	@Nested
	@DisplayName("Identical schemas produce no changes")
	class IdenticalSchemas {

		@Test
		@DisplayName("new CompareResult with no modifications is empty")
		void testEmptyCompareResult() {
			CompareResult result = new CompareResult();
			assertTrue(result.isEmpty(), "A fresh CompareResult should be empty");
			assertTrue(result.getAddColumns().isEmpty());
			assertTrue(result.getDropColumns().isEmpty());
			assertTrue(result.getChangeColumns().isEmpty());
			assertTrue(result.getAddConstraints().isEmpty());
			assertTrue(result.getDropConstraints().isEmpty());
			assertTrue(result.getOtherChange().isEmpty());
		}

		@Test
		@DisplayName("isEmpty returns true when all lists are explicitly set to empty")
		void testExplicitlyEmptyLists() {
			CompareResult result = new CompareResult();
			result.setAddColumns(Collections.emptyList());
			result.setDropColumns(Collections.emptyList());
			result.setChangeColumns(Collections.emptyList());
			result.setAddConstraints(new ArrayList<>());
			result.setDropConstraints(new ArrayList<>());
			assertTrue(result.isEmpty());
		}
	}

	@Nested
	@DisplayName("Detection of added columns")
	class AddedColumns {

		@Test
		@DisplayName("single added column makes result non-empty")
		void testSingleAddedColumn() {
			CompareResult result = new CompareResult();
			ColumnMapping mockMapping = createMockColumnMapping("new_column", Types.VARCHAR, 255);
			result.setAddColumns(Collections.singletonList(mockMapping));

			assertFalse(result.isEmpty(), "CompareResult with added column should not be empty");
			assertEquals(1, result.getAddColumns().size());
		}

		@Test
		@DisplayName("ofAddSingleColumn creates result with one added column")
		void testOfAddSingleColumn() {
			CompareResult original = new CompareResult();
			ColumnMapping mockMapping = createMockColumnMapping("email", Types.VARCHAR, 128);
			CompareResult derived = original.ofAddSingleColumn(mockMapping);

			assertFalse(derived.isEmpty());
			assertEquals(1, derived.getAddColumns().size());
			// Original should remain empty
			assertTrue(original.isEmpty());
		}

		@Test
		@DisplayName("multiple added columns are all detected")
		void testMultipleAddedColumns() {
			CompareResult result = new CompareResult();
			List<ColumnMapping> columns = Arrays.asList(
					createMockColumnMapping("col_a", Types.INTEGER, 0),
					createMockColumnMapping("col_b", Types.VARCHAR, 64),
					createMockColumnMapping("col_c", Types.TIMESTAMP, 0)
			);
			result.setAddColumns(columns);

			assertFalse(result.isEmpty());
			assertEquals(3, result.getAddColumns().size());
		}
	}

	@Nested
	@DisplayName("Detection of removed columns")
	class RemovedColumns {

		@Test
		@DisplayName("single removed column makes result non-empty")
		void testSingleRemovedColumn() {
			CompareResult result = new CompareResult();
			result.setDropColumns(Collections.singletonList("obsolete_column"));

			assertFalse(result.isEmpty());
			assertEquals(1, result.getDropColumns().size());
			assertEquals("obsolete_column", result.getDropColumns().get(0));
		}

		@Test
		@DisplayName("ofDropSingleColumn creates result with one dropped column")
		void testOfDropSingleColumn() {
			CompareResult original = new CompareResult();
			CompareResult derived = original.ofDropSingleColumn("removed_col");

			assertFalse(derived.isEmpty());
			assertEquals(1, derived.getDropColumns().size());
			assertEquals("removed_col", derived.getDropColumns().get(0));
			assertTrue(original.isEmpty());
		}

		@Test
		@DisplayName("multiple removed columns are all detected")
		void testMultipleRemovedColumns() {
			CompareResult result = new CompareResult();
			result.setDropColumns(Arrays.asList("col_x", "col_y", "col_z"));

			assertFalse(result.isEmpty());
			assertEquals(3, result.getDropColumns().size());
		}
	}

	@Nested
	@DisplayName("Detection of column type changes")
	class ColumnTypeChanges {

		@Test
		@DisplayName("single column type change makes result non-empty")
		void testSingleColumnTypeChange() {
			CompareResult result = new CompareResult();
			ColumnModification modification = createTypeChangeModification("name", Types.VARCHAR, 64, Types.VARCHAR, 255);
			result.setChangeColumns(Collections.singletonList(modification));

			assertFalse(result.isEmpty());
			assertEquals(1, result.getChangeColumns().size());
			ColumnModification mod = result.getChangeColumns().get(0);
			assertNotNull(mod.getChanges());
			assertTrue(mod.hasChange(AlterColumnOps.SET_DATATYPE));
		}

		@Test
		@DisplayName("ofSingleChangeColumn creates result with one changed column")
		void testOfSingleChangeColumn() {
			CompareResult original = new CompareResult();
			ColumnModification modification = createTypeChangeModification("status", Types.INTEGER, 0, Types.BIGINT, 0);
			CompareResult derived = original.ofSingleChangeColumn(modification);

			assertFalse(derived.isEmpty());
			assertEquals(1, derived.getChangeColumns().size());
			assertTrue(original.isEmpty());
		}

		@Test
		@DisplayName("column change includes SET_DATATYPE in changes list")
		void testColumnChangeContainsDataTypeChange() {
			ColumnModification modification = createTypeChangeModification("amount", Types.INTEGER, 0, Types.DECIMAL, 10);
			List<ColumnChange> changes = modification.getChanges();

			assertFalse(changes.isEmpty());
			assertEquals(AlterColumnOps.SET_DATATYPE, changes.get(0).getType());
		}
	}

	@Nested
	@DisplayName("Detection of index changes")
	class IndexChanges {

		@Test
		@DisplayName("added index constraint makes result non-empty")
		void testAddedIndex() {
			CompareResult result = new CompareResult();
			Constraint indexConstraint = createConstraint("idx_user_email", ConstraintTypeDef.KEY, "email");
			result.getAddConstraints().add(indexConstraint);

			assertFalse(result.isEmpty());
			assertEquals(1, result.getAddConstraints().size());
			Constraint added = result.getAddConstraints().get(0);
			assertEquals("idx_user_email", added.getName());
			assertEquals(ConstraintTypeDef.KEY, added.getConstraintType());
			assertTrue(added.getConstraintType().isIndex());
			assertEquals(ConstraintClassify.INDEX_COLUMNS, added.getConstraintType().classify);
		}

		@Test
		@DisplayName("dropped index constraint makes result non-empty")
		void testDroppedIndex() {
			CompareResult result = new CompareResult();
			Constraint indexConstraint = createConstraint("idx_old_index", ConstraintTypeDef.HASH, "hash_col");
			result.getDropConstraints().add(indexConstraint);

			assertFalse(result.isEmpty());
			assertEquals(1, result.getDropConstraints().size());
			assertEquals(ConstraintClassify.INDEX_COLUMNS, result.getDropConstraints().get(0).getConstraintType().classify);
		}

		@Test
		@DisplayName("addDropConstraints and addCreateConstraints batch methods work")
		void testBatchConstraintMethods() {
			CompareResult result = new CompareResult();
			List<Constraint> toAdd = Arrays.asList(
					createConstraint("idx_a", ConstraintTypeDef.KEY, "col_a"),
					createConstraint("idx_b", ConstraintTypeDef.FULLTEXT, "col_b")
			);
			List<Constraint> toDrop = Collections.singletonList(
					createConstraint("idx_old", ConstraintTypeDef.SPATIAL, "col_old")
			);

			result.addCreateConstraints(toAdd);
			result.addDropConstraints(toDrop);

			assertFalse(result.isEmpty());
			assertEquals(2, result.getAddConstraints().size());
			assertEquals(1, result.getDropConstraints().size());
			// All should be classified as INDEX_COLUMNS
			for (Constraint c : result.getAddConstraints()) {
				assertTrue(c.getConstraintType().isIndex());
			}
		}

		@Test
		@DisplayName("ofAddSingleConstraint creates result with one added index")
		void testOfAddSingleConstraint() {
			CompareResult original = new CompareResult();
			Constraint idx = createConstraint("idx_new", ConstraintTypeDef.KEY, "col_new");
			CompareResult derived = original.ofAddSingleConstraint(idx);

			assertFalse(derived.isEmpty());
			assertEquals(1, derived.getAddConstraints().size());
			assertTrue(original.isEmpty());
		}

		@Test
		@DisplayName("ofDropSingleConstraint creates result with one dropped index")
		void testOfDropSingleConstraint() {
			CompareResult original = new CompareResult();
			Constraint idx = createConstraint("idx_removed", ConstraintTypeDef.BITMAP, "col_removed");
			CompareResult derived = original.ofDropSingleConstraint(idx);

			assertFalse(derived.isEmpty());
			assertEquals(1, derived.getDropConstraints().size());
			assertTrue(original.isEmpty());
		}
	}

	@Nested
	@DisplayName("Detection of constraint changes")
	class ConstraintChanges {

		@Test
		@DisplayName("added PRIMARY_KEY constraint is classified as COLUMNS")
		void testAddedPrimaryKey() {
			CompareResult result = new CompareResult();
			Constraint pk = createConstraint("pk_users", ConstraintTypeDef.PRIMARY_KEY, "id");
			result.getAddConstraints().add(pk);

			assertFalse(result.isEmpty());
			assertEquals(1, result.getAddConstraints().size());
			Constraint added = result.getAddConstraints().get(0);
			assertEquals(ConstraintTypeDef.PRIMARY_KEY, added.getConstraintType());
			assertEquals(ConstraintClassify.COLUMNS, added.getConstraintType().classify);
		}

		@Test
		@DisplayName("added UNIQUE constraint is classified as COLUMNS")
		void testAddedUniqueConstraint() {
			CompareResult result = new CompareResult();
			Constraint unique = createConstraint("unq_email", ConstraintTypeDef.UNIQUE, "email");
			result.getAddConstraints().add(unique);

			assertFalse(result.isEmpty());
			Constraint added = result.getAddConstraints().get(0);
			assertEquals(ConstraintTypeDef.UNIQUE, added.getConstraintType());
			assertEquals(ConstraintClassify.COLUMNS, added.getConstraintType().classify);
		}

		@Test
		@DisplayName("dropped UNIQUE constraint makes result non-empty")
		void testDroppedUniqueConstraint() {
			CompareResult result = new CompareResult();
			Constraint unique = createConstraint("unq_old", ConstraintTypeDef.UNIQUE, "old_col");
			result.getDropConstraints().add(unique);

			assertFalse(result.isEmpty());
			assertEquals(1, result.getDropConstraints().size());
			assertEquals(ConstraintClassify.COLUMNS, result.getDropConstraints().get(0).getConstraintType().classify);
		}

		@Test
		@DisplayName("CHECK constraint is classified as CHECK")
		void testCheckConstraint() {
			CompareResult result = new CompareResult();
			Constraint check = createConstraint("chk_age", ConstraintTypeDef.CHECK, "age");
			result.getAddConstraints().add(check);

			assertFalse(result.isEmpty());
			assertEquals(ConstraintClassify.CHECK, result.getAddConstraints().get(0).getConstraintType().classify);
		}

		@Test
		@DisplayName("FOREIGN_KEY constraint is classified as REF")
		void testForeignKeyConstraint() {
			CompareResult result = new CompareResult();
			Constraint fk = createConstraint("fk_order_user", ConstraintTypeDef.FOREIGN_KEY, "user_id");
			result.getAddConstraints().add(fk);

			assertFalse(result.isEmpty());
			assertEquals(ConstraintClassify.REF, result.getAddConstraints().get(0).getConstraintType().classify);
		}
	}

	@Nested
	@DisplayName("Other changes (table comment, collation)")
	class OtherChanges {

		@Test
		@DisplayName("table comment change makes result non-empty")
		void testTableCommentChange() {
			CompareResult result = new CompareResult();
			result.setTableCommentChange("New table comment");

			assertFalse(result.isEmpty());
			assertTrue(result.hasOtherChange(DDLOps.COMMENT_ON_TABLE));
		}

		@Test
		@DisplayName("table collation change makes result non-empty")
		void testTableCollationChange() {
			CompareResult result = new CompareResult();
			result.setTableCollation("utf8mb4_unicode_ci");

			assertFalse(result.isEmpty());
			assertTrue(result.hasOtherChange(DDLOps.COLLATE));
		}

		@Test
		@DisplayName("ofOthers creates result with only other changes")
		void testOfOthers() {
			CompareResult original = new CompareResult();
			original.setTableCommentChange("Updated comment");
			original.setDropColumns(Collections.singletonList("some_col"));

			CompareResult derived = original.ofOthers();
			// derived should have the other change but not the drop column
			assertFalse(derived.isEmpty());
			assertTrue(derived.hasOtherChange(DDLOps.COMMENT_ON_TABLE));
			assertTrue(derived.getDropColumns().isEmpty());
		}
	}

	@Nested
	@DisplayName("Combined changes")
	class CombinedChanges {

		@Test
		@DisplayName("result with multiple change types is non-empty")
		void testMultipleChangeTypes() {
			CompareResult result = new CompareResult();
			// Add a column
			result.setAddColumns(Collections.singletonList(
					createMockColumnMapping("new_col", Types.VARCHAR, 100)));
			// Drop a column
			result.setDropColumns(Collections.singletonList("old_col"));
			// Add a constraint
			result.getAddConstraints().add(
					createConstraint("idx_new", ConstraintTypeDef.KEY, "new_col"));

			assertFalse(result.isEmpty());
			assertEquals(1, result.getAddColumns().size());
			assertEquals(1, result.getDropColumns().size());
			assertEquals(1, result.getAddConstraints().size());
		}
	}

	// ---- Helper methods ----

	private static ColumnMapping createMockColumnMapping(String name, int jdbcType, int size) {
		ColumnMetadata colMeta = ColumnMetadata.named(name).ofType(jdbcType).withSize(size);
		ColumnMapping mapping = mock(ColumnMapping.class);
		when(mapping.getColumn()).thenReturn(colMeta);
		when(mapping.getName()).thenReturn(name);
		when(mapping.getJdbcType()).thenReturn(jdbcType);
		when(mapping.getSize()).thenReturn(size);
		return mapping;
	}

	private static ColumnModification createTypeChangeModification(String columnName, int fromType, int fromSize, int toType, int toSize) {
		ColumnMetadata fromMeta = ColumnMetadata.named(columnName).ofType(fromType).withSize(fromSize);
		ColumnMetadata toMeta = ColumnMetadata.named(columnName).ofType(toType).withSize(toSize);
		ColumnMetadataEx fromEx = new SimpleColumnMetadataEx(fromMeta);
		ColumnMetadataEx toEx = new SimpleColumnMetadataEx(toMeta);

		ColumnChange typeChange = ColumnChange.dataType(
				Expressions.constant(fromType + "(" + fromSize + ")"),
				Expressions.constant(toType + "(" + toSize + ")")
		);

		StringPath path = Expressions.stringPath(columnName);
		return new ColumnModification(path, fromEx, Collections.singletonList(typeChange), toEx, null);
	}

	private static Constraint createConstraint(String name, ConstraintTypeDef type, String... columns) {
		Constraint constraint = new Constraint();
		constraint.setName(name);
		constraint.setConstraintType(type);
		constraint.setColumnNames(new ArrayList<>(Arrays.asList(columns)));
		return constraint;
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
