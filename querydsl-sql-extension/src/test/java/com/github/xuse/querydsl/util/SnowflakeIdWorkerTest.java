package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SnowflakeIdWorker}.
 * Validates: Requirements 9.6, 9.7, 9.10
 */
@DisplayName("SnowflakeIdWorker Unit Tests")
class SnowflakeIdWorkerTest {

	@Test
	@DisplayName("1000 sequential calls produce 1000 unique positive values in strictly increasing order")
	void testSequentialUniquenessAndOrder() {
		SnowflakeIdWorker worker = new SnowflakeIdWorker(1);
		List<Long> ids = new ArrayList<>(1000);
		for (int i = 0; i < 1000; i++) {
			ids.add(worker.nextId());
		}
		// All values should be positive
		for (Long id : ids) {
			assertTrue(id > 0, "ID should be positive, got: " + id);
		}
		// All values should be unique
		Set<Long> uniqueIds = ConcurrentHashMap.newKeySet();
		uniqueIds.addAll(ids);
		assertEquals(1000, uniqueIds.size(), "All 1000 IDs should be unique");
		// Values should be in strictly increasing order
		for (int i = 1; i < ids.size(); i++) {
			assertTrue(ids.get(i) > ids.get(i - 1),
					"IDs should be strictly increasing: id[" + (i - 1) + "]=" + ids.get(i - 1) + " >= id[" + i + "]=" + ids.get(i));
		}
	}

	@Test
	@DisplayName("4 concurrent threads × 1000 calls = 4000 unique IDs")
	void testConcurrentUniqueness() throws InterruptedException {
		SnowflakeIdWorker worker = new SnowflakeIdWorker(1);
		int threadCount = 4;
		int idsPerThread = 1000;
		Set<Long> allIds = ConcurrentHashMap.newKeySet();
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(threadCount);
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		for (int t = 0; t < threadCount; t++) {
			executor.submit(() -> {
				try {
					startLatch.await();
					for (int i = 0; i < idsPerThread; i++) {
						allIds.add(worker.nextId());
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					doneLatch.countDown();
				}
			});
		}

		startLatch.countDown();
		doneLatch.await();
		executor.shutdown();

		assertEquals(threadCount * idsPerThread, allIds.size(),
				"All 4000 IDs from 4 concurrent threads should be unique");
	}

	@Test
	@DisplayName("workerId > 255 throws IllegalArgumentException")
	void testInvalidWorkerIdTooLarge() {
		assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdWorker(256));
	}

	@Test
	@DisplayName("workerId < 0 throws IllegalArgumentException")
	void testInvalidWorkerIdNegative() {
		assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdWorker(-1));
	}

	@Test
	@DisplayName("workerId at boundary 255 is valid")
	void testWorkerIdAtMaxBoundary() {
		SnowflakeIdWorker worker = new SnowflakeIdWorker(255);
		long id = worker.nextId();
		assertTrue(id > 0, "ID from worker with max workerId should be positive");
	}

	@Test
	@DisplayName("workerId at boundary 0 is valid")
	void testWorkerIdAtMinBoundary() {
		SnowflakeIdWorker worker = new SnowflakeIdWorker(0);
		long id = worker.nextId();
		assertTrue(id > 0, "ID from worker with min workerId should be positive");
	}
}
