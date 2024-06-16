package com.github.xuse.querydsl.util.binary.codec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.github.xuse.querydsl.util.binary.Unsigned;

public class StringCodec implements Codec<String> {
	public static final StringCodec INSTANCE = new StringCodec();

	public static final StringCodec SHORT = new StringCodec() {

		@Override
		public String decode(ByteBuffer buffer, CodecContext context) {
			byte lengths = buffer.get();
			if (lengths == -1) {
				return null;
			}
			int length = Unsigned.of(lengths);
			byte[] tmpArray = new byte[length];
			buffer.get(tmpArray);
			return new String(tmpArray, 0, length, StandardCharsets.UTF_8);
		}

	};

	public static final StringCodec NULL = new StringCodec() {
		@Override
		public String decode(ByteBuffer buffer, CodecContext context) {
			return null;
		}
	};

	public static final StringCodec EMPTY = new StringCodec() {
		@Override
		public String decode(ByteBuffer buffer, CodecContext context) {
			return "";
		}
	};

	@Override
	public String decode(ByteBuffer buffer, CodecContext context) {
		short lengths = buffer.getShort();
		if (lengths == -1) {
			return null;
		}
		int length = Unsigned.of(lengths);
		byte[] tmpArray = new byte[length];
		buffer.get(tmpArray);
		return new String(tmpArray, 0, length, StandardCharsets.UTF_8);
	}

	@Override
	public void encode(ByteBuffer buffer, Object str, CodecContext context) {
		if (str == null) {
			buffer.put(CodecContext.TYPE_NULL_STRING);
			return;
		}
		byte[] bits = ((String) str).getBytes(StandardCharsets.UTF_8);
		int length = bits.length;
		if (length < 255) {
			buffer.put(CodecContext.TYPE_SHORT_STRING);
			buffer.put((byte) length);
		} else if (length > 0) {
			buffer.put(CodecContext.TYPE_STRING);
			buffer.putShort((short) length);
		} else {
			buffer.put(CodecContext.TYPE_EMPTY_STRING);
			return;
		}
		buffer.put(bits);

	}
}
