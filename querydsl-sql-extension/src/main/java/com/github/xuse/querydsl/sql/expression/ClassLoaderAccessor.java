package com.github.xuse.querydsl.sql.expression;

public final class ClassLoaderAccessor extends ClassLoader {
	public ClassLoaderAccessor(ClassLoader parent) {
		super(parent);
	}

	public Class<?> defineClz(String name, byte[] b) {
		return super.defineClass(name, b, 0, b.length);
	}
}
