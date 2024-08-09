package com.github.xuse.querydsl.init.csv;

public interface CsvCodec<T> {
	String toString(T t);

	T fromString(String s);
}
