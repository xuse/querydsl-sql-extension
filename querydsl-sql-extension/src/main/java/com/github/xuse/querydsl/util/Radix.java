package com.github.xuse.querydsl.util;

/**
 * Radix compute utility.
 *
 * Use {@link #encode(long)} {@link #encodeInt(int)} to convert a decimal number
 * to a N-module number. Use {@link #decodeInt(String)} {@link #decode(String)}
 * to convert the N-module number back to the decimal number
 *
 * @author Joey
 */
public enum Radix {

	/**
	 *  二进制 Binary
	 */
	D2("01".toCharArray(), 64, '-'),
	/**
	 *  三进制 Ternary
	 */
	D3("012".toCharArray(), 40, '-'),
	/**
	 *  七进制 Septenary
	 */
	D7("0123456".toCharArray(), 30, '-'),
	/**
	 *  八进制 Octal
	 */
	D8("01234567".toCharArray(), 21, '-') {
		@Override
		protected void encode0(long num, StringBuilder sb) {
			while (num >= scale) {
				int remainder = (int) (num & 7);
				sb.append(codeTable[remainder]);
				num = num >> 3;
			}
			sb.append(codeTable[(int) num]);
		}
	}
	,
	/**
	 *  九进制 Nonary
	 */
	D9("012345678".toCharArray(), 20, '-'),
	/**
	 *  10进制 Decimal
	 */
	D10("0123456789".toCharArray(), 19, '-'),
	/**
	 *  十进制中文, Decimal in Chinese characters.
	 */
	D10C("零一二三四五六七八九".toCharArray(), 19, '负'),
	/**
	 *  十进制中文2, Decimal in Chinese characters.
	 */
	D10CT("零壹贰叁肆伍陆柒捌玖".toCharArray(), 19, '负'),
	/** 
	 *  16进制 Hexadecimal
	 */
	D16("0123456789ABCDEF".toCharArray(), 16, '-') {
		@Override
		protected void encode0(long num, StringBuilder sb) {
			while (num >= scale) {
				int remainder = (int) (num & 15);
				sb.append(codeTable[remainder]);
				num = num >> 4;
			}
			sb.append(codeTable[(int) num]);
		}
	},
	/**
	 *  36进制 Base36
	 */
	D36("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray(), 14, '-'),
	/**
	 *  62进制 Base62
	 */
	D62("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray(), 12, '-'),
	/**
	 *  64进制 Base64，
	 */
	D64("$0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz".toCharArray(), 11, '-') {

		@Override
		protected void encode0(long num, StringBuilder sb) {
			while (num >= scale) {
				int remainder = (int) (num & 63);
				sb.append(codeTable[remainder]);
				num = num >> 6;
			}
			sb.append(codeTable[(int) num]);
		}
	}
	,
	/**
	 *  七十二进制 Base72
	 */
	D72("$0123456789=@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_abcdefghijklmnopqrstuvwxyz{}~".toCharArray(), 11, '-'),
	/**
	 *  八十四进制 Base84
	 */
	D84("!#$%&()*+.0123456789:=@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_abcdefghijklmnopqrstuvwxyz{|}~".toCharArray(), 11, '-');

	/**
	 *  进制
	 */
	public final int scale;

	/**
	 *  码表
	 */
	final char[] codeTable;

	/**
	 *  转换乘方表，避免每次执行乘方运算
	 */
	private final long[] powTable;

	private final char minusChar;
	/**
	 *  字符码表是否符合编码顺序
	 */
	private int codeAlgorithm = BINARY_SEARCH;

	private static final int FAST_CONVERT = 0;

	private static final int BINARY_SEARCH = 1;

	private static final int INDEXOF = 2;

	/**
	 * @implNote Exponentiation cannot use Math.pow(), as it will lose
	 *           precision. For example, Math.pow(3,39) =
	 *           {@code 4052555153018976256} The correct result should be
	 *           {@code 4052555153018976267} This causes computation errors in
	 *           base-3 and base-7 systems, among others, and it is only after
	 *           repeated verification that we discovered that Math.pow is
	 *           inaccurate for large integer calculations.
	 *
	 * @param base base
	 * @param pow  pow
	 * @return Exponentiation result
	 */
	public static long pow(long base, int pow) {
		long n = 1;
		for (int i = 0; i < pow; i++) {
			n *= base;
		}
		return n;
	}

	/**
	 * @param cs cs
	 * @param powerTableSize int
	 */
	Radix(char[] cs, int powerTableSize,char minusChar) {
		this.minusChar=minusChar;
		this.scale = cs.length;
		this.codeTable = cs;
		// 计算乘方表
		powTable = new long[powerTableSize];
		for (int i = 0; i < powerTableSize; i++) {
			// must not use Math.pow()
			powTable[i] = pow(scale, i);
		}
		// 判断码表是否为有序集合，这将决定解码时的查找算法
		if (cs[0] - 48 == 0 && cs[scale - 1] - 48 == scale - 1) {
			codeAlgorithm = FAST_CONVERT;
		} else {
			char last = 0;
			for (char c : cs) {
				if (c < last) {
					codeAlgorithm = INDEXOF;
					break;
				}
				last = c;
			}
		}
	}

	/**
	 * 按指定进制编码，带填充。（解码时无需去除填充字符）
	 * @param num num
	 * @param minDigit 最少字符位数，不足时会在字符前方填充该种编码下的‘0’字符
	 * @return 编码后的数值表达
	 */
	public String encodeIntWithPadding(int num, int minDigit) {
		if (num < 0) {
			return encode(num + 0x0100000000L);
		} else {
			StringBuilder sb = new StringBuilder();
			encode1(num, sb);
			for (int i = sb.length(); i < minDigit; i++) {
				sb.append(codeTable[0]);
			}
			return sb.reverse().toString();
		}
	}

	/**
	 * 按指定进制编码，带填充。（解码时无需去除填充字符）
	 * @param num num
	 * @param minDigit 最少字符位数，不足时会在字符前方填充该种编码下的‘0’字符
	 * @return 编码后的数值
	 */
	public String encodeWithPadding(long num, int minDigit) {
		StringBuilder sb = new StringBuilder();
		boolean minus = num < 0;
		encode0(minus ? -num : num, sb);
		for (int i = sb.length()+(minus?1:0); i < minDigit; i++) {
			sb.append(codeTable[0]);
		}
		if(minus) {
			sb.append(minusChar);
		}
		return sb.reverse().toString();
	}

	/**
	 * 对int进行序列化 (支持负数)
	 *
	 * @param num num
	 * @return 编码后的数值
	 */
	public String encodeInt(int num) {
		if (num < 0) {
			return encode(num + 0x0100000000L);
		} else {
			StringBuilder sb = new StringBuilder();
			encode1(num, sb);
			return sb.reverse().toString();
		}
	}

	/**
	 * 解码为整数 (支持负数)
	 * @param val val
	 * @return 解码后数值
	 */
	public int decodeInt(String val) {
		long num = decode(val);
		return num > Integer.MAX_VALUE ? (int) (num - 0x0100000000L) : (int) num;
	}

	/**
	 *  编码为指定的进制
	 *
	 *  @param num Long 型数字
	 *  @return 编码后进制字符串
	 */
	public String encode(long num) {
		StringBuilder sb = new StringBuilder();
		if (num < 0) {
			encode0(-num, sb);
			sb.append(minusChar);
		} else {
			encode0(num, sb);
		}
		return sb.reverse().toString();
	}

	/**
	 *  62进制字符串转为数字
	 *
	 *  @param str 编码后的62进制字符串
	 *  @return 解码后的 10 进制字符串
	 */
	public long decode(String str) {
		if (str == null || str.isEmpty()) {
			return 0;
		}
		boolean minus = str.charAt(0) == minusChar;
		long num = decode0(str, minus ? 1 : 0);
		return minus ? -num : num;
	}

	private long decode0(String str, int begin) {
		int len = str.length();
		long num = 0;
		switch(codeAlgorithm) {
			case FAST_CONVERT:
				for (int i = begin; i < len; i++) {
					int index = str.charAt(i) - 48;
					num += index * powTable[len - i - 1];
				}
				break;
			case BINARY_SEARCH:
				for (int i = begin; i < len; i++) {
					int index = binarySearch(str.charAt(i));
					num += index * powTable[len - i - 1];
				}
				;
				break;
			case INDEXOF:
				for (int i = begin; i < len; i++) {
					int index = indexOf(str.charAt(i));
					num += index * powTable[len - i - 1];
				}
				break;
		}
		return num;
	}

	/*
	 * 默认算法效率较低，对于2的n次方进制，可以通过按位右移来代替除法和求余
	 */
	protected void encode0(long num, StringBuilder sb) {
		while (num >= scale) {
			long num1 = num / scale;
			sb.append(codeTable[(int) (num - num1 * scale)]);
			num = num1;
		}
		sb.append(codeTable[(int) num]);
	}

	protected void encode1(int num, StringBuilder sb) {
		while (num >= scale) {
			int num1 = num / scale;
			sb.append(codeTable[num - num1 * scale]);
			num = num1;
		}
		sb.append(codeTable[num]);
	}

	/**
	 * 对于无序码表，遍历查找
	 *
	 * @param charAt charAt
	 * @return int
	 */
	private int indexOf(char charAt) {
		for (int i = 0; i < scale; i++) {
			if (codeTable[i] == charAt) {
				return i;
			}
		}
		throw new IllegalArgumentException("Invalid char:"+charAt);
	}

	/**
	 * 对于有序码表，二分法查找 对于10以内的数字，其实最快的还是char-48。
	 *
	 * @param charAt charAt
	 * @return int
	 */
	private int binarySearch(char charAt) {
		int min = 0;
		int max = scale - 1;
		while (min <= max) {
			int middle = (min + max) / 2;
			char middleChar = codeTable[middle];
			if (middleChar == charAt) {
				return middle;
			} else if (charAt > middleChar) {
				min = middle + 1;
			} else {
				max = middle - 1;
			}
		}
		throw new IllegalArgumentException("Invalid char:"+charAt);
	}
}
