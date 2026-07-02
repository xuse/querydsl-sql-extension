package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for {@link Threads} utility class and ThreadPoolBuilder.
 */
class ThreadsTest {

	@Test
	void testThreadPoolBuilder_basic() {
		ExecutorServiceEx pool = Threads.newPoolBuilder()
				.namePrefix("test-pool")
				.coreSize(2)
				.maximumSize(4)
				.queueSize(10)
				.noJMX()
				.build();
		assertNotNull(pool);
		pool.shutdown();
	}

	@Test
	void testThreadPoolBuilder_withPressure() {
		ExecutorServiceEx pool = Threads.newPoolBuilder()
				.namePrefix("pressure-pool")
				.coreSize(1)
				.maximumSize(2)
				.queueSize(20)
				.queuePressureSize(5)
				.noJMX()
				.build();
		assertNotNull(pool);
		pool.shutdown();
	}

	@Test
	void testThreadPoolBuilder_invalidPressureSize() {
		assertThrows(IllegalArgumentException.class, () -> {
			Threads.newPoolBuilder()
					.namePrefix("bad-pool")
					.coreSize(1)
					.maximumSize(2)
					.queueSize(10)
					.queuePressureSize(20) // pressure > queue
					.noJMX()
					.build();
		});
	}

	@Test
	void testThreadPoolBuilder_executeTask() throws Exception {
		ExecutorServiceEx pool = Threads.newPoolBuilder()
				.namePrefix("exec-pool")
				.coreSize(2)
				.maximumSize(4)
				.queueSize(10)
				.noJMX()
				.build();
		AtomicInteger counter = new AtomicInteger(0);
		CountDownLatch latch = new CountDownLatch(3);
		for (int i = 0; i < 3; i++) {
			pool.execute(() -> {
				counter.incrementAndGet();
				latch.countDown();
			});
		}
		assertTrue(latch.await(5, TimeUnit.SECONDS));
		assertEquals(3, counter.get());
		pool.shutdown();
	}

	@Test
	void testThreadPoolBuilder_defaultQueueSize() {
		// queueSize=0 should default to Integer.MAX_VALUE
		ExecutorServiceEx pool = Threads.newPoolBuilder()
				.namePrefix("default-q")
				.coreSize(1)
				.maximumSize(2)
				.queueSize(0)
				.noJMX()
				.build();
		assertNotNull(pool);
		pool.shutdown();
	}

	@Test
	void testThreadPoolBuilder_withJmx() {
		// Test with JMX registration (default behavior)
		ExecutorServiceEx pool = Threads.newPoolBuilder()
				.namePrefix("jmx-pool")
				.coreSize(1)
				.maximumSize(2)
				.queueSize(10)
				.build();
		assertNotNull(pool);
		pool.shutdown();
	}

	@Test
	void testThreadFactory() {
		ExecutorServiceEx pool = Threads.newPoolBuilder()
				.namePrefix("named")
				.coreSize(1)
				.maximumSize(1)
				.queueSize(5)
				.noJMX()
				.build();
		CountDownLatch latch = new CountDownLatch(1);
		final String[] threadName = new String[1];
		pool.execute(() -> {
			threadName[0] = Thread.currentThread().getName();
			latch.countDown();
		});
		try {
			latch.await(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		assertNotNull(threadName[0]);
		assertTrue(threadName[0].startsWith("named"), "Thread name should start with prefix: " + threadName[0]);
		pool.shutdown();
	}

	@Test
	void testThreadPoolBuilder_noNamePrefix() {
		// Without name prefix, should use default thread factory
		ExecutorServiceEx pool = Threads.newPoolBuilder()
				.coreSize(1)
				.maximumSize(1)
				.queueSize(5)
				.noJMX()
				.build();
		assertNotNull(pool);
		pool.shutdown();
	}

	@Test
	void testThreadPoolBuilder_invalidCoreSize() {
		assertThrows(IllegalArgumentException.class, () -> {
			Threads.newPoolBuilder()
					.coreSize(0)
					.maximumSize(2)
					.queueSize(10)
					.noJMX()
					.build();
		});
	}

	@Test
	void testThreadPoolBuilder_invalidMaximumSize() {
		assertThrows(IllegalArgumentException.class, () -> {
			Threads.newPoolBuilder()
					.coreSize(4)
					.maximumSize(2) // max < core
					.queueSize(10)
					.noJMX()
					.build();
		});
	}

	@Test
	void testAsyncCatch_normalExecution() throws Exception {
		AtomicBoolean executed = new AtomicBoolean(false);
		Runnable wrapped = Threads.asyncCatch(() -> executed.set(true), null);
		wrapped.run();
		assertTrue(executed.get(), "Task should have executed normally");
	}

	@Test
	void testAsyncCatch_catchesException() throws Exception {
		// Should not propagate the exception
		Runnable wrapped = Threads.asyncCatch(() -> {
			throw new RuntimeException("test error");
		}, null);
		// Should not throw
		wrapped.run();
	}

	@Test
	void testAsyncCatch_withCustomLogger() throws Exception {
		Logger customLog = LoggerFactory.getLogger("custom-async-test");
		AtomicBoolean executed = new AtomicBoolean(false);
		Runnable wrapped = Threads.asyncCatch(() -> {
			executed.set(true);
			throw new IllegalStateException("custom logger test");
		}, customLog);
		// Should not throw, should log to custom logger
		wrapped.run();
		assertTrue(executed.get(), "Task body should have been executed");
	}

	@Test
	void testAsyncCatch_catchesError() throws Exception {
		// Even Throwable (Error) should be caught
		Runnable wrapped = Threads.asyncCatch(() -> {
			throw new OutOfMemoryError("simulated OOM");
		}, null);
		// Should not propagate
		wrapped.run();
	}

	@Test
	void testAsyncCatch_inThreadPool() throws Exception {
		ExecutorService pool = Threads.newFixedThreadPool(2, "async-catch-pool");
		CountDownLatch latch = new CountDownLatch(2);
		AtomicInteger successCount = new AtomicInteger(0);

		// Submit a normal task
		pool.submit(Threads.asyncCatch(() -> {
			successCount.incrementAndGet();
			latch.countDown();
		}, null));

		// Submit a failing task - should not break the pool
		pool.submit(Threads.asyncCatch(() -> {
			latch.countDown();
			throw new RuntimeException("pool task failure");
		}, null));

		assertTrue(latch.await(5, TimeUnit.SECONDS), "Both tasks should complete");
		assertEquals(1, successCount.get());
		pool.shutdown();
	}

	@Test
	void testAsyncCatch_nullLogger_usesDefault() {
		// Verify null logger doesn't cause NPE
		AtomicBoolean executed = new AtomicBoolean(false);
		Runnable wrapped = Threads.asyncCatch(() -> {
			executed.set(true);
			throw new RuntimeException("null logger test");
		}, null);
		wrapped.run();
		assertTrue(executed.get());
	}
}
