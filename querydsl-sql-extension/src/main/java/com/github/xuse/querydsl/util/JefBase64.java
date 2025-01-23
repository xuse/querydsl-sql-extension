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
package com.github.xuse.querydsl.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * <b>Chinese</b><p>
 * 高性能Base64工具，并支持用户自定义码表
 * <b>English</b><p>
 * Base64 Encoder/Decoder with high performance, and supports user custom code table.
 * @author Joey
 */
public final class JefBase64 {

	/**
	 * Standard Base64 code table without line breaks
	 */
	public static final Base64Context STANDARD = new Base64Context(false);

	/**
	 * Standard Base64 code table with line breaks (according to RFC2045, breaks every 76 characters)
	 */
	public static final Base64Context STANDARD_WITH_WRAP = new Base64Context(true);

	/**
	 * 腾讯部分签名算法在实践操作中对Base64结果的微调，便于在URL中传输Base64的编码文本。
	 * <p>
	 * 用*代替+，用-代替/，用_代替=。 不带换行
	 */
	public static final Base64Context TENCENT_URL_ESCAPE = createContext(
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789*-", '_', false);

	/**
	 * Chinese<p>
	 * Base64码表，用户可以创建自定义码表实例，并定义是否要换行<p>
	 * English<p>
	 * Base64 code table, users can create custom code table instances and define whether to use line breaks.
	 */
	public static class Base64Context {
		/**
		 * warp line or not.(Ref to RFC2045)
		 */
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
	 * 对二进制数据进行编码。
	 * {@code encodeToString(byte[])}和{@code encode(byte[])}两个方法是完全相同的，为了兼容某些其他库的API而设计。
	 *
	 * @param data 二进制数据
	 * @return 编码后的数据
	 */
	public static String encode(byte[] data) {
		if (data == null)
			return null;
		return encode(data, 0, data.length, STANDARD);
	}

	public static String encode(ByteBuffer s, Base64Context codeTable) {
		return encode(s.array(), s.arrayOffset(), s.limit(), codeTable);
	}

	private static final ByteDataFunction<String> toAsciiString = (b, o, l) -> new String(b, o, l,
			StandardCharsets.US_ASCII);

	/**
	 * Base64 encode without line wrap.
	 * @param data bytes to encode.
	 * @param offset  the offset of source data.
	 * @param length  int
	 * @param codeTable Base64 code table.
	 * @return base64 text
	 */
	public static String encode(byte[] data, int offset, int length, Base64Context codeTable) {
		return encode0(data, offset, length, codeTable,toAsciiString);
	}
	
	/**
	 * Base64 encode, result write to the output stream.
	 * @param data
	 * @param codeTable
	 * @param out The output stream.
	 */
	public static void encodeTo(byte[] data, Base64Context codeTable,OutputStream out) {
		encode0(data, 0, data.length, codeTable,(b, o, l) ->{
			try {
				out.write(b, o, l);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return null;
		});
	}
	
	private static <R> R encode0(byte[] data, int offset, int length, Base64Context codeTable,ByteDataFunction<R> func) {
		if (data == null || data.length < (offset + length)) {
			throw new IllegalArgumentException();
		}
		byte[] ENCODE_TABLE = codeTable.ENCODE_TABLE;
		int fullGroups = length / 3;
		int resultBytes = 4 * ((length + 2) / 3);
		if (codeTable.wrap) {
			resultBytes = resultBytes * 78 / 76 + 1;
		}

		byte[] result = new byte[resultBytes];
		int resultIndex = 0;
		int dataIndex = offset;
		int temp;
		int wrapIndex = 0;
		for (int i = 0; i < fullGroups; i++) {
			temp = (data[dataIndex++] & 0xff) << 16 | (data[dataIndex++] & 0xff) << 8 | data[dataIndex++] & 0xff;
			result[resultIndex++] = ENCODE_TABLE[(temp >> 18) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[(temp >> 12) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[(temp >> 6) & 0x3f];
			result[resultIndex++] = ENCODE_TABLE[temp & 0x3f];
			if (codeTable.wrap) {
				wrapIndex += 4;
				if (wrapIndex >= 76) {
					result[resultIndex++] = 13;
					result[resultIndex++] = 10;
					wrapIndex = 0;
				}
			}
		}
		temp = 0;
		int max = length + offset;
		while (dataIndex < max) {
			temp <<= 8;
			temp |= data[dataIndex++] & 0xff;
		}
		byte pad = codeTable.PAD;
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
		return func.apply(result, 0, resultIndex);
	}

	/**
	 * Base64 decoding
	 * 
	 * @param bytes The byte data to decode. If you have the Base64 encoded string,
	 *              please use {@link #decodeFast(CharSequence, Base64Context)}
	 *              method directly.
	 * @return The decoded data
	 */
	public static final byte[] decode(byte[] bytes) {
		return decodeFast(bytes, 0, bytes.length, STANDARD);
	}

	public static final byte[] decodeFast(byte[] chars, int offset, int charsLen, Base64Context codeTable) {
		// Check special case
		if (charsLen == 0) {
			return new byte[0];
		}
		int[] IA = codeTable.IA;
		// Start and end index
		int sIx = offset, eIx = offset + charsLen - 1;
		// Trim illegal chars from start
		while (sIx < eIx && IA[chars[sIx]] < 0)
			sIx++;
		// Trim illegal chars from end
		while (eIx > 0 && IA[chars[eIx]] < 0)
			eIx--;
		// get the padding count (=) (0, 1 or 2)
		// Count
		int pad = chars[eIx] == codeTable.PAD ? (chars[eIx - 1] == codeTable.PAD ? 2 : 1) : 0;
		// '='
		// at
		// end.
		// Content count including possible separators
		int cCnt = eIx - sIx + 1;
		int sepCnt = charsLen > 76 ? (chars[76] == '\r' ? cCnt / 78 : 0) << 1 : 0;
		// The number of decoded
		int len = ((cCnt - sepCnt) * 6 >> 3) - pad;
		// bytes
		// Preallocate byte[] of exact length
		byte[] bytes = new byte[len];
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
	 * The method is about twice as fast as {@link #decode(CharSequence)}. The
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
	public static final byte[] decode(CharSequence s) {
		return decodeFast(s, STANDARD);
	}

	public static final byte[] decodeFast(CharSequence s, Base64Context codeTable) {
		// Check special case
		int sLen = s.length();
		if (sLen == 0)
			return new byte[0];
		int[] IA = codeTable.IA;
		// Start and end index after trimming.
		int sIx = 0, eIx = sLen - 1;
		// Trim illegal chars from start
		while (sIx < eIx && IA[s.charAt(sIx) & 0xff] < 0)
			sIx++;
		// Trim illegal chars from end
		while (eIx > 0 && IA[s.charAt(eIx) & 0xff] < 0)
			eIx--;
		// get the padding count (=) (0, 1 or 2)
		byte PAD = codeTable.PAD;
		// Count
		int pad = s.charAt(eIx) == PAD ? (s.charAt(eIx - 1) == PAD ? 2 : 1) : 0;
		// '='
		// at
		// end.
		// Content count including possible separators
		int cCnt = eIx - sIx + 1;
		int sepCnt = sLen > 76 ? (s.charAt(76) == '\r' ? cCnt / 78 : 0) << 1 : 0;
		// The number of decoded
		int len = ((cCnt - sepCnt) * 6 >> 3) - pad;
		// bytes
		// Preallocate byte[] of exact length
		byte[] dArr = new byte[len];
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
	 * Decode into a UTF-8 string
	 * 
	 * @param encoded The Base64 encoded string
	 * @return The decoded string re-encoded with UTF-8
	 */
	public static String decodeUTF8(CharSequence encoded) {
		return new String(decodeFast(encoded, STANDARD), StandardCharsets.UTF_8);
	}

	/**
	 * Decode into a UTF-8 string
	 * 
	 * @param encoded The Base64 encoded string
	 * @param codeTable The base64 code table.
	 * @return The decoded string re-encoded with UTF-8
	 */
	public static String decodeUTF8(CharSequence encoded, Base64Context codeTable) {
		return new String(decodeFast(encoded, codeTable), StandardCharsets.UTF_8);
	}

	/**
	 * Encode the input string in Base64 after being encoded in UTF-8.
	 *
	 * @param s Input string
	 * @return base64 string
	 */
	public static String encodeUTF8(String s) {
		byte[] data = s.getBytes(StandardCharsets.UTF_8);
		return encode(data, 0, data.length, STANDARD);
	}

	/**
	 * Encode the input string in Base64 after being encoded in UTF-8.
	 * @param s Input string
	 * @param wrap has line wrap.
	 * @return base64 string
	 */
	public static String encodeUTF8(String s, boolean wrap) {
		byte[] data = s.getBytes(StandardCharsets.UTF_8);
		return encode(data, 0, data.length, wrap ? STANDARD_WITH_WRAP : STANDARD);
	}

	/**
	 * Special feature: decoding the first byte of a Base64 content.
	 * 
	 * @param s       String contains base64 content.
	 * @param offset  The begin offset of the base64 content.
	 * @param codeTable The base64 code table.
	 * @return First byte decoded.
	 */
	public static final byte decodeFirstByte(CharSequence s, int offset, Base64Context codeTable) {
		if (s.length() < offset)
			throw new IllegalArgumentException("Length not enough");
		int[] IA = codeTable.IA;
		int sIx = offset;
		int i = IA[s.charAt(sIx++)] << 18 | IA[s.charAt(sIx++)] << 12 | IA[s.charAt(sIx++)] << 6 | IA[s.charAt(sIx++)];
		return (byte) (i >> 16);
	}

	private JefBase64() {
	}
}
