package com.github.xuse.querydsl.datatype.util.binary;

import java.nio.ByteBuffer;

final class BooleanT implements Codec<Boolean>{

	@Override
	public Boolean decode(ByteBuffer buffer,CodecContext context) {
		return Boolean.FALSE;
	}

	@Override
	public void encode(ByteBuffer buffer, Object obj,CodecContext context) {
		buffer.put(CodecContext.TYPE_BOOLEAN_T);
		
	}

}
