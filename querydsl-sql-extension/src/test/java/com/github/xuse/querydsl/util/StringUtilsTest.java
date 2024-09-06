
package com.github.xuse.querydsl.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class StringUtilsTest {
	private String empty = "";
	private String blank = " ";
	private String hello = "Hello, World";
	private String china = "Hello, 中文";
	private String japanHi = "Hello, たま";
	private String japanKa = "Hello, タマ";
	private char[] trims={'\t','\r','\n',' '};
	private String trimString=" \n\ta\t\n ";
	

	@Test
	public void testStringUtils() {
		assertTrue(StringUtils.isBlank(null));
		assertTrue(StringUtils.isBlank(empty));
		assertTrue(StringUtils.isBlank(blank));
		assertFalse(StringUtils.isBlank(hello));
		assertFalse(StringUtils.isNotBlank(null));
		assertFalse(StringUtils.isNotBlank(empty));
		assertFalse(StringUtils.isNotBlank(blank));
		assertTrue(StringUtils.isNotBlank(hello));

		assertTrue(StringUtils.isEmpty(null));
		assertTrue(StringUtils.isEmpty(empty));
		assertFalse(StringUtils.isEmpty(blank));
		assertFalse(StringUtils.isNotEmpty(null));
		assertFalse(StringUtils.isNotEmpty(empty));
		assertTrue(StringUtils.isNotEmpty(blank));

		assertEquals("abc", StringUtils.concat("a", "b", "c", null));
		assertTrue(StringUtils.equalsIgnoreCase("abc", "abc"));
		assertTrue(StringUtils.equalsIgnoreCase("Abc", "abc"));
		assertTrue(StringUtils.equalsIgnoreCase(null, null));
		assertFalse(StringUtils.equalsIgnoreCase("Abc", null));
		assertFalse(StringUtils.equalsIgnoreCase("abc", "abcd"));
		assertFalse(StringUtils.equalsIgnoreCase("abc", "abdd"));
		
		assertFalse(StringUtils.hasAsian(hello));
		assertTrue(StringUtils.hasAsian(china));
		assertTrue(StringUtils.hasAsian(japanKa));
	
		assertTrue(StringUtils.indexOfAny(hello, 'w','o')>-1);
		assertTrue(StringUtils.indexOfAny(hello, 'x','s')==-1);
		assertTrue(StringUtils.indexOfAny("")==-1);
		assertTrue(StringUtils.indexOfAny(hello, "wo")>-1);
		assertTrue(StringUtils.indexOfAny(hello, "xs")==-1);
		assertTrue(StringUtils.indexOfAny("", new char[0],0)==-1);
		assertTrue(StringUtils.indexOfAny("", "")==-1);
		assertTrue(StringUtils.indexOfAny(" ", ' ')==0);
		
		assertTrue(StringUtils.indexOfAny(hello, "wo".toCharArray(),1)>-1);
		assertTrue(StringUtils.indexOfAny(hello, "xs".toCharArray(),2)==-1);

		assertEquals("48 65 6c 6c 6f 2c 20 57 6f 72 6c 64",StringUtils.join(hello.getBytes(StandardCharsets.UTF_8), (char)32));
		assertEquals("48 65 6C 6C 6F 2C 20 57 6F 72 6C 64",StringUtils.joinUpper(hello.getBytes(StandardCharsets.UTF_8), ' ', 0, 12));
		assertEquals("",StringUtils.join((List<String>)null, ' '));
		assertEquals(null,StringUtils.join((List<String>)null, " "));
		assertEquals("Hello World",StringUtils.join(Arrays.asList("Hello","World"), " "));
		assertEquals("Hello World",StringUtils.join(Arrays.asList("Hello","World"), ' '));
		assertEquals("true 1000",StringUtils.join(Arrays.<Object>asList(Boolean.TRUE,1000L), " "));
		assertEquals("true 1000",StringUtils.join(new Object[] {Boolean.TRUE,1000L}, " "));
		assertEquals("",StringUtils.join((byte[])null, ' '));
		
		StringBuilder sb=new StringBuilder();
		StringUtils.joinTo(Arrays.asList("Hello","World"), " ", sb);
		assertEquals("Hello World",sb.toString());
		
		sb.setLength(0);
		StringUtils.joinTo(null, " ", sb);
		assertEquals("",sb.toString());
		
		assertEquals("hello, world",StringUtils.lowerCase(hello));
		assertEquals("HELLO, WORLD",StringUtils.upperCase(hello));
		assertNull(StringUtils.lowerCase(null));
		assertNull(StringUtils.upperCase(null));
		

		assertEquals(null,StringUtils.leftPad(null, 3));
		assertEquals("   ",StringUtils.leftPad(blank, 3));
		assertEquals("00 ",StringUtils.leftPad(blank, 3, '0'));
		assertEquals("00 ",StringUtils.leftPad(blank, 3, "0"));
		assertEquals("00 ",StringUtils.leftPad(blank, 3, "00"));
		assertEquals("000 ",StringUtils.leftPad(blank, 4, "00"));
		assertEquals(hello,StringUtils.leftPad(hello, 3, "00"));
		
		assertEquals(null,StringUtils.rightPad(null, 3));
		assertEquals("   ",StringUtils.rightPad(blank, 3));
		assertEquals(" 00",StringUtils.rightPad(blank, 3, '0'));
		assertEquals(" 00",StringUtils.rightPad(blank, 3, "0"));
		assertEquals(" 00",StringUtils.rightPad(blank, 3, "00"));
		assertEquals(" 000",StringUtils.rightPad(blank, 4, "00"));
		assertEquals(hello,StringUtils.rightPad(hello, 3, "00"));
		
		assertEquals(8, StringUtils.lastIndexOfAny(hello, new char[] { 'o' }, 3));
		assertEquals(-1, StringUtils.lastIndexOfAny(hello, new char[] { 'x' }, 3));
		assertEquals(-1, StringUtils.lastIndexOfAny(hello, new char[] { 'x' }, -1));
		assertEquals(-1, StringUtils.lastIndexOfAny(null, new char[] { 'x' }, -1));
		
		assertEquals("a",StringUtils.lrtrim(trimString ,trims , trims));
		assertEquals(4, StringUtils.ltrim(trimString ,trims).length());
		assertEquals(4, StringUtils.rtrim(trimString ,trims).length());
		
		assertEquals(4, StringUtils.ltrim(trimString).length());
		assertEquals(4, StringUtils.rtrim(trimString).length());
		
		assertEquals("a", StringUtils.trim(trimString));
		assertEquals("", StringUtils.trimToEmpty(null));
		assertEquals("", StringUtils.trimToEmpty(" "));
		assertEquals(null, StringUtils.trimToNull(" "));
		assertEquals(null, StringUtils.trimToNull(null));
		
		
		assertTrue(Integer.parseInt(StringUtils.randomString())>0);
		assertEquals("a", StringUtils.removeBucket("(a)"));
		assertEquals("", StringUtils.removeBucket(""));
		assertEquals("a", StringUtils.removeBucket("a"));
		assertEquals("Hell,Wrld", StringUtils.removeChars(hello, 'o',' '));
		assertEquals(null, StringUtils.removeChars(null, 'o',' '));
		
		assertEquals("www.aaa", StringUtils.removeEnd("www.aaa.www", ".www"));
		assertEquals("aaa.www", StringUtils.removeStart("www.aaa.www", "www."));
		assertEquals(null,StringUtils.repeat(null, 4));
		assertEquals("",StringUtils.repeat("a", -1));
		assertEquals("aaaa",StringUtils.repeat('a', 4));
		assertEquals("aaaa",StringUtils.repeat("a", 4));
		assertEquals("aaaaaaaa",StringUtils.repeat("aa", 4));
		assertEquals("aaaaaaaaaaaa",StringUtils.repeat("aaa", 4));
		sb.setLength(0);
		StringUtils.repeat(sb, "a", -1);
		StringUtils.repeat(sb, "a", 4);
		assertEquals("aaaa",sb.toString());
		
		assertEquals("?,?,?",StringUtils.repeat("?", ",", 3));
		assertEquals("applebusclock",
				StringUtils.replaceEach("abc",new String[] {"a","b","c", null}, new String[] {"apple","bus","clock", null})
		);
		assertArrayEquals(null,	StringUtils.split(null));
		assertArrayEquals(new String[] {"a","b","c"},	StringUtils.split("a b c"));
		assertArrayEquals(new String[] {"a","b","c"},	StringUtils.split("a b c",' '));
		assertArrayEquals(new String[] {"a","b","c"},	StringUtils.split("a b c"," "));
		assertArrayEquals(new String[] {"a","b c"},		StringUtils.split("a b c"," ",2));
		assertArrayEquals(new String[] {" "," c"},		StringUtils.split("a b c","ab",2));
		
		
		assertArrayEquals(new String[] {"a","c"},		StringUtils.splitByWholeSeparator("a b c", " b "));
		assertArrayEquals(new String[] {"a","c d"},		StringUtils.splitByWholeSeparator("a b c d", " b ",2));
		
		assertArrayEquals(new String[] {" a","c d"},		StringUtils.splitByWholeSeparatorPreserveAllTokens(" a b c d", " b "));
		assertArrayEquals(new String[] {" a","c d"},		StringUtils.splitByWholeSeparatorPreserveAllTokens(" a b c d", " b ",3));
		assertArrayEquals(new String[] {" a b c d"},		StringUtils.splitByWholeSeparatorPreserveAllTokens(" a b c d", " b ",1));
		assertArrayEquals(new String[] {" a","c d"},		StringUtils.splitByWholeSeparator(" a b c d", " b ",2));
		
		assertArrayEquals(new String[] {"","a","b","c","d"},		StringUtils.splitPreserveAllTokens(" a b c d"));
		assertArrayEquals(new String[] {"","a","b","c"},		 StringUtils.splitPreserveAllTokens(" a b c",' '));
		
		assertArrayEquals(new String[] {"a","b","c"},		StringUtils.tokenizeToStringArray("a b c", " "));
		assertArrayEquals(new String[] {"a","b","c"},		StringUtils.tokenizeToStringArray(" a b c ", " ", true,true));
		assertArrayEquals(new String[] {"a","b","c"},		StringUtils.tokenizeToStringArray(" a b  c", " ", false, true));
		assertArrayEquals(new String[] {"a","b","c"},		StringUtils.tokenizeToStringArray(" a b  c ", " ", true, false));
		assertArrayEquals(new String[] {"a","b","c"},		StringUtils.tokenizeToStringArray(" a b c ", " ", false, false));
		assertNull(StringUtils.tokenizeToStringArray(null, " "));
		
		assertTrue(StringUtils.startsWithIgnoreCase(china, 0, "hello"));
		assertTrue(StringUtils.startsWithIgnoreCaseAndWs(china, "hello"));
		assertFalse(StringUtils.startsWithIgnoreCaseAndWs(null, "hello"));
		assertTrue(StringUtils.startsWithIgnoreCaseAndWs(null, null));
		assertTrue(StringUtils.startsWithIgnoreCaseAndWs(" HELLO, China", "hello",0));
		
		
		assertEquals(" World",StringUtils.substringAfter("Hello, World", ","));
		assertEquals("t",StringUtils.substringAfterLast("Hello, World,t", ","));
		assertEquals("",StringUtils.substringAfter("Hello", ","));
		assertEquals(null,StringUtils.substringAfter(null, ","));
		assertEquals("Hello",StringUtils.substringAfter("Hello", ""));
		assertEquals("Hello",StringUtils.substringAfter("Hello", null));
		assertEquals("Hello",StringUtils.substringAfterIfExist("Hello", ","));
		assertEquals("ello",StringUtils.substringAfterIfExist("Hello", "H"));
		assertEquals("Hello",StringUtils.substringAfterLastIfExist("Hello", ","));
		
		assertEquals(null,StringUtils.substringAfterLastIfExist(null, ","));
		assertEquals("ello",StringUtils.substringAfterLastIfExist("Hello", "H"));
		assertEquals("",StringUtils.substringAfterLast("Hello", ""));
		assertEquals("",StringUtils.substringAfterLastIfExist("Hello", ""));
		assertEquals("",StringUtils.substringAfterLast("Hello", ","));
		assertEquals(null,StringUtils.substringAfter(null, ","));
		assertEquals(null,StringUtils.substringAfterLast(null, ","));
		assertEquals(null,StringUtils.substringAfterIfExist(null, ","));
		
		assertEquals("Hello",StringUtils.substringBefore("Hello, World", ","));
		assertEquals("Hello, World",StringUtils.substringBeforeLast("Hello, World,", ","));
		assertEquals(null,StringUtils.substringBefore(null, ","));
		assertEquals(null,StringUtils.substringBeforeLast(null, ","));
		assertEquals("",StringUtils.substringBefore("Hello, World", ""));
		assertEquals("Hello, World,",StringUtils.substringBeforeLast("Hello, World,", ""));
		assertEquals("Hello, World,",StringUtils.substringBefore("Hello, World,", "x"));
		assertEquals("Hello, World,",StringUtils.substringBeforeLast("Hello, World,", "x"));
		
		assertEquals("b)c(d",StringUtils.substringBetween("a(b)c(d)e", "(", ")"));
		assertEquals("",StringUtils.substringBetween("a(b)c(d)e", "x", ")"));
		assertEquals("",StringUtils.substringBetween("a(b)c(d)e", "(", "x"));
		assertEquals("",StringUtils.substringBetween("a(b)c(d)e", "c", "c"));
		assertEquals("",StringUtils.substringBetween("a(b)c(d)e", "d", "b"));
		
		
		assertEquals(true,StringUtils.toBoolean("true", Boolean.TRUE));
		assertEquals(true,StringUtils.toBoolean("y", Boolean.TRUE));
		assertEquals(true,StringUtils.toBoolean("on", Boolean.TRUE));
		assertEquals(true,StringUtils.toBoolean("T", Boolean.TRUE));
		assertEquals(false,StringUtils.toBoolean("false", Boolean.TRUE));
		assertEquals(false,StringUtils.toBoolean("0", Boolean.TRUE));
		assertEquals(false,StringUtils.toBoolean("off", Boolean.TRUE));
		assertEquals(false,StringUtils.toBoolean("F", Boolean.TRUE));
		assertEquals(true,StringUtils.toBoolean("null", Boolean.TRUE));
		
		byte[] data=StringUtils.toByteArray(StringUtils.join(china.getBytes(StandardCharsets.UTF_8), (char)0));
		assertArrayEquals(data,china.getBytes(StandardCharsets.UTF_8));
		
		assertEquals(3.14159, StringUtils.toDouble("3.14159", null), 0.00001);
		assertEquals(3.14159, StringUtils.toDouble("", 3.14159d), 0.00001);
		assertEquals(2.787, StringUtils.toDouble("3.1.4", 2.787), 0.00001);
		
		assertEquals(3.1415, StringUtils.toFloat("3.1415", null), 0.00001);
		assertEquals(3.1415, StringUtils.toFloat("", 3.1415f), 0.00001);
		assertEquals(2.787, StringUtils.toFloat("3.1.4", 2.787f), 0.00001);		
		
		assertEquals(31415, StringUtils.toInt("31415", null));
		assertEquals(31415, StringUtils.toInt("", 31415));
		assertEquals(29999, StringUtils.toInt("3..2", 29999));		
		assertEquals(31415L, StringUtils.toLong("31415", null));
		assertEquals(31415L, StringUtils.toLong("", 31415L));
		assertEquals(29999L, StringUtils.toLong("3..2", 29999L));		
		
		
		assertEquals("aa()~!@#$.txt",StringUtils.toFilename("a\\a//<>()~!@#$.txt", ""));
		assertEquals("aa()~!@#$",StringUtils.toFilename("a\\a//<>()~!@#$.", ""));
		
		assertEquals("48656c6c6f2c20e4b8ade69687",StringUtils.toHexString(china.getBytes(StandardCharsets.UTF_8)));
		assertEquals("48656C6C6F2C20E4B8ADE69687",StringUtils.toHexStringUppercase(china.getBytes(StandardCharsets.UTF_8)));
		assertEquals("",StringUtils.toHexString(null));
		assertEquals("",StringUtils.toHexStringUppercase(null));
		
		assertEquals("", StringUtils.toString(null));
		assertEquals("true", StringUtils.toString(Boolean.TRUE));
		
		assertEquals("Hello, W...",StringUtils.truncate(hello, 8, "..."));
		assertEquals("Hello, World",StringUtils.truncate(hello, 14, "..."));
		assertEquals("Hello, W",StringUtils.truncate(hello, 8));
		
		assertEquals("http%3A%2F%2Fa%3Eb.com",StringUtils.urlEncode("http://a>b.com"));
		assertEquals("http%3A%2F%2Fa%3Eb.com",StringUtils.urlEncode("http://a>b.com",StandardCharsets.UTF_8));
		assertEquals("http://a>b.com",StringUtils.urlDecode("http%3A%2F%2Fa%3Eb.com"));
		assertEquals("http://a>b.com",StringUtils.urlDecode("http%3A%2F%2Fa%3Eb.com",StandardCharsets.UTF_8));
		assertEquals("a", StringUtils.concat("a"));
	}
	
	@Test
	public void testNullInput() {
		String ret=null;
		byte[] data=null;
		assertEquals("",StringUtils.join(data, (char)0));
		assertEquals("",StringUtils.join(data, (char)0, 0, 3));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testExceptionCase() {
		StringUtils.toBoolean("s", null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testExceptionCase2() {
		StringUtils.toDouble("s", null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testExceptionCase3() {
		StringUtils.toFloat("s", null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testExceptionCase4() {
		StringUtils.toInt("s", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testExceptionCase5() {
		StringUtils.toLong("s", null);
	}
	
	@Test
	public void testTmp() {
	}
	
	@Test
	public void testDigest() {
		assertTrue(StringUtils.generateGuid().length()==36);
		assertEquals("265b86c6",StringUtils.getCRC(new ByteArrayInputStream(hello.getBytes(StandardCharsets.UTF_8))));
		assertEquals("265b86c6",StringUtils.getCRC(hello));
		assertEquals("82bb413746aee42f89dea2b59614f9ef",StringUtils.getMD5(new ByteArrayInputStream(hello.getBytes(StandardCharsets.UTF_8))));
		assertEquals("82bb413746aee42f89dea2b59614f9ef",StringUtils.getMD5(hello));
		assertEquals("grtBN0au5C+J3qK1lhT57w==",StringUtils.getMD5InBase64(hello));
		assertEquals("907d14fb3af2b0d4f18c2d46abe8aedce17367bd",StringUtils.getSHA1(new ByteArrayInputStream(hello.getBytes(StandardCharsets.UTF_8))));
		assertEquals("907d14fb3af2b0d4f18c2d46abe8aedce17367bd",StringUtils.getSHA1(hello));
		assertEquals("03675ac53ff9cd1535ccc7dfcdfa2c458c5218371f418dc136f2d19ac1fbe8a5",StringUtils.getSHA256(new ByteArrayInputStream(hello.getBytes(StandardCharsets.UTF_8))));
		assertEquals("03675ac53ff9cd1535ccc7dfcdfa2c458c5218371f418dc136f2d19ac1fbe8a5",StringUtils.getSHA256(hello));
	}

	@Test
	public void testNumbers() {
		assertEquals("965.14M", NumberUtils.formatSize(1012023432L));
		assertEquals("2G", NumberUtils.formatSize(Integer.MAX_VALUE));
		assertEquals("2.932T", NumberUtils.formatSize(3223372037758L, 3));
		

		assertEquals("8.8%",NumberUtils.toPercent(88, 1000));
		
	}
}
