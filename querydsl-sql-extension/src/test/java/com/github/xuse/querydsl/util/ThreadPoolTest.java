package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.util.ExecutorServiceEx.PoolExecutor;

/**
 * Unit tests for thread pool expansion and rejection behavior.
 * Validates Requirements 9.3 and 9.4.
 */
@DisplayName("ThreadPool expansion and rejection tests")
class ThreadPoolTest {

	/**
	 * Test pool expansion beyond coreSize when queue pressure threshold is reached.
	 * 
	 * The FrontPressureBlockingQueue returns false from offer() when size >= pressureSize,
	 * which triggers ThreadPoolExecutor to create new threads up to maximumSize.
	 * 
	 * Validates: Requirement 9.3
	 */
	@Test
	@DisplayName("Pool expands beyond coreSize when queue pressure threshold reached")
	void testPoolExpansionOnQueuePressure() throws InterruptedException {
		// Use a latch to keep tasks running so they occupy threads and fill the queue
		CountDownLatch blockLatch = new CountDownLatch(1);
		AtomicInteger completedTasks = new AtomicInteger(0);

		Runnable blockingTask = () -> {
			try {
				blockLatch.await(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			completedTasks.incrementAndGet();
		};

		// coreSize=1, maximumSize=5, queueSize=6, pressureSize=3
		// When queue reaches 3 items, offer() returns false -> pool expands
		PoolExecutor pool = Threads.newPoolBuilder()
				.namePrefix("expand-test")
				.coreSize(1)
				.maximumSize(5)
				.queueSize(6)
				.queuePressureSize(3)
				.noJMX()
				.onReject(new ThreadPoolExecutor.AbortPolicy())
				.build();

		try {
			// Submit first task - occupies the single core thread
			pool.submit(blockingTask);
			// Give the thread pool a moment to start the task
			Thread.sleep(50);
			assertEquals(1, pool.getPoolSize(), "Should have 1 thread after first task");

			// Submit 3 more tasks - these go into the queue (queue size: 1, 2, 3)
			pool.submit(blockingTask);
			pool.submit(blockingTask);
			pool.submit(blockingTask);
			// At this point queue has 3 items, which equals pressureSize
			Thread.sleep(50);
			assertEquals(1, pool.getPoolSize(), "Pool should still have 1 thread (queue at pressure threshold)");

			// Submit another task - queue is at pressureSize, offer() returns false -> expansion
			pool.submit(blockingTask);
			Thread.sleep(50);
			assertTrue(pool.getPoolSize() > 1,
					"Pool should expand beyond coreSize when pressure threshold reached, actual: " + pool.getPoolSize());

			// Submit more tasks to trigger further expansion
			pool.submit(blockingTask);
			pool.submit(blockingTask);
			Thread.sleep(100);
			assertTrue(pool.getPoolSize() > 2,
					"Pool should continue expanding, actual: " + pool.getPoolSize());

		} finally {
			// Release all blocked tasks
			blockLatch.countDown();
			pool.shutdown();
			assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
		}
	}

	/**
	 * Test RejectedExecutionHandler is invoked when queue is full and all threads are occupied.
	 * 
	 * When the pool is at maximumSize and the queue (including the overflow via TempQueuedPolicy)
	 * is completely full, the configured RejectedExecutionHandler is invoked.
	 * 
	 * Validates: Requirement 9.4
	 */
	@Test
	@DisplayName("RejectedExecutionHandler invoked when queue full and all threads occupied")
	void testRejectedExecutionHandlerInvoked() throws InterruptedException {
		CountDownLatch blockLatch = new CountDownLatch(1);
		AtomicBoolean rejectionOccurred = new AtomicBoolean(false);
		AtomicInteger rejectionCount = new AtomicInteger(0);

		Runnable blockingTask = () -> {
			try {
				blockLatch.await(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		};

		// coreSize=2, maximumSize=3, queueSize=4, pressureSize=2
		// Total capacity: 3 threads + 4 queue slots = 7 tasks before rejection
		PoolExecutor pool = Threads.newPoolBuilder()
				.namePrefix("reject-test")
				.coreSize(2)
				.maximumSize(3)
				.queueSize(4)
				.queuePressureSize(2)
				.noJMX()
				.onReject((r, executor) -> {
					rejectionOccurred.set(true);
					rejectionCount.incrementAndGet();
					throw new RejectedExecutionException("Pool is saturated");
				})
				.build();

		try {
			// Fill up the pool: submit enough tasks to occupy all threads and fill the queue
			// The pool has maximumSize=3 threads and queueSize=4
			// We need to fill: 3 threads + 4 queue = 7 tasks
			for (int i = 0; i < 7; i++) {
				pool.submit(blockingTask);
				Thread.sleep(30); // Give time for thread creation
			}

			// Allow pool to stabilize
			Thread.sleep(200);

			// Now the pool should be fully saturated
			// The next task should trigger rejection
			RejectedExecutionException caughtException = null;
			try {
				pool.submit(blockingTask);
				// Give a moment for async rejection
				Thread.sleep(50);
			} catch (RejectedExecutionException e) {
				caughtException = e;
			}

			// Either the exception was thrown directly or our handler was called
			assertTrue(rejectionOccurred.get() || caughtException != null,
					"RejectedExecutionHandler should be invoked when pool is fully saturated");

			if (rejectionOccurred.get()) {
				assertTrue(rejectionCount.get() > 0, "Rejection count should be positive");
			}
			if (caughtException != null) {
				assertNotNull(caughtException.getMessage());
			}

		} finally {
			blockLatch.countDown();
			pool.shutdown();
			assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
		}
	}
}
