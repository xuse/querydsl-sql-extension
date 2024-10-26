package com.github.xuse.querydsl.util;

import org.junit.Ignore;
import org.junit.Test;

public class DigitPerformance {
	@Test
	@Ignore
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
		@SuppressWarnings("unused")
		String s;
		for (int i = 0; i < 100000; i++) {
			s = Integer.toHexString(i);
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
}
