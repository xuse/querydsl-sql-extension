package com.github.xuse.querydsl.util;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.enums.Gender;

public class AssertTest {

	private Object nullObj = null;

	private Object obj = new Object();

	private String msg = "assert failure";

	private String a = "123";

	private String b = "1".concat("23");

	private String c = "456";

	private String[] sArray = new String[] { a };

	private int[] iArray = { 1, 2, 3 };

	private static File file = initFile(AssertTest.class, "test.ini");

	private static File nfile = initFile(AssertTest.class, "test1.ini");

	private List<String> hasEle = Arrays.asList("");
	private List<String> emptyEle = Collections.emptyList();

	private Map<String, String> singleMap = Collections.singletonMap("a", a);
	private Map<String, String> emptyMap = Collections.emptyMap();

	@Test
	public void testAssert() {
	}

	@Test
	public void testAssertException1() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.nonNull(obj, msg);
			Assert.nonNull(nullObj, msg);
		});
	}

	@Test
	public void testAssertException2() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.notNull(obj);
			Assert.notNull(nullObj);
		});
	}

	@Test
	public void testAssertException3() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.notNull(obj, " is null");
			Assert.notNull(nullObj, " is null");
		});
	}

	@Test
	public void testAssertException4() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.isNull(nullObj);
			Assert.isNull(obj);
		});
	}

	@Test
	public void testAssertException5() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.isNull(nullObj, " is not null");
			Assert.isNull(obj, " is not null");
		});
	}

	@Test
	public void testAssertException6() {
		Assertions.assertThrows(ClassCastException.class, () -> {
			Assert.isType("", String.class);
			Assert.isType(obj, String.class);
		});
	}

	@Test
	public void testAssertException7() {
		Assertions.assertThrows(ClassCastException.class, () -> {
			Assert.isType("", String.class, msg);
			Assert.isType(obj, String.class, msg);
		});
	}

	@Test
	public void testAssertException8() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.isFalse(false);
			Assert.isFalse(true);
		});
	}

	@Test
	public void testAssertException9() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.isFalse(false, msg);
			Assert.isFalse(true, msg);
		});
	}

	@Test
	public void testAssertException10() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.isTrue(true, "");
			Assert.isTrue(false, "");
		});
	}

	@Test
	public void testAssertException11() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.isTrue(true);
			Assert.isTrue(false);
		});
	}

	@Test
	public void testAssertException12() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.isTrue(Boolean.TRUE);
			Assert.isTrue(Boolean.FALSE);
		});
	}

	@Test
	public void testAssertException13() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.isTrue(Boolean.TRUE, msg);
			Assert.isTrue(Boolean.FALSE, msg);
		});
	}

	@Test
	public void testAssertExceptionEQ1() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.equals(a, b, msg);
			Assert.equals(a, c, msg);
		});
	}

	@Test
	public void testAssertExceptionEQ2() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.equals(a, b);
			Assert.equals(a, c);
		});
	}

	@Test
	public void testAssertExceptionNE1() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.notEquals(a, c);
			Assert.notEquals(a, b);
		});
	}

	@Test
	public void testAssertExceptionNE2() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.notEquals(a, c, msg);
			Assert.notEquals(a, b, msg);
		});
	}

	@Test
	public void testAssertExceptionSame() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.sameObject(a, b, msg);
			Assert.sameObject(a, b, msg);
		});
	}

	@Test
	public void testAssertExceptionBetween() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.between(2, 1, 3, msg);
			Assert.between(5, 1, 3, msg);
		});
	}

	@Test
	public void testAssertExceptionBetween2() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.isEnumOf("MALE", Gender.class, msg);
			Assert.isEnumOf("MALE1", Gender.class, msg);
		});
	}

	@Test
	public void testAssertExceptionEnum1() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.isEnumOf("MALE", Gender.class);
			Assert.isEnumOf("MALE1", Gender.class);
		});
	}

	@Test
	public void testAssertExceptionEnum2() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.isEnumOf("MALE", Gender.class, msg);
			Assert.isEnumOf("MALE1", Gender.class, msg);
		});
	}

	@Test
	public void testAssertExceptionInArray() {
		Assertions.assertThrows(NoSuchElementException.class, () -> {
			Assert.isInArray(a, sArray);
			Assert.isInArray(c, sArray);
		});
	}

	@Test
	public void testAssertExceptionInArray2() {
		Assertions.assertThrows(NoSuchElementException.class, () -> {
			Assert.isInArray(a, sArray, msg);
			Assert.isInArray(c, sArray, msg);
		});
	}

	@Test
	public void testAssertExceptionInArray3() {
		Assertions.assertThrows(NoSuchElementException.class, () -> {
			Assert.isInArray(1, iArray, msg);
			Assert.isInArray(4, iArray, msg);
		});
	}

	@Test
	public void fileExist() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.exist(file);
			Assert.exist(nfile);
		});
	}

	@Test
	public void fileExist2() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.fileExist(file);
			Assert.fileExist(nfile);
		});
	}

	@Test
	public void folderExist() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			File folder = initFile(Assert.class, "com");
			Assert.folderExist(folder);
			Assert.folderExist(file);
		});
	}

	@Test
	public void folderExist2() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.folderExist(nfile);
		});
	}

	@Test
	public void exceptionNotEmpty() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.isNotEmpty(a);
			Assert.isNotEmpty("");
		});
	}

	@Test
	public void exceptionNotEmpty2() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.isNotEmpty(a, msg);
			Assert.isNotEmpty("", msg);
		});
	}

	@Test
	public void exceptionNotEmpty3() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.hasLength(a);
			Assert.hasLength("");
		});
	}

	@Test
	public void exceptionNotEmpty4() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.hasLength(a, msg);
			Assert.hasLength("", msg);
		});
	}

	@Test
	public void exceptionHasElements1() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.hasElements(hasEle);
			Assert.hasElements(emptyEle);
		});
	}

	@Test
	public void exceptionHasElements2() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.hasElements(hasEle, msg);
			Assert.hasElements(emptyEle, msg);
		});
	}

	@Test
	public void exceptionHasElements3() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.hasElements(singleMap);
			Assert.hasElements(emptyMap);
		});
	}

	@Test
	public void exceptionHasElements4() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.hasElements(singleMap, msg);
			Assert.hasElements(emptyMap, msg);
		});
	}

	@Test
	public void exceptionArrayNotEmpty() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.notEmpty(hasEle.toArray(), msg);
			Assert.notEmpty(emptyEle.toArray(), msg);
		});
	}

	@Test
	public void exceptionArrayNotEmpty2() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.notEmpty(hasEle.toArray());
			Assert.notEmpty(emptyEle.toArray());
		});
	}

	@Test
	public void exceptionArrayNoNullEle1() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.noNullElements(new Object[] { a, b });
			Assert.noNullElements(new Object[] { a, null });
		});
	}

	@Test
	public void exceptionArrayNoNullEle2() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Assert.noNullElements(new Object[] { a, b }, msg);
			Assert.noNullElements(new Object[] { a, null }, msg);
		});
	}

	private static File initFile(Class<?> clz, String string) {
		URL url = clz.getResource("/");
		File file = new File(url.getPath(), string);
		return file;
	}
}
