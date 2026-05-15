package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.sql.column.DateGenerateType;

/**
 * Tests for DateGenerateType enum: field values and generateLong() behavior.
 */
class DateGenerateTypeTest {

	@Test
	void testCreatedIsNotModifyNotJavaTime() {
		assertFalse(DateGenerateType.created.isModify);
		assertFalse(DateGenerateType.created.isJavaTime);
		assertThrows(UnsupportedOperationException.class, () -> DateGenerateType.created.generateLong());
	}

	@Test
	void testModifiedIsModifyNotJavaTime() {
		assertTrue(DateGenerateType.modified.isModify);
		assertFalse(DateGenerateType.modified.isJavaTime);
		assertThrows(UnsupportedOperationException.class, () -> DateGenerateType.modified.generateLong());
	}

	@Test
	void testCreatedSysGeneratesMillis() {
		assertFalse(DateGenerateType.created_sys.isModify);
		assertTrue(DateGenerateType.created_sys.isJavaTime);
		long before = System.currentTimeMillis();
		long value = DateGenerateType.created_sys.generateLong();
		long after = System.currentTimeMillis();
		assertTrue(value >= before && value <= after);
	}

	@Test
	void testModifiedSysGeneratesMillis() {
		assertTrue(DateGenerateType.modified_sys.isModify);
		assertTrue(DateGenerateType.modified_sys.isJavaTime);
		long value = DateGenerateType.modified_sys.generateLong();
		assertTrue(value > 0);
	}

	@Test
	void testModifiedNanoGeneratesNano() {
		assertTrue(DateGenerateType.modified_nano.isModify);
		assertTrue(DateGenerateType.modified_nano.isJavaTime);
		long value = DateGenerateType.modified_nano.generateLong();
		assertTrue(value > 0);
	}

	@Test
	void testAllValuesExist() {
		assertEquals(5, DateGenerateType.values().length);
		assertNotNull(DateGenerateType.valueOf("created"));
		assertNotNull(DateGenerateType.valueOf("modified_nano"));
	}
}