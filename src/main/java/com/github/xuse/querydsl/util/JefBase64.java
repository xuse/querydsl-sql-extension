/*
 * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
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
package com.github.xuse.querydsl.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * BASE64编解码处理 其中编码算法为自行实现，解码算法最后采用了Mikael Grev的实现。
 * 整个算法采用内存处理，以速度为优先进行过几轮优化。是目前已知实现中的最快实现。
 * 
 * @author Jiyi
 * 
 *         A very fast and memory efficient class to encode and decode to and
 *         from BASE64 in full accordance with RFC 2045.<br>
 *         <br>
 *         On Windows XP sp1 with 1.4.2_04 and later ;), this encoder and
 *         decoder is about 10 times faster on small arrays (10 - 1000 bytes)
 *         and 2-3 times as fast on larger arrays (10000 - 1000000 bytes)
 *         compared to <code>sun.misc.Encoder()/Decoder()</code>.<br>
 *         <br>
 *         On byte arrays the encoder is about 20% faster than Jakarta Commons
 *         Base64 Codec for encode and about 50% faster for decoding large
 *         arrays. This implementation is about twice as fast on very small
 *         arrays (&lt 30 bytes). If source/destination is a <code>String</code>
 *         this version is about three times as fast due to the fact that the
 *         Commons Codec result has to be recoded to a <code>String</code> from
 *         <code>byte[]</code>, which is very expensive.<br>
 *         <br>
 *         This encode/decode algorithm doesn't create any temporary arrays as
 *         many other codecs do, it only allocates the resulting array. This
 *         produces less garbage and it is possible to handle arrays twice as
 *         large as algorithms that create a temporary array. (E.g. Jakarta
 *         Commons Codec). It is unknown whether Sun's
 *         <code>sun.misc.Encoder()/Decoder()</code> produce temporary arrays
 *         but since performance is quite low it probably does.<br>
 *         <br>
 *         The encoder produces the same output as the Sun one except that the
 *         Sun's encoder appends a trailing line separator if the last character
 *         isn't a pad. Unclear why but it only adds to the length and is
 *         probably a side effect. Both are in conformance with RFC 2045
 *         though.<br>
 *         Commons codec seem to always att a trailing line separator.<br>
 *         <br>
 *         <b>Note!</b> The encode/decode method pairs (types) come in three
 *         versions with the <b>exact</b> same algorithm and thus a lot of code
 *         redundancy. This is to not create any temporary arrays for
 *         transcoding to/from different format types. The methods not used can
 *         simply be commented out.<br>
 *         <br>
 *         There is also a "fast" version of all decode methods that works the
 *         same way as the normal ones, but har a few demands on the decoded
 *         input. Normally though, these fast verions should be used if the
 *         source if the input is known and it hasn't bee tampered with.<br>
 *         <br>
 *         If you find the code useful or you find a bug, please send me a note
 *         at base64 @ miginfocom . com. Licence (BSD): ==============
 * 
 *         Copyright (c) 2004, Mikael Grev, MiG InfoCom AB. (base64 @ miginfocom
 *         . com) All rights reserved. Redistribution and use in source and
 *         binary forms, with or without modification, are permitted provided
 *         that the following conditions are met: Redistributions of source code
 *         must retain the above copyright notice, this list of conditions and
 *         the following disclaimer. Redistributions in binary form must
 *         reproduce the above copyright notice, this list of conditions and the
 *         following disclaimer in the documentation and/or other materials
 *         provided with the distribution. Neither the name of the MiG InfoCom
 *         AB nor the names of its contributors may be used to endorse or
 *         promote products derived from this software without specific prior
 *         written permission.
 * 
 *         Mikael Grev Date: 2004-aug-02 Time: 11:31:11
 */

public final class JefBase64 {
	/**
	 * 标准的Base64编码规格。不带换行。
	 */
	public static final Base64Context STANDARD = new Base64Context(false);

	/**
	 * 标准的Base64编码规格，带换行。根据RFC2045，每76个字符要换行。
	 * 
	 */
	public static final Base64Context STANDARD_WITH_WRAP = new Base64Context(true);

	/**
	 * 腾讯部分签名算法在实践操作中对Base64结果的微调，便于在URL中传输Base64的编码文本。</br>
	 * 用*代替+，用-代替/，用_代替=。 不带换行
	 */
	public static final Base64Context TENCENT_URL_ESCAPE = createContext(
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789*-", '_', false);

	/**
	 * 一个可调整的Base64编码策略，可以自定义码表字符，定义是否要带换行
	 */
	public static class Base64Context {
		final boolean wrap;
		final byte PAD;
		final byte[] ENCODE_TABLE;
		final char[] CA;
		final int[] IA = new int[256];

		/**
		 * @param wrap 是否换行
		 */
		public Base64Context(boolean wrap) {
			this.wrap = wrap;
			this.PAD = '=';
			ENCODE_TABLE = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84,
					85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112,
					113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };
			CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
			Arrays.fill(IA, -1);
			for (int i = 0, iS = CA.length; i < iS; i++)
				IA[CA[i]] = i;
			IA[PAD] = 0;
		}

		public Base64Context(boolean wrap, String str, char PAD) {
			this.wrap = wrap;
			this.PAD = (byte) PAD;
			CA = str.toCharArray();
			ENCODE_TABLE = new byte[CA.length];
			Arrays.fill(IA, -1);
			for (int i = 0, iS = CA.length; i < iS; i++) {
				ENCODE_TABLE[i] = (byte) CA[i];
				IA[CA[i]] = i;
			}
			IA[PAD] = 0;
		}
	}

	/**
	 * 支持自定义Base64的码表。
	 * 
	 * @param codec   码表字符集
	 * @param padChar 用与填补长度的字符，在标准Base64中是字符'='
	 * @param wrap    属否换行
	 * @return Base64配置
	 */
	public static Base64Context createContext(String codec, char padChar, boolean wrap) {
		return new Base64Context(wrap, codec, padChar);
	}

	/**
	 * 对二进制数据进行编码，
	 * {@code encodeToString(byte[])}和{@code encode(byte[])}两个方法是完全相同的，为了兼容某些其他库的API而设计。
	 * 
	 * @param data 二进制数据
	 */
	public static String encodeToString(byte[] data) {
		if (data == null)
			return null;
		return encode(data, data.length, STANDARD);
	}

	/**
	 * 对二进制数据进行编码。
	 * {@code encodeToString(byte[])}和{@code encode(byte[])}两个方法是完全相同的，为了兼容某些其他库的API而设计。
	 * 
	 * @param data 二进制数据
	 * @return 编码后的数据
	 */
	public static String encode(byte[] data) {
		if (data == null)
			return null;
		return encode(data, data.length, STANDARD);
	}

	public static String encode(ByteBuffer s, Base64Context context) {
		int offset = s.arrayOffset();
		if (offset == 0) {
			return encode(s.array(), s.limit(), context);
		} else {
			// 暂未支持
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * 不带换行的Base64编码
	 * 
	 * @param data
	 * @return 编码后文本
	 */
	public static String encode(byte[] data, int length, Base64Context context) {
		if (data == null || data.length < length) {
			throw new IllegalArgumentException();
		}
		byte[] ENCODE_TABLE = context.ENCODE_TABLE;

		int fullGroups = length / 3;
		int resultBytes = 4 * ((length + 2) / 3);
		if (context.wrap) {
			resultBytes = resultBytes * 78 / 76 + 1;
		}
		// int resultBytes = fullGroups * 4;
		// if (length % 3 != 0)
		// resultBytes += 4;
		byte[] result = new byte[resultBytes];
		int resultIndex = 0;
		int dataIndex = 0;
		int temp = 0;
		int wrapIndex = 0;
		for (int i = 0; i < fullGroups; i++) {
			temp = (data[dataIndex++] & 0xff) << 16 | (data[dataIndex++] & 0xff) << 8 | data[dataIndex++] & 0xff;

			result[resultIndex++] = ENCODE_TABLE[(temp >> 18) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[(temp >> 12) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[(temp >> 6) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[temp & 0x3f];
			if (context.wrap) {
				wrapIndex += 4;
				if (wrapIndex >= 76) {
					result[resultIndex++] = 13;
					result[resultIndex++] = 10;
					wrapIndex = 0;
				}
			}
		}

		temp = 0;
		while (dataIndex < length) {
			temp <<= 8;
			temp |= data[dataIndex++] & 0xff;
		}
		byte pad = context.PAD;
		switch (length % 3) {
		case 1:
			temp <<= 8;
			temp <<= 8;
			result[resultIndex++] = ENCODE_TABLE[(temp >> 18) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[(temp >> 12) & 0x3f];
			result[resultIndex++] = pad;
			result[resultIndex++] = pad;
			break;
		case 2:
			temp <<= 8;
			result[resultIndex++] = ENCODE_TABLE[(temp >> 18) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[(temp >> 12) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[(temp >> 6) & 0x3f];
			result[resultIndex++] = pad;
			break;
		default:
			break;
		}
		return new String(result, 0, resultIndex);
	}

	/**
	 * Decodes a BASE64 encoded char array that is known to be resonably well
	 * formatted. The
	 * preconditions are:<br>
	 * + The array must have a line length of 76 chars OR no line separators at all
	 * (one line).<br>
	 * + Line separator must be "\r\n", as specified in RFC 2045 + The array must
	 * not contain illegal characters within the encoded string<br>
	 * + The array CAN have illegal characters at the beginning and end, those will
	 * be dealt with appropriately.<br>
	 * @param offset
	 * @param charsLen
	 * @param context
	 * @param chars The source array. Length 0 will return an empty array.
	 *              <code>null</code> will throw an exception.
	 * @return The decoded array of bytes. May be of length 0.
	 */
	public final static byte[] decodeFast(char[] chars, int offset, int charsLen, Base64Context context) {
		// Check special case
		if (charsLen == 0) {
			return new byte[0];
		}

		int[] IA = context.IA;

		int sIx = offset, eIx = offset + charsLen - 1; // Start and end index
														// after trimming.

		// Trim illegal chars from start
		while (sIx < eIx && IA[chars[sIx]] < 0)
			sIx++;

		// Trim illegal chars from end
		while (eIx > 0 && IA[chars[eIx]] < 0)
			eIx--;

		// get the padding count (=) (0, 1 or 2)
		int pad = chars[eIx] == context.PAD ? (chars[eIx - 1] == context.PAD ? 2 : 1) : 0; // Count
																			// '='
																			// at
																			// end.
		int cCnt = eIx - sIx + 1; // Content count including possible separators
		int sepCnt = charsLen > 76 ? (chars[76] == '\r' ? cCnt / 78 : 0) << 1 : 0;

		int len = ((cCnt - sepCnt) * 6 >> 3) - pad; // The number of decoded
													// bytes
		byte[] bytes = new byte[len]; // Preallocate byte[] of exact length

		// Decode all but the last 0 - 2 bytes.
		int d = 0;
		for (int cc = 0, eLen = (len / 3) * 3; d < eLen;) {
			// Assemble three bytes into an int from four "valid" characters.
			int i = IA[chars[sIx++]] << 18 | IA[chars[sIx++]] << 12 | IA[chars[sIx++]] << 6 | IA[chars[sIx++]];

			// Add the bytes
			bytes[d++] = (byte) (i >> 16);
			bytes[d++] = (byte) (i >> 8);
			bytes[d++] = (byte) i;

			// If line separator, jump over it.
			if (sepCnt > 0 && ++cc == 19) {
				sIx += 2;
				cc = 0;
			}
		}

		if (d < len) {
			// Decode last 1-3 bytes (incl '=') into 1-3 bytes
			int i = 0;
			for (int j = 0; sIx <= eIx - pad; j++)
				i |= IA[chars[sIx++]] << (18 - j * 6);

			for (int r = 16; d < len; r -= 8)
				bytes[d++] = (byte) (i >> r);
		}

		return bytes;
	}

	/**
	 * Base64解码
	 * 
	 * @param bytes 要解码的字节数据。如果已经得到字符串格式的Base64编码，请直接用 {@link #decodeFast(CharSequence, Base64Context)}方法
	 * @return 解码后的数据
	 */
	public final static byte[] decode(byte[] bytes) {
		return decodeFast(bytes, 0, bytes.length, STANDARD);
	}

	public final static byte[] decodeFast(byte[] chars, int offset, int charsLen, Base64Context context) {
		// Check special case
		if (charsLen == 0) {
			return new byte[0];
		}
		int[] IA = context.IA;
		int sIx = offset, eIx = offset + charsLen - 1; // Start and end index
														// after trimming.

		// Trim illegal chars from start
		while (sIx < eIx && IA[chars[sIx]] < 0)
			sIx++;

		// Trim illegal chars from end
		while (eIx > 0 && IA[chars[eIx]] < 0)
			eIx--;

		// get the padding count (=) (0, 1 or 2)
		int pad = chars[eIx] == '=' ? (chars[eIx - 1] == '=' ? 2 : 1) : 0; // Count
																			// '='
																			// at
																			// end.
		int cCnt = eIx - sIx + 1; // Content count including possible separators
		int sepCnt = charsLen > 76 ? (chars[76] == '\r' ? cCnt / 78 : 0) << 1 : 0;

		int len = ((cCnt - sepCnt) * 6 >> 3) - pad; // The number of decoded
													// bytes
		byte[] bytes = new byte[len]; // Preallocate byte[] of exact length

		// Decode all but the last 0 - 2 bytes.
		int d = 0;
		for (int cc = 0, eLen = (len / 3) * 3; d < eLen;) {
			// Assemble three bytes into an int from four "valid" characters.
			int i = IA[chars[sIx++]] << 18 | IA[chars[sIx++]] << 12 | IA[chars[sIx++]] << 6 | IA[chars[sIx++]];

			// Add the bytes
			bytes[d++] = (byte) (i >> 16);
			bytes[d++] = (byte) (i >> 8);
			bytes[d++] = (byte) i;

			// If line separator, jump over it.
			if (sepCnt > 0 && ++cc == 19) {
				sIx += 2;
				cc = 0;
			}
		}

		if (d < len) {
			// Decode last 1-3 bytes (incl '=') into 1-3 bytes
			int i = 0;
			for (int j = 0; sIx <= eIx - pad; j++)
				i |= IA[chars[sIx++]] << (18 - j * 6);

			for (int r = 16; d < len; r -= 8)
				bytes[d++] = (byte) (i >> r);
		}

		return bytes;
	}

	/**
	 * Decodes a BASE64 encoded string that is known to be resonably well formatted.
	 * The method is about twice as fast as {@link #decode(String)}. The
	 * preconditions are:<br>
	 * + The array must have a line length of 76 chars OR no line separators at all
	 * (one line).<br>
	 * + Line separator must be "\r\n", as specified in RFC 2045 + The array must
	 * not contain illegal characters within the encoded string<br>
	 * + The array CAN have illegal characters at the beginning and end, those will
	 * be dealt with appropriately.<br>
	 * 
	 * @param s The source string. Length 0 will return an empty array.
	 *          <code>null</code> will throw an exception.
	 * @return The decoded array of bytes. May be of length 0.
	 */
	@SuppressWarnings("javadoc")
	public final static byte[] decode(CharSequence s) {
		return decodeFast(s, STANDARD);
	}

	public final static byte[] decodeFast(CharSequence s, Base64Context context) {
		// Check special case
		int sLen = s.length();
		if (sLen == 0)
			return new byte[0];
		int[] IA = context.IA;
		int sIx = 0, eIx = sLen - 1; // Start and end index after trimming.

		// Trim illegal chars from start
		while (sIx < eIx && IA[s.charAt(sIx) & 0xff] < 0)
			sIx++;

		// Trim illegal chars from end
		while (eIx > 0 && IA[s.charAt(eIx) & 0xff] < 0)
			eIx--;

		// get the padding count (=) (0, 1 or 2)
		byte PAD=context.PAD;
		int pad = s.charAt(eIx) == PAD ? (s.charAt(eIx - 1) == PAD ? 2 : 1) : 0; // Count
																					// '='
																					// at
																					// end.
		int cCnt = eIx - sIx + 1; // Content count including possible separators
		int sepCnt = sLen > 76 ? (s.charAt(76) == '\r' ? cCnt / 78 : 0) << 1 : 0;

		int len = ((cCnt - sepCnt) * 6 >> 3) - pad; // The number of decoded
													// bytes
		byte[] dArr = new byte[len]; // Preallocate byte[] of exact length

		// Decode all but the last 0 - 2 bytes.
		int d = 0;
		for (int cc = 0, eLen = (len / 3) * 3; d < eLen;) {
			// Assemble three bytes into an int from four "valid" characters.
			int i = IA[s.charAt(sIx++)] << 18 | IA[s.charAt(sIx++)] << 12 | IA[s.charAt(sIx++)] << 6
					| IA[s.charAt(sIx++)];

			// Add the bytes
			dArr[d++] = (byte) (i >> 16);
			dArr[d++] = (byte) (i >> 8);
			dArr[d++] = (byte) i;

			// If line separator, jump over it.
			if (sepCnt > 0 && ++cc == 19) {
				sIx += 2;
				cc = 0;
			}
		}

		if (d < len) {
			// Decode last 1-3 bytes (incl '=') into 1-3 bytes
			int i = 0;
			for (int j = 0; sIx <= eIx - pad; j++)
				i |= IA[s.charAt(sIx++)] << (18 - j * 6);

			for (int r = 16; d < len; r -= 8)
				dArr[d++] = (byte) (i >> r);
		}

		return dArr;
	}

	/**
	 * 解码为UTF8的字符串
	 * 
	 * @param encoded Base64字符串
	 * @return 解码后再按UTF8编码重新组成的字符串
	 */
	public static String decodeUTF8(CharSequence encoded) {
		return new String(decodeFast(encoded, STANDARD), StandardCharsets.UTF_8);
	}

	/**
	 * 解码为UTF8的字符串
	 * 
	 * @param encoded Base64字符串
	 * @param context 解码用的上下文
	 * @return 解码后再按UTF8编码重新组成的字符串
	 */
	public static String decodeUTF8(CharSequence encoded, Base64Context context) {
		return new String(decodeFast(encoded, context), StandardCharsets.UTF_8);
	}

	/**
	 * 字符串按UTF8进行base64编码
	 * 
	 * @param s 字符串
	 * @return base64编码
	 */
	public static String encodeUTF8(String s) {
		return encode(s.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 特别的功能，解出Base64串的第一个字节
	 * @param s
	 * @param index
	 * @param context
	 * @return 第一个字节
	 */
	public final static byte decodeFirstByte(CharSequence s, int index,Base64Context context) {
		if (s.length()<index)
			throw new IllegalArgumentException("Length not enough");
		int[] IA = context.IA;
		int sIx = index;
		int i = IA[s.charAt(sIx++)] << 18 | IA[s.charAt(sIx++)] << 12 | IA[s.charAt(sIx++)] << 6 | IA[s.charAt(sIx++)];
		return (byte) (i >> 16);
	}

	private JefBase64() {
	}
}
