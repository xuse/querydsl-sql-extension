package com.github.xuse.querydsl.util.binary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.xuse.querydsl.util.binary.codec.CodecContext;

public class BufferedDecoder {

	protected ByteBuffer buffer;

	public byte[] getByteArray(int length) {
		byte[] dst = new byte[length];
		this.buffer.get(dst);
		return dst;
	}

	public int remaining() {
		return buffer.remaining();
	}

	protected byte decodeByte(int length) {

		byte result = 0;

		if (0 >= length) {
			length = 1;
		}
		if (1 < length) {
			buffer.position(buffer.position() + length - 1);
		}

		int upper = Math.min(1, length);

		for (int i = 0; i < upper; i++) {
			result |= (buffer.get() & (byte) 0xFF) << (upper - 1 - i) * 8;
		}
		return result;
	}

	protected char decodeChar(int length) {
		char result = 0;

		if (0 >= length) {
			length = 2;
		}
		if (2 < length) {
			buffer.position(buffer.position() + length - 2);
		}

		int upper = Math.min(2, length);
		for (int i = 0; i < upper; i++) {
			result |= ((char) buffer.get() & (char) 0xFF) << (upper - 1 - i) * 8;
		}
		return result;
	}

	protected short decodeShort(int length) {
		short result = 0;

		if (0 >= length) {
			length = 2;
		}
		if (2 < length) {
			buffer.position(buffer.position() + length - 2);
		}

		int upper = Math.min(2, length);
		for (int i = 0; i < upper; i++) {
			result |= (buffer.get() & (short) 0xFF) << (upper - 1 - i) * 8;
		}
		return result;
	}

	protected int decodeInt(int length) {
		int result = 0;

		if (0 >= length) {
			length = 4;
		}
		if (4 < length) {
			buffer.position(buffer.position() + length - 4);
		}

		int upper = Math.min(4, length);
		for (int i = 0; i < upper; i++) {
			result |= (buffer.get() & 0xFF) << (upper - 1 - i) * 8;
		}
		return result;
	}

	protected long decodeLong(int length) {
		long result = 0;

		if (0 >= length) {
			length = 8;
		}
		if (8 < length) {
			buffer.position(buffer.position() + length - 8);
		}

		int upper = Math.min(8, length);
		for (int i = 0; i < upper; i++) {
			result |= ((long) buffer.get() & (long) 0xFF) << (upper - 1 - i) * 8;
		}
		return result;
	}

	public BufferedDecoder(ByteBuffer byteArray) {
		this.buffer = byteArray;
		this.buffer.order(ByteOrder.BIG_ENDIAN);
	}
	
	
	public BufferedDecoder(byte[] byteArray) {
		this(byteArray, ByteOrder.BIG_ENDIAN);
	}

	public BufferedDecoder(byte[] byteArray, ByteOrder byteOrder) {
		this.buffer = ByteBuffer.wrap(byteArray);
		this.buffer.order(byteOrder);
	}

	public byte getByte() {
		return decodeByte(1);
	}

	public char getChar() {
		return decodeChar(2);
	}

	public int getInt() {
		return decodeInt(4);
	}

	public long getLong() {
		return decodeLong(8);
	}

	public short getShort() {
		return decodeShort(2);
	}
	
	public String getString() {
		return CodecContext.INSTANCE.getString(buffer.get(),buffer);
	}

	public long getUnsignedInt() {
		return decodeLong(4);
	}

	public int getUnsignedShort() {
		return decodeInt(2);
	}

	/**
	 * 向后跳若干字节
	 * 
	 * @param offset
	 */
	public void skip(int offset) {
		buffer.position(buffer.position() + offset);

	}

	/**
	 * 跳转到buffer的绝对位置
	 * 
	 * @param offset
	 */
	public void position(int offset) {
		buffer.position(offset);
	}

	public List<String> getStringList() {
		short length = buffer.getShort();
		if (length < 0) {
			return null;
		} else if (length == 0) {
			return Collections.emptyList();
		} else if (length > 32768) {
			throw new IllegalArgumentException("list length too big: " + length);
		}
		List<String> result = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			result.add(getString());
		}
		return result;
	}

	/**
	 * 解码未知类型对象。
	 * 
	 * @param <T>
	 * @param clz
	 * @return object 
	 */
	public <T> T getObject(Class<T> clz) {
		return CodecContext.INSTANCE.readObject(buffer, clz);
	}
}
