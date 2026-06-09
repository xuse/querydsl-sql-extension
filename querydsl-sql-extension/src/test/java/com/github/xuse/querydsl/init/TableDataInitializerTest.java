package com.github.xuse.querydsl.init;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.repository.CRUDRepository;
import com.github.xuse.querydsl.sql.integration.AbstractTestBase;

/**
 * Unit tests for {@link TableDataInitializer} verifying CSV data import
 * and missing resource file handling.
 *
 * Requirements: 11.6, 11.7
 */
@DisplayName("TableDataInitializer - CSV import and resource validation")
class TableDataInitializerTest {

	private static final LambdaTable<Foo> FOO = () -> Foo.class;

	/**
	 * Integration tests using H2 embedded database for actual data initialization.
	 */
	@Nested
	@DisplayName("CSV Resource Import")
	class CsvResourceImport extends AbstractTestBase {

		@BeforeEach
		void truncateTable() {
			factory.getMetadataFactory().truncate(FOO).execute();
		}

		/**
		 * Requirement 11.6: execute with CSV resource returns count equal to records in file,
		 * and the table contains the inserted rows.
		 */
		@Test
		@DisplayName("execute with CSV resource: returned count equals records in file, table contains rows")
		void testExecuteWithCsvResource() {
			TableDataInitializer initializer = new TableDataInitializer(factory, FOO);
			initializer.from("init-test-data.csv");
			initializer.isNewTable(true);

			int count = initializer.execute();

			// The CSV file contains 3 data records
			assertEquals(3, count, "Returned count should equal number of records in CSV file");

			// Verify table contains the inserted rows
			CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
			List<Foo> results = repo.query().fetch();
			assertEquals(3, results.size(), "Table should contain 3 rows after initialization");

			// Verify data content
			boolean hasInit01 = results.stream().anyMatch(f -> "INIT01".equals(f.getCode()));
			boolean hasInit02 = results.stream().anyMatch(f -> "INIT02".equals(f.getCode()));
			boolean hasInit03 = results.stream().anyMatch(f -> "INIT03".equals(f.getCode()));
			assertTrue(hasInit01, "Table should contain record with code INIT01");
			assertTrue(hasInit02, "Table should contain record with code INIT02");
			assertTrue(hasInit03, "Table should contain record with code INIT03");

			// Verify specific field values
			Foo record1 = results.stream().filter(f -> "INIT01".equals(f.getCode())).findFirst().orElse(null);
			assertEquals("Init Record One", record1.getName());
			assertEquals(10, record1.getVolume());
			assertEquals(1, record1.getCodeType());
		}
	}

	/**
	 * Tests for missing resource file behavior.
	 */
	@Nested
	@DisplayName("Missing Resource File")
	class MissingResourceFile extends AbstractTestBase {

		/**
		 * Requirement 11.7: missing resource file with ignoreIfResourceFileNotFound=false
		 * throws IllegalStateException.
		 */
		@Test
		@DisplayName("missing resource file with ignoreResource(false): IllegalStateException thrown")
		void testMissingResourceFileThrowsException() {
			TableDataInitializer initializer = new TableDataInitializer(factory, FOO);
			initializer.from("non_existent_file_that_does_not_exist.csv");
			initializer.ignoreResource(false);

			IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
				initializer.execute();
			});

			assertTrue(ex.getMessage().contains("was not found"),
					"Exception message should indicate missing resource, got: " + ex.getMessage());
		}

		/**
		 * Additional: missing resource file with ignoreResource(true) returns 0 without exception.
		 */
		@Test
		@DisplayName("missing resource file with ignoreResource(true): returns 0 without exception")
		void testMissingResourceFileIgnored() {
			TableDataInitializer initializer = new TableDataInitializer(factory, FOO);
			initializer.from("non_existent_file_that_does_not_exist.csv");
			initializer.ignoreResource(true);

			int count = initializer.execute();

			assertEquals(0, count, "Should return 0 when resource is missing and ignore is true");
		}
	}
}
