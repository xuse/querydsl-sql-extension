package com.github.xuse.querydsl.util;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 线程池累计指标收集器。使用无锁计数器，适合高并发场景。
 * <p>
 * Thread pool metrics collector. Uses lock-free counters for high-concurrency scenarios.
 */
public final class PoolMetrics {

	// --- 执行耗时指标 ---
	private final LongAdder completedCount = new LongAdder();
	private final LongAdder rejectedCount = new LongAdder();
	private final LongAdder totalExecutionMs = new LongAdder();
	private final AtomicLong maxExecutionMs = new AtomicLong(0);
	private final AtomicLong minExecutionMs = new AtomicLong(Long.MAX_VALUE);

	// --- 等待耗时指标 ---
	private final LongAdder totalWaitMs = new LongAdder();
	private final AtomicLong maxWaitMs = new AtomicLong(0);

	// --- 饱和度指标 ---
	/** 线程池进入压力状态的累计次数（队列达到 pressureSize，触发扩容） */
	private final LongAdder pressureCount = new LongAdder();
	/** 线程池队列满的累计次数（任务被 force add 到剩余队列空间） */
	private final LongAdder saturatedCount = new LongAdder();

	/**
	 * 记录一次任务完成及其执行耗时和等待耗时。
	 * @param executionMs 任务执行耗时（毫秒）
	 * @param waitMs 任务等待耗时（毫秒），从入队到开始执行
	 */
	public void recordCompleted(long executionMs, long waitMs) {
		completedCount.increment();
		totalExecutionMs.add(executionMs);
		updateMax(maxExecutionMs, executionMs);
		updateMin(minExecutionMs, executionMs);
		totalWaitMs.add(waitMs);
		updateMax(maxWaitMs, waitMs);
	}

	/**
	 * 记录一次任务拒绝。
	 */
	public void recordRejected() {
		rejectedCount.increment();
	}

	/**
	 * 记录一次进入压力状态（队列达到 pressureSize，线程池开始扩容）。
	 */
	public void recordPressure() {
		pressureCount.increment();
	}

	/**
	 * 记录一次队列饱和（队列满，任务被 force add）。
	 */
	public void recordSaturated() {
		saturatedCount.increment();
	}

	// ========== 查询方法 ==========

	/** @return 累计完成任务数 */
	public long getCompletedCount() {
		return completedCount.sum();
	}

	/** @return 累计拒绝任务数 */
	public long getRejectedCount() {
		return rejectedCount.sum();
	}

	/** @return 平均执行耗时（毫秒），无任务完成时返回 0 */
	public double getAverageExecutionTimeMs() {
		long count = completedCount.sum();
		if (count == 0) {
			return 0.0;
		}
		return (double) totalExecutionMs.sum() / count;
	}

	/** @return 最大执行耗时（毫秒），无任务完成时返回 0 */
	public long getMaxExecutionTimeMs() {
		long val = maxExecutionMs.get();
		return val == 0 ? 0 : val;
	}

	/** @return 最小执行耗时（毫秒），无任务完成时返回 0 */
	public long getMinExecutionTimeMs() {
		long val = minExecutionMs.get();
		return val == Long.MAX_VALUE ? 0 : val;
	}

	/** @return 平均等待耗时（毫秒），无任务完成时返回 0 */
	public double getAverageWaitTimeMs() {
		long count = completedCount.sum();
		if (count == 0) {
			return 0.0;
		}
		return (double) totalWaitMs.sum() / count;
	}

	/** @return 最大等待耗时（毫秒），无任务完成时返回 0 */
	public long getMaxWaitTimeMs() {
		long val = maxWaitMs.get();
		return val == 0 ? 0 : val;
	}

	/** @return 累计进入压力状态的次数（线程池开始扩容） */
	public long getPressureCount() {
		return pressureCount.sum();
	}

	/** @return 累计队列饱和次数（队列满，任务被 force add） */
	public long getSaturatedCount() {
		return saturatedCount.sum();
	}

	private static void updateMax(AtomicLong holder, long value) {
		long current;
		do {
			current = holder.get();
			if (value <= current) {
				return;
			}
		} while (!holder.compareAndSet(current, value));
	}

	private static void updateMin(AtomicLong holder, long value) {
		long current;
		do {
			current = holder.get();
			if (value >= current) {
				return;
			}
		} while (!holder.compareAndSet(current, value));
	}

	/**
	 * 重置所有指标数据。
	 * <p>
	 * Reset all metrics to initial state.
	 */
	public void reset() {
		completedCount.reset();
		rejectedCount.reset();
		totalExecutionMs.reset();
		maxExecutionMs.set(0);
		minExecutionMs.set(Long.MAX_VALUE);
		totalWaitMs.reset();
		maxWaitMs.set(0);
		pressureCount.reset();
		saturatedCount.reset();
	}
}
