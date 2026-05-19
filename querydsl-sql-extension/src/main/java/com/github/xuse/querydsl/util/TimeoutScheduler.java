package com.github.xuse.querydsl.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 延迟初始化的全局超时调度器。单线程 daemon，仅负责发送 cancel 信号，极轻量。
 * <p>
 * Lazily-initialized global timeout scheduler. Single daemon thread, only responsible for
 * sending cancel signals. Near-zero overhead when idle.
 */
final class TimeoutScheduler {
	static final ScheduledExecutorService INSTANCE = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread t = new Thread(r, "pool-timeout-guard");
		t.setDaemon(true);
		t.setPriority(Thread.NORM_PRIORITY);
		return t;
	});

	private TimeoutScheduler() {
	}
}
