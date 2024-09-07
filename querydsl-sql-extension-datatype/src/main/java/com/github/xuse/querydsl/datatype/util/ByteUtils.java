/*
 * querydsl-sql-extension - Copyright 2017-2024 Joey (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.xuse.querydsl.datatype.util;

import java.nio.ByteOrder;

public abstract class ByteUtils {
	/**
	 * 将long转换为byte[8]
	 * @param l l
	 * @return long to binary.
	 */
	public byte[] toBytes(long l) {
		byte[] b = new byte[8];
		putLong(b, l, 0);
		return b;
	}

	/**
	 * 将int转换为byte[4]
	 * @param i i
	 * @return int to binary.
	 */
	public byte[] toBytes(int i) {
		byte[] b = new byte[4];
		putInt(b, i, 0);
		return b;
	}

	/**
	 * 将short转换为byte[2].
	 * @param i i
	 * @return short to binary.
	 */
	public byte[] toBytes(short i) {
		byte[] b = new byte[2];
		putShort(b, i, 0);
		return b;
	}
	
	public int getInt(byte[] bb) {
		return getInt(bb, 0);
	};

	public short getShort(byte[] bb) {
		return getShort(bb, 0);
	};

	public long getLong(byte[] bb) {
		return getLong(bb, 0);
	};
	
	public abstract void putShort(byte[] b, short s, int offset);
	public abstract void putInt(byte[] bb, int x, int offset);
	public abstract void putLong(byte[] bb, long x, int offset);
	
	public abstract int getInt(byte[] bb, int offset);
	public abstract short getShort(byte[] bb, int offset);
	public abstract long getLong(byte[] bb, int offset);
	
	public static final ByteUtils BE=new ByteUtils(ByteOrder.BIG_ENDIAN) {

		/**
		 *  将short放入byte数组，高位在前，低位在后
		 *  @param b byte数组
		 *  @param s 要放入的值
		 *  @param offset 所在序号
		 */
		public void putShort(byte[] b, short s, int offset) {
			int len = b.length;
			if (offset < len)
				b[offset] = (byte) (s >> 8);
			if (offset + 1 < len)
				b[offset + 1] = (byte) (s >> 0);
		}
		
		/**
		 *  将short放入byte数组，高位在前，低位在后
		 *  @param bb byte数组
		 *  @param x 要放入的值
		 *  @param offset 所在序号
		 */
		public void putInt(byte[] bb, int x, int offset) {
			bb[offset + 0] = (byte) (x >> 24);
			bb[offset + 1] = (byte) (x >> 16);
			bb[offset + 2] = (byte) (x >> 8);
			bb[offset + 3] = (byte) (x >> 0);
		}

		/**
		 *  将long放入byte数组，高位在前，低位在后
		 *  @param bb byte数组
		 *  @param x 要放入的值
		 *  @param offset 所在序号
		 */
		public void putLong(byte[] bb, long x, int offset) {
			bb[offset + 0] = (byte) (x >> 56);
			bb[offset + 1] = (byte) (x >> 48);
			bb[offset + 2] = (byte) (x >> 40);
			bb[offset + 3] = (byte) (x >> 32);
			bb[offset + 4] = (byte) (x >> 24);
			bb[offset + 5] = (byte) (x >> 16);
			bb[offset + 6] = (byte) (x >> 8);
			bb[offset + 7] = (byte) (x >> 0);
		}

		/**
		 * 从字节流中读取一个int(占4个 byte,高位在前低位在后)
		 * @param bb bb
		 * @param offset offset
		 * @return int value
		 */
		public int getInt(byte[] bb, int offset) {
			return (int) ((((bb[offset + 0] & 0xff) << 24) | ((bb[offset + 1] & 0xff) << 16) | ((bb[offset + 2] & 0xff) << 8) | ((bb[offset + 3] & 0xff))));
		}
		
		/**
		 * @param bb bb
		 * @param offset offset
		 * @return long value
		 */
		public long getLong(byte[] bb, int offset) {
			return ((((long) bb[offset + 0] & 0xff) << 56) | (((long) bb[offset + 1] & 0xff) << 48) | (((long) bb[offset + 2] & 0xff) << 40) | (((long) bb[offset + 3] & 0xff) << 32) | (((long) bb[offset + 4] & 0xff) << 24) | (((long) bb[offset + 5] & 0xff) << 16) | (((long) bb[offset + 6] & 0xff) << 8) | (((long) bb[offset + 7] & 0xff) << 0));
		}

		/**
		 * 从字节组中获得long（正常顺序：高位在前，低位在后）
		 * @param b b
		 * @param offset offset
		 * @return short value
		 */
		public short getShort(byte[] b, int offset) {
			return (short) (((b[offset] << 8) | b[offset + 1] & 0xff));
		}
	};
	
	public static final ByteUtils LE=new ByteUtils(ByteOrder.LITTLE_ENDIAN) {

		/**
		 *  将short放入byte数组，低位在前，高位在后
		 *  @param b byte数组
		 *  @param s 要放入的值
		 *  @param offset 所在序号
		 */
		public void putShort(byte[] b, short s, int offset) {
			int len = b.length;
			if (offset < len)
				b[offset] = (byte) (s >> 0);
			if (offset < len)
				b[offset + 1] = (byte) (s >> 8);
		}


		/**
		 * 从字节数组中获得short,(高位在后，低位在前)
		 * @param b b
		 * @param offset offset
		 * @return short value
		 */
		public short getShort(byte[] b, int offset) {
			return (short) (((b[offset + 1] << 8) | b[offset] & 0xff));
		}
		
		/**
		 *  将int放入byte数组，低位在前，高位在后
		 *  @param bb byte数组
		 *  @param x 要放入的值
		 *  @param offset 所在序号
		 */
		public void putInt(byte[] bb, int x, int offset) {
			bb[offset + 3] = (byte) (x >> 24);
			bb[offset + 2] = (byte) (x >> 16);
			bb[offset + 1] = (byte) (x >> 8);
			bb[offset + 0] = (byte) (x >> 0);
		}
		
		/**
		 * 从字节流中读取一个int(占4个 byte,低位在前，高位在后)
		 * @param bb bb
		 * @param offset offset
		 * @return int value
		 */
		public int getInt(byte[] bb, int offset) {
			return (int) ((((bb[offset + 3] & 0xff) << 24) | ((bb[offset + 2] & 0xff) << 16) | ((bb[offset + 1] & 0xff) << 8) | ((bb[offset + 0] & 0xff))));
		}
		
		/**
		 *  将long放入byte数组，低位在前，高位在后
		 *  @param bb byte数组
		 *  @param x 要放入的值
		 *  @param offset 所在序号
		 */
		public void putLong(byte[] bb, long x, int offset) {
			bb[offset + 7] = (byte) (x >> 56);
			bb[offset + 6] = (byte) (x >> 48);
			bb[offset + 5] = (byte) (x >> 40);
			bb[offset + 4] = (byte) (x >> 32);
			bb[offset + 3] = (byte) (x >> 24);
			bb[offset + 2] = (byte) (x >> 16);
			bb[offset + 1] = (byte) (x >> 8);
			bb[offset + 0] = (byte) (x >> 0);
		}

		/**
		 * @param bb bb
		 * @param offset offset
		 * @return long value
		 */
		public long getLong(byte[] bb, int offset) {
			return ((((long) bb[offset + 7] & 0xff) << 56) | (((long) bb[offset + 6] & 0xff) << 48) | (((long) bb[offset + 5] & 0xff) << 40) | (((long) bb[offset + 4] & 0xff) << 32) | (((long) bb[offset + 3] & 0xff) << 24) | (((long) bb[offset + 2] & 0xff) << 16) | (((long) bb[offset + 1] & 0xff) << 8) | (((long) bb[offset + 0] & 0xff) << 0));
		}
	};
	
	public static ByteUtils bigEndian(){
		return BE;
	}
	
	public static ByteUtils littleEndian(){
		return LE;
	}
	
	
	private final ByteOrder order;
	
	public ByteOrder getByteOrder() {
		return order;
	}
	
	private ByteUtils(ByteOrder order) {
		this.order=order;
	}


	/**
	 * 将byte转换为int
	 * @param b b
	 * @return int value.
	 */
	public static int byteToInt(byte b) {
		return b >= 0 ? b : 256 + b;
	}

	/**
	 * @param buf buf
	 * @param key key
	 * @return 判断buf中的开头字节是否匹配key中的所有字节
	 */
	public static boolean matchStart(byte[] buf, byte[] key) {
		if (key.length > buf.length) {
			return false;
		}
		for (int i = 0; i < key.length; i++) {
			if (buf[i] != key[i]) {
				return false;
			}
		}
		return true;
	}
}
