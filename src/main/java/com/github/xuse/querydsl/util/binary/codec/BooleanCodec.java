package com.github.xuse.querydsl.util.binary.codec;

import java.nio.ByteBuffer;

public class BooleanCodec implements Codec<Boolean>{

	@Override
	public Boolean decode(ByteBuffer buffer, CodecContext context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void encode(ByteBuffer buffer, Object obj, CodecContext context) {
		if((Boolean)obj) {
			buffer.put(CodecContext.TYPE_BOOLEAN_T);
		}else {
			buffer.put(CodecContext.TYPE_BOOLEAN_F);
		}
	}

}
