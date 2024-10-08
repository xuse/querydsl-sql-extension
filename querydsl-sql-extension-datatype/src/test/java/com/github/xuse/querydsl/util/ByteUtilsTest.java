package com.github.xuse.querydsl.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.xuse.querydsl.datatype.util.ByteUtils;

public class ByteUtilsTest {
	
	private long long1 = 3_707_610_042L;
	
	private byte[] byte4 = new byte[] {0x1f,0x1f,0x10,0x22};
	private byte[] byte8 = new byte[] {(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff};
	
	@Test
	public void testBytes() {
		assertEquals(522129442,ByteUtils.bigEndian().getInt(byte4));
		assertEquals(31,ByteUtils.byteToInt((byte)0x1f));
		assertArrayEquals(new byte[] {0,0,0,0,-36,-3,-93,-70},ByteUtils.bigEndian().toBytes(long1));
	}
}
