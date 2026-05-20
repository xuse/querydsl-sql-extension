package com.github.xuse.querydsl.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 扩展的 ExecutorService 接口，提供带超时控制的任务提交和分批并发执行能力。
 * <p>
 * Extended ExecutorService interface with timeout-aware submission and batch execution.
 */
public interface ExecutorServiceEx extends ExecutorService {

	Logger log = LoggerFactory.getLogger(ExecutorServiceEx.class);

	/**
	 * 提交一个带超时控制的任务。超时后任务将被中断取消。
	 * <p>
	 * Submit a task with timeout control. The task will be cancelled (interrupted) if it exceeds the timeout.
	 *
	 * @param <T>     返回值类型
	 * @param task    待执行的任务
	 * @param timeout 超时时间
	 * @param unit    超时时间单位
	 * @return Future 对象，可用于获取结果或检查状态
	 */
	default <T> Future<T> submitWithTimeout(Callable<T> task, long timeout, TimeUnit unit) {
		Future<T> future = submit(task);
		ScheduledFuture<?> cancelTask = TimeoutScheduler.INSTANCE.schedule(
				() -> future.cancel(true), timeout, unit);
		return new TimeoutAwareFuture<>(future, cancelTask);
	}

	/**
	 * 提交一个带超时控制的 Runnable 任务。超时后任务将被中断取消。
	 *
	 * @param task    待执行的任务
	 * @param timeout 超时时间
	 * @param unit    超时时间单位
	 * @return Future 对象
	 */
	default Future<?> submitWithTimeout(Runnable task, long timeout, TimeUnit unit) {
		Future<?> future = submit(task);
		ScheduledFuture<?> cancelTask = TimeoutScheduler.INSTANCE.schedule(
				() -> future.cancel(true), timeout, unit);
		return new TimeoutAwareFuture<>(future, cancelTask);
	}

	/**
	 * 分队列并发执行任务，控制最大并发度。将任务按 round-robin 分配到 N 个独立队列，
	 * 每个线程串行消费自己的队列，避免"等最慢任务"导致并行度退化。
	 * <p>
	 * Distributes items into N independent queues (round-robin) and processes each queue
	 * in a dedicated thread. This avoids the "wait for slowest" problem of batch-based approaches.
	 *
	 * @implNote
	 * 传入函数应自行处理执行异常。如某个任务异常，将不会添加到结果集List.
	 * @param items 待处理的数据列表
	 * @param task 将单个数据项转换为结果的函数
	 * @param concurrencyLevel 最大并发数（队列数）
	 * @param <T> 输入类型
	 * @param <R> 输出类型
	 * @return 所有非null结果的列表（顺序不保证）
	 */
	default <T, R> List<R> batchSubmit(List<T> items, Function<T, R> task, int concurrencyLevel) {
		if (items == null || items.isEmpty()) {
			return Collections.emptyList();
		}
		if (concurrencyLevel <= 1) {
			// 单线程直接串行执行
			List<R> results = new ArrayList<>();
			for (T item : items) {
				try {
					R result = task.apply(item);
					if (result != null) {
						results.add(result);
					}
				} catch (Exception ex) {
					log.error("Batch concurrent executing error", ex);
				}
			}
			return results;
		}
		int parallelism = Math.min(concurrencyLevel, items.size());
		// 按 round-robin 分配到各队列
		@SuppressWarnings("unchecked")
		List<T>[] queues = new List[parallelism];
		for (int i = 0; i < parallelism; i++) {
			queues[i] = new ArrayList<>();
		}
		for (int i = 0; i < items.size(); i++) {
			queues[i % parallelism].add(items.get(i));
		}
		// 每个队列提交一个任务，串行消费队列内所有 item
		List<CompletableFuture<List<R>>> futures = new ArrayList<>(parallelism);
		for (List<T> queue : queues) {
			if (queue.isEmpty()) {
				continue;
			}
			futures.add(CompletableFuture.supplyAsync(() -> {
				List<R> partial = new ArrayList<>();
				for (T item : queue) {
					try {
						R result = task.apply(item);
						if (result != null) {
							partial.add(result);
						}
					} catch (Exception ex) {
						log.error("Batch concurrent executing error", ex);
					}
				}
				return partial;
			}, this));
		}
		// 汇总结果
		List<R> results = new ArrayList<>();
		for (CompletableFuture<List<R>> f : futures) {
			try {
				results.addAll(f.join());
			} catch (Exception ex) {
				log.error("Batch concurrent executing error", ex);
			}
		}
		return results;
	}

	/**
	 * 分队列并发执行无返回值任务，控制最大并发度。将任务按 round-robin 分配到 N 个独立队列，
	 * 每个线程串行消费自己的队列。
	 *
	 * @implNote 失败的 item 会被跳过，仅记录日志
	 * @param items            待处理的数据列表
	 * @param task             处理单个数据项的逻辑
	 * @param concurrencyLevel 最大并发数（队列数）
	 * @param <T>              输入类型
	 */
	default <T> void batchSubmit(List<T> items, Consumer<T> task, int concurrencyLevel) {
		if (items == null || items.isEmpty()) {
			return;
		}
		if (concurrencyLevel <= 1) {
			for (T item : items) {
				try {
					task.accept(item);
				} catch (Exception ex) {
					log.error("Batch concurrent executing error", ex);
				}
			}
			return;
		}
		int parallelism = Math.min(concurrencyLevel, items.size());
		@SuppressWarnings("unchecked")
		List<T>[] queues = new List[parallelism];
		for (int i = 0; i < parallelism; i++) {
			queues[i] = new ArrayList<>();
		}
		for (int i = 0; i < items.size(); i++) {
			queues[i % parallelism].add(items.get(i));
		}
		CompletableFuture<?>[] futures = new CompletableFuture[parallelism];
		int idx = 0;
		for (List<T> queue : queues) {
			if (queue.isEmpty()) {
				continue;
			}
			futures[idx++] = CompletableFuture.runAsync(() -> {
				for (T item : queue) {
					try {
						task.accept(item);
					} catch (Exception ex) {
						log.error("Batch concurrent executing error", ex);
					}
				}
			}, this);
		}
		try {
			CompletableFuture.allOf(java.util.Arrays.copyOf(futures, idx)).join();
		} catch (Exception ex) {
			log.error("Batch concurrent executing error", ex);
		}
	}
}
