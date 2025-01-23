package com.github.xuse.querydsl.util;

/**
 * A byte data function. consume a byte data, and returns other things.
 * @param <R> type of the result.
 */
@FunctionalInterface
public interface ByteDataFunction<R> {
	R apply(byte[] data,int offset, int length);
}
