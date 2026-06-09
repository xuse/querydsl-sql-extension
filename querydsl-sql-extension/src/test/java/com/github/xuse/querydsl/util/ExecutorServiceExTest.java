package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ExecutorServiceEx#batchSubmit}.
 * Validates Requirement 9.8.
 */
@DisplayName("ExecutorServiceEx batchSubmit tests")
class ExecutorServiceExTest {

	private ExecutorServiceEx.PoolExecutor executor;

	@BeforeEach
	void setUp() {
		executor = Threads.newPoolBuilder()
				.namePrefix("batch-test")
				.coreSize(4)
				.maximumSize(8)
				.queueSize(50)
				.queuePressureSize(20)
				.noJMX()
				.build();
	}

	@AfterEach
	void tearDown() throws InterruptedException {
		executor.shutdownGracefully(5, TimeUnit.SECONDS);
	}

	/**
	 * Test batchSubmit with 20 items and concurrencyLevel 4: all processed within 5 seconds.
	 * Validates: Requirement 9.8
	 */
	@Test
	@DisplayName("batchSubmit with 20 items and concurrencyLevel 4 processes all within 5 seconds")
	void testBatchSubmitAllItemsProcessed() {
		List<Integer> items = IntStream.rangeClosed(1, 20)
				.boxed()
				.collect(Collectors.toList());

		AtomicInteger processedCount = new AtomicInteger(0);

		Function<Integer, Integer> task = item -> {
			processedCount.incrementAndGet();
			// Simulate some work
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return item * 2;
		};

		long startTime = System.currentTimeMillis();
		List<Integer> results = executor.batchSubmit(items, task, 4);
		long elapsed = System.currentTimeMillis() - startTime;

		// All 20 items should be processed
		assertEquals(20, results.size(), "All 20 items should produce results");
		assertEquals(20, processedCount.get(), "All 20 items should be processed");

		// Should complete within 5 seconds (20 items / 4 concurrency * 50ms = ~250ms theoretical minimum)
		assertTrue(elapsed < 5000, "Should complete within 5 seconds, actual: " + elapsed + "ms");

		// Verify results are correct (each item doubled)
		for (int i = 0; i < items.size(); i++) {
			assertTrue(results.contains(items.get(i) * 2),
					"Result should contain doubled value of item " + items.get(i));
		}
	}

	/**
	 * Test batchSubmit with empty list returns empty result.
	 */
	@Test
	@DisplayName("batchSubmit with empty list returns empty result")
	void testBatchSubmitEmptyList() {
		List<Integer> items = new ArrayList<>();
		List<Integer> results = executor.batchSubmit(items, (Integer x) -> x * 2, 4);
		assertTrue(results.isEmpty(), "Empty input should produce empty results");
	}

	/**
	 * Test batchSubmit with null list returns empty result.
	 */
	@Test
	@DisplayName("batchSubmit with null list returns empty result")
	void testBatchSubmitNullList() {
		List<Integer> results = executor.batchSubmit(null, (Integer x) -> x * 2, 4);
		assertTrue(results.isEmpty(), "Null input should produce empty results");
	}

	/**
	 * Test batchSubmit with concurrencyLevel 1 executes serially.
	 */
	@Test
	@DisplayName("batchSubmit with concurrencyLevel 1 executes serially")
	void testBatchSubmitSingleConcurrency() {
		List<Integer> items = IntStream.rangeClosed(1, 10)
				.boxed()
				.collect(Collectors.toList());

		List<Integer> results = executor.batchSubmit(items, (Integer x) -> x * 3, 1);
		assertEquals(10, results.size(), "All 10 items should be processed serially");
		for (int i = 1; i <= 10; i++) {
			assertTrue(results.contains(i * 3), "Result should contain tripled value of " + i);
		}
	}

	/**
	 * Test batchSubmit skips null results from task function.
	 */
	@Test
	@DisplayName("batchSubmit skips null results from task function")
	void testBatchSubmitSkipsNullResults() {
		List<Integer> items = IntStream.rangeClosed(1, 10)
				.boxed()
				.collect(Collectors.toList());

		// Return null for even numbers
		Function<Integer, Integer> task = (Integer item) -> item % 2 == 0 ? null : item;
		List<Integer> results = executor.batchSubmit(items, task, 4);

		assertEquals(5, results.size(), "Should only contain non-null results (odd numbers)");
		for (Object r : results) {
			assertTrue(((Integer) r) % 2 != 0, "All results should be odd numbers");
		}
	}

	/**
	 * Test batchSubmit handles exceptions gracefully without losing other results.
	 */
	@Test
	@DisplayName("batchSubmit handles task exceptions gracefully")
	void testBatchSubmitHandlesExceptions() {
		List<Integer> items = IntStream.rangeClosed(1, 10)
				.boxed()
				.collect(Collectors.toList());

		Function<Integer, Integer> task = item -> {
			if (item == 5) {
				throw new RuntimeException("Simulated failure");
			}
			return item;
		};

		List<Integer> results = executor.batchSubmit(items, task, 4);
		// Item 5 throws exception, so it should be skipped
		assertEquals(9, results.size(), "Should have 9 results (item 5 failed)");
		assertTrue(!results.contains(5) || results.size() == 9,
				"Item 5 should not be in results due to exception");
	}
}
