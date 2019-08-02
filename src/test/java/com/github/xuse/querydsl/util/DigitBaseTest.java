package com.github.xuse.querydsl.util;

import org.junit.Test;
import org.springframework.util.Assert;

/**
 * 10进制、62进制互转 edited by zhibo on 2015/05/21.
 */
public class DigitBaseTest {
	@Test
	public void testAll() {
		for (Radix hex : Radix.values()) {
			check(hex);
			check2(hex);
			check3(hex);
		}
	}

	@Test
	public void testPerformance() {
		for (int i = 0; i < 1000; i++) {
			Radix.D64.encode(i);
		}
		long time = System.nanoTime();
		for (int i = 0; i < 100000; i++) {
			Radix.D3.encode(i);
		}
		System.out.println(System.nanoTime() - time);

		time = System.nanoTime();
		for (int i = 0; i < 100000; i++) {
			Radix.D16.encode(i);
		}
		System.out.println(System.nanoTime() - time);

		time = System.nanoTime();
		for (int i = 0; i < 100000; i++) {
			Radix.D64.encode(i);
		}
		System.out.println(System.nanoTime() - time);

		time = System.nanoTime();
		for (int i = 0; i < 100000; i++) {
			Radix.D62.encode(i);
		}
		System.out.println(System.nanoTime() - time);

		time = System.nanoTime();
		for (int i = 0; i < 100000; i++) {
			Radix.D84.encode(i);
		}
		System.out.println(System.nanoTime() - time);

	}

	private void check2(Radix hex) {
		String s = hex.encode(Integer.MAX_VALUE);
		long value = hex.decode(s);
		Assert.isTrue(value == Integer.MAX_VALUE, hex + "错误");
	}

	private void check3(Radix hex) {
		String s = hex.encode(-1L);
		long value = hex.decode(s);
		Assert.isTrue(value == -1L, hex + "错误");
	}

	private static void check(Radix hex) {
		long num = 9223372036854775807L;

		// System.out.println("测试数值" + num);
		String s = hex.encode(num);
		System.out.println(hex.scale + "进制：" + s);
		long value = hex.decode(s);
		// System.out.println(hex.scale + "进制：" + value);
		Assert.isTrue(value == num, hex + "错误：" + num + " -> " + value);
	}
}
