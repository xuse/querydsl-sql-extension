package com.github.xuse.querydsl.datatype.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Threads and thread pool utilities. Resolves issues related to anomaly
 * handling and InterruptedException throw.
 * <p>
 * Provides a thread pool implementation that starts expanding early, rather
 * than waiting until the task queue is full.
 * 
 * <p>
 * 线程和线程池工具。解决异常和InterruptedException抛出的问题。
 * <p>
 * 提供了一个提前开始扩容运算的线程池实现，而不是要到任务队列满后才开始扩容。
 */
@Slf4j
public abstract class Threads {

	public static final int WAIT_INTERRUPTED = 0;
	public static final int WAIT_TIMEOUT = -1;
	public static final int WAIT_NOTIFIED = 1;

	/**
	 * Releases the lock on the specified object and suspends the current thread.
	 * This method will wait until:
	 * <li>1. Another thread has called `notify` on the object and the lock is not
	 * owned by any other thread.
	 * <li>2. Another thread has interrupted the current thread.
	 * 
	 * 让出指定对象的锁，并且挂起当前线程。只有当以下情况发生此方法才会返回。
	 * <li>1. 有别的线程notify了对象，并且锁没有被其他线程占用。</li>
	 * <li>2 有别的线程interrupt了当前线程。</li>
	 * 
	 * @param obj the object on which the lock is held
	 *            <p>
	 *            锁所在的对象
	 * @return `true` if the wait ends normally, `false` if it ends due to an
	 *         InterruptedException
	 *         <p>
	 *         等待正常结束返回true，异常结束返回false
	 */
	public static final boolean doWait(Object obj) {
		synchronized (obj) {
			try {
				obj.wait();
				return true;
			} catch (InterruptedException e) {
				log.error("", e);
				return false;
			}
		}
	}

	/**
	 * Call wait method of a object.
	 * <p>
	 * 调用对象的wait方法，并设置超时时间
	 * 
	 * @param obj     the object on which the lock is held
	 *                <p>
	 *                锁所在的对象
	 * @param timeout timeout in milliseconds
	 *                <p>
	 *                超时时间，单位毫秒
	 * @return 超时返回 {@link #WAIT_TIMEOUT}; 正常唤醒{@link #WAIT_NOTIFIED};
	 *         异常打断{@link #WAIT_INTERRUPTED }.
	 * @implNote WAIT_TIMEOUT和WAIT_NOTIFIED这两个状态判断是不准确的。一般来说，应当使用
	 *           {@link #doWait(Object)}方法。
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

	public static final void doNotify(Object obj) {
		synchronized (obj) {
			obj.notify();
		}
	}

	public static final void doNotifyAll(Object obj) {
		synchronized (obj) {
			obj.notifyAll();
		}
	}

	/**
	 * 
	 * do {@link Thread#sleep(long)} method without exception throw.
	 * <p>
	 * 当前线程等待若干毫秒
	 * 
	 * @param l milliseconds. 毫秒数
	 * @return `true` if the wait ends normally, `false` if it ends due to an
	 *         InterruptedException
	 *         <p>
	 *         如果是正常休眠后返回的true，因为InterruptedException被打断的返回false
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
	 * Wait on a CountDownLatch.
	 * `true` if the wait ends normally, `false` if it ends due to an
	 * InterruptedException
	 * <p>
	 * 对CountDownLatch进行等待。
	 * 
	 * @param cl CountDownLatch
	 * @return `true` if the wait ends normally, `false` if it ends due to an
	 *         InterruptedException
	 *         <p>
	 *         如果是正常休眠后返回的true，因为InterruptedException被打断的返回false
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
	 * Wait on a CountDownLatch with timeout.
	 * <p>
	 * 对CountDownLatch进行等待。
	 * 
	 * @param cl          CountDownLatch
	 * @param millseconds timeout in milliseconds
	 *                    <p>
	 *                    超时时间，单位毫秒
	 * @return `true` if the wait ends normally, `false` if it ends due to an
	 *         InterruptedException or reaches the timeout.
	 *         <p>
	 *         如果正常退出true。 如果超时或异常退出false
	 */
	public static boolean doAwait(CountDownLatch cl, long millseconds) {
		try {
			return cl.await(millseconds, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	/**
	 * Join a thread.
	 * <p>
	 * Join到指定的线程进行同步，正常结束返回true
	 * 
	 * @param thread The thread to join.
	 * @return true if the thread ends normally. false if the thread was
	 *         Interrupted.
	 *         <p>
	 *         如果被Interrupt返回false
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
		Thread t = new Thread(runnable);
		t.setName(name);
		t.setDaemon(true);
		t.start();
		return t;
	}

	/**
	 * Execute the runnable in a new thread.
	 * <p>
	 * 在新的线程中运行指定的任务
	 * 
	 * @param runnable Runnable
	 */
	public static final Thread doTask(Runnable runnable) {
		Thread t = new Thread(runnable);
		t.setDaemon(true);
		t.start();
		return t;
	}

	/**
	 * 
	 * Create a thread pool with assigned core size and thread name prefix.
	 * <p>
	 * 用默认策略创建一个线程池
	 * 
	 * @param coreSize         core size of the thread pool.
	 * @param threadNamePrefix the prefix of thread names in the thread pool.
	 * @return ExecutorService
	 */
	public static ExecutorService newFixedThreadPool(int coreSize, String threadNamePrefix) {
		return new ThreadPoolExecutor(coreSize, coreSize, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(coreSize * 2), threadFactory(threadNamePrefix),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}

	/**
	 * Build a front-pressure thread pool. Front pressure means using a queue that
	 * puts pressure on the thread pool expansion action, causing the thread pool to
	 * expand when the pressure threshold is reached.
	 * 
	 * At this time, the task queue still has remaining capacity, which can avoid a
	 * large number of burst traffic triggering the Reject action.
	 * <p>
	 * 创建一个正压线程池。 正压的意思是使用了一个对线程池扩容动作进行正压的队列，当达到压力阈值时，线程池即开始扩容。
	 * 此时任务队列还有剩余容量，可以避免大量突发性流量触发Reject动作。
	 * 
	 * @implNote When the number of tasks reaches half of the queue size, the thread
	 *           pool will add threads beyond the coreSize until the maximum is
	 *           reached (native JDK thread pools will start expanding only after
	 *           the task queue is full). Once the number of threads reaches the
	 *           maximum, if the task generation speed is still greater than the
	 *           consumption speed, tasks will accumulate in the remaining half of
	 *           the queue. The RejectPolicy will only be executed when the bounded
	 *           queue is full.
	 *           <p>
	 *           For scenarios with a large number of burst tasks in a short time,
	 *           this design can advance the timing of thread pool expansion and
	 *           improve business smoothness.
	 *           <p>
	 *           当任务数达到队列的一半时，线程池就会在coreSize之外增加线程直到maximum为止（原生JDK线程池要在任务队列满后才会开始扩容）。
	 *           线程数达到maximum后，任务生成速度依然大于消费速度那么会堆积在队列剩下的一半中。直到有界队列满后，才会执行RejectPolicy。
	 *           <p>
	 *           针对短时间内突发的大量任务的场景，这一设计可以提前线程池扩容的时机，提升业务平滑程度。
	 * 
	 * @param minimum          The minimum thread count / 最小线程数
	 * @param maximum          The maximum thread count / 最大线程数
	 * @param queueSize        The size of task queue / 最大队列数
	 * @param threadNamePrefix the prefix of thread names / 线程名前缀
	 * @return thread pool builded. 线程池
	 */
	public static ExecutorService newThreadPool(int minimum, int maximum, int queueSize, String threadNamePrefix) {
		return newPoolBuilder().coreSize(minimum).maximumSize(maximum).namePrefix(threadNamePrefix).queueSize(queueSize)
				.onReject(new ThreadPoolExecutor.CallerRunsPolicy()).build();
	}

	/**
	 * return a ThreadPoolBuilder. 可以创建一个在任务队列未满前开始扩容的线程池
	 * 
	 * @return 获得一个ThreadPoolBuilder
	 */
	public static ThreadPoolBuilder newPoolBuilder() {
		return new ThreadPoolBuilder();
	}
	
	/**
	 * 异步执行一个任务。
	 * @param <T> Type of result
	 * @param callable callable
	 * @return Future<T> Future
	 */
    public static <T> Future<T> asyncExecute(Callable<T> callable) {
        BasicFuture<T> f=new BasicFuture<T>();
        new Thread(() -> {
            try {
                f.result = callable.call();
                f.completed = true;
                log.info("Async Exec Success:{}", callable);
                Threads.doNotifyAll(f);
            } catch (Exception e) {
                f.ex=e;
                f.completed = true;
                log.error("Async Calling {}", callable, e);
            }
        }).start();
        return f;
    }
    
    static class BasicFuture<T> implements Future<T>{
        private volatile boolean completed;
        private volatile T result;
        private volatile Exception ex;
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean isCancelled() {
            return false;
        }
        @Override
        public boolean isDone() {
            return completed;
        }
        private T getResult() {
            if (this.ex != null) {
                throw new IllegalStateException(this.ex);
            }
            return this.result;
        }
        
        @Override
        public T get() {
            while (!this.completed) {
                doWait(this);
            }
            return getResult();
        }
        
        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            Assert.notNull(unit, "Time unit");
            if (!completed) {
                long wait=unit.toMillis(timeout);
                if(wait<=0) {
                    throw new TimeoutException();
                }
                synchronized (this) {
                    wait();
                }
            }
            return getResult();
        }
    }

	/**
	 * 构造器，用于创建一个在任务队列未满前开始扩容的线程池。
	 */
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
			if (rejectionHandler == null) {
				rejectionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
			}
			ThreadFactory factory = StringUtils.isEmpty(namePrefix) ? Executors.defaultThreadFactory()
					: threadFactory(namePrefix);
			FrontPressureBlockingQueue<Runnable> queue = new FrontPressureBlockingQueue<>(queueSize, queuePressureSize);
			return new ThreadPoolExecutor(coreSize, maximumSize, 60L, TimeUnit.SECONDS, queue, factory,
					new TempQueuedPolicy(queue, rejectionHandler));
		}

		/**
		 * @param namePrefix 线程名前缀 / Thread name prefix
		 * @return ThreadPoolBuilder / 线程池构造器
		 */
		public ThreadPoolBuilder namePrefix(String namePrefix) {
			this.namePrefix = namePrefix;
			return this;
		}

		/**
		 * @param coreSize 最小线程数 / Minimum thread count
		 * @return ThreadPoolBuilder / 线程池构造器
		 */
		public ThreadPoolBuilder coreSize(int coreSize) {
			this.coreSize = coreSize;
			return this;
		}

		/**
		 * @param maximumSize 最大线程数 / Maximum thread count
		 * @return ThreadPoolBuilder / 线程池构造器
		 */
		public ThreadPoolBuilder maximumSize(int maximumSize) {
			this.maximumSize = maximumSize;
			return this;
		}

		/**
		 * Assign the queue pressure size of the building thread pool
		 * 指定构建的线程池任务队列的压力值
		 * 
		 * 
		 * @param queuePressureSize Thread pool expansion threshold. When the task queue
		 *                          reaches the size of `queuePressureSize`, new tasks
		 *                          will cause the number of threads to increase. The
		 *                          `queuePressureSize` should be smaller than
		 *                          {@link #queueSize(int)}. / 线程池扩容阈值。当任务队列达到`queuePressureSize`的大小时，新的任务将导致线程数增加。
		 *                          `queuePressureSize`应该小于{@link #queueSize(int)}。
		 * @return ThreadPoolBuilder / 线程池构造器
		 */
		public ThreadPoolBuilder queuePressureSize(int queuePressureSize) {
			this.queuePressureSize = queuePressureSize;
			return this;
		}

		/**
		 * Assign the queue size of the building thread pool
		 * 
		 * @param queueSize 任务队列最大值
		 * @return ThreadPoolBuilder / 线程池构造器
		 */
		public ThreadPoolBuilder queueSize(int queueSize) {
			this.queueSize = queueSize;
			return this;
		}

		/**
		 * RejectedExecutionHandler of the building thread pool
		 * <p>
		 * 当任务队列满后的拒绝策略。
		 * 
		 * @param rejectionHandler
		 * @return ThreadPoolBuilder / 线程池构造器
		 */
		public ThreadPoolBuilder onReject(RejectedExecutionHandler rejectionHandler) {
			this.rejectionHandler = rejectionHandler;
			return this;
		}
	}

	static final class FrontPressureBlockingQueue<E> extends LinkedBlockingQueue<E> {
		private static final long serialVersionUID = 1L;
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

	static final class TempQueuedPolicy implements RejectedExecutionHandler {
		private final FrontPressureBlockingQueue<Runnable> queue;
		private final RejectedExecutionHandler nextRejectHandler;

		public TempQueuedPolicy(FrontPressureBlockingQueue<Runnable> queue,
				RejectedExecutionHandler nextRejectHandler) {
			this.queue = queue;
			this.nextRejectHandler = nextRejectHandler;
		}

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			if (!queue.offerWithoutPressure(r)) {
				nextRejectHandler.rejectedExecution(r, executor);
			}
		}
	}

	/**
	 * 创建ThreadFactory对象
	 * 
	 * @param name
	 * @return ThreadFactory
	 */
	public static ThreadFactory threadFactory(String name) {
		return new DefaultThreadFactory(name);
	}

	static final class DefaultThreadFactory implements ThreadFactory {
		private final ThreadGroup group;
		private final String namePrefix;
		private final AtomicInteger threadNumber = new AtomicInteger(1);

		public DefaultThreadFactory(String namePrefix) {
			group = Thread.currentThread().getThreadGroup();
			this.namePrefix = namePrefix + "-";
		}

		public Thread newThread(Runnable r) {
			return new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0L);
		}
	}
}