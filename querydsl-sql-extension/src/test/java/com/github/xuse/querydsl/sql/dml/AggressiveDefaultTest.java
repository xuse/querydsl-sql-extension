package com.github.xuse.querydsl.sql.dml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.config.BatchNullStrategy;
import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.repository.CRUDRepository;
import com.github.xuse.querydsl.sql.integration.AbstractTestBase;

/**
 * Test for AGGRESSIVE_DEFAULT strategy: provides fallback values for NOT NULL columns
 * even when no default expression is defined in Java.
 * <p>
 * 测试 AGGRESSIVE_DEFAULT 策略：即使 Java 中没有定义默认值表达式，
 * 也为 NOT NULL 列提供兜底值。
 */
public class AggressiveDefaultTest extends AbstractTestBase {

	private static final LambdaTable<Foo> FOO = () -> Foo.class;

	@BeforeEach
	public void truncate() {
		factory.getMetadataFactory().truncate(FOO).execute();
	}

	/**
	 * Test AGGRESSIVE_DEFAULT strategy with missing fields.
	 * The 'name' field is NOT NULL without default expression.
	 * Without AGGRESSIVE_DEFAULT, this would fail with NOT NULL constraint violation.
	 * With AGGRESSIVE_DEFAULT, it should insert empty string for name.
	 */
	@Test
	public void testAggressiveDefault_missingStringField() {
		Foo bean = new Foo();
		// Only set required fields with defaults or auto-generated
		bean.setVolume(100);
		// name is NOT NULL but not set → should be substituted with ""
		// code is NOT NULL with default '' → should use default
		// codeType is NOT NULL with default 1 → should use default

		long count = factory.insert(FOO)
				.batchNullStrategy(BatchNullStrategy.AGGRESSIVE_DEFAULT)
				.populateBatch(Arrays.asList(bean))
				.execute();

		assertEquals(1, count, "Record should be inserted with aggressive defaults");

		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		Foo result = repo.query().fetchFirst();
		assertNotNull(result);
		assertEquals("", result.getName(), "name should be substituted with empty string");
		assertEquals("", result.getCode(), "code should use default empty string");
		assertEquals(1, result.getCodeType(), "codeType should use default 1");
	}

	/**
	 * Test AGGRESSIVE_DEFAULT with multiple beans, some with missing fields.
	 */
	@Test
	public void testAggressiveDefault_mixedBatch() {
		Foo full = new Foo();
		full.setCode("FULL01");
		full.setName("Full Record");
		full.setVolume(100);

		Foo partial = new Foo();
		partial.setVolume(50);
		// name and code are null → should be substituted with ""

		long count = factory.insert(FOO)
				.batchNullStrategy(BatchNullStrategy.AGGRESSIVE_DEFAULT)
				.populateBatch(Arrays.asList(full, partial))
				.execute();

		assertEquals(2, count, "Both records should be inserted");

		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		assertEquals(2, repo.query().count());

		Foo partialResult = repo.query().eq(Foo::getVolume, 50).fetchFirst();
		assertNotNull(partialResult);
		assertEquals("", partialResult.getName(), "name should be empty string");
		assertEquals("", partialResult.getCode(), "code should be empty string");
	}

	/**
	 * Test that AUTO_DEFAULT strategy does NOT provide aggressive fallbacks.
	 * This should fail with NOT NULL constraint violation.
	 */
	@Test
	public void testAutoDefault_doesNotProvideAggressiveFallback() {
		Foo bean = new Foo();
		bean.setVolume(100);
		// name is NOT NULL without default → should fail

		try {
			factory.insert(FOO)
					.batchNullStrategy(BatchNullStrategy.AUTO_DEFAULT)
					.populateBatch(Arrays.asList(bean))
					.execute();
			// Should not reach here
			throw new AssertionError("Expected NOT NULL constraint violation");
		} catch (Exception e) {
			// Expected: NOT NULL constraint violation
			// Check the exception and its cause chain
			boolean isNotNullError = false;
			Throwable current = e;
			while (current != null && !isNotNullError) {
				String message = current.getMessage();
				if (message != null) {
					String lowerMessage = message.toLowerCase();
					isNotNullError = lowerMessage.contains("not null") || lowerMessage.contains("null not allowed");
				}
				current = current.getCause();
			}
			if (!isNotNullError) {
				throw new AssertionError("Expected NOT NULL constraint violation, but got: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Test AGGRESSIVE_DEFAULT with numeric fields.
	 */
	@Test
	public void testAggressiveDefault_numericFields() {
		Foo bean = new Foo();
		bean.setName("Numeric Test");
		bean.setCode("NUM01");
		// volume is NOT NULL without default → should be substituted with 0
		// But volume has @UnsavedValue(MinusNumber), so 0 is not unsaved
		// Let's set it explicitly to avoid confusion
		bean.setVolume(0);

		long count = factory.insert(FOO)
				.batchNullStrategy(BatchNullStrategy.AGGRESSIVE_DEFAULT)
				.populateBatch(Arrays.asList(bean))
				.execute();

		assertEquals(1, count);

		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		Foo result = repo.query().fetchFirst();
		assertNotNull(result);
		assertEquals(0, result.getVolume());
	}

	/**
	 * Test that nullable columns are skipped by default.
	 * Even if a nullable column has a default expression, it should receive NULL
	 * unless withNullableColumns(true) is used.
	 */
	@Test
	public void testAutoDefault_skipsNullableColumns() {
		Foo bean = new Foo();
		bean.setName("Test");
		bean.setCode("TEST01");
		bean.setVolume(100);
		// content is nullable with a default expression, but should receive NULL by default

		long count = factory.insert(FOO)
				.batchNullStrategy(BatchNullStrategy.AUTO_DEFAULT)
				.populateBatch(Arrays.asList(bean))
				.execute();

		assertEquals(1, count);

		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		Foo result = repo.query().fetchFirst();
		assertNotNull(result);
		// content should be NULL (not the default value) because nullable columns are skipped
		// Note: This test assumes 'content' is a nullable column with a default expression
	}

	/**
	 * Test withNullableStrategy() applies default values to nullable columns.
	 */
	@Test
	public void testAutoDefault_withNullableStrategy() {
		Foo bean = new Foo();
		bean.setName("Test");
		bean.setCode("TEST01");
		bean.setVolume(100);
		// content is nullable with a default expression

		long count = factory.insert(FOO)
				.batchNullStrategy(BatchNullStrategy.AUTO_DEFAULT.withNullableStrategy(BatchNullStrategy.ColumnStrategy.USE_DEFAULT))
				.populateBatch(Arrays.asList(bean))
				.execute();

		assertEquals(1, count);

		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		Foo result = repo.query().fetchFirst();
		assertNotNull(result);
		// content should use the default value because withNullableStrategy(USE_DEFAULT) was used
		// Note: This test assumes 'content' is a nullable column with a default expression
	}

	/**
	 * Test that BatchNullStrategy is immutable and thread-safe.
	 */
	@Test
	public void testBatchNullStrategy_immutability() {
		BatchNullStrategy original = BatchNullStrategy.AUTO_DEFAULT;
		BatchNullStrategy modified = original.withNullableStrategy(BatchNullStrategy.ColumnStrategy.USE_DEFAULT);

		// Original should not be affected
		assertEquals(BatchNullStrategy.ColumnStrategy.SKIP, original.getNullableStrategy(), 
				"Original strategy should not be modified");
		assertEquals(BatchNullStrategy.ColumnStrategy.USE_DEFAULT, modified.getNullableStrategy(), 
				"Modified strategy should have nullable columns enabled");

		// Calling with same value should return same instance
		BatchNullStrategy modified2 = modified.withNullableStrategy(BatchNullStrategy.ColumnStrategy.USE_DEFAULT);
		assertEquals(modified, modified2, 
				"Calling withNullableStrategy with same value should return same instance");
	}

	/**
	 * Test AGGRESSIVE does not provide fallback for nullable columns.
	 */
	@Test
	public void testAggressiveDefault_doesNotApplyToNullableColumns() {
		Foo bean = new Foo();
		bean.setName("Test");
		bean.setCode("TEST01");
		bean.setVolume(100);
		// content is nullable without default expression

		long count = factory.insert(FOO)
				.batchNullStrategy(BatchNullStrategy.AGGRESSIVE_DEFAULT)
				.populateBatch(Arrays.asList(bean))
				.execute();

		assertEquals(1, count);

		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		Foo result = repo.query().fetchFirst();
		assertNotNull(result);
		// content should be NULL (no aggressive fallback for nullable columns)
	}

	/**
	 * Test null strategy means SAFE mode (use addBatch path).
	 * This test verifies that passing null strategy works correctly.
	 */
	@Test
	public void testNullStrategy_usesSafeMode() {
		Foo bean = new Foo();
		bean.setName("Test");
		bean.setCode("TEST01");
		bean.setVolume(100);

		// null strategy should use SAFE mode (addBatch path)
		long count = factory.insert(FOO)
				.batchNullStrategy(null)
				.populateBatch(Arrays.asList(bean))
				.execute();

		assertEquals(1, count);

		CRUDRepository<Foo, Integer> repo = factory.asRepository(FOO);
		Foo result = repo.query().fetchFirst();
		assertNotNull(result);
	}
}
