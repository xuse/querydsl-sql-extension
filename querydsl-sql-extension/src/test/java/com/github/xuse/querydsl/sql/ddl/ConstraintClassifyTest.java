package com.github.xuse.querydsl.sql.ddl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ConstraintClassify} and {@link ConstraintTypeDef} classification.
 * Verifies that each ConstraintTypeDef maps to the correct ConstraintClassify category:
 * <ul>
 *   <li>PRIMARY_KEY, UNIQUE → COLUMNS</li>
 *   <li>KEY, HASH, FULLTEXT, SPATIAL, BITMAP → INDEX_COLUMNS</li>
 *   <li>FOREIGN_KEY → REF</li>
 *   <li>CHECK → CHECK</li>
 * </ul>
 *
 * Requirements: 4.14
 */
@DisplayName("ConstraintClassify Category Tests")
class ConstraintClassifyTest {

	@Nested
	@DisplayName("COLUMNS classification (PRIMARY_KEY, UNIQUE)")
	class ColumnsClassification {

		@Test
		@DisplayName("PRIMARY_KEY maps to ConstraintClassify.COLUMNS")
		void testPrimaryKeyClassify() {
			assertEquals(ConstraintClassify.COLUMNS, ConstraintTypeDef.PRIMARY_KEY.classify);
		}

		@Test
		@DisplayName("UNIQUE maps to ConstraintClassify.COLUMNS")
		void testUniqueClassify() {
			assertEquals(ConstraintClassify.COLUMNS, ConstraintTypeDef.UNIQUE.classify);
		}

		@Test
		@DisplayName("PRIMARY_KEY isColumnList returns true")
		void testPrimaryKeyIsColumnList() {
			assertTrue(ConstraintTypeDef.PRIMARY_KEY.isColumnList());
		}

		@Test
		@DisplayName("UNIQUE isColumnList returns true")
		void testUniqueIsColumnList() {
			assertTrue(ConstraintTypeDef.UNIQUE.isColumnList());
		}

		@Test
		@DisplayName("PRIMARY_KEY isIndex returns false")
		void testPrimaryKeyIsNotIndex() {
			assertFalse(ConstraintTypeDef.PRIMARY_KEY.isIndex());
		}

		@Test
		@DisplayName("UNIQUE isIndex returns false")
		void testUniqueIsNotIndex() {
			assertFalse(ConstraintTypeDef.UNIQUE.isIndex());
		}
	}

	@Nested
	@DisplayName("INDEX_COLUMNS classification (KEY, HASH, FULLTEXT, SPATIAL, BITMAP)")
	class IndexColumnsClassification {

		@Test
		@DisplayName("KEY maps to ConstraintClassify.INDEX_COLUMNS")
		void testKeyClassify() {
			assertEquals(ConstraintClassify.INDEX_COLUMNS, ConstraintTypeDef.KEY.classify);
		}

		@Test
		@DisplayName("HASH maps to ConstraintClassify.INDEX_COLUMNS")
		void testHashClassify() {
			assertEquals(ConstraintClassify.INDEX_COLUMNS, ConstraintTypeDef.HASH.classify);
		}

		@Test
		@DisplayName("FULLTEXT maps to ConstraintClassify.INDEX_COLUMNS")
		void testFulltextClassify() {
			assertEquals(ConstraintClassify.INDEX_COLUMNS, ConstraintTypeDef.FULLTEXT.classify);
		}

		@Test
		@DisplayName("SPATIAL maps to ConstraintClassify.INDEX_COLUMNS")
		void testSpatialClassify() {
			assertEquals(ConstraintClassify.INDEX_COLUMNS, ConstraintTypeDef.SPATIAL.classify);
		}

		@Test
		@DisplayName("BITMAP maps to ConstraintClassify.INDEX_COLUMNS")
		void testBitmapClassify() {
			assertEquals(ConstraintClassify.INDEX_COLUMNS, ConstraintTypeDef.BITMAP.classify);
		}

		@Test
		@DisplayName("KEY isIndex returns true")
		void testKeyIsIndex() {
			assertTrue(ConstraintTypeDef.KEY.isIndex());
		}

		@Test
		@DisplayName("HASH isIndex returns true")
		void testHashIsIndex() {
			assertTrue(ConstraintTypeDef.HASH.isIndex());
		}

		@Test
		@DisplayName("FULLTEXT isIndex returns true")
		void testFulltextIsIndex() {
			assertTrue(ConstraintTypeDef.FULLTEXT.isIndex());
		}

		@Test
		@DisplayName("SPATIAL isIndex returns true")
		void testSpatialIsIndex() {
			assertTrue(ConstraintTypeDef.SPATIAL.isIndex());
		}

		@Test
		@DisplayName("BITMAP isIndex returns true")
		void testBitmapIsIndex() {
			assertTrue(ConstraintTypeDef.BITMAP.isIndex());
		}

		@Test
		@DisplayName("all INDEX_COLUMNS types have isColumnList true")
		void testIndexColumnsIsColumnList() {
			assertTrue(ConstraintTypeDef.KEY.isColumnList());
			assertTrue(ConstraintTypeDef.HASH.isColumnList());
			assertTrue(ConstraintTypeDef.FULLTEXT.isColumnList());
			assertTrue(ConstraintTypeDef.SPATIAL.isColumnList());
			assertTrue(ConstraintTypeDef.BITMAP.isColumnList());
		}
	}

	@Nested
	@DisplayName("REF classification (FOREIGN_KEY, REF)")
	class RefClassification {

		@Test
		@DisplayName("FOREIGN_KEY maps to ConstraintClassify.REF")
		void testForeignKeyClassify() {
			assertEquals(ConstraintClassify.REF, ConstraintTypeDef.FOREIGN_KEY.classify);
		}

		@Test
		@DisplayName("REF maps to ConstraintClassify.REF")
		void testRefClassify() {
			assertEquals(ConstraintClassify.REF, ConstraintTypeDef.REF.classify);
		}

		@Test
		@DisplayName("FOREIGN_KEY isIgnored returns true (framework does not process)")
		void testForeignKeyIsIgnored() {
			assertTrue(ConstraintTypeDef.FOREIGN_KEY.isIgnored());
		}

		@Test
		@DisplayName("FOREIGN_KEY isIndex returns false")
		void testForeignKeyIsNotIndex() {
			assertFalse(ConstraintTypeDef.FOREIGN_KEY.isIndex());
		}

		@Test
		@DisplayName("FOREIGN_KEY isColumnList returns false")
		void testForeignKeyIsNotColumnList() {
			assertFalse(ConstraintTypeDef.FOREIGN_KEY.isColumnList());
		}
	}

	@Nested
	@DisplayName("CHECK classification")
	class CheckClassification {

		@Test
		@DisplayName("CHECK maps to ConstraintClassify.CHECK")
		void testCheckClassify() {
			assertEquals(ConstraintClassify.CHECK, ConstraintTypeDef.CHECK.classify);
		}

		@Test
		@DisplayName("CHECK isCheckClause returns true")
		void testCheckIsCheckClause() {
			assertTrue(ConstraintTypeDef.CHECK.isCheckClause());
		}

		@Test
		@DisplayName("CHECK isIndex returns false")
		void testCheckIsNotIndex() {
			assertFalse(ConstraintTypeDef.CHECK.isIndex());
		}

		@Test
		@DisplayName("CHECK isColumnList returns false")
		void testCheckIsNotColumnList() {
			assertFalse(ConstraintTypeDef.CHECK.isColumnList());
		}

		@Test
		@DisplayName("CHECK isIgnored returns false")
		void testCheckIsNotIgnored() {
			assertFalse(ConstraintTypeDef.CHECK.isIgnored());
		}
	}

	@Nested
	@DisplayName("IGNORE classification (READ_ONLY, SUPPLEMENTAL, VIEW_CHECK)")
	class IgnoreClassification {

		@Test
		@DisplayName("READ_ONLY maps to ConstraintClassify.IGNORE")
		void testReadOnlyClassify() {
			assertEquals(ConstraintClassify.IGNORE, ConstraintTypeDef.READ_ONLY.classify);
		}

		@Test
		@DisplayName("SUPPLEMENTAL maps to ConstraintClassify.IGNORE")
		void testSupplementalClassify() {
			assertEquals(ConstraintClassify.IGNORE, ConstraintTypeDef.SUPPLEMENTAL.classify);
		}

		@Test
		@DisplayName("VIEW_CHECK maps to ConstraintClassify.IGNORE")
		void testViewCheckClassify() {
			assertEquals(ConstraintClassify.IGNORE, ConstraintTypeDef.VIEW_CHECK.classify);
		}

		@Test
		@DisplayName("IGNORE types are all marked as isIgnored")
		void testIgnoreTypesAreIgnored() {
			assertTrue(ConstraintTypeDef.READ_ONLY.isIgnored());
			assertTrue(ConstraintTypeDef.SUPPLEMENTAL.isIgnored());
			assertTrue(ConstraintTypeDef.VIEW_CHECK.isIgnored());
		}
	}
}
