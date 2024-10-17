package com.github.xuse.querydsl.datatype.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 线程和线程池工具。解决异常和InterruptedException抛出的问题。
 */
@Slf4j
public abstract class Threads {
	
	public static final int WAIT_INTERRUPTED = 0;
	public static final int WAIT_TIMEOUT = -1;
	public static final int WAIT_NOTIFIED = 1;

	

	/**
	 * 让出指定对象的锁，并且挂起当前线程。只有当—— <li>1. 有别的线程notify了对象，并且锁没有被其他线程占用。</li> <li>2
	 * 有别的线程interrupt了当前线程。</li> 此方法才会返回。
	 * @param obj 锁所在的对象
	 * @return 等待正常结束返回true，异常结束返回false
	 */
	public static final boolean doWait(Object obj) {
		synchronized (obj) {
			try {
				obj.wait();
				return true;
			} catch (InterruptedException e) {
				log.error("",e);
				return false;
			}
		}
	}
	
	/**
	 * 调用对象的wait方法，并设置超时时间
	 * @param obj 锁所在的对象
	 * @param timeout 超时时间，单位毫秒
	 * @return  超时返回 {@link #WAIT_TIMEOUT};
	 * 正常唤醒{@link #WAIT_NOTIFIED};
	 * 异常打断{@link #WAIT_INTERRUPTED }. 
	 */
	public static final int doWait(Object obj, long timeout) {
		synchronized (obj) {
			try {
				long expectTimeout = System.currentTimeMillis() + timeout;
				obj.wait(timeout);
				return System.currentTimeMillis() >= expectTimeout ? WAIT_TIMEOUT : WAIT_NOTIFIED;
			} catch (InterruptedException e) {
				return WAIT_INTERRUPTED;
			}
		}
	}


	/**
	 * 唤醒一个在等待obj锁的线程
	 */
	public static final void doNotify(Object obj) {
		synchronized (obj) {
			obj.notify();
		}
	}

	/**
	 * 唤醒所有在等待obj的锁的线程。
	 * 
	 * @param obj
	 */
	public static final void doNotifyAll(Object obj) {
		synchronized (obj) {
			obj.notifyAll();
		}
	}

	/**
	 * 当前线程等待若干毫秒
	 * 
	 * @param l
	 *            毫秒数
	 * @return 如果是正常休眠后返回的true，因为InterruptedException被打断的返回false
	 */
	public static final boolean doSleep(long l) {
		if (l <= 0)
			return true;
		try {
			Thread.sleep(l);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

	/**
	 * 对CountDownLatch进行等待。如果正常退出返回true，异常退出返回false
	 * @param cl CountDownLatch
	 * @return 果正常退出返回true，异常退出返回false
	 */
	public static boolean doAwait(CountDownLatch cl) {
		try {
			cl.await();
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

	/**
	 * 对CountDownLatch进行等待。如果正常退出返回true，超时或者异常退出返回false
	 * @param cl CountDownLatch
	 * @param millseconds 超时时间，单位毫秒
	 * @return 如果正常退出true。 如果超时或异常退出false
	 */
	public static boolean doAwait(CountDownLatch cl,long millseconds) {
		try {
			return cl.await(millseconds, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	/**
	 * Join到指定的线程进行同步，正常结束返回true
	 * @param thread
	 * @return 如果被Interrupt返回false
	 */
	public static boolean doJoin(Thread thread) {
		try {
			thread.join();
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}
	
	public static Thread doTask(String name, Runnable runnable) {
		Thread t=new Thread(runnable);
		t.setName(name);
		t.setDaemon(true);
		t.start();
		return t;
	}
	
	/**
	 * 在新的线程中运行指定的任务
	 * @param runnable Runnable
	 */
	public static final Thread doTask(Runnable runnable) {
		Thread t = new Thread(runnable);
		t.setDaemon(true);
		t.start();
		return t;
	}
	
	
	/**
	 * 用默认策略创建一个线程池
	 * @param coreSize
	 * @param threadNamePrefix
	 */
	public static ExecutorService newFixedThreadPool(int coreSize, String threadNamePrefix) {
		return new ThreadPoolExecutor(coreSize, coreSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(coreSize * 2), threadFactory(threadNamePrefix),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}
	
	/**
	 * 创建一个正压线程池。
	 * 正压的意思是使用了一个对线程池扩容动作进行正压的队列，当达到压力阈值时，线程池即开始扩容。
	 * 此时任务队列还有剩余容量，可以避免大量突发性流量触发Reject动作。
	 * @implNote
	 *  当任务数达到队列的一半时，线程池就会在coreSize之外增加线程直到maximum为止（原生JDK线程池要在任务队列满后才会开始扩容）。
	 *  线程数达到maximum后，任务生成速度依然大于消费速度那么会堆积在队列剩下的一半中。直到有界队列满后，才会执行RejectPolicy。
	 *  <p>
	 *  针对短时间内突发的大量任务的场景，这一设计可以提前线程池扩容的时机，提升业务平滑程度。
	 *  
	 * @param minium 最小线程数
	 * @param maxium 最大线程数
	 * @param queueSize 最大队列数
	 * @param threadNamePrefix 线程名前缀
	 * @return 线程池
	 */
	public static ExecutorService newThreadPool(int minium,int maxium,int queueSize,String threadNamePrefix) {
		return newPoolBuilder()
				.coreSize(minium)
				.maximumSize(maxium)
				.namePrefix(threadNamePrefix)
				.queueSize(queueSize)
				.onReject(new ThreadPoolExecutor.CallerRunsPolicy()).build();
	}
	
	/**
	 * return a ThreadPoolBuilder. 可以创建一个在任务队列未满前开始扩容的线程池
	 * @return 获得一个ThreadPoolBuilder
	 */
	public static ThreadPoolBuilder newPoolBuilder() {
		return new ThreadPoolBuilder();
	}
	
	public static class ThreadPoolBuilder {
		private String namePrefix;
		private int coreSize;
		private int maximumSize;
		private int queueSize = Integer.MAX_VALUE;
		private int queuePressureSize = 0;
		private RejectedExecutionHandler rejectionHandler;

		public ThreadPoolExecutor build() {
			if (queueSize <= 0) {
				queueSize = Integer.MAX_VALUE;
			}
			if (queuePressureSize <= 0) {
				queuePressureSize = queueSize >>> 1;
			} else if (queuePressureSize > queueSize) {
				throw Exceptions.illegalArgument("Queue Pressure Size({}) must be less than the Queue Size({}).",
						queuePressureSize, queueSize);
			}
			if(rejectionHandler==null) {
				rejectionHandler=new ThreadPoolExecutor.CallerRunsPolicy();
			}
			ThreadFactory factory = StringUtils.isEmpty(namePrefix) ? Executors.defaultThreadFactory()
					: threadFactory(namePrefix);
			FrontPressureBlockingQueue<Runnable> queue = new FrontPressureBlockingQueue<>(queueSize, queuePressureSize);
			return new ThreadPoolExecutor(coreSize, maximumSize,
	                 60L, TimeUnit.SECONDS,
	                 queue,
	                 factory,
	                 new TempQueuedPolicy(queue, rejectionHandler));
		}

		public ThreadPoolBuilder namePrefix(String namePrefix) {
			this.namePrefix = namePrefix;
			return this;
		}
		/**
		 * @param coreSize  最小线程数
		 * @return ThreadPoolBuilder
		 */
		public ThreadPoolBuilder coreSize(int coreSize) {
			this.coreSize = coreSize;
			return this;
		}
		/**
		 * @param maximumSize  最大线程数
		 * @return ThreadPoolBuilder
		 */
		public ThreadPoolBuilder maximumSize(int maximumSize) {
			this.maximumSize = maximumSize;
			return this;
		}
		
		/**
		 * @param queuePressureSize 线程池扩容阈值。在任务队列达到queuePressureSize大小后，新增任务会引起线程数扩大。queuePressureSize应当小于{@link #queueSize}.
		 * @return ThreadPoolBuilder
		 */
		public ThreadPoolBuilder queuePressureSize(int queuePressureSize) {
			this.queuePressureSize = queuePressureSize;
			return this;
		}
		/**
		 * @param queueSize  任务队列最大值
		 * @return ThreadPoolBuilder
		 */
		public ThreadPoolBuilder queueSize(int queueSize) {
			this.queueSize = queueSize;
			return this;
		}
		/**
		 * 当任务队列满后的拒绝策略。
		 * @param rejectionHandler
		 * @return
		 */
		public ThreadPoolBuilder onReject(RejectedExecutionHandler rejectionHandler) {
			this.rejectionHandler = rejectionHandler;
			return this;
		}
	}
	
	
	public static final class FrontPressureBlockingQueue<E> extends LinkedBlockingQueue<E>{
		private final int pressureSize;
		public FrontPressureBlockingQueue(int queueSize, int pressureSize) {
			super(queueSize);
			this.pressureSize = pressureSize;
		}
		@Override
		public boolean offer(E e) {
			if (size() >= pressureSize)
	            return false;
			return super.offer(e);
		}
		public boolean offerWithoutPressure(E e) {
			return super.offer(e);
		}
	}
	
	public static final class TempQueuedPolicy implements RejectedExecutionHandler {
		private final FrontPressureBlockingQueue<Runnable> queue;
		private final RejectedExecutionHandler nextRejectHandler;
		
		public TempQueuedPolicy(FrontPressureBlockingQueue<Runnable> queue, RejectedExecutionHandler nextRejectHandler) {
			this.queue = queue;
			this.nextRejectHandler = nextRejectHandler;
		}
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			if(!queue.offerWithoutPressure(r)) {
				nextRejectHandler.rejectedExecution(r, executor);
			};
		}
	}

	/**
	 * 创建ThreadFactory对象
	 * @param name
	 */
	public static ThreadFactory threadFactory(String name) {
		return new DefaultThreadFactory(name);
	}

	static final class DefaultThreadFactory implements ThreadFactory {
		private final ThreadGroup group;
		private final String namePrefix;
		private final AtomicInteger threadNumber = new AtomicInteger(1);

		public DefaultThreadFactory(String namePrefix) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			this.namePrefix = namePrefix + "-";
		}

		public Thread newThread(Runnable r) {
			return new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0L);
		}
	}
}