package com.github.xuse.querydsl.util;

import java.util.concurrent.Callable;

/**
 * 任务装饰器接口。用于在任务提交到线程池前对其进行包装，典型用途包括：
 * <ul>
 *   <li>传递 MDC 上下文（日志追踪 traceId）</li>
 *   <li>传递 SecurityContext（用户身份）</li>
 *   <li>传递租户信息（多租户场景）</li>
 * </ul>
 * 
 * Task decorator interface. Wraps tasks before submission to the thread pool.
 * Typical use cases include propagating MDC context, SecurityContext, or tenant info.
 *
 * <p>Example (MDC propagation):
 * <pre>{@code
 * TaskDecorator mdcDecorator = task -> {
 *     Map<String, String> ctx = MDC.getCopyOfContextMap();
 *     return () -> {
 *         MDC.setContextMap(ctx);
 *         try { task.run(); } finally { MDC.clear(); }
 *     };
 * };
 * ExecutorServiceEx pool = Threads.newPoolBuilder()
 *     .coreSize(4).maximumSize(16).queueSize(100)
 *     .taskDecorator(mdcDecorator)
 *     .build();
 * }</pre>
 */
@FunctionalInterface
public interface TaskDecorator {
	/**
	 * 不做任何装饰的默认实例。
	 */
	TaskDecorator NONE = new TaskDecorator() {
		@Override
		public Runnable decorate(Runnable task) {
			return task;
		}
		@Override
		public <T> Callable<T> decorate(Callable<T> task) {
			return task;
		}
	};

	/**
	 * 装饰一个 Runnable 任务。
	 * @param task 原始任务
	 * @return 装饰后的任务
	 */
	Runnable decorate(Runnable task);

	/**
	 * 装饰一个 Callable 任务。默认实现将 Callable 适配为 Runnable 进行装饰。
	 * <p>
	 * 如果装饰器需要对 Callable 做特殊处理，可以覆盖此方法。
	 * @param <T> 返回值类型
	 * @param task 原始任务
	 * @return 装饰后的任务
	 */
	default <T> Callable<T> decorate(Callable<T> task) {
		// 将 Callable 包装为 Runnable 以复用 decorate(Runnable) 的上下文传递逻辑
		@SuppressWarnings("unchecked")
		Object[] holder = new Object[1];
		Exception[] error = new Exception[1];
		Runnable asRunnable = () -> {
			try {
				holder[0] = task.call();
			} catch (Exception e) {
				error[0] = e;
			}
		};
		Runnable decorated = decorate(asRunnable);
		return () -> {
			decorated.run();
			if (error[0] != null) {
				throw error[0];
			}
			@SuppressWarnings("unchecked")
			T result = (T) holder[0];
			return result;
		};
	}
}
