package com.github.xuse.querydsl.datatype.util.binary;

import java.nio.ByteBuffer;

public interface BeanCodec<T> {
	
	Class<T> getType();
	
	T decode(ByteBuffer buffer,CodecContext context);

	void encode(ByteBuffer buffer, T obj,CodecContext context);
}
