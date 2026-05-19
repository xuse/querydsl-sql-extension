package com.github.xuse.querydsl.util;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.github.xuse.querydsl.jmx.IntrospectedMXBean;
import com.github.xuse.querydsl.util.Exceptions.WrapException;

import lombok.AllArgsConstructor;
import lombok.NonNull;
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
				long startNanos = System.nanoTime();
				obj.wait(timeout);
				long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L;
				return elapsedMillis >= timeout ? WAIT_TIMEOUT : WAIT_NOTIFIED;
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

	/**
	 * 将StackTrace转换为字符串，添加到指定的Appendable对象中。
	 * @param stacks stack trace.
	 * @param skipLines ignore first n lines.
	 * @param output Appendable.
	 * @see Appendable
	 */
	public static void toStackTraceString(StackTraceElement[] stacks, int skipLines,Appendable output) {
		String newLine = ")\n";
		try {
			for (int i = skipLines; i < stacks.length; i++) {
				StackTraceElement e = stacks[i];
				output.append('\t').append(e.getClassName()).append('.').append(e.getMethodName());
				output.append("(").append(e.getFileName()).append(':').append(String.valueOf(e.getLineNumber())).append(newLine);
			}	
		}catch(IOException e) {
			throw new WrapException(e);
		}
	}
	
	/**
	 * 将StackTrace转换为字符串。
	 * @param stacks StackTraceElement
	 * @param skipLines 跳过的Stack，传入1或以上的数字。
	 * @return text of the StackTrace
	 */
	public static String toStackTraceString(StackTraceElement[] stacks, int skipLines) {
		StringBuilder sb = new StringBuilder();
		toStackTraceString(stacks,skipLines,sb);
		return sb.toString();
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
	public static ExecutorServiceEx newFixedThreadPool(int coreSize, String threadNamePrefix) {
		return wrapPoolEx(new ThreadPoolExecutor(coreSize, coreSize, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(coreSize * 2), threadFactory(threadNamePrefix),
				new ThreadPoolExecutor.CallerRunsPolicy()));
	}

	private static ExecutorServiceEx wrapPoolEx(final ThreadPoolExecutor pool) {
		return new ExecutorServiceExImpl(pool);
	}
	
	static final class ExecutorServiceExImpl extends AbstractExecutorService implements ExecutorServiceEx{
		private final ExecutorService pool;
		ExecutorServiceExImpl(ExecutorService pool){
			this.pool = pool;
		}
		@Override
		public void shutdown() {
			pool.shutdown();
		}

		@Override
		public List<Runnable> shutdownNow() {
			return pool.shutdownNow();
		}

		@Override
		public boolean isShutdown() {
			return pool.isShutdown();
		}

		@Override
		public boolean isTerminated() {
			return pool.isTerminated();
		}

		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
			return pool.awaitTermination(timeout, unit);
		}

		@Override
		public void execute(Runnable command) {
			pool.execute(command);
		}
		@Override
		public Future<?> submit(Runnable task) {
			return pool.submit(task);
		}
		@Override
		public <T> Future<T> submit(Runnable task, T result) {
			return pool.submit(task, result);
		}
		@Override
		public <T> Future<T> submit(Callable<T> task) {
			return pool.submit(task);
		}
		@Override
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
				throws InterruptedException, ExecutionException {
			return pool.invokeAny(tasks);
		}
		@Override
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			return pool.invokeAny(tasks, timeout, unit);
		}
		@Override
		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
			return pool.invokeAll(tasks);
		}
		@Override
		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
				throws InterruptedException {
			return pool.invokeAll(tasks, timeout, unit);
		}
		
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
	public static ExecutorServiceEx newThreadPool(int minimum, int maximum, int queueSize, String threadNamePrefix) {
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
	
	public interface ExecutorServiceEx extends ExecutorService{
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
		default <T, R> List<R> batchSubmit(List<T> items, Function<T, R> task, int concurrencyLevel){
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
		default <T> void batchSubmit(List<T> items, java.util.function.Consumer<T> task, int concurrencyLevel) {
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
	
	/**
	 * 异步执行一个任务。
	 * @param <T> Type of result
	 * @param callable callable
	 * @return Future<T> Future
	 */
    public static <T> Future<T> asyncExecute(Callable<T> callable) {
        BasicFuture<T> f=new BasicFuture<>();
        new Thread(() -> {
            try {
                f.result = callable.call();
                f.completed = true;
                log.info("Async Exec Success:{}", callable);
            } catch (Exception e) {
                f.ex=e;
                f.completed = true;
                log.error("Async Calling {}", callable, e);
            }finally {
            	doNotifyAll(f);
            }
        }).start();
        return f;
    }
    
	
	/**
	 * Start a daemon thread.
	 * @param name thread name
	 * @param runnable daemon task.
	 * @return Thread
	 */
	public static Thread startDaemon(String name, Runnable runnable) {
		Thread t = new Thread(runnable);
		if (name != null && !name.isEmpty()) {
			t.setName(name);
		}
		t.setDaemon(true);
		t.start();
		return t;
	}
    
    static class BasicFuture<T> implements Future<T>{
        volatile boolean completed;
        volatile T result;
        volatile Exception ex;
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
            	throw Exceptions.toRuntime(this.ex);
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
            long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
			synchronized (this) {
				while (!completed) {
					long remaining = deadline - System.currentTimeMillis();
					if (remaining <= 0) {
						throw new TimeoutException();
					}
					wait(remaining);
				}
			}
            return getResult();
        }
    }
    
    @AllArgsConstructor
	public static class PoolMonitor implements FrontPressurePoolMXBean {
    	@NonNull
    	private final ThreadPoolExecutor pool;
    	@NonNull
    	private final FrontPressureBlockingQueue<Runnable> queue;

    	private String name;
    	
    	private int queueCapacity;
    	
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
			return queue.pressureSize;
		}
		@Override
		public int getActiveCount() {
			return pool.getActiveCount();
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
			int max=pool.getMaximumPoolSize();
			if (size > max) {
				//调节不可以大于maximumSize
				size = max;
			}
			int from = pool.getCorePoolSize(); 
			if (from > size) {
				throw Exceptions.illegalArgument("RISK is too high to adjust core pool size from {} to {} once in a PRD environment.", from, size);
			}
			pool.setCorePoolSize(size);
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
		private ThreadPoolListener listener = ThreadPoolListener.EMPTY;
		private boolean noJmx;

		public ExecutorServiceEx build() {
			if (coreSize <= 0) {
				throw Exceptions.illegalArgument("Core size({}) must be positive.", coreSize);
			}
			if (maximumSize <= 0) {
				throw Exceptions.illegalArgument("Maximum size({}) must be positive.", maximumSize);
			}
			if (maximumSize < coreSize) {
				throw Exceptions.illegalArgument("Maximum size({}) must be >= core size({}).", maximumSize, coreSize);
			}
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
			FrontPressureBlockingQueue<Runnable> queue = new FrontPressureBlockingQueue<>(queueSize, queuePressureSize, listener);
			ThreadPoolExecutor pool = new ThreadPoolExecutor(coreSize, maximumSize, 60L, TimeUnit.SECONDS, queue, factory,
					new TempQueuedPolicy(queue, rejectionHandler));
			if(!noJmx) {
				PoolMonitor monitor=new PoolMonitor(pool, queue, namePrefix, queueSize);
				registerJmx(monitor);	
			}
			return wrapPoolEx(pool);
		}

		private void registerJmx(PoolMonitor monitor) {
			String name = monitor.name;
			if (name == null) {
				name = "";
			}
			name = name + "-" + StringUtils.truncate(StringUtils.randomString(), 5);
			try {
				ObjectName mxbeanName = new ObjectName("querydsl-ext.utils:type=ThreadPool-"+name);
				MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
				mbs.registerMBean(new IntrospectedMXBean(monitor, FrontPressurePoolMXBean.class), mxbeanName);	
				log.info("Thread Pool {} was registered in JMX Server.", name);
			}catch(Exception ex) {
				log.error("JMX Register fail. name={}", name, ex);
			}
		}
		
		/**
		 * 注册一个监听器
		 * @param listener 监听器
		 * @return this
		 */
		public ThreadPoolBuilder withListener(ThreadPoolListener listener) {
			this.listener = listener;
			return this;
		}
		
		/**
		 * 不自动注册JMX Bean.
		 * @return this
		 */
		public ThreadPoolBuilder noJMX() {
			this.noJmx = true;
			return this;
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
		private final ThreadPoolListener listener;

		public FrontPressureBlockingQueue(int queueSize, int pressureSize,ThreadPoolListener listener) {
			super(queueSize);
			this.pressureSize = pressureSize;
			this.listener = listener;
		}

		@Override
		public boolean offer(E e) {
			int size = size();
			boolean result = size < pressureSize && super.offer(e);
			if(result) {
				 listener.onTaskAdd(size);
			}
			return result;
		}

		public boolean offerWithoutPressure(E e) {
			boolean result=super.offer(e);
			int size = size();
			if (result) {
				listener.onTaskForceAdd(size);
			} else {
				listener.onTaskReject(size);
			}
			return result;
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
	 * @param name The name prefix of thread.
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
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0L);
			t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}
}