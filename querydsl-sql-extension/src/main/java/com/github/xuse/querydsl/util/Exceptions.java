package com.github.xuse.querydsl.util;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import lombok.extern.slf4j.Slf4j;

/**
 * 异常处理工具
 *
 * @author Joey
 */
public class Exceptions {
	/**
	 * Retry executing the specified function until it returns true or the maximum
	 * number of retries is reached. If an exception is thrown during execution, the
	 * process will be interrupted.
	 * @param times Retry times.
	 * @param input The parameter.
	 * @param The specified function
	 * @return Success  or not.
	 * @param <T> The type of target object.
	 */
	public static <T> boolean retry(int times, T input, Predicate<T> retryInvoke) {
		for (int i = 0; i < times; i++) {
			if (retryInvoke.test(input)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Execute the specified function, and return the default value if an exception occurs.
	 * @param function     The specified function
	 * @param s             The parameter
	 * @param defaultValue  Default value
	 * @return Result of the specified function, or default value if function execution failed.
	 * @param <S> The type of target object.
	 * @param <T> The type of target object.
	 */
	public static <S, T> T apply(Function<S, T> function, S s, T defaultValue) {
		try {
			return function.apply(s);
		} catch (Exception e) {
			log.error("apply {} error,", function, e);
			return defaultValue;
		}
	}

	/**
	 * Execute the specified function, and return the default value if an exception occurs.
	 * @param function     The specified function
	 * @param p1           The first parameter.
	 * @param p2           The second parameter
	 * @param defaultValue 默认值
	 * @return Result of the specified function, or default value if function execution failed.
	 * @param <P1> The type of target object.
	 * @param <P2> The type of target object.
	 * @param <T> The type of target object.
	 */
	public static <P1, P2, T> T apply(BiFunction<P1, P2, T> function, P1 p1, P2 p2, T defaultValue) {
		try {
			return function.apply(p1, p2);
		} catch (Exception e) {
			log.error("apply {} error,", function, e);
			return defaultValue;
		}
	}

	/**
	 *  Execute the specified function, and return the default value if an exception occurs.
	 * @param function     The specified function
	 * @param defaultValue The default value.
	 * @return Result of the specified function, or default value if function execution failed.
	 * @param <T> The type of target object.
	 */
	public static <T> T apply(Supplier<T> function, T defaultValue) {
		try {
			return function.get();
		} catch (Exception e) {
			log.error("apply {} error,", function, e);
			return defaultValue;
		}
	}

	/**
	 * Execute the specified function, and return the default value if an exception occurs or the execution result is null.
	 * @param function     The specified function
	 * @param parameter           The parameter
	 * @param defaultValue the default value
	 * @return Result of the specified function, or default value if function execution failed. 
	 * @param <S> The type of target object.
	 * @param <T> The type of target object.
	 */
	public static <S, T> T applyNotNull(Function<S, T> function, S parameter, T defaultValue) {
		try {
			T t = function.apply(parameter);
			return t == null ? defaultValue : t;
		} catch (Exception e) {
			log.error("apply {} error,", function, e);
			return defaultValue;
		}
	}
	
	public static <T> Predicate<T> handled(Predicate<T> predicate) {
		return (t)->{
			try {
				return predicate.test(t);	
			}catch(Exception e) {
				log.error("",e);
				return false;
			}
		};
	}

	/**
	 * Execute the specified function, and return the default value if an exception occurs or the execution result is null.
	 * @param function     The specified function
	 * @param p1           The first Parameter.
	 * @param p2           The second Parameter.
	 * @param defaultValue The default value
	 * @return Result of the specified function, or default value if function execution failed. 
	 * @param <P1> The type of target object.
	 * @param <P2> The type of target object.
	 * @param <T> The type of target object.
	 */
	public static <P1, P2, T> T applyNotNull(BiFunction<P1, P2, T> function, P1 p1, P2 p2, T defaultValue) {
		try {
			T t = function.apply(p1, p2);
			return t == null ? defaultValue : t;
		} catch (Exception e) {
			log.error("apply {} error,", function, e);
			return defaultValue;
		}
	}

	/**
	 * Execute the specified function, and return the default value if an exception occurs or the execution result is null.
	 * @param function     The specified function
	 * @param defaultValue The default value.
	 * @return Result of the specified function, or default value if function execution failed. 
	 * @param <T> The type of target object.
	 */
	public static <T> T applyNotNull(Supplier<T> function, T defaultValue) {
		try {
			T t = function.get();
			return t == null ? defaultValue : t;
		} catch (Exception e) {
			log.error("apply {} error,", function, e);
			return defaultValue;
		}
	}

	protected static final Logger log = LoggerFactory.getLogger(Exceptions.class);

	public static final class WrapException extends RuntimeException {

		private static final long serialVersionUID = -9058355728108119655L;

		WrapException(Throwable t) {
			super(t);
		}

		@Override
		public synchronized Throwable fillInStackTrace() {
			return this;
		}
	}

	/**
	 * Wrap the specified exception as an IllegalArgumentException.
	 * @param t t
	 * @return IllegalArgumentException
	 */
	public static IllegalArgumentException asIllegalArgument(Throwable t) {
		return illegalArgument(t, true);
	}

	/**
	 * Wrap the specified exception as an RuntimeException.
	 * @param t t
	 * @return RuntimeException
	 */
	public static RuntimeException toRuntime(Throwable t) {
		if (t instanceof RuntimeException) {
			return (RuntimeException) t;
		} else if (t instanceof InvocationTargetException) {
			return toRuntime(t.getCause());
		} else {
			return new WrapException(t);
		}
	}

	/**
	 * Generate an IllegalArgumentException.
	 * @param message message
	 * @param objects objects
	 * @return {@link IllegalArgumentException}
	 */
	public static IllegalArgumentException illegalArgument(String message, Object... objects) {
		FormattingTuple f = MessageFormatter.arrayFormat(message, objects);
		return f.getThrowable() == null ? new IllegalArgumentException(f.getMessage()) : new IllegalArgumentException(f.getMessage(), f.getThrowable());
	}

	/**
	 *  Wrap the specified exception as an IllegalArgumentException.
	 *
	 *  @param t                 The throwable
	 *  @param allowOtherRuntime true则允许抛出其他RuntimeException.
	 *  @return IllegalArgumentException
	 */
	public static IllegalArgumentException illegalArgument(Throwable t, boolean allowOtherRuntime) {
		if (t instanceof IllegalArgumentException) {
			return (IllegalArgumentException) t;
		} else if (t instanceof InvocationTargetException) {
			return illegalArgument(t.getCause(), allowOtherRuntime);
		} else if (allowOtherRuntime && (t instanceof RuntimeException)) {
			throw (RuntimeException) t;
		}
		return new IllegalArgumentException(t);
	}

	/**
	 *  Wrap the specified exception as an IllegalStateException.
	 *  @param t The throwable
	 *  @return IllegalStateException
	 */
	public static IllegalStateException illegalState(Throwable t) {
		return illegalState(t, true);
	}

	/**
	 *  转封装为IllegalStateException
	 *
	 *  @param t                 The throwable
	 *  @param allowOtherRuntime true则允许抛出其他RuntimeException.
	 *  @return IllegalStateException
	 */
	public static IllegalStateException illegalState(Throwable t, boolean allowOtherRuntime) {
		if (t instanceof IllegalStateException) {
			return (IllegalStateException) t;
		} else if (t instanceof InvocationTargetException) {
			return illegalState(t.getCause(), allowOtherRuntime);
		} else if (allowOtherRuntime && (t instanceof RuntimeException)) {
			throw (RuntimeException) t;
		}
		return new IllegalStateException(t);
	}

	/**
	 * 使用slf4j的机制来生成异常信息
	 * @param message message
	 * @param objects objects
	 * @return {@link IllegalStateException}
	 */
	public static IllegalStateException illegalState(String message, Object... objects) {
		FormattingTuple f = MessageFormatter.arrayFormat(message, objects);
		return f.getThrowable() == null ? new IllegalStateException(f.getMessage()) : new IllegalStateException(f.getMessage(), f.getThrowable());
	}

	/**
	 * 使用slf4j的机制来生成异常信息
	 * @param message message
	 * @param objects objects
	 * @return NoSuchElementException
	 */
	public static NoSuchElementException noSuchElement(String message, Object... objects) {
		FormattingTuple f = MessageFormatter.arrayFormat(message, objects);
		return new NoSuchElementException(f.getMessage());
	}

	/**
	 * 使用slf4j的机制来生成异常信息
	 * @param message message
	 * @param objects objects
	 * @return IndexOutOfBoundsException
	 */
	public static IndexOutOfBoundsException indexOutOfBounds(String message, Object... objects) {
		FormattingTuple f = MessageFormatter.arrayFormat(message, objects);
		return new IndexOutOfBoundsException(f.getMessage());
	}

	/**
	 * 使用slf4j的机制来生成异常信息
	 * @param message message
	 * @param objects objects
	 * @return UnsupportedOperationException
	 */
	public static UnsupportedOperationException unsupportedOperation(String message, Object... objects) {
		FormattingTuple f = MessageFormatter.arrayFormat(message, objects);
		return f.getThrowable() == null ? new UnsupportedOperationException(f.getMessage()):new UnsupportedOperationException(f.getMessage(),f.getThrowable());
	}

	/**
	 * 进行消息格式化
	 * @param message message
	 * @param objects objects
	 * @return formatted String
	 */
	public static String format(String message, Object... objects) {
		FormattingTuple f = MessageFormatter.arrayFormat(message, objects);
		return f.getMessage();
	}
}
