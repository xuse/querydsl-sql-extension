package com.github.xuse.querydsl.util.binary.codec;

import java.nio.ByteBuffer;

public class BooleanT implements Codec<Boolean>{

	@Override
	public Boolean decode(ByteBuffer buffer,CodecContext context) {
		return Boolean.FALSE;
	}

	@Override
	public void encode(ByteBuffer buffer, Object obj,CodecContext context) {
		buffer.put(CodecContext.TYPE_BOOLEAN_T);
		
	}

}
