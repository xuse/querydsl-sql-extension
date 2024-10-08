package com.github.xuse.querydsl.datatype.util.binary;

import java.nio.ByteBuffer;

class IntCodec implements Codec<Integer>	 {
	public static final IntCodec INSTANCE = new IntCodec();
	
	public static final IntCodec FOR_ZERO = new IntCodec() {
		@Override
		public Integer decode(ByteBuffer buffer,CodecContext context) {
			return 0;
		}
	};
	public static final IntCodec FOR_ONE = new IntCodec() {
		@Override
		public Integer decode(ByteBuffer buffer,CodecContext context) {
			return 1;
		}
	};
	public static final IntCodec BYTE1 = new IntCodec() {
		@Override
		public Integer decode(ByteBuffer buffer,CodecContext context) {
			return (int)buffer.get();
		}
	};
	public static final IntCodec BYTE2 = new IntCodec() {
		@Override
		public Integer decode(ByteBuffer buffer,CodecContext context) {
			return (int)buffer.getShort();
		}
	};
	
	@Override
	public Integer decode(ByteBuffer buffer,CodecContext context) {
		return buffer.getInt();
	}

	@Override
	public void encode(ByteBuffer buffer, Object obj,CodecContext context) {
		int i=(Integer)obj;
		if(i==0) {
			buffer.put(CodecContext.TYPE_ZERO_INT);
		} else if(i==1) {
			buffer.put(CodecContext.TYPE_ONE_INT);
		}else if(i>=Byte.MIN_VALUE && i<=Byte.MAX_VALUE) {
			buffer.put(CodecContext.TYPE_1BYTE_INT);
			buffer.put((byte)i);
		}else if(i>=Short.MIN_VALUE && i<=Short.MAX_VALUE) {
			buffer.put(CodecContext.TYPE_2BYTE_INT);
			buffer.putShort((short)i);
		}else {
			buffer.put(CodecContext.TYPE_INT);
			buffer.putInt(i);
		}
	}

}
