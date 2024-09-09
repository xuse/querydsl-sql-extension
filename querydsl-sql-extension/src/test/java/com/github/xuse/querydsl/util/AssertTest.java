package com.github.xuse.querydsl.util;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.github.xuse.querydsl.enums.Gender;

public class AssertTest {

	private Object nullObj = null;
	
	private Object obj=new Object();
	
	private String msg = "assert failure";
	
	private String a="123";
	
	private String b="1".concat("23");
	
	private String c="456";
	
	private String[] sArray=new String[] {a};
	
	private int[] iArray= {1,2,3};
	
	private static File file = initFile(AssertTest.class, "test.ini");

	private static File nfile = initFile(AssertTest.class, "test1.ini");
	
	private List<String> hasEle = Arrays.asList("");
	private List<String> emptyEle = Collections.emptyList();
	
	private Map<String,String> singleMap = Collections.singletonMap("a", a);
	private Map<String,String> emptyMap = Collections.emptyMap();
	
	
	@Test
	public void testAssert() {
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertException1() {
		Assert.nonNull(obj, msg);
		Assert.nonNull(nullObj, msg);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertException2() {
		Assert.notNull(obj);
		Assert.notNull(nullObj);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertException3() {
		Assert.notNull(obj, " is null");
		Assert.notNull(nullObj, " is null");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAssertException4() {
		Assert.isNull(nullObj);
		Assert.isNull(obj);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertException5() {
		Assert.isNull(nullObj," is not null");
		Assert.isNull(obj, " is not null");
	}

	@Test(expected = ClassCastException.class)
	public void testAssertException6() {
		Assert.isType("", String.class);
		Assert.isType(obj, String.class);
	}

	@Test(expected = ClassCastException.class)
	public void testAssertException7() {
		Assert.isType("",String.class, msg);
		Assert.isType(obj,String.class, msg);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertException8() {
		Assert.isFalse(false);
		Assert.isFalse(true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertException9() {
		Assert.isFalse(false, msg);
		Assert.isFalse(true, msg);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertException10() {
		Assert.isTrue(true, "");
		Assert.isTrue(false, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertException11() {
		Assert.isTrue(true);
		Assert.isTrue(false);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAssertException12() {
		Assert.isTrue(Boolean.TRUE);
		Assert.isTrue(Boolean.FALSE);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAssertException13() {
		Assert.isTrue(Boolean.TRUE,msg);
		Assert.isTrue(Boolean.FALSE,msg);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAssertExceptionEQ1() {
		Assert.equals(a,b,msg);
		Assert.equals(a,c,msg);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAssertExceptionEQ2() {
		Assert.equals(a,b);
		Assert.equals(a,c);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAssertExceptionNE1() {
		Assert.notEquals(a,c);
		Assert.notEquals(a,b);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAssertExceptionNE2() {
		Assert.notEquals(a, c, msg);
		Assert.notEquals(a, b, msg);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAssertExceptionSame() {
		Assert.sameObject(a, b, msg);
		Assert.sameObject(a, b, msg);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAssertExceptionBetween() {
		Assert.between(2, 1, 3, msg);
		Assert.between(5, 1, 3, msg);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAssertExceptionBetween2() {
		Assert.isEnumOf("MALE", Gender.class, msg);
		Assert.isEnumOf("MALE1", Gender.class, msg);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAssertExceptionEnum1() {
		Assert.isEnumOf("MALE", Gender.class);
		Assert.isEnumOf("MALE1", Gender.class);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAssertExceptionEnum2() {
		Assert.isEnumOf("MALE", Gender.class, msg);
		Assert.isEnumOf("MALE1", Gender.class, msg);
	}	
	
	@Test(expected = NoSuchElementException.class)
	public void testAssertExceptionInArray() {
		Assert.isInArray(a, sArray);
		Assert.isInArray(c, sArray);
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testAssertExceptionInArray2() {
		Assert.isInArray(a, sArray, msg);
		Assert.isInArray(c, sArray, msg);
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testAssertExceptionInArray3() {
		Assert.isInArray(1, iArray, msg);
		Assert.isInArray(4, iArray, msg);
	}

	
	@Test(expected = IllegalArgumentException.class)
	public void fileExist() {
		Assert.exist(file);
		Assert.exist(nfile);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void fileExist2() {
		Assert.fileExist(file);
		Assert.fileExist(nfile);
	}

	@Test(expected = IllegalArgumentException.class)
	public void folderExist() {
		File folder=initFile(Assert.class,"com");
		Assert.folderExist(folder);
		Assert.folderExist(file);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void folderExist2() {
		Assert.folderExist(nfile);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionNotEmpty() {
		Assert.isNotEmpty(a);
		Assert.isNotEmpty("");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionNotEmpty2() {
		Assert.isNotEmpty(a,msg);
		Assert.isNotEmpty("",msg);
	}

	@Test(expected = IllegalArgumentException.class)
	public void exceptionNotEmpty3() {
		Assert.hasLength(a);
		Assert.hasLength("");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionNotEmpty4() {
		Assert.hasLength(a,msg);
		Assert.hasLength("",msg);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionHasElements1() {
		Assert.hasElements(hasEle);
		Assert.hasElements(emptyEle);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionHasElements2() {
		Assert.hasElements(hasEle, msg);
		Assert.hasElements(emptyEle ,msg);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionHasElements3() {
		Assert.hasElements(singleMap);
		Assert.hasElements(emptyMap);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionHasElements4() {
		Assert.hasElements(singleMap, msg);
		Assert.hasElements(emptyMap ,msg);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionArrayNotEmpty() {
		Assert.notEmpty(hasEle.toArray(),msg);
		Assert.notEmpty(emptyEle.toArray(),msg);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionArrayNotEmpty2() {
		Assert.notEmpty(hasEle.toArray());
		Assert.notEmpty(emptyEle.toArray());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionArrayNoNullEle1() {
		Assert.noNullElements(new Object[] {a,b});
		Assert.noNullElements(new Object[] {a,null});
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exceptionArrayNoNullEle2() {
		Assert.noNullElements(new Object[] {a,b},msg);
		Assert.noNullElements(new Object[] {a,null},msg);
	}
	
	
	private static File initFile(Class<?> clz,String string) {
		URL url=clz.getResource("/");
		File file=new File(url.getPath(),string);
		return file;
	}
}
