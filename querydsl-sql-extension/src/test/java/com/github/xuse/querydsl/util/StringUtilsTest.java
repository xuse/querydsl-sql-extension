
package com.github.xuse.querydsl.util;

import static com.github.xuse.querydsl.util.JefBase64.STANDARD;
import static com.github.xuse.querydsl.util.JefBase64.STANDARD_WITH_WRAP;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
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
		assertFalse(StringUtils.equalsIgnoreCase("abc", "abd"));
		
		assertFalse(StringUtils.hasAsian(hello));
		assertTrue(StringUtils.hasAsian(china));
		assertTrue(StringUtils.hasAsian(japanKa));
	
		assertTrue(StringUtils.indexOfAny(hello, 'w','o')>-1);
		assertTrue(StringUtils.indexOfAny(hello, 'x','s')==-1);
		assertTrue(StringUtils.indexOfAny("")==-1);
		assertTrue(StringUtils.indexOfAny(hello, "wo")>-1);
		assertTrue(StringUtils.indexOfAny(hello, "xs")==-1);
		assertTrue(StringUtils.indexOfAny("", "")==-1);
		assertTrue(StringUtils.indexOfAny("", new char[0],0)==-1);
		assertTrue(StringUtils.indexOfAny("a", "")==-1);
		assertTrue(StringUtils.indexOfAny(" ", ' ')==0);
		assertTrue(StringUtils.indexOfAny(" ",  new char[0])==-1);
		
		assertTrue(StringUtils.indexOfAny("a\uD801\uA001123", '\uD801','\uA001')>0);
		assertTrue(StringUtils.indexOfAny("a\uD801\uA001123", '\uD801','\uA002')==-1);
		
		
		assertTrue(StringUtils.indexOfAny(hello, "wo".toCharArray(),1)>-1);
		assertTrue(StringUtils.indexOfAny(hello, "xs".toCharArray(),2)==-1);

		assertEquals("48 65 6c 6c 6f 2c 20 57 6f 72 6c 64",StringUtils.join(hello.getBytes(StandardCharsets.UTF_8), (char)32));
		assertEquals("48 65 6C 6C 6F 2C 20 57 6F 72 6C 64",StringUtils.joinUpper(hello.getBytes(StandardCharsets.UTF_8), ' ', 0, 12));
		assertEquals("",StringUtils.join((List<String>)null, ' '));
		assertEquals("",StringUtils.join(Collections.emptyList(), ' '));
		assertEquals(null,StringUtils.join((List<String>)null, " "));
		assertEquals("",StringUtils.join(Collections.emptyList(), " "));
		assertEquals("Hello World",StringUtils.join(Arrays.asList("Hello","World"), " "));
		assertEquals("Hello World",StringUtils.join(Arrays.asList("Hello","World"), ' '));
		assertEquals("true 1000 ",StringUtils.join(Arrays.<Object>asList(Boolean.TRUE,1000L,null), " "));
		assertEquals("true 1000 ",StringUtils.join(new Object[] {Boolean.TRUE,1000L,null}, " "));
		assertEquals("",StringUtils.join((Object[])null, " "));
		assertEquals("",StringUtils.join(new Object[0], " "));
		
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
		assertEquals(null,StringUtils.leftPad(null, 4, "00"));
		assertEquals(hello,StringUtils.leftPad(hello, 3, "00"));
		assertEquals(hello,StringUtils.leftPad(hello, 3, '0'));
		assertEquals(hello,StringUtils.leftPad(hello, 5, ""));
		assertEquals("ab"+hello,StringUtils.leftPad(hello, 14, "abcd"));
		
		assertEquals(null,StringUtils.rightPad(null, 3));
		assertEquals("   ",StringUtils.rightPad(blank, 3));
		assertEquals(" 00",StringUtils.rightPad(blank, 3, '0'));
		assertEquals(" 00",StringUtils.rightPad(blank, 3, "0"));
		assertEquals(" 00",StringUtils.rightPad(blank, 3, "00"));
		assertEquals(null,StringUtils.rightPad(null, 3, "00"));
		assertEquals(" 000",StringUtils.rightPad(blank, 4, "00"));
		assertEquals(hello,StringUtils.rightPad(hello, 3, "00"));
		assertEquals(hello,StringUtils.rightPad(hello, 3, '0'));
		assertEquals(hello,StringUtils.rightPad(hello, 3, ""));
		assertEquals(hello+"ab",StringUtils.rightPad(hello, 14, "abcd"));
		
		assertEquals(8, StringUtils.lastIndexOfAny(hello, new char[] { 'o' }, 3));
		assertEquals(-1, StringUtils.lastIndexOfAny(hello, new char[] { 'x' }, 3));
		assertEquals(-1, StringUtils.lastIndexOfAny(hello, new char[] { 'x' }, -1));
		assertEquals(-1, StringUtils.lastIndexOfAny(null, new char[] { 'x' }, -1));
		assertEquals(-1, StringUtils.lastIndexOfAny(hello, null, -1));
		
		assertEquals("a",StringUtils.lrtrim(trimString ,trims , trims));
		assertEquals(4, StringUtils.ltrim(trimString ,trims).length());
		assertEquals(4, StringUtils.rtrim(trimString ,trims).length());
		
		assertEquals(4, StringUtils.ltrim(trimString).length());
		assertEquals(4, StringUtils.rtrim(trimString).length());
		assertEquals(null, StringUtils.ltrim(null));
		assertEquals(null, StringUtils.rtrim(null));
		assertEquals(null, StringUtils.ltrim(null,' '));
		assertEquals(null, StringUtils.rtrim(null,' '));
		assertEquals("", StringUtils.ltrim(" "));
		assertEquals("", StringUtils.rtrim(" "));
		assertEquals("", StringUtils.ltrim(" ",' '));
		assertEquals("", StringUtils.rtrim(" ",' '));
		assertEquals("a", StringUtils.ltrim("a"));
		assertEquals("a", StringUtils.rtrim("a"));
		assertEquals("a", StringUtils.ltrim("a",' '));
		assertEquals("a", StringUtils.rtrim("a",' '));
		
		
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
		assertEquals(null, StringUtils.removeStart(null, ".www"));
		assertEquals("a", StringUtils.removeStart("a", ""));
		assertEquals("a", StringUtils.removeStart("a", "b"));
		
		assertEquals(null, StringUtils.removeEnd(null, ".www"));
		assertEquals("aaa", StringUtils.removeEnd("aaa", null));
		assertEquals("aaa", StringUtils.removeEnd("aaa", "bbb"));
		
		assertEquals(null,StringUtils.repeat(null, 4));
		assertEquals("",StringUtils.repeat("", 4));
		assertEquals("a",StringUtils.repeat("a", 1));
		assertEquals("",StringUtils.repeat("a", -1));
		assertEquals("aaaa",StringUtils.repeat('a', 4));
		assertEquals("aaaa",StringUtils.repeat("a", 4));
		assertEquals("aaaaaaaa",StringUtils.repeat("aa", 4));
		assertEquals("aaaaaaaaaaaa",StringUtils.repeat("aaa", 4));
		sb.setLength(0);
		StringUtils.repeatTo(sb, "a", -1);
		StringUtils.repeatTo(sb, "a", 4);
		assertEquals("aaaa",sb.toString());
		
		assertEquals("?,?,?",StringUtils.repeat("?", ",", 3));
		assertEquals(null,StringUtils.repeat(null, ",", 3));
		assertEquals("???",StringUtils.repeat("?", null, 3));
		assertEquals("",StringUtils.repeat('?', 0));
		
		assertEquals("applebusclock",
				StringUtils.replaceEach("abc",new String[] {"a","b","c", null}, new String[] {"apple","bus","clock", null})
		);
		assertEquals(null,
				StringUtils.replaceEach(null,new String[] {"a","b","c", null}, new String[] {"apple","bus","clock", null})
		);
		assertEquals("",
				StringUtils.replaceEach("",new String[] {"a","b","c", null}, new String[] {"apple","bus","clock", null})
		);
		assertEquals("a",
				StringUtils.replaceEach("a",null, null)
		);
		assertEquals("a",
				StringUtils.replaceEach("a",new String[] {null}, new String[] {null})
		);
		
		assertArrayEquals(null,	StringUtils.split(null));
		assertArrayEquals(new String[] {"a","b","c"},	StringUtils.split("a b c"));
		assertArrayEquals(new String[] {"a","b","c"},	StringUtils.split("a b c",' '));
		assertArrayEquals(null,	StringUtils.split(null,' '));
		assertArrayEquals(new String[0],	StringUtils.split("",' '));
		
		assertArrayEquals(new String[] {"a","b","c"},	StringUtils.split("a b c"," "));
		assertArrayEquals(new String[] {"a","b c"},		StringUtils.split("a b c"," ",2));
		assertArrayEquals(new String[] {" "," c"},		StringUtils.split("a b c","ab",2));
		
		
		assertArrayEquals(new String[] {"a","c"},		StringUtils.splitByWholeSeparator("a b c", " b "));
		assertArrayEquals(new String[] {"a","c d"},		StringUtils.splitByWholeSeparator("a b c d", " b ",2));
		assertArrayEquals(null,		StringUtils.splitByWholeSeparator(null, " b ",2));
		assertArrayEquals(new String[] {" a","c d"},		StringUtils.splitByWholeSeparator(" a b c d", " b ",2));
		assertArrayEquals(new String[0],		StringUtils.splitByWholeSeparator("", " b ",2));
		assertArrayEquals(new String[] {"a","b"},		StringUtils.splitByWholeSeparator("a b", null));
		assertArrayEquals(new String[] {"a","b"},		StringUtils.splitByWholeSeparator("a b", ""));
		assertArrayEquals(new String[] {"a","b",""},		StringUtils.splitByWholeSeparator("a b ", ""));
		assertArrayEquals(new String[] {"a","b",""},		StringUtils.splitByWholeSeparator(" a b ", ""));
		
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
		assertEquals("",StringUtils.substringAfterLast("Hello, World,t", "Hello, World,t"));
		
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
		assertEquals(true,StringUtils.toBoolean("1", Boolean.TRUE));
		assertEquals(true,StringUtils.toBoolean("y", Boolean.TRUE));
		assertEquals(true,StringUtils.toBoolean("ON", Boolean.TRUE));
		assertEquals(true,StringUtils.toBoolean("T", Boolean.TRUE));
		assertEquals(true,StringUtils.toBoolean("yes", Boolean.TRUE));
		assertEquals(false,StringUtils.toBoolean("false", Boolean.TRUE));
		assertEquals(false,StringUtils.toBoolean("0", Boolean.TRUE));
		assertEquals(false,StringUtils.toBoolean("off", Boolean.TRUE));
		assertEquals(false,StringUtils.toBoolean("F", Boolean.TRUE));
		assertEquals(false,StringUtils.toBoolean("no", Boolean.TRUE));
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
		
		assertEquals("48656c6c6f2c20e4b8ade69687",StringUtils.toHexString(china.getBytes(StandardCharsets.UTF_8),false));
		assertEquals("48656C6C6F2C20E4B8ADE69687",StringUtils.toHexStringUppercase(china.getBytes(StandardCharsets.UTF_8),false));
		assertEquals("48 65 6c 6c 6f 2c 20 e4 b8 ad e6 96 87",StringUtils.toHexString(china.getBytes(StandardCharsets.UTF_8),true));
		assertEquals("48 65 6C 6C 6F 2C 20 E4 B8 AD E6 96 87",StringUtils.toHexStringUppercase(china.getBytes(StandardCharsets.UTF_8),true));
		assertEquals("",StringUtils.toHexString(null,false));
		assertEquals("",StringUtils.toHexStringUppercase(null,false));
		assertEquals("",StringUtils.toHexString(new byte[0],false));
		assertEquals("",StringUtils.toHexStringUppercase(new byte[0],false));
		
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
		byte[] data=null;
		assertEquals("",StringUtils.join(data, (char)0));
		assertEquals("",StringUtils.join(data, (char)0, 0, 3));
		
		assertEquals("", StringUtils.join(data, (char) 0, 0, 0));
		assertEquals("", StringUtils.joinUpper(data, (char) 0, 0, 3));
		
		data = new byte[0];
		assertEquals("", StringUtils.join(data, (char) 0));
		assertEquals("", StringUtils.join(data, (char) 0, 0, 3));

		assertEquals("", StringUtils.join(data, (char) 0, 0, 0));
		assertEquals("", StringUtils.joinUpper(data, (char) 0, 0, 3));
		
		data = new byte[] {0x01};
		assertEquals("01", StringUtils.join(data, (char) 0));
		assertEquals("01", StringUtils.join(data, (char) 0, 0, 3));

		assertEquals("", StringUtils.join(data, (char) 0, 0, 0));
		assertEquals("01", StringUtils.joinUpper(data, (char) 0, 0, 3));
		
		assertEquals("01", StringUtils.join(data, ' '));
		assertEquals("01", StringUtils.join(data, ' ', 0, 3));

		assertEquals("", StringUtils.join(data, ' ', 0, 0));
		assertEquals("", StringUtils.joinUpper(data, ' ', 0, 0));
		assertEquals("01", StringUtils.joinUpper(data, ' ', 0, 3));
		
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
	
	@Test
	public void testBase64() {
		String base64=JefBase64.encodeUTF8(china);
		assertEquals("SGVsbG8sIOS4reaWhw==", base64);
		
		byte[] data=JefBase64.decodeFast(base64, STANDARD_WITH_WRAP);
		assertArrayEquals(china.getBytes(StandardCharsets.UTF_8), data);
		assertEquals(china, JefBase64.decodeUTF8(base64));
		
		assertArrayEquals(new byte[0], JefBase64.decodeFast(new byte[0],0,0,STANDARD));
		
		data=JefBase64.decodeFast(base64, STANDARD_WITH_WRAP);
		assertArrayEquals(china.getBytes(StandardCharsets.UTF_8), data);
		assertArrayEquals(new byte[0],JefBase64.decodeFast("", STANDARD));
		
		base64=JefBase64.encode(data, 0, data.length, JefBase64.TENCENT_URL_ESCAPE);
		assertEquals(china, new String(JefBase64.decodeFast(base64,JefBase64.TENCENT_URL_ESCAPE),StandardCharsets.UTF_8));
		
		data=base64.getBytes();
		assertEquals(china, new String(JefBase64.decodeFast(data,0,data.length ,JefBase64.TENCENT_URL_ESCAPE),StandardCharsets.UTF_8));
		
		ByteBuffer buff=ByteBuffer.wrap(china.getBytes(StandardCharsets.UTF_8));
		assertEquals(base64, JefBase64.encode(buff,JefBase64.TENCENT_URL_ESCAPE));
		
		
		String raw="sdivgd发誓待送发送地sogndngrerrewf是的撒肤色更首度公开答复管道";
		base64 = JefBase64.encodeUTF8(raw, true);
		assertEquals("c2Rpdmdk5Y+R6KqT5b6F6YCB5Y+R6YCB5Zywc29nbmRuZ3JlcnJld2bmmK/nmoTmkpLogqToibLm\r\n"
				+ "m7TpppbluqblhazlvIDnrZTlpI3nrqHpgZM=", base64);
		
		data= JefBase64.decode(base64.getBytes());
		assertEquals(raw, new String(data, StandardCharsets.UTF_8));
		assertEquals(raw, JefBase64.decodeUTF8(base64, STANDARD));
		assertNull(JefBase64.encode(null));
		
		assertEquals(115,JefBase64.decodeFirstByte(base64, 0, STANDARD));
		
	}
	
	@Test
	public void testReplaceEach() throws NoSuchMethodException, SecurityException, IllegalAccessException {
		String a=null;
		assertEquals(null,StringUtils.replaceEach(a, new String[0], new String[0]));
		assertEquals("",StringUtils.replaceEach("", new String[0], new String[0]));
		
		a="1232";
		assertEquals(a,StringUtils.replaceEach(a, new String[] {""}, new String[0]));
		assertEquals(a,StringUtils.replaceEach(a, new String[] {""}, null));
		
		String[] search=new String[] {"23","32"};
		String result= StringUtils.replaceEach(a, search, new String[]{"23323","abc"});
		assertEquals("1233232",result);
		try {
			result= StringUtils.replaceEach(a, search, new String[]{"23323"});
		}catch(Exception e){
			assertEquals(IllegalArgumentException.class,e.getClass());
		}
			
		result= StringUtils.replaceEach(a, search, new String[]{"23","abc"});
		assertEquals("1232",result);
		
		
		Method privateMethod=StringUtils.class.getDeclaredMethod("replaceEach", String.class,String[].class,String[].class,boolean.class,int.class);
		privateMethod.setAccessible(true);
		try{
			result=(String) privateMethod.invoke(this, a, search,search,true, -1);
			assertEquals("",result);
		}catch(InvocationTargetException e) {
			assertEquals(IllegalStateException.class,e.getTargetException().getClass());
		}
		
		try{
			result=(String)privateMethod.invoke(this, a, search,new String[] {"333","2332"},true, -1);
			assertEquals("",result);
		}catch(InvocationTargetException e) {
			assertEquals(IllegalStateException.class,e.getTargetException().getClass());
		}
		try{
			result=(String)privateMethod.invoke(this, a, new String[] {"23",null,"32"},new String[] {"333",null,"abc"},true, 2);
			assertEquals("133abc",result);
		}catch(InvocationTargetException e) {
			assertEquals(IllegalStateException.class,e.getTargetException().getClass());
		}

		
	}
	
	
	@Test
	public void testHexText() {
		byte[] data = StringUtils.fromHex("01 02 03 ab Ab AB", true);
		assertEquals(6, data.length);

		data = StringUtils.fromHex("01 02 03 ab Ab AB ", true);
		assertEquals(6, data.length);

		data = StringUtils.fromHex("010203abAbAB", false);
		assertEquals(6, data.length);
		
		data = StringUtils.fromHex("01 02 03 ab Ab AB".toCharArray(), true);
		assertEquals(6, data.length);

		data = StringUtils.fromHex("01 02 03 ab Ab AB ".toCharArray(), true);
		assertEquals(6, data.length);

		data = StringUtils.fromHex("010203abAbAB".toCharArray(), false);
		assertEquals(6, data.length);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void hexTextTestException() {
		byte[] data=StringUtils.fromHex("DE !C",true);
		assertEquals(6, data.length);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void hexTextTestException2() {
		byte[] data=StringUtils.fromHex("DE FG",true);
		assertEquals(6, data.length);
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void hexTextTestException3() {
		byte[] data=StringUtils.fromHex("DE !C".toCharArray(),true);
		assertEquals(6, data.length);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void hexTextTestException4() {
		byte[] data=StringUtils.fromHex("DE FG".toCharArray(),true);
		assertEquals(6, data.length);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testBase64Exception2() {
		JefBase64.encode(new byte[0],0,5,STANDARD);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testBase64Exception1() {
		JefBase64.decodeFirstByte("",3,JefBase64.STANDARD);
	}
	
	@Test
	public void testInnerArrayInputStream() {
		com.github.xuse.querydsl.util.StringUtils.ByteArrayInputStream in=
				new com.github.xuse.querydsl.util.StringUtils.ByteArrayInputStream(new byte[] {0x01,0x02});
		int b=in.read();
		assertEquals(1,b);
		b=in.read();
		b=in.read();
		assertEquals(-1,b);

		in.close();
		
		 in=new com.github.xuse.querydsl.util.StringUtils.ByteArrayInputStream(new byte[] {0x01,0x02});
		 byte[] data=new byte[100];
		 int len=in.read(data, 0,0);
		 assertEquals(0,len); 
		 len=in.read(data, 0,100);
		 assertEquals(2,len); 
	}
}
