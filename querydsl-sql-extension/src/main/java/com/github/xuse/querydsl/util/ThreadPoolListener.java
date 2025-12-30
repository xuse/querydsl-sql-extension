package com.github.xuse.querydsl.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 监听线程池队列状态
 */
public abstract class ThreadPoolListener {
	
	public static final ThreadPoolListener EMPTY=new ThreadPoolListener() {protected void onStatusChange(int state) {}};
	
	private final AtomicInteger ctl = new AtomicInteger();
	
	/**
	 * 线程队列正常
	 */
	public static final int NORMAL = 0;
	/**
	 * 线程队列较大，线程池扩容中
	 */
	public static final int PRESSED = 1;
	/**
	 * 线程队列满，任务被拒绝
	 */
	public static final int FULL = 2;
	
	
	public final void onTaskAdd(int queueSize) {
		int value = ctl.get();
		if (value!=NORMAL && ctl.compareAndSet(value, NORMAL)) {
			onStatusChange(NORMAL);
		}
	}
	
	public final void onTaskForceAdd(int queueSize) {
		int value = ctl.get();
		if (value!=PRESSED && ctl.compareAndSet(value, PRESSED)) {
			onStatusChange(PRESSED);
		}
	}

	public final void onTaskReject(int queueSize) {
		int value = ctl.get();
		if (value!=FULL && ctl.compareAndSet(value, FULL)) {
			onStatusChange(FULL);
		}
	}
	
	/**
	 * 监听，线程池进入哪种状态。当线程池恢复时。需要依赖新任务添加来触发，因此监听会有滞后。
	 * @param state 状态
	 */
	protected abstract void onStatusChange(int state);
}
