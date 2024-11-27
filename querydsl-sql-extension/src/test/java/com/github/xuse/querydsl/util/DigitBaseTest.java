package com.github.xuse.querydsl.util;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 10进制、62进制互转
 */
public class DigitBaseTest {

	@Test
	public void testRadix() {
		
		assertEquals("64", Radix.D16.encodeInt(100));
		assertEquals("FFFFFF9C", Radix.D16.encodeInt(-100));
		assertEquals(100, Radix.D16.decodeInt("64"));
		assertEquals(-100, Radix.D16.decodeInt("FFFFFF9C"));
		
		
		assertEquals("64", Radix.D16.encode(100L));
		assertEquals("-64", Radix.D16.encode(-100L));
		assertEquals(100L, Radix.D16.decode("64"));
		assertEquals(-100L, Radix.D16.decode("-64"));
		assertEquals(0L, Radix.D16.decode(null));
		assertEquals(0L, Radix.D16.decode(""));
		
		assertEquals("00064", Radix.D16.encodeIntWithPadding(100, 5));
		assertEquals("FFFFFF9C", Radix.D16.encodeIntWithPadding(-100, 5));
		assertEquals("00064", Radix.D16.encodeWithPadding(100L, 5));
		
		//FIXME ME 这个case有点问题
		assertEquals("00-64", Radix.D16.encodeWithPadding(-100L, 5));
		assertEquals("$$$0Z", Radix.D64.encodeWithPadding(100L, 5));

		
		assertEquals("144", Radix.D8.encodeInt(100));
		assertEquals("37777777634", Radix.D8.encodeInt(-100));
		assertEquals(100, Radix.D8.decodeInt("144"));
		assertEquals(-100, Radix.D8.decodeInt("37777777634"));
		assertEquals("144", Radix.D8.encode(100L));
		assertEquals("-144", Radix.D8.encode(-100L));
		assertEquals(100L, Radix.D8.decode("144"));
		assertEquals(-100L, Radix.D8.decode("-144"));
		
		assertEquals("一零零", Radix.D10C.encodeInt(100));
		assertEquals("四二九四九六七一九六", Radix.D10C.encodeInt(-100));
		assertEquals(100, Radix.D10C.decodeInt("一零零"));
		assertEquals(-100, Radix.D10C.decodeInt("四二九四九六七一九六"));
		assertEquals("壹零零", Radix.D10CT.encode(100L));
		assertEquals("-壹零零", Radix.D10CT.encode(-100L));
		assertEquals(100L, Radix.D10CT.decode("壹零零"));
		assertEquals(-100L, Radix.D10CT.decode("-壹零零"));
	}

	@Test
	public void testAll() {
		for (Radix hex : Radix.values()) {
			check(hex);
			check2(hex);
			check3(hex);
		}
	}

	@Test
	public void testPerformance2() {
		String s = null;
		s = Radix.D16.encode(100000);
		long time = System.nanoTime();
		for (int i = 0; i < 100000; i++) {
			s = Radix.D16.encode(i);
		}
		System.out.println(System.nanoTime() - time);
		System.out.println(s);

		time = System.nanoTime();
		for (int i = 0; i < 100000; i++) {
			s = Integer.toHexString(i);
		}
		System.out.println(System.nanoTime() - time);
		System.out.println(s);
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
