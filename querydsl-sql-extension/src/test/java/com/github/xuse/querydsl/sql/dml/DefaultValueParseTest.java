package com.github.xuse.querydsl.sql.dml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;

/**
 * Unit tests for parsing Constant expressions (from defaultValue(), defaultValueInString()).
 * Tests the convertConstant() logic in DefaultValueHelper.
 */
public class DefaultValueParseTest {

	// ========== Numeric Types ==========

	@Test
	public void testConstant_integerToInteger() {
		// defaultValue(42)
		Expression<?> expr = ConstantImpl.create(42);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Integer.class, "testField");
		
		assertEquals(42, result);
		assertTrue(result instanceof Integer);
	}

	@Test
	public void testConstant_integerToLong() {
		// defaultValue(42) but field is Long
		Expression<?> expr = ConstantImpl.create(42);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Long.class, "testField");
		
		assertEquals(42L, result);
		assertTrue(result instanceof Long);
	}

	@Test
	public void testConstant_integerToShort() {
		// defaultValue(42) but field is Short
		Expression<?> expr = ConstantImpl.create(42);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Short.class, "testField");
		
		assertEquals((short) 42, result);
		assertTrue(result instanceof Short);
	}

	@Test
	public void testConstant_integerToByte() {
		// defaultValue(42) but field is Byte
		Expression<?> expr = ConstantImpl.create(42);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Byte.class, "testField");
		
		assertEquals((byte) 42, result);
		assertTrue(result instanceof Byte);
	}

	@Test
	public void testConstant_integerToFloat() {
		// defaultValue(42) but field is Float
		Expression<?> expr = ConstantImpl.create(42);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Float.class, "testField");
		
		assertEquals(42.0f, result);
		assertTrue(result instanceof Float);
	}

	@Test
	public void testConstant_integerToDouble() {
		// defaultValue(42) but field is Double
		Expression<?> expr = ConstantImpl.create(42);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Double.class, "testField");
		
		assertEquals(42.0, result);
		assertTrue(result instanceof Double);
	}

	@Test
	public void testConstant_integerToBigDecimal() {
		// defaultValue(42) but field is BigDecimal
		Expression<?> expr = ConstantImpl.create(42);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, BigDecimal.class, "testField");
		
		assertEquals(new BigDecimal("42"), result);
		assertTrue(result instanceof BigDecimal);
	}

	@Test
	public void testConstant_integerToBigInteger() {
		// defaultValue(42) but field is BigInteger
		Expression<?> expr = ConstantImpl.create(42);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, BigInteger.class, "testField");
		
		assertEquals(BigInteger.valueOf(42), result);
		assertTrue(result instanceof BigInteger);
	}

	@Test
	public void testConstant_doubleToDouble() {
		// defaultValue(3.14)
		Expression<?> expr = ConstantImpl.create(3.14);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Double.class, "testField");
		
		assertEquals(3.14, result);
		assertTrue(result instanceof Double);
	}

	@Test
	public void testConstant_doubleToFloat() {
		// defaultValue(3.14) but field is Float
		Expression<?> expr = ConstantImpl.create(3.14);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Float.class, "testField");
		
		assertEquals(3.14f, (Float) result, 0.001f);
		assertTrue(result instanceof Float);
	}

	@Test
	public void testConstant_doubleToInteger() {
		// defaultValue(3.14) but field is Integer (truncates)
		Expression<?> expr = ConstantImpl.create(3.14);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Integer.class, "testField");
		
		assertEquals(3, result);
		assertTrue(result instanceof Integer);
	}

	// ========== Boolean Types ==========

	@Test
	public void testConstant_booleanTrue() {
		// defaultValue(true)
		Expression<?> expr = ConstantImpl.create(true);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Boolean.class, "testField");
		
		assertEquals(Boolean.TRUE, result);
	}

	@Test
	public void testConstant_booleanFalse() {
		// defaultValue(false)
		Expression<?> expr = ConstantImpl.create(false);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Boolean.class, "testField");
		
		assertEquals(Boolean.FALSE, result);
	}

	@Test
	public void testConstant_booleanToInteger() {
		// defaultValue(true) but field is Integer → 1
		Expression<?> expr = ConstantImpl.create(true);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Integer.class, "testField");
		
		assertEquals(1, result);
	}

	@Test
	public void testConstant_booleanFalseToInteger() {
		// defaultValue(false) but field is Integer → 0
		Expression<?> expr = ConstantImpl.create(false);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Integer.class, "testField");
		
		assertEquals(0, result);
	}

	// ========== String Types ==========

	@Test
	public void testConstant_stringToString() {
		// defaultValue("hello")
		Expression<?> expr = ConstantImpl.create("hello");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, String.class, "testField");
		
		assertEquals("hello", result);
	}

	@Test
	public void testConstant_emptyStringToString() {
		// defaultValue("")
		Expression<?> expr = ConstantImpl.create("");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, String.class, "testField");
		
		assertEquals("", result);
	}

	@Test
	public void testConstant_spaceStringToString() {
		// defaultValue(" ")
		Expression<?> expr = ConstantImpl.create(" ");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, String.class, "testField");
		
		assertEquals(" ", result);
	}

	// ========== Date/Time Types from String Constants ==========

	@Test
	public void testConstant_stringToLocalDate() {
		// defaultValueInString("2023-01-01") on LocalDate field
		Expression<?> expr = ConstantImpl.create("2023-01-01");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, LocalDate.class, "testField");
		
		assertNotNull(result, "Should parse LocalDate from string constant");
		assertEquals(LocalDate.of(2023, 1, 1), result);
	}

	@Test
	public void testConstant_stringToLocalTime() {
		// defaultValueInString("12:30:45") on LocalTime field
		Expression<?> expr = ConstantImpl.create("12:30:45");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, LocalTime.class, "testField");
		
		assertNotNull(result, "Should parse LocalTime from string constant");
		assertEquals(LocalTime.of(12, 30, 45), result);
	}

	@Test
	public void testConstant_stringToLocalDateTime() {
		// defaultValueInString("2023-01-01T12:30:45") on LocalDateTime field
		Expression<?> expr = ConstantImpl.create("2023-01-01T12:30:45");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, LocalDateTime.class, "testField");
		
		assertNotNull(result, "Should parse LocalDateTime from string constant");
		assertEquals(LocalDateTime.of(2023, 1, 1, 12, 30, 45), result);
	}

	@Test
	public void testConstant_stringToSqlDate() {
		// defaultValueInString("2023-01-01") on java.sql.Date field
		Expression<?> expr = ConstantImpl.create("2023-01-01");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, java.sql.Date.class, "testField");
		
		assertNotNull(result, "Should parse java.sql.Date from string constant");
		assertEquals(java.sql.Date.valueOf("2023-01-01"), result);
	}

	@Test
	public void testConstant_stringToSqlTime() {
		// defaultValueInString("12:30:45") on java.sql.Time field
		Expression<?> expr = ConstantImpl.create("12:30:45");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, java.sql.Time.class, "testField");
		
		assertNotNull(result, "Should parse java.sql.Time from string constant");
		assertEquals(java.sql.Time.valueOf("12:30:45"), result);
	}

	@Test
	public void testConstant_stringToSqlTimestamp() {
		// defaultValueInString("2023-01-01 12:30:45") on java.sql.Timestamp field
		Expression<?> expr = ConstantImpl.create("2023-01-01 12:30:45");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, java.sql.Timestamp.class, "testField");
		
		assertNotNull(result, "Should parse java.sql.Timestamp from string constant");
		assertEquals(java.sql.Timestamp.valueOf("2023-01-01 12:30:45"), result);
	}

	@Test
	public void testConstant_stringToUtilDate() {
		// defaultValueInString("2023-01-01 12:30:45") on java.util.Date field
		Expression<?> expr = ConstantImpl.create("2023-01-01 12:30:45");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, java.util.Date.class, "testField");
		
		assertNotNull(result, "Should parse java.util.Date from string constant");
		assertEquals(new java.util.Date(java.sql.Timestamp.valueOf("2023-01-01 12:30:45").getTime()), result);
	}

	// ========== Instant from Number ==========

	@Test
	public void testConstant_longToInstant() {
		// defaultValue(1672574400000L) on Instant field (epoch milliseconds)
		Expression<?> expr = ConstantImpl.create(1672574400000L);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Instant.class, "testField");
		
		assertNotNull(result, "Should convert epoch millis to Instant");
		assertEquals(Instant.ofEpochMilli(1672574400000L), result);
	}

	// ========== Edge Cases ==========

	@Test
	public void testConstant_sameTypeNoConversion() {
		// defaultValue(42) on Integer field — no conversion needed
		Expression<?> expr = ConstantImpl.create(42);
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Integer.class, "testField");
		
		assertEquals(42, result);
		assertTrue(result instanceof Integer);
	}
}
