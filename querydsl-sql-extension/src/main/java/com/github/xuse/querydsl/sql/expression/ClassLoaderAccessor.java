package com.github.xuse.querydsl.sql.expression;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import com.github.xuse.querydsl.util.lang.JDKEnvironment;

public final class ClassLoaderAccessor extends ClassLoader {

	private static final Method LOOKUP_DEFINE_CLASS;

	static {
		Method m = null;
		if (JDKEnvironment.JVM_VERSION >= 9) {
			try {
				m = MethodHandles.Lookup.class.getMethod("defineClass", byte[].class);
			} catch (NoSuchMethodException ignored) {
			}
		}
		LOOKUP_DEFINE_CLASS = m;
	}

	public ClassLoaderAccessor(ClassLoader parent) {
		super(parent);
	}

	public Class<?> defineClz(String name, byte[] b) {
		return super.defineClass(name, b, 0, b.length);
	}

	/**
	 * 将生成的类定义到目标类所在的 ClassLoader 中，确保与目标类处于同一运行时包。
	 * <p>
	 * 在 JDK 9+ 上使用 {@code MethodHandles.Lookup.defineClass(byte[])}，该方法会将类定义到
	 * Lookup 所关联的类的 ClassLoader 中，从而保证生成类与目标类在同一运行时包内，
	 * 可以访问 package-private 的目标类。
	 * </p>
	 * <p>
	 * 在 JDK 8 或 Lookup 方式不可用时，回退到使用自身 ClassLoader 的 defineClass。
	 * JDK 8 没有模块系统限制，同包名即可互相访问。
	 * </p>
	 *
	 * @param name     类的全限定名
	 * @param b        类的字节码
	 * @param beanType 目标 Bean 类（用于获取其 ClassLoader 上下文）
	 * @return 定义后的 Class 对象
	 */
	public Class<?> defineClz(String name, byte[] b, Class<?> beanType) {
		if (LOOKUP_DEFINE_CLASS != null) {
			try {
				MethodHandles.Lookup lookup = JDKEnvironment.trustedLookup(beanType);
				return (Class<?>) LOOKUP_DEFINE_CLASS.invoke(lookup, b);
			} catch (Throwable ignored) {
				// fall through to default defineClass
			}
		}
		return super.defineClass(name, b, 0, b.length);
	}
}
