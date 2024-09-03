package com.github.xuse.querydsl.datatype.util.binary;

import java.nio.ByteBuffer;

class LongCodec implements Codec<Long>	 {
	public static final LongCodec INSTANCE = new LongCodec();
	
	public static final LongCodec BYTE0 = new LongCodec() {
		@Override
		public Long decode(ByteBuffer buffer,CodecContext context) {
			return 0L;
		}
	};
	public static final LongCodec BYTE1 = new LongCodec() {
		@Override
		public Long decode(ByteBuffer buffer,CodecContext context) {
			return (long)buffer.get();
		}
	};
	public static final LongCodec BYTE2 = new LongCodec() {
		@Override
		public Long decode(ByteBuffer buffer,CodecContext context) {
			return (long)buffer.getShort();
		}
	};
	public static final LongCodec BYTE4 = new LongCodec() {
		@Override
		public Long decode(ByteBuffer buffer,CodecContext context) {
			return (long)buffer.getInt();
		}
	};
	
	@Override
	public Long decode(ByteBuffer buffer,CodecContext context) {
		return buffer.getLong();
	}

	@Override
	public void encode(ByteBuffer buffer, Object obj,CodecContext context) {
		long i=(Long)obj;
		if(i==0L) {
			buffer.put(CodecContext.TYPE_ZERO_LONG);
		}else if(i>=Byte.MIN_VALUE && i<=Byte.MAX_VALUE) {
			buffer.put(CodecContext.TYPE_1BYTE_LONG);
			buffer.put((byte)i);
		}else if(i>=Short.MIN_VALUE && i<=Short.MAX_VALUE) {
			buffer.put(CodecContext.TYPE_2BYTE_LONG);
			buffer.putShort((short)i);
		}else if(i>=Integer.MIN_VALUE && i<=Integer.MAX_VALUE) {
			buffer.putInt((int)i);
		}else {
			buffer.put(CodecContext.TYPE_LONG);
			buffer.putLong((Long)obj);
		}
	}
	
	
}
