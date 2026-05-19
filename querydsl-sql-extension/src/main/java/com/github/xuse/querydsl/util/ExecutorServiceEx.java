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
import java.util.stream.Collectors;

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
	 * 分批并发执行任务，控制最大并发度。每批任务全部完成后再提交下一批，不会额外占用线程池资源。
	 * @implNote
	 * 传入函数应自行处理执行异常。如某个任务异常，将不会添加到结果集List.
	 * @param items 待处理的数据列表
	 * @param task 将单个数据项转换为结果的函数
	 * @param concurrencyLevel 最大并发数（每批提交的任务数）
	 * @param <T> 输入类型
	 * @param <R> 输出类型
	 * @return 所有非null结果的列表（顺序不保证）
	 */
	default <T, R> List<R> batchSubmit(List<T> items, Function<T, R> task, int concurrencyLevel) {
		if (items == null || items.isEmpty()) {
			return Collections.emptyList();
		}
		List<R> results = new ArrayList<>();
		for (int i = 0; i < items.size(); i += concurrencyLevel) {
			List<T> batch = items.subList(i, Math.min(i + concurrencyLevel, items.size()));
			List<CompletableFuture<R>> futures = batch.stream()
					.map(item -> CompletableFuture.supplyAsync(() -> task.apply(item), this))
					.collect(Collectors.toList());
			for (CompletableFuture<R> f : futures) {
				try {
					R result = f.join();
					if (result != null) {
						results.add(result);
					}
				} catch (Exception ex) {
					log.error("Batch concurrent executing error", ex);
				}
			}
		}
		return results;
	}

	/**
	 * 分批并发执行无返回值任务，控制最大并发度。
	 *
	 * @implNote 失败的 item 会被跳过，仅记录日志
	 * @param items            待处理的数据列表
	 * @param task             处理单个数据项的逻辑
	 * @param concurrencyLevel 最大并发数（每批提交的任务数）
	 * @param <T>              输入类型
	 */
	default <T> void batchSubmit(List<T> items, Consumer<T> task, int concurrencyLevel) {
		if (items == null || items.isEmpty()) {
			return;
		}
		for (int i = 0; i < items.size(); i += concurrencyLevel) {
			List<T> batch = items.subList(i, Math.min(i + concurrencyLevel, items.size()));
			CompletableFuture<?>[] futures = batch.stream()
					.map(item -> CompletableFuture.runAsync(() -> task.accept(item), this))
					.toArray(CompletableFuture<?>[]::new);
			try {
				CompletableFuture.allOf(futures).join();
			} catch (Exception ex) {
				log.error("Batch concurrent executing error", ex);
			}
		}
	}
}
