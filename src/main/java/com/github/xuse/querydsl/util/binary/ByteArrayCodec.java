package com.github.xuse.querydsl.util.binary;

import java.nio.ByteBuffer;

class ByteArrayCodec implements Codec<byte[]>{
	
	public static final ByteArrayCodec INSTANCE= new ByteArrayCodec();
	
	public static final ByteArrayCodec INSTANCE_SHORT= new ByteArrayCodec() {
		public byte[] decode(ByteBuffer buffer, CodecContext context) {
			int length=Unsigned.of(buffer.get());
			byte[] result=new byte[length];
			buffer.get(result);
			return result;
		}

	};
	private static final byte[] EMPTY=new byte[0];
	public static final ByteArrayCodec INSTANCE_EMPTY= new ByteArrayCodec() {
		public byte[] decode(ByteBuffer buffer, CodecContext context) {
			return EMPTY;
		}

	};
	
	@Override
	public byte[] decode(ByteBuffer buffer, CodecContext context) {
		int length=Unsigned.of(buffer.getShort());
		byte[] result=new byte[length];
		buffer.get(result);
		return result;
	}

	@Override
	public void encode(ByteBuffer buffer, Object obj, CodecContext context) {
		byte[] data=(byte[])obj;
		if(data.length==0) {
			buffer.put(CodecContext.TYPE_EMPTY_BYTEARRAY);
		}else if(data.length<256) {
			buffer.put(CodecContext.TYPE_SHORT_BYTEARRAY);
			buffer.put(Unsigned.toByte(data.length));
		}else {
			buffer.put(CodecContext.TYPE_BYTEARRAY);
			buffer.putShort((short)data.length);
		}
		buffer.put(data);
	}
}
