package com.github.xuse.querydsl.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 扩展的 ExecutorService 接口，提供分批并发执行能力。
 * <p>
 * Extended ExecutorService interface with batch concurrent execution.
 */
public interface ExecutorServiceEx extends ExecutorService {

	Logger log = LoggerFactory.getLogger(ExecutorServiceEx.class);

	/**
	 * 优雅关闭线程池。先停止接受新任务，等待已提交任务完成；超时后强制终止。
	 * <p>
	 * Gracefully shuts down the executor. Stops accepting new tasks and waits for
	 * in-progress tasks to complete. If the timeout elapses, forces shutdown.
	 *
	 * @param timeout 等待已提交任务完成的最大时间
	 * @param unit    时间单位
	 * @return {@code true} 如果所有任务在超时前完成；{@code false} 如果超时后被强制终止
	 */
	default boolean shutdownGracefully(long timeout, TimeUnit unit) {
		shutdown();
		try {
			if (!awaitTermination(timeout, unit)) {
				shutdownNow();
				return awaitTermination(timeout, unit);
			}
			return true;
		} catch (InterruptedException e) {
			shutdownNow();
			Thread.currentThread().interrupt();
			return false;
		}
	}
	
	default Future<?> safeSubmit(Runnable task){
		return submit(Threads.asyncCatch(task, null));
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
	
	static interface PoolExecutor extends ExecutorServiceEx{
		int getPoolSize();
		
		int getCorePoolSize();
		
		BlockingQueue<Runnable> getQueue();		
	}
}
