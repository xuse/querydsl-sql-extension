package com.github.xuse.querydsl.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * 线程池 JMX 监控 Bean，暴露线程池运行状态和累计指标。
 * <p>
 * Thread pool JMX monitor bean, exposing pool runtime status and accumulated metrics.
 */
@AllArgsConstructor
public class PoolMonitor implements FrontPressurePoolMXBean {
	@NonNull
	private final ThreadPoolExecutor pool;
	@NonNull
	private final LinkedBlockingQueue<Runnable> queue;

	String name;

	private int queueCapacity;

	private int queuePressureSize;

	@NonNull
	private final PoolMetrics metrics;

	@Override
	public int getCoreSize() {
		return pool.getCorePoolSize();
	}

	@Override
	public int getCurrentSize() {
		return pool.getPoolSize();
	}

	@Override
	public int getMaximumSize() {
		return pool.getMaximumPoolSize();
	}

	@Override
	public int getLargestSize() {
		return pool.getLargestPoolSize();
	}

	@Override
	public int getQueueLength() {
		return queue.size();
	}

	@Override
	public int getQueueMaximumLength() {
		return queueCapacity;
	}

	@Override
	public int getQueuePressureLength() {
		return queuePressureSize;
	}

	@Override
	public int getActiveCount() {
		return pool.getActiveCount();
	}

	@Override
	public String getMetricsJson() {
		return metrics.toJson();
	}

	@Override
	public void setMaximumSize(int size) {
		int from = pool.getMaximumPoolSize();
		if (from > size) {
			throw Exceptions.illegalArgument("RISK is too high to adjust pool size from {} to {} once in a PRD environment.", from, size);
		}
		pool.setMaximumPoolSize(size);
	}

	@Override
	public void setCoreSize(int size) {
		int max = pool.getMaximumPoolSize();
		if (size > max) {
			size = max;
		}
		int from = pool.getCorePoolSize();
		if (from > size) {
			throw Exceptions.illegalArgument("RISK is too high to adjust core pool size from {} to {} once in a PRD environment.", from, size);
		}
		pool.setCorePoolSize(size);
	}

	@Override
	public void resetMetrics() {
		metrics.reset();
	}
}
