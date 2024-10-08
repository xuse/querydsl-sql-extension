package com.github.xuse.querydsl.datatype.util.binary;

import java.nio.ByteBuffer;

public interface Codec<T> {
	T decode(ByteBuffer buffer,CodecContext context);

	void encode(ByteBuffer buffer, Object obj,CodecContext context);

}
