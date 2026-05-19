package com.github.xuse.querydsl.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 包装 Future，在任务正常完成时自动取消超时定时器，避免无谓的 cancel 调用。
 * <p>
 * Wraps a Future with a scheduled cancel task. Automatically cancels the timer
 * when the task completes normally.
 */
final class TimeoutAwareFuture<T> implements Future<T> {
	private final Future<T> delegate;
	private final ScheduledFuture<?> cancelTask;

	TimeoutAwareFuture(Future<T> delegate, ScheduledFuture<?> cancelTask) {
		this.delegate = delegate;
		this.cancelTask = cancelTask;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		cancelTask.cancel(false);
		return delegate.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return delegate.isCancelled();
	}

	@Override
	public boolean isDone() {
		return delegate.isDone();
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		try {
			return delegate.get();
		} finally {
			cancelTask.cancel(false);
		}
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		try {
			return delegate.get(timeout, unit);
		} finally {
			cancelTask.cancel(false);
		}
	}
}
