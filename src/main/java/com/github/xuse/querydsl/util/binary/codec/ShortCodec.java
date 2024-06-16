package com.github.xuse.querydsl.util.binary.codec;

import java.nio.ByteBuffer;

public class ShortCodec implements Codec<Short>{
	public static final ShortCodec INSTANCE = new ShortCodec();

	@Override
	public Short decode(ByteBuffer buffer,CodecContext context) {
		return buffer.getShort();
	}

	@Override
	public void encode(ByteBuffer buffer, Object obj,CodecContext context) {
		buffer.put(CodecContext.TYPE_SHORT);
		buffer.putShort((Short)obj);
	}

}
