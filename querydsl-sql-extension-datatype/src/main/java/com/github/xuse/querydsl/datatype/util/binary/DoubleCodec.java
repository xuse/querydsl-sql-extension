package com.github.xuse.querydsl.datatype.util.binary;

import java.nio.ByteBuffer;

class DoubleCodec implements Codec<Double>{
	public static final DoubleCodec INSTANCE = new DoubleCodec();
	
	public static final DoubleCodec FOR_ZERO = new DoubleCodec() {
		@Override
		public Double decode(ByteBuffer buffer, CodecContext context) {
			return 0d;
		}
	};
	
	@Override
	public void encode(ByteBuffer buffer, Object obj, CodecContext context) {
		Double num=(Double)obj;
		if(num==0d) {
			buffer.put(CodecContext.TYPE_ZERO_DOUBLE);	
		}else {
			buffer.put(CodecContext.TYPE_DOUBLE);
			buffer.putDouble(num);	
		}
	}
	
	@Override
	public Double decode(ByteBuffer buffer, CodecContext context) {
		return buffer.getDouble();
	}
}
