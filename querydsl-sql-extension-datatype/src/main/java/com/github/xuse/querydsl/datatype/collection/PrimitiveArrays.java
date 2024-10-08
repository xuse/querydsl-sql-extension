package com.github.xuse.querydsl.datatype.collection;

/**
 * 基元数组工具包
 * @author Joey
 *
 */
public class PrimitiveArrays {

	/**
	 * 算法效率不高，仅限于少量元素合并。
	 * 
	 * @param array1 array1
	 * @param array2 array2
	 * @return 合并数组，消除重复
	 */
	public static int[] merge(int[] array1, int[] array2) {
		IntList list = new IntList();
		for (int str : array1) {
			list.add(str);
		}
		for (int str : array2) {
			if (!list.contains(str)) {
				list.add(str);
			}
		}
		return list.toArrayUnsafe();
	}

	/**
	 * 算法效率不高，仅限于少量元素合并。
	 * 
	 * @param array1 array1
	 * @param array2 array2
	 * @return 合并数组，消除重复
	 */
	public static char[] merge(char[] array1, char[] array2) {
		CharList list = new CharList();
		for (char str : array1) {
			list.add(str);
		}
		for (char str : array2) {
			if (!list.contains(str)) {
				list.add(str);
			}
		}
		return list.toArrayUnsafe();
	}

	/**
	 * 算法效率不高，仅限于少量元素合并。
	 * 
	 * @param array1 array1
	 * @param array2 array2
	 * @return 合并数组，消除重复
	 */
	public static boolean[] merge(boolean[] array1, boolean[] array2) {
		BooleanList list = new BooleanList();
		for (boolean str : array1) {
			list.add(str);
		}
		for (boolean str : array2) {
			if (!list.contains(str)) {
				list.add(str);
			}
		}
		return list.toArrayUnsafe();
	}

	/**
	 * 算法效率不高，仅限于少量元素合并。
	 * 
	 * @param array1 array1
	 * @param array2 array2
	 * @return 合并数组，消除重复
	 */
	public static long[] merge(long[] array1, long[] array2) {
		LongList list = new LongList();
		for (long str : array1) {
			list.add(str);
		}
		for (long str : array2) {
			if (!list.contains(str)) {
				list.add(str);
			}
		}
		return list.toArrayUnsafe();
	}

	/**
	 * 算法效率不高，仅限于少量元素合并。
	 * 
	 * @param array1 array1
	 * @param array2 array2
	 * @return 合并数组，消除重复
	 */
	public static byte[] merge(byte[] array1, byte[] array2) {
		ByteList list = new ByteList();
		for (byte str : array1) {
			list.add(str);
		}
		for (byte str : array2) {
			if (!list.contains(str)) {
				list.add(str);
			}
		}
		return list.toArrayUnsafe();
	}

	/**
	 * 算法效率不高，仅限于少量元素合并。
	 * 
	 * @param array array
	 * @return 消除重复
	 */
	public static int[] removeDups(int[] array) {
		IntList list = new IntList(array.length);
		for (int str : array) {
			if (!list.contains(str))
				list.add(str);
		}
		return list.toArrayUnsafe();
	}

	/**
	 * 算法效率不高，仅限于少量元素合并。
	 * 
	 * @param array array
	 * @return 消除重复
	 */
	public static char[] removeDups(char[] array) {
		CharList list = new CharList();
		for (char str : array) {
			if (!list.contains(str))
				list.add(str);
		}
		return list.toArrayUnsafe();
	}

	/**
	 * 算法效率不高，仅限于少量元素合并。
	 * 
	 * @param array array
	 * @return 消除重复
	 */
	public static byte[] removeDups(byte[] array) {
		ByteList list = new ByteList();
		for (byte str : array) {
			if (!list.contains(str))
				list.add(str);
		}
		return list.toArrayUnsafe();
	}

	/**
	 * 算法效率不高，仅限于少量元素合并。
	 * 
	 * @param array array
	 * @return 消除重复
	 */
	public static double[] removeDups(double[] array) {
		DoubleList list = new DoubleList();
		for (double str : array) {
			if (!list.contains(str))
				list.add(str);
		}
		return list.toArrayUnsafe();
	}

	/**
	 * 算法效率不高，仅限于少量元素合并。
	 * 
	 * @param array array
	 * @return 消除重复
	 */
	public static boolean[] removeDups(boolean[] array) {
		BooleanList list = new BooleanList();
		for (boolean str : array) {
			if (!list.contains(str))
				list.add(str);
		}
		return list.toArrayUnsafe();
	}

}
