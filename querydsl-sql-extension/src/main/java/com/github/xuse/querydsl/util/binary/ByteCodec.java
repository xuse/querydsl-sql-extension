package com.github.xuse.querydsl.util.binary;

import java.nio.ByteBuffer;

final class ByteCodec implements Codec<Byte>{

	public static final ByteCodec INSTANCE = new ByteCodec();
	
	@Override
	public Byte decode(ByteBuffer buffer, CodecContext context) {
		return buffer.get();
	}

	@Override
	public void encode(ByteBuffer buffer, Object obj, CodecContext context) {
		buffer.put(CodecContext.TYPE_BYTE);
		buffer.put((Byte)obj);
	}

}
