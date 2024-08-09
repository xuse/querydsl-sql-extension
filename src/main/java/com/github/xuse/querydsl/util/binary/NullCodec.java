package com.github.xuse.querydsl.util.binary;

import java.nio.ByteBuffer;

final class NullCodec implements Codec<Void>{
	public static final NullCodec INSTANCE = new NullCodec();

	@Override
	public Void decode(ByteBuffer buffer, CodecContext context) {
		return null;
	}

	@Override
	public void encode(ByteBuffer buffer, Object obj, CodecContext context) {
		buffer.put(CodecContext.TYPE_NULL);
	}
}
