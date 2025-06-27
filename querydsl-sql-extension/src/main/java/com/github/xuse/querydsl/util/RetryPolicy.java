package com.github.xuse.querydsl.util;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

/**
 * <H3>English</H3>
 * A simple retry framework supporting the following features:
 * <ul>
 * <li>Specify the maximum number of retry attempts</li>
 * <li>No delay between retries</li>
 * <li>Fixed delay between retries</li>
 * <li>Exponential backoff delay (waiting time doubles each attempt)</li>
 * <li>Jitter (random variation) added to delay intervals</li>
 * <li>Retry only for specific exception types</li>
 * </ul>
 * <p>
 * <H3>Chinese</H3>
 * 一个简单的重试框架，支持以下特性：
 * <ul>
 * <li>指定最大重试次数</li>
 * <li>无等待重试</li>
 * <li>固定等待时间</li>
 * <li>指数退避（每次等待时间翻倍）</li>
 * <li>为等待时间增加随机抖动</li>
 * <li>仅对指定异常类型进行重试</li>
 * </ul>
 * <p>
 * 
 * Example：
 * 
 * <pre>
 * RetryPolicy retryPolicy = RetryPolicy.builder()
 * 		.withMaxAttempts(3)
 * 		.withRetryFor(IOException.class)
 * 		.withFixedDelay(Duration.ofSeconds(1))
 * 		.build();
 * retryPolicy.execute(() -> task.call());
 * </pre>
 */
@Slf4j
public class RetryPolicy {
	/** 最大重试次数 The maximum number of retry attempts */
	private final int maxAttempts;

	/** 重试异常类型 The exception type to retry for */
	private final Class<? extends Throwable> retryFor;

	/** 当前重试次数 The current retry attempt count */
	private int attempts = 0;

	final RetryDelayCalculator delay;

	RetryPolicy(RetryDelayCalculator delay, int maxAttempts, Class<? extends Throwable> retryFor) {
		this.delay = delay;
		this.maxAttempts = maxAttempts;
		this.retryFor = retryFor;
		Assert.notNull(retryFor);
	}

	/**
	 * Execute the retry task until it returns true.
	 * <p>
	 * 执行重试任务，直到返回true。
	 * 
	 * @param task  重试任务，返回true表示成功，false表示失败。/ The task to execute. Returns true on
	 *              success, false to retry.
	 * @param input 任务参数 / The parameter for the task
	 * @param <P>   参数类型 / The parameter type
	 * @return true if the task succeeded, false if all retries failed. / Whether
	 *         the task succeeded after all retries
	 */
	public <P> boolean executeUntilReturnTrue(Predicate<P> task, P input) {
		attempts++;
		try {
			boolean result = task.test(input);
			if (result) {
				return result;
			}
		} catch (Throwable t) {
			if (attempts <= maxAttempts && retryFor.isInstance(t)) {
				long wait = delay.getDelay(attempts);
				log.info("Caught exception in task [{}]. will retry(attempts={}) after {}ms:", task, attempts, wait, t);
				if (wait > 0) {
					doSleep(wait);
				}
				return executeUntilReturnTrue(task, input);
			} else {
				throw t;
			}
		}
		if (attempts <= maxAttempts) {
			long wait = delay.getDelay(attempts);
			log.info("Result is false in task [{}]. will retry(attempts={}) after {}ms:", task, attempts, wait);
			if (wait > 0) {
				doSleep(wait);
			}
			return executeUntilReturnTrue(task, input);
		} else {
			return false;
		}
	}

	/**
	 * Execute the retry task.
	 * <p>
	 * 执行重试任务。
	 * 
	 * @param task 重试任务 / The retry task
	 */
	public void execute(Runnable task) {
		attempts++;
		try {
			task.run();
		} catch (Throwable t) {
			if (attempts <= maxAttempts && retryFor.isInstance(t)) {
				long wait = delay.getDelay(attempts);
				log.info("Caught exception in task [{}]. will retry(attempts={}) after {}ms:", task, attempts, wait, t);
				if (wait > 0) {
					doSleep(wait);
				}
				execute(task);
			} else {
				throw t;
			}
		}
	}

	/**
	 * Execute the retry task.
	 * <p>
	 * 执行重试任务。
	 * 
	 * @param task 重试任务 / The retry task
	 * @param <T>  返回值类型 / The return type
	 * @return 返回值 / The return value
	 */
	public <T> T execute(Callable<T> task) {
		attempts++;
		try {
			return task.call();
		} catch (Throwable t) {
			if (attempts <= maxAttempts && retryFor.isInstance(t)) {
				long wait = delay.getDelay(attempts);
				log.info("Caught exception in task [{}]. will retry(attempts={}) after {}ms:", task, attempts, wait, t);
				if (wait > 0) {
					doSleep(wait);
				}
				return execute(task);
			} else {
				if (t instanceof Error) {
					throw (Error) t;
				} else {
					throw Exceptions.toRuntime(t);
				}
			}
		}
	}

	/**
	 * Execute the retry task.
	 * <p>
	 * 执行重试任务。
	 * 
	 * @param task 重试任务 / The retry task
	 * @param <T>  返回值类型 / The return type
	 * @return 返回值 / The return value
	 */
	public <T> T execute(Supplier<T> task) {
		attempts++;
		try {
			return task.get();
		} catch (Throwable t) {
			if (attempts <= maxAttempts && retryFor.isInstance(t)) {
				long wait = delay.getDelay(attempts);
				log.info("Caught exception in task [{}]. will retry(attempts={}) after {}ms:", task, attempts, wait, t);
				if (wait > 0) {
					doSleep(wait);
				}
				return execute(task);
			} else {
				throw t;
			}
		}
	};

	/**
	 * Execute the retry task.
	 * <p>
	 * 执行重试任务。
	 * 
	 * @param task  重试任务 / The retry task
	 * @param <T>   返回值类型 / The return type
	 * @param <P>   参数类型 / The type of the parameter
	 * @param param 执行参数
	 * @return 返回值 / The return value
	 */
	public <T, P> T execute(Function<P, T> task, P param) {
		attempts++;
		try {
			return task.apply(param);
		} catch (Throwable t) {
			if (attempts <= maxAttempts && retryFor.isInstance(t)) {
				long wait = delay.getDelay(attempts);
				log.info("Caught exception in task [{}]. will retry(attempts={}) after {}ms:", task, attempts, wait, t);
				if (wait > 0) {
					doSleep(wait);
				}
				return execute(task, param);
			} else {
				throw t;
			}
		}
	};

	/** 线程等待 / Thread waiting */
	private static final boolean doSleep(long l) {
		try {
			Thread.sleep(l);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

	/** 重试策略枚举 / Retry policy enumeration */
	public static enum Strategy {
		/**
		 * 固定等待时间 Fixed delay between retries
		 */
		FIXED_DELAY,
		/**
		 * 指数退避：每次等待时间翻倍 Exponential backoff: waiting time doubles each retry
		 */
		BACKOFF,
	}

	/** 重试策略计算器 / Retry policy calculator */
	static class RetryDelayCalculator {
		/**
		 * 重试策略 The policy of retry
		 */
		Strategy strategy = Strategy.FIXED_DELAY;

		/**
		 * 最小等待时间
		 * <p>
		 * Minimum delay between retries
		 */
		Duration minDelay = Duration.ZERO;

		/**
		 * 最大等待时间（仅在BACKOFF策略生效时有效）
		 * <p>
		 * Maximum delay (only applies to BACKOFF strategy)
		 */
		Duration maxDelay = Duration.ofMinutes(5);
		/**
		 * 抖动时间，默认为0，在所有策略下均有效
		 * <p>
		 * The jitter time, default 0, valid in all strategies
		 */
		Duration jitter = Duration.ZERO;

		/**
		 * 计算2的指定次幂。 This method calculates the power of two raised to the specified
		 * exponent.
		 *
		 * @param num 指数，表示2的幂次，如果输入大于32的数按32处理。/ The exponent indicating the power of
		 *            two. If the input is greater than 32, it is processed as 32.
		 * @return 返回2的num次幂的结果。/ Returns the result of two raised to the power of num.
		 */
		static long powerOfTwo(int num) {
			if (num <= 0) {
				return 1;
			}
			return 1L << Math.min(num, 32);
		}

		/**
		 * 根据重试次数计算延迟时间。 This method calculates the delay millis based on the number of
		 * retry attempts.
		 * 
		 * @param attempts 已经尝试的次数，用于指数退避策略中。 The number of attempts already made, used
		 *                 in the exponential backoff strategy.
		 * @return 返回下一次重试的延迟时间，单位为毫秒。 Returns the delay time for the next retry in
		 *         milliseconds.
		 */
		public long getDelay(int attempts) {
			if (strategy == Strategy.FIXED_DELAY) {
				return jitter(minDelay.toMillis());
			} else {
				long mills = minDelay.toMillis() * powerOfTwo(attempts - 1);
				if (maxDelay != null) {
					long maxMillis;
					if ((maxMillis = maxDelay.toMillis()) > 0) {
						mills = Math.min(mills, maxMillis);
					}
				}
				return jitter(mills);
			}
		}

		/** 抖动时间，返回一个随机值，范围为 [-jitter/2, jitter/2] */
		long jitter(long millis) {
			long jitterMillis = this.jitter.toMillis();
			if (jitterMillis <= 0) {
				return millis;
			}
			long jitter = Math.min(Integer.MAX_VALUE, jitterMillis);
			int value = ThreadLocalRandom.current().nextInt((int) jitter);
			value -= (jitter >>> 1);
			return millis + value;
		}
	}

	/** 重试策略构建器 / Retry policy builder */
	public static class PolicyBuilder {
		/**
		 * 重试次数 max attempts
		 */
		private int maxAttempts = 3;

		private final RetryDelayCalculator delay = new RetryDelayCalculator();

		private Class<? extends Throwable> retryFor = Exception.class;

		/**
		 * 指定最大延迟，防止在指数退避中出现特别大的重试延迟。
		 * <p>
		 * Specify the maximum delay to prevent an exponential backoff from occurring.
		 * @param maxDelay The maximum delay / 最大延迟间隔
		 * @see Duration
		 * @return The current PolicyBuilder instance for method chaining./
		 *         当前的PolicyBuilder实例，用于方法链式调用。
		 *         
		 */
		public PolicyBuilder maxDelay(Duration maxDelay) {
			delay.maxDelay = maxDelay;
			return this;
		}

		/**
		 * Sets the retry policy to use a backoff strategy with a specified minimum
		 * delay. 设置重试策略以使用具有指定最小延迟的退避策略。
		 * 
		 * @param minDelay The minimum delay before the next retry attempt.
		 *                 下次重试尝试之前的最小延迟。
		 * @see Duration
		 * @return The current PolicyBuilder instance for method chaining.
		 *         当前的PolicyBuilder实例，用于方法链式调用。
		 * @throws IllegalArgumentException if the specified minimum delay is less than
		 *                                  or equal to zero.
		 *                                  如果指定的最小延迟小于或等于零，则抛出IllegalArgumentException。
		 */
		public PolicyBuilder backoff(Duration minDelay) {
			Assert.notNull(minDelay, "Duration must not be null");
			Assert.isTrue(minDelay.toMillis() > 0, "Delay of backoff must greater than zero.");
			delay.strategy = Strategy.BACKOFF;
			delay.minDelay = minDelay;
			return this;
		}

		/**
		 * Sets the retry policy to have no delay between attempts. 设置重试策略，在尝试之间没有延迟。
		 * 
		 * This method configures the delay strategy to fixed delay with zero minimum
		 * delay and zero jitter, effectively removing any wait time between retry
		 * attempts. 该方法将延迟策略配置为固定延迟，最小延迟和抖动均为零， 实际上消除了重试尝试之间的任何等待时间。
		 * 
		 * @return The current instance of {@code PolicyBuilder} for method chaining.
		 *         返回{@code PolicyBuilder}的当前实例以进行链式调用。
		 */
		public PolicyBuilder nowait() {
			delay.strategy = Strategy.FIXED_DELAY;
			delay.minDelay = Duration.ZERO;
			delay.jitter = Duration.ZERO;
			return this;
		}

		/**
		 * Sets the delay strategy to fixed delay. 设置延迟策略为固定延迟。
		 *
		 * @param duration The duration of the delay. 延迟的时间长度。
		 * @see Duration
		 * @return Returns the current instance of PolicyBuilder, allowing for method
		 *         chaining. 返回当前PolicyBuilder实例，以实现链式调用。
		 */
		public PolicyBuilder fixedDelay(Duration duration) {
			Assert.notNull(duration, "duration must not be null");
			Assert.isTrue(duration.toMillis() > 0, "Delay must be greater than zero.");
			delay.minDelay = duration;
			delay.strategy = Strategy.FIXED_DELAY;
			return this;
		}

		/**
		 * 设置默认抖动时间：将抖动时间设为最小延迟时间的一半。
		 * <p>
		 * Set default jitter to half of the minimum delay duration.
		 * 
		 * @return 当前构造器实例 / The current PolicyBuilder instance
		 */
		public PolicyBuilder withDefaultJitter() {
			if (delay.minDelay.isZero()) {
				throw new IllegalArgumentException("minDelay must be set before using 'withDefaultJitter'.");
			}
			return withJitter(delay.minDelay.dividedBy(2));
		}

		/**
		 * 设置抖动时间.
		 * 
		 * @param jitter The jitter time. / 抖动时间
		 * @return Returns the current instance of PolicyBuilder, allowing for method
		 *         chaining. 允许链式调用。
		 */
		public PolicyBuilder withJitter(Duration jitter) {
			Assert.notNull(jitter, "Jitter must not be null.");
			Assert.isFalse(jitter.isNegative(), "Jitter time must be negative.");
			Assert.isTrue(jitter.toMillis() <= Integer.MAX_VALUE,
					"Jitter time must not greater than MAX_VALUE of Integer.");
			delay.jitter = jitter;
			return this;
		}

		/**
		 * 设置最大重试次数.
		 * 
		 * @param attempts The maximum number of attempts. / 最大尝试次数
		 * @return Returns the current instance of PolicyBuilder, allowing for method
		 *         chaining. 允许链式调用。
		 */
		public PolicyBuilder maxAttempts(int attempts) {
			Assert.isTrue(attempts >= 1, "The max attempts must greater than zero.");
			this.maxAttempts = attempts;
			return this;
		}

		public PolicyBuilder retryForException(Class<? extends Throwable> ex) {
			Assert.notNull(ex, "The input type of exception must not be null.");
			this.retryFor = ex;
			return this;
		}

		/**
		 * 构建重试策略.
		 * 
		 * @return Returns a new instance of RetryPolicy, configured with the specified
		 *         exception class. 返回一个RetryPolicy实例。
		 */
		public RetryPolicy build() {
			return new RetryPolicy(this.delay, maxAttempts, retryFor);
		}
	}

	/**
	 * 创建一个重试策略Builder.
	 * 
	 * @return Returns a new instance of RetryPolicy, configured with the specified
	 *         exception class. 构建重试策略
	 */
	public static PolicyBuilder newBuilder() {
		return new PolicyBuilder();
	}
}
