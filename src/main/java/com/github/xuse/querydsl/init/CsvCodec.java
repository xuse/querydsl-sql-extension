package com.github.xuse.querydsl.init;

public interface CsvCodec<T> {
	String toString(T t);

	T fromString(String s);
}
