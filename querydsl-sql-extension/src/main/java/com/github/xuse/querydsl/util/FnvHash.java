package com.github.xuse.querydsl.util;

/**
 * FNV哈希算法全名为Fowler-Noll-Vo算法，是以三位发明人Glenn Fowler，Landon Curt
 * Noll，PhongVo的名字来命名的，最早在1991年提出。
 * 特点和用途：FNV能快速hash大量数据并保持较小的冲突率，它的高度分散使它适用于hash一些非常相近的字符串，比如URL，hostname，文件名，text，IP地址等。
 */
public final class FnvHash {
	private static final long BASIC_64 = 0xcbf29ce484222325L;
	private static final long PRIME_64 = 1099511628211L;

	private static final int BASIC_32 = 0x811c9dc5;
	private static final int PRIME_32 = 16777619;

	public static long fnv1a64(CharSequence input) {
		if (input == null) {
			return 0;
		}
		long hash = BASIC_64;
		for (int i = 0; i < input.length(); ++i) {
			char c = input.charAt(i);
			hash ^= c;
			hash *= PRIME_64;
		}
		return hash;
	}

	public static int fnv1a32(CharSequence key) {
		if (key == null) {
			return 0;
		}
		int hashCode = BASIC_32;
		for (int i = 0; i < key.length(); ++i) {
			char c = key.charAt(i);
			hashCode ^= c;
			hashCode *= PRIME_32;
		}
		return hashCode;
	}

	public static long fnv1a64(CharSequence input, int offset, int end) {
		if (input == null) {
			return 0;
		}

		if (input.length() < end) {
			end = input.length();
		}

		long hash = BASIC_64;
		for (int i = offset; i < end; ++i) {
			char c = input.charAt(i);
			hash ^= c;
			hash *= PRIME_64;
		}

		return hash;
	}

	public static long fnv1a64(byte[] input, int offset, int end) {
		if (input == null) {
			return 0;
		}

		if (input.length < end) {
			end = input.length;
		}

		long hash = BASIC_64;
		for (int i = offset; i < end; ++i) {
			byte c = input[i];
			hash ^= c;
			hash *= PRIME_64;
		}

		return hash;
	}

	public static long fnv1a64(char[] chars) {
		if (chars == null) {
			return 0;
		}
		long hash = BASIC_64;
		for (int i = 0; i < chars.length; ++i) {
			char c = chars[i];
			hash ^= c;
			hash *= PRIME_64;
		}

		return hash;
	}

	// 如果出现字母，按小写字母进行hash计算
	public static long fnv1a64Lower(CharSequence key) {
		if(key==null) {
			return 0;
		}
		long hashCode = BASIC_64;
		for (int i = 0; i < key.length(); ++i) {
			char ch = key.charAt(i);

			if (ch >= 'A' && ch <= 'Z') {
				ch = (char) (ch + 32);
			}

			hashCode ^= ch;
			hashCode *= PRIME_64;
		}

		return hashCode;
	}

	// 如果出现字母，按小写字母进行hash计算
	public static int fnv1a32Lower(CharSequence key) {
		if(key==null) {
			return 0;
		}
		int hashCode = BASIC_32;
		for (int i = 0; i < key.length(); ++i) {
			char ch = key.charAt(i);
			if (ch >= 'A' && ch <= 'Z') {
				ch = (char) (ch + 32);
			}
			hashCode ^= ch;
			hashCode *= 0x01000193;
		}
		return hashCode;
	}
}
