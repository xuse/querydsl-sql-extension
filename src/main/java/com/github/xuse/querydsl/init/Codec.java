package com.github.xuse.querydsl.init;

public interface Codec<T> {
	String toString(T t);

	T fromString(String s);
}
