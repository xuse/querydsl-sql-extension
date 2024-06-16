package com.github.xuse.querydsl.util.binary;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import com.github.xuse.querydsl.util.binary.codec.CodecContext;
import com.github.xuse.querydsl.util.binary.codec.StringCodec;

/**
 */
public class BufferedEncoder {

	protected ByteBuffer buffer;

	public byte[] toByteArray() {
		buffer.flip();// 调整buffers大小适应
		int remaining = buffer.remaining();
		byte[] dst = new byte[remaining];
		buffer.get(dst);
		return dst;
	}

	public void putByteArray(byte[] bts) {
		this.buffer.put(bts);
	}

	public void putByteArray(byte[] src, int offset, int length) {
		buffer.put(src, offset, length);
	}

	public int remaining() {
		return buffer.remaining();
	}

	protected void encodeByte(byte value, int length) {
		if (0 >= length) {
			length = 1;
		}
		for (int i = 0; i < length - 1; i++) {
			buffer.put((byte) 0);
		}
		buffer.put(value);
	}

	protected void encodeChar(char value, int length) {
		if (0 >= length) {
			length = 2;
		}
		for (int i = 0; i < length - 2; i++) {
			buffer.put((byte) 0);
		}
		length = 2 < length ? 2 : length;
		for (int i = 0; i < length; i++) {
			buffer.put((byte) (value >>> 8 * (length - 1 - i) & 0xff));
		}
	}

	protected void encodeShort(short value, int length) {
		if (0 >= length) {
			length = 2;
		}
		for (int i = 0; i < length - 2; i++) {
			buffer.put((byte) 0);
		}
		length = 2 < length ? 2 : length;
		for (int i = 0; i < length; i++) {
			buffer.put((byte) (value >>> 8 * (length - 1 - i) & 0xff));
		}
	}

	protected void encodeInt(int value, int length) {
		if (0 >= length) {
			length = 4;
		}
		for (int i = 0; i < length - 4; i++) {
			buffer.put((byte) 0);
		}
		length = 4 < length ? 4 : length;
		for (int i = 0; i < length; i++) {
			buffer.put((byte) (value >>> 8 * (length - 1 - i) & 0xff));
		}
	}

	protected void encodeLong(long value, int length) {
		if (0 >= length) {
			length = 8;
		}
		for (int i = 0; i < length - 8; i++) {
			buffer.put((byte) 0);
		}
		length = 8 < length ? 8 : length;
		for (int i = 0; i < length; i++) {
			buffer.put((byte) (value >>> 8 * (length - 1 - i) & 0xff));
		}
	}

	protected void encodeString(String str, int length) {
		if (length < 0) {
			throw new RuntimeException("length must be larger than 0");
		}
		byte[] bits = null;
		try {
			bits = str.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		encodeInt(length, 4);// 字符串的长度
		for (int i = 0; i < (bits.length < length ? bits.length : length); i++) {
			encodeByte(bits[i], 1);
		}
		for (int i = 0; i < length - bits.length; i++) {
			if (i == 0) {
				buffer.put((byte) 0);
			} else {
				buffer.put((byte) 32);
			}
		}
	}

	protected void encodeString(String str) {
		if (str == null) {
			throw new RuntimeException("length must not be null");
		}
		byte[] bits = null;
		try {
			bits = str.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		int length = bits.length;
		encodeInt(length, 4);// 字符串的长度
		for (int i = 0; i < length; i++) {
			encodeByte(bits[i], 1);
		}
	}

	public BufferedEncoder(int capacity) {
		this(capacity, ByteOrder.BIG_ENDIAN);
	}

	public BufferedEncoder(int capacity, ByteOrder byteOrder) {
		super();
		this.buffer = ByteBuffer.allocate(capacity);
		this.buffer.order(byteOrder);
	}

	public void putByte(byte data) {
		encodeByte(data, 1);
	}

	public void putChar(char data) {
		encodeChar(data, 2);
	}

	public void putInt(int data) {
		encodeInt(data, 4);
	}

	public void putLong(long data) {
		encodeLong(data, 8);
	}

	public void putShort(short data) {
		encodeShort(data, 2);
	}

	public void putString(String str) {
		StringCodec.INSTANCE.encode(buffer, str, CodecContext.INSTANCE);
	}

	public void putUnsignedInt(long data) {
		encodeLong(data, 4);
	}

	public void putUnsignedShort(int data) {
		encodeInt(data, 2);
	}

	public void putInt(int data, int length) {
		encodeInt(data, length);
	}

	public void putStringList(List<String> relationAlarmId) {
		if (relationAlarmId == null) {
			buffer.putShort((short) -1);
			return;
		}
		buffer.putShort((short) relationAlarmId.size());
		for (String s : relationAlarmId) {
			putString(s);
		}
	}

	public void putObject(Object value) {
		CodecContext.INSTANCE.writeObject(buffer, value);
	}

}
