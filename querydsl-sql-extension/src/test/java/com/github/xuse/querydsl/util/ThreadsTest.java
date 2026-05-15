package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Threads} utility class and ThreadPoolBuilder.
 */
class ThreadsTest {

	@Test
	void testThreadPoolBuilder_basic() {
		ThreadPoolExecutor pool = Threads.newPoolBuilder()
				.namePrefix("test-pool")
				.coreSize(2)
				.maximumSize(4)
				.queueSize(10)
				.noJMX()
				.build();
		assertNotNull(pool);
		assertEquals(2, pool.getCorePoolSize());
		assertEquals(4, pool.getMaximumPoolSize());
		pool.shutdown();
	}

	@Test
	void testThreadPoolBuilder_withPressure() {
		ThreadPoolExecutor pool = Threads.newPoolBuilder()
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
		ThreadPoolExecutor pool = Threads.newPoolBuilder()
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
		ThreadPoolExecutor pool = Threads.newPoolBuilder()
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
		ThreadPoolExecutor pool = Threads.newPoolBuilder()
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
		ThreadPoolExecutor pool = Threads.newPoolBuilder()
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
		ThreadPoolExecutor pool = Threads.newPoolBuilder()
				.coreSize(1)
				.maximumSize(1)
				.queueSize(5)
				.noJMX()
				.build();
		assertNotNull(pool);
		pool.shutdown();
	}
}
