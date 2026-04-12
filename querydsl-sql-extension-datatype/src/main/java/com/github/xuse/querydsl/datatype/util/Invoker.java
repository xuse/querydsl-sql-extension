package com.github.xuse.querydsl.datatype.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.slf4j.Logger;

import lombok.extern.slf4j.Slf4j;

/**
 * 将异常转换为日志的执行包装. <code>
 * Invoker.call(()-> ... ).logError("message").logPrams(params).defaultValue(0).invoke();
 * </code>
 */
@Slf4j
public class Invoker<T> {
	private Object[] params;
	private String errorText = "Invocation error.";
	private T defaultValue;
	private Callable<T> call;
	private Consumer<T> onSuccess = (e) -> {
	};
	private Logger errLog;

	Invoker(Callable<T> call) {
		this.call = call;
	}

	public static <T> T getResultOrDefault(Callable<T> call,T defaultValue) {
		return call(call).defaultValue(defaultValue).invoke();
	}

	public static <T> Invoker<T> call(Callable<T> call) {
		return new Invoker<>(call);
	}

	public Invoker<T> logParams(Object... params) {
		this.params = new Object[params.length + 1];
		System.arraycopy(params, 0, this.params, 0, params.length);
		return this;
	}

	public Invoker<T> logError(String text) {
		this.errorText = text;
		return this;
	}

	public Invoker<T> error(String text, Object... params) {
		this.errorText = text;
		this.params = new Object[params.length + 1];
		System.arraycopy(params, 0, this.params, 0, params.length);
		return this;
	}

	public Invoker<T> logTo(Logger log) {
		this.errLog = log;
		return this;
	}

	public Invoker<T> onSuccess(Consumer<T> c) {
		this.onSuccess = c;
		return this;
	}

	public Invoker<T> defaultValue(T t) {
		this.defaultValue = t;
		return this;
	}

	public T invoke(Callable<T> call) {
		this.call = call;
		return invoke();
	}

	public T invoke() {
		try {
			T t = call.call();
			onSuccess.accept(t);
			return t;
		} catch (Exception ex) {
			if (errorText != null) {
				Object[] params = this.params;
				if (params == null) {
					params = new Object[1];
				}
				params[params.length - 1] = ex;
				Logger l = errLog == null ? log : errLog; 
				l.error(errorText, params);
			}
			return defaultValue;
		}
	}

}
