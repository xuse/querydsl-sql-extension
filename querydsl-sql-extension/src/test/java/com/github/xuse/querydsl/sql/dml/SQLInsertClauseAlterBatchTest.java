package com.github.xuse.querydsl.sql.dml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.config.BatchNullStrategy;
import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.entity.QTableDataTypes;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.github.xuse.querydsl.repository.CRUDRepository;
import com.github.xuse.querydsl.sql.MySQLQueryFactory2;
import com.github.xuse.querydsl.sql.integration.AbstractTestBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLBindings;

/**
 * Tests for SQLInsertClauseAlter batch operations covering:
 * - Empty batch handling
 * - Single-element batch
 * - Large batch (50+ elements)
 * - Null field strategies (SKIP, USE_DEFAULT, AGGRESSIVE)
 * - ON DUPLICATE KEY UPDATE (upsert) on MySQL dialect
 *
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7
 */
@DisplayName("SQLInsertClauseAlter Batch Tests")
public class SQLInsertClauseAlterBatchTest {

	private static final LambdaTable<Foo> FOO = () -> Foo.class;

	/**
	 * Integration tests using H2 embedded database for actual batch insert operations.
	 */
	@Nested
	@DisplayName("Batch Insert Integration Tests")
	class BatchInsertIntegration extends AbstractTestBase {

		@BeforeEach
		public void truncate() {
			factory.getMetadataFactory().truncate(FOO).execute();
		}

		/**
		 * Requirement 5.1: Empty batch returns 0, no SQL issued.
		 */
		@Test
		@DisplayName("Empty batch: execute() returns 0")
		public void testEmptyBatchInsert() {
			long count = factory.insert(FOO)
					.populateBatch(Collections.emptyList())
					.execute();

			assertEquals(0, count, "Empty batch should return 0");

			CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
			assertEquals(0, repo.query().count(), "No records should be inserted");
		}

		/**
		 * Requirement 5.2: Single-element batch persists one row.
		 */
		@Test
		@DisplayName("Single-element batch: one row persisted")
		public void testSingleItemBatchInsert() {
			Foo bean = makeFoo("SINGLE01", "Single Record", 42);

			long count = factory.insert(FOO)
					.populateBatch(Collections.singletonList(bean))
					.execute();

			assertEquals(1, count, "Single-element batch should insert 1 record");

			CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
			List<Foo> results = repo.query().fetch();
			assertEquals(1, results.size());
			assertEquals("Single Record", results.get(0).getName());
			assertEquals(42, results.get(0).getVolume());
		}

		/**
		 * Requirement 5.3: Large batch (50+ elements) inserts all records.
		 */
		@Test
		@DisplayName("Large batch (50+ elements): all records inserted")
		public void testLargeBatchInsert() {
			int batchSize = 55;
			List<Foo> batch = new ArrayList<>(batchSize);
			for (int i = 0; i < batchSize; i++) {
				batch.add(makeFoo("LARGE_" + String.format("%03d", i), "Record_" + i, i + 1));
			}

			long count = factory.insert(FOO)
					.populateBatch(batch)
					.execute();

			assertEquals(batchSize, count, "All records should be inserted");

			CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
			assertEquals(batchSize, repo.query().count(), "Count in DB should match batch size");
		}

		/**
		 * Requirement 5.4: Null fields with SKIP strategy (null BatchNullStrategy)
		 * causes the traditional addBatch path where null columns are omitted from SQL.
		 */
		@Test
		@DisplayName("Null fields with SKIP strategy: null columns omitted")
		public void testBatchInsertWithNullFields_SkipStrategy() {
			Foo bean = new Foo();
			bean.setCode("SKIP01");
			bean.setName("Skip Test");
			bean.setVolume(100);
			// code is set, but other nullable fields (content, gender, ext, map) are null
			// Using null strategy = SAFE mode (addBatch path), null columns omitted from SQL

			long count = factory.insert(FOO)
					.batchNullStrategy(null) // SAFE mode: null columns omitted
					.populateBatch(Collections.singletonList(bean))
					.execute();

			assertEquals(1, count, "Record should be inserted with SKIP strategy");

			CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
			Foo result = repo.query().fetchFirst();
			assertNotNull(result);
			assertEquals("Skip Test", result.getName());
		}

		/**
		 * Requirement 5.5: Null fields with USE_DEFAULT strategy substitutes
		 * the configured defaultExpression for NOT NULL columns.
		 */
		@Test
		@DisplayName("Null fields with USE_DEFAULT strategy: default expression substituted")
		public void testBatchInsertWithNullFields_UseDefaultStrategy() {
			Foo bean = new Foo();
			// code is null → NOT NULL with default '' → should use default
			bean.setName("Default Test");
			bean.setVolume(100);

			long count = factory.insert(FOO)
					.batchNullStrategy(BatchNullStrategy.AUTO_DEFAULT)
					.populateBatch(Collections.singletonList(bean))
					.execute();

			assertEquals(1, count, "Record should be inserted with USE_DEFAULT strategy");

			CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
			Foo result = repo.query().fetchFirst();
			assertNotNull(result);
			assertEquals("", result.getCode(), "code should be substituted with default empty string");
			assertEquals(1, result.getCodeType(), "codeType should use default 1");
		}

		/**
		 * Requirement 5.6: Null fields with AGGRESSIVE strategy provides
		 * type-appropriate fallback values even without defaultExpression.
		 */
		@Test
		@DisplayName("Null fields with AGGRESSIVE strategy: type-appropriate fallback used")
		public void testBatchInsertWithNullFields_AggressiveStrategy() {
			Foo bean = new Foo();
			// name is NOT NULL without default → AGGRESSIVE provides ""
			// code is NOT NULL with default '' → uses default
			bean.setVolume(100);

			long count = factory.insert(FOO)
					.batchNullStrategy(BatchNullStrategy.AGGRESSIVE_DEFAULT)
					.populateBatch(Collections.singletonList(bean))
					.execute();

			assertEquals(1, count, "Record should be inserted with AGGRESSIVE strategy");

			CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
			Foo result = repo.query().fetchFirst();
			assertNotNull(result);
			assertEquals("", result.getName(), "name should be substituted with empty string fallback");
			assertEquals("", result.getCode(), "code should use default empty string");
		}

		/**
		 * Requirement 5.3 (additional): Large batch with batchToBulk mode.
		 */
		@Test
		@DisplayName("Large batch with batchToBulk=true: all records inserted")
		public void testLargeBatchInsert_BulkMode() {
			int batchSize = 50;
			List<Foo> batch = new ArrayList<>(batchSize);
			for (int i = 0; i < batchSize; i++) {
				batch.add(makeFoo("BULK_" + String.format("%03d", i), "BulkRecord_" + i, i + 1));
			}

			long count = factory.insert(FOO)
					.batchToBulk(true)
					.populateBatch(batch)
					.execute();

			assertEquals(batchSize, count, "All records should be inserted in bulk mode");

			CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
			assertEquals(batchSize, repo.query().count(), "Count in DB should match batch size");
		}

		private Foo makeFoo(String code, String name, int volume) {
			Foo f = new Foo();
			f.setCode(code);
			f.setName(name);
			f.setVolume(volume);
			return f;
		}
	}

	/**
	 * MySQL-specific upsert test using MockedTestBase (H2 in MySQL mode).
	 * Requirement 5.7: ON DUPLICATE KEY UPDATE clause generation.
	 */
	@Nested
	@DisplayName("MySQL Upsert Tests")
	class MySQLUpsertTests extends MockedTestBase {

		/**
		 * Requirement 5.7: Verify ON DUPLICATE KEY UPDATE SQL generation.
		 */
		@Test
		@DisplayName("ON DUPLICATE KEY UPDATE: SQL contains upsert clause")
		public void testInsertOnDuplicateKeyUpdate_SqlGeneration() {
			MySQLQueryFactory2 mysqlFactory = new MySQLQueryFactory2(
					factory.getConfiguration(), () -> factory.getConnection());

			QTableDataTypes entity = QTableDataTypes.aaa;
			String updateClause = "name = VALUES(name)";

			SQLInsertClauseAlter insertClause = mysqlFactory.insertOnDuplicateKeyUpdate(entity, updateClause);
			insertClause.set(entity.name, "test_value");
			insertClause.set(entity.dataInt, 42);

			List<SQLBindings> sqlList = insertClause.getSQL();
			assertNotNull(sqlList);
			assertEquals(1, sqlList.size());

			String sql = sqlList.get(0).getSQL();
			assertTrue(sql.toLowerCase().contains("on duplicate key update"),
					"SQL should contain ON DUPLICATE KEY UPDATE clause, got: " + sql);
			assertTrue(sql.contains("name = VALUES(name)"),
					"SQL should contain the update expression, got: " + sql);
		}

		/**
		 * Requirement 5.7: Verify ON DUPLICATE KEY UPDATE with Expression clause.
		 */
		@Test
		@DisplayName("ON DUPLICATE KEY UPDATE with Expression: SQL contains upsert clause")
		public void testInsertOnDuplicateKeyUpdate_ExpressionClause() {
			MySQLQueryFactory2 mysqlFactory = new MySQLQueryFactory2(
					factory.getConfiguration(), () -> factory.getConnection());

			QTableDataTypes entity = QTableDataTypes.aaa;

			SQLInsertClauseAlter insertClause = mysqlFactory.insertOnDuplicateKeyUpdate(
					entity, Expressions.constant("data_int = data_int + 1"));
			insertClause.set(entity.name, "test_value");
			insertClause.set(entity.dataInt, 10);

			List<SQLBindings> sqlList = insertClause.getSQL();
			assertNotNull(sqlList);
			assertEquals(1, sqlList.size());

			String sql = sqlList.get(0).getSQL();
			assertTrue(sql.toLowerCase().contains("on duplicate key update"),
					"SQL should contain ON DUPLICATE KEY UPDATE clause, got: " + sql);
		}

		/**
		 * Requirement 5.7: Verify ON DUPLICATE KEY UPDATE with multiple Expression clauses.
		 */
		@Test
		@DisplayName("ON DUPLICATE KEY UPDATE with multiple expressions")
		public void testInsertOnDuplicateKeyUpdate_MultipleExpressions() {
			MySQLQueryFactory2 mysqlFactory = new MySQLQueryFactory2(
					factory.getConfiguration(), () -> factory.getConnection());

			QTableDataTypes entity = QTableDataTypes.aaa;

			SQLInsertClauseAlter insertClause = mysqlFactory.insertOnDuplicateKeyUpdate(
					entity,
					Expressions.constant("name = VALUES(name)"),
					Expressions.constant("data_int = data_int + 1"));
			insertClause.set(entity.name, "test_value");
			insertClause.set(entity.dataInt, 10);

			List<SQLBindings> sqlList = insertClause.getSQL();
			assertNotNull(sqlList);
			assertEquals(1, sqlList.size());

			String sql = sqlList.get(0).getSQL();
			assertTrue(sql.toLowerCase().contains("on duplicate key update"),
					"SQL should contain ON DUPLICATE KEY UPDATE clause, got: " + sql);
		}
	}
}
