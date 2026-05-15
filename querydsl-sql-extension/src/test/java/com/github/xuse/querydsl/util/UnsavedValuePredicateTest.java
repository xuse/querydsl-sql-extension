package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.sql.column.UnsavedValuePredicateFactory;

/**
 * Tests for UnsavedValuePredicateFactory: all predicate types and parseValue branches.
 */
class UnsavedValuePredicateTest {

	@Test
	void testNullPredicate() {
		Predicate<Object> p = UnsavedValuePredicateFactory.Null;
		assertTrue(p.test(null));
		assertFalse(p.test("abc"));
		assertFalse(p.test(0));
	}

	@Test
	void testNullOrEmptyPredicate() {
		Predicate<Object> p = UnsavedValuePredicateFactory.NullOrEmpty;
		assertTrue(p.test(null));
		assertTrue(p.test(""));
		assertFalse(p.test("abc"));
		assertFalse(p.test(0));
	}

	@Test
	void testMinusNumberPredicate() {
		Predicate<Object> p = UnsavedValuePredicateFactory.MinusNumber;
		assertTrue(p.test(null));
		assertTrue(p.test(-1));
		assertTrue(p.test(-100L));
		assertFalse(p.test(0));
		assertFalse(p.test(1));
		assertFalse(p.test("abc"));
	}

	@Test
	void testZeroAndMinusPredicate() {
		Predicate<Object> p = UnsavedValuePredicateFactory.ZeroAndMinus;
		assertTrue(p.test(null));
		assertTrue(p.test(0));
		assertTrue(p.test(-1));
		assertFalse(p.test(1));
		assertFalse(p.test("abc"));
	}

	@Test
	void testZeroNumberPredicate() {
		Predicate<Object> p = UnsavedValuePredicateFactory.ZeroNumber;
		assertTrue(p.test(null));
		assertTrue(p.test(0));
		assertTrue(p.test(0L));
		assertFalse(p.test(1));
		assertFalse(p.test(-1));
		assertFalse(p.test("abc"));
	}

	// ===== parseValue with UnsavedValue constants =====

	@Test
	void testParseValueNull() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(String.class, UnsavedValue.Null);
		assertTrue(p.test(null));
		assertFalse(p.test("abc"));
	}

	@Test
	void testParseValueZero() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(int.class, UnsavedValue.Zero);
		assertTrue(p.test(null));
		assertTrue(p.test(0));
		assertFalse(p.test(1));
	}

	@Test
	void testParseValueMinusNumber() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(int.class, UnsavedValue.MinusNumber);
		assertTrue(p.test(-1));
		assertFalse(p.test(0));
	}

	@Test
	void testParseValueZeroAndMinus() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(int.class, UnsavedValue.ZeroAndMinus);
		assertTrue(p.test(0));
		assertTrue(p.test(-1));
		assertFalse(p.test(1));
	}

	@Test
	void testParseValueNever() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(int.class, UnsavedValue.Never);
		assertFalse(p.test(null));
		assertFalse(p.test(0));
	}

	@Test
	void testParseValueNullOrEmptyForPrimitive() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(int.class, UnsavedValue.NullOrEmpty);
		// For primitive, NullOrEmpty falls back to Null
		assertTrue(p.test(null));
		assertFalse(p.test(0));
	}

	@Test
	void testParseValueNullOrEmptyForString() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(String.class, UnsavedValue.NullOrEmpty);
		assertTrue(p.test(null));
		assertTrue(p.test(""));
		assertFalse(p.test("abc"));
	}

	// ===== parseValue with custom literal values =====

	@Test
	void testParseValueCustomInt() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(int.class, "42");
		assertTrue(p.test(42));
		assertFalse(p.test(0));
	}

	@Test
	void testParseValueCustomLong() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(long.class, "100");
		assertTrue(p.test(100L));
		assertFalse(p.test(0L));
	}

	@Test
	void testParseValueCustomBoolean() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(boolean.class, "true");
		assertTrue(p.test(true));
		assertFalse(p.test(false));
	}

	@Test
	void testParseValueCustomFloat() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(float.class, "0");
		assertTrue(p.test(0f));
	}

	@Test
	void testParseValueCustomDouble() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(double.class, "0");
		assertTrue(p.test(0d));
	}

	@Test
	void testParseValueCustomChar() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(char.class, "A");
		assertTrue(p.test('A'));
		assertFalse(p.test('B'));
	}

	@Test
	void testParseValueCustomCharEmpty() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(char.class, "");
		assertTrue(p.test((char) 0));
	}

	@Test
	void testParseValueCustomByte() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(byte.class, "1");
		assertTrue(p.test((byte) 1));
	}

	@Test
	void testParseValueCustomShort() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(short.class, "5");
		assertTrue(p.test(5));
	}

	@Test
	void testParseValueCustomString() {
		Predicate<Object> p = UnsavedValuePredicateFactory.parseValue(String.class, "DEFAULT");
		assertTrue(p.test("DEFAULT"));
		assertFalse(p.test("other"));
	}

	// ===== create() =====

	@Test
	void testCreateWithAnnotationValue() {
		Predicate<Object> p = UnsavedValuePredicateFactory.create(int.class, UnsavedValue.Zero);
		assertTrue(p.test(0));
	}

	@Test
	void testCreateWithoutAnnotationForPrimitive() {
		Predicate<Object> p = UnsavedValuePredicateFactory.create(int.class, null);
		// Default for primitive: matches the default value (0 for int)
		assertTrue(p.test(0));
		assertFalse(p.test(1));
	}

	@Test
	void testCreateWithoutAnnotationForObject() {
		Predicate<Object> p = UnsavedValuePredicateFactory.create(String.class, null);
		assertTrue(p.test(null));
		assertFalse(p.test("abc"));
	}

	@Test
	void testCreateWithEmptyAnnotation() {
		Predicate<Object> p = UnsavedValuePredicateFactory.create(String.class, "");
		assertTrue(p.test(null));
		assertFalse(p.test("abc"));
	}
}