package com.github.xuse.querydsl.util.binary;

import java.nio.ByteBuffer;

class FloatCodec implements Codec<Float>{
	public static final FloatCodec INSTANCE = new FloatCodec();
	
	public static final FloatCodec FOR_ZERO = new FloatCodec() {
		@Override
		public Float decode(ByteBuffer buffer, CodecContext context) {
			return 0f;
		}
	};
	
	@Override
	public void encode(ByteBuffer buffer, Object obj, CodecContext context) {
		Float num=(Float)obj;
		if(num==0d) {
			buffer.put(CodecContext.TYPE_ZERO_FLOAT);	
		}else {
			buffer.put(CodecContext.TYPE_FLOAT);
			buffer.putFloat(num);	
		}
	}
	
	@Override
	public Float decode(ByteBuffer buffer, CodecContext context) {
		return buffer.getFloat();
	}
}
