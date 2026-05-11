package com.github.xuse.querydsl.sql.dml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;

/**
 * Unit tests for parsing TemplateExpression (from @ColumnSpec(defaultValue="...") or defaultExpression(String)).
 * Tests the doParseValue() logic in DefaultValueHelper.
 */
public class TemplateExpressionParseTest {

	// ========== String Literals ==========

	@Test
	public void testTemplate_emptyString() {
		// @ColumnSpec(defaultValue="''")
		Expression<?> expr = Expressions.template(Object.class, "''");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, String.class, "testField");
		
		assertEquals("", result);
	}

	@Test
	public void testTemplate_quotedString() {
		// @ColumnSpec(defaultValue="'hello'")
		Expression<?> expr = Expressions.template(Object.class, "'hello'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, String.class, "testField");
		
		assertEquals("hello", result);
	}

	@Test
	public void testTemplate_quotedStringWithEscapedQuote() {
		// @ColumnSpec(defaultValue="'it''s'") → SQL: 'it''s' → Java: "it's"
		Expression<?> expr = Expressions.template(Object.class, "'it''s'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, String.class, "testField");
		
		assertEquals("it's", result);
	}

	@Test
	public void testTemplate_unquotedString() {
		// @ColumnSpec(defaultValue="hello") — rare but possible
		Expression<?> expr = Expressions.template(Object.class, "hello");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, String.class, "testField");
		
		assertEquals("hello", result);
	}

	// ========== Numeric Literals ==========

	@Test
	public void testTemplate_integerZero() {
		// @ColumnSpec(defaultValue="0")
		Expression<?> expr = Expressions.template(Object.class, "0");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Integer.class, "testField");
		
		assertEquals(0, result);
		assertTrue(result instanceof Integer);
	}

	@Test
	public void testTemplate_integerPositive() {
		// @ColumnSpec(defaultValue="42")
		Expression<?> expr = Expressions.template(Object.class, "42");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Integer.class, "testField");
		
		assertEquals(42, result);
		assertTrue(result instanceof Integer);
	}

	@Test
	public void testTemplate_integerNegative() {
		// @ColumnSpec(defaultValue="-1")
		Expression<?> expr = Expressions.template(Object.class, "-1");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Integer.class, "testField");
		
		assertEquals(-1, result);
		assertTrue(result instanceof Integer);
	}

	@Test
	public void testTemplate_longValue() {
		// @ColumnSpec(defaultValue="9999999999")
		Expression<?> expr = Expressions.template(Object.class, "9999999999");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Long.class, "testField");
		
		assertEquals(9999999999L, result);
		assertTrue(result instanceof Long);
	}

	@Test
	public void testTemplate_shortValue() {
		// @ColumnSpec(defaultValue="100")
		Expression<?> expr = Expressions.template(Object.class, "100");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Short.class, "testField");
		
		assertEquals((short) 100, result);
		assertTrue(result instanceof Short);
	}

	@Test
	public void testTemplate_byteValue() {
		// @ColumnSpec(defaultValue="10")
		Expression<?> expr = Expressions.template(Object.class, "10");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Byte.class, "testField");
		
		assertEquals((byte) 10, result);
		assertTrue(result instanceof Byte);
	}

	@Test
	public void testTemplate_floatValue() {
		// @ColumnSpec(defaultValue="3.14")
		Expression<?> expr = Expressions.template(Object.class, "3.14");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Float.class, "testField");
		
		assertEquals(3.14f, (Float) result, 0.001f);
		assertTrue(result instanceof Float);
	}

	@Test
	public void testTemplate_doubleValue() {
		// @ColumnSpec(defaultValue="3.14159")
		Expression<?> expr = Expressions.template(Object.class, "3.14159");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Double.class, "testField");
		
		assertEquals(3.14159, (Double) result, 0.00001);
		assertTrue(result instanceof Double);
	}

	@Test
	public void testTemplate_bigDecimalValue() {
		// @ColumnSpec(defaultValue="123.456")
		Expression<?> expr = Expressions.template(Object.class, "123.456");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, BigDecimal.class, "testField");
		
		assertEquals(new BigDecimal("123.456"), result);
		assertTrue(result instanceof BigDecimal);
	}

	@Test
	public void testTemplate_bigIntegerValue() {
		// @ColumnSpec(defaultValue="123456789012345")
		Expression<?> expr = Expressions.template(Object.class, "123456789012345");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, BigInteger.class, "testField");
		
		assertEquals(new BigInteger("123456789012345"), result);
		assertTrue(result instanceof BigInteger);
	}

	// ========== Boolean Literals ==========

	@Test
	public void testTemplate_booleanTrue() {
		// @ColumnSpec(defaultValue="true")
		Expression<?> expr = Expressions.template(Object.class, "true");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Boolean.class, "testField");
		
		assertEquals(Boolean.TRUE, result);
	}

	@Test
	public void testTemplate_booleanFalse() {
		// @ColumnSpec(defaultValue="false")
		Expression<?> expr = Expressions.template(Object.class, "false");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Boolean.class, "testField");
		
		assertEquals(Boolean.FALSE, result);
	}

	@Test
	public void testTemplate_booleanOne() {
		// @ColumnSpec(defaultValue="1") on Boolean field
		Expression<?> expr = Expressions.template(Object.class, "1");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Boolean.class, "testField");
		
		assertEquals(Boolean.TRUE, result);
	}

	@Test
	public void testTemplate_booleanZero() {
		// @ColumnSpec(defaultValue="0") on Boolean field
		Expression<?> expr = Expressions.template(Object.class, "0");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Boolean.class, "testField");
		
		assertEquals(Boolean.FALSE, result);
	}

	// ========== Date/Time Types - Quoted Strings ==========

	@Test
	public void testTemplate_localDate_quoted() {
		// @ColumnSpec(defaultValue="'2023-01-01'")
		Expression<?> expr = Expressions.template(Object.class, "'2023-01-01'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, LocalDate.class, "testField");
		
		assertNotNull(result, "Should parse LocalDate from quoted string");
		assertEquals(LocalDate.of(2023, 1, 1), result);
	}

	@Test
	public void testTemplate_localDate_unquoted() {
		// @ColumnSpec(defaultValue="2023-01-01")
		Expression<?> expr = Expressions.template(Object.class, "2023-01-01");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, LocalDate.class, "testField");
		
		assertNotNull(result, "Should parse LocalDate from unquoted string");
		assertEquals(LocalDate.of(2023, 1, 1), result);
	}

	@Test
	public void testTemplate_localTime_quoted() {
		// @ColumnSpec(defaultValue="'12:30:45'")
		Expression<?> expr = Expressions.template(Object.class, "'12:30:45'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, LocalTime.class, "testField");
		
		assertNotNull(result, "Should parse LocalTime from quoted string");
		assertEquals(LocalTime.of(12, 30, 45), result);
	}

	@Test
	public void testTemplate_localTime_unquoted() {
		// @ColumnSpec(defaultValue="12:30:45")
		Expression<?> expr = Expressions.template(Object.class, "12:30:45");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, LocalTime.class, "testField");
		
		assertNotNull(result, "Should parse LocalTime from unquoted string");
		assertEquals(LocalTime.of(12, 30, 45), result);
	}

	@Test
	public void testTemplate_localDateTime_quoted_isoFormat() {
		// @ColumnSpec(defaultValue="'2023-01-01T12:30:45'")
		Expression<?> expr = Expressions.template(Object.class, "'2023-01-01T12:30:45'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, LocalDateTime.class, "testField");
		
		assertNotNull(result, "Should parse LocalDateTime from ISO format");
		assertEquals(LocalDateTime.of(2023, 1, 1, 12, 30, 45), result);
	}

	@Test
	public void testTemplate_localDateTime_quoted_sqlFormat() {
		// @ColumnSpec(defaultValue="'2023-01-01 12:30:45'") — space separator
		Expression<?> expr = Expressions.template(Object.class, "'2023-01-01 12:30:45'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, LocalDateTime.class, "testField");
		
		assertNotNull(result, "Should parse LocalDateTime from SQL format with space");
		assertEquals(LocalDateTime.of(2023, 1, 1, 12, 30, 45), result);
	}

	@Test
	public void testTemplate_localDateTime_unquoted() {
		// @ColumnSpec(defaultValue="2023-01-01T12:30:45")
		Expression<?> expr = Expressions.template(Object.class, "2023-01-01T12:30:45");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, LocalDateTime.class, "testField");
		
		assertNotNull(result, "Should parse LocalDateTime from unquoted string");
		assertEquals(LocalDateTime.of(2023, 1, 1, 12, 30, 45), result);
	}

	@Test
	public void testTemplate_instant_quoted_isoFormat() {
		// @ColumnSpec(defaultValue="'2023-01-01T12:30:45Z'")
		Expression<?> expr = Expressions.template(Object.class, "'2023-01-01T12:30:45Z'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Instant.class, "testField");
		
		assertNotNull(result, "Should parse Instant from ISO format");
		assertEquals(Instant.parse("2023-01-01T12:30:45Z"), result);
	}

	@Test
	public void testTemplate_instant_quoted_sqlTimestamp() {
		// @ColumnSpec(defaultValue="'2023-01-01 12:30:45'") — fallback to Timestamp.valueOf
		Expression<?> expr = Expressions.template(Object.class, "'2023-01-01 12:30:45'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Instant.class, "testField");
		
		assertNotNull(result, "Should parse Instant from SQL timestamp format");
		assertTrue(result instanceof Instant);
		// Verify it's a valid Instant (exact value depends on system timezone)
		assertTrue(((Instant) result).getEpochSecond() > 0);
	}

	@Test
	public void testTemplate_offsetDateTime_quoted() {
		// @ColumnSpec(defaultValue="'2023-01-01T12:30:45+08:00'")
		Expression<?> expr = Expressions.template(Object.class, "'2023-01-01T12:30:45+08:00'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, OffsetDateTime.class, "testField");
		
		assertNotNull(result, "Should parse OffsetDateTime from ISO format");
		assertEquals(OffsetDateTime.parse("2023-01-01T12:30:45+08:00"), result);
	}

	@Test
	public void testTemplate_zonedDateTime_quoted() {
		// @ColumnSpec(defaultValue="'2023-01-01T12:30:45+08:00[Asia/Shanghai]'")
		Expression<?> expr = Expressions.template(Object.class, "'2023-01-01T12:30:45+08:00[Asia/Shanghai]'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, ZonedDateTime.class, "testField");
		
		assertNotNull(result, "Should parse ZonedDateTime from ISO format");
		assertEquals(ZonedDateTime.parse("2023-01-01T12:30:45+08:00[Asia/Shanghai]"), result);
	}

	@Test
	public void testTemplate_sqlDate_quoted() {
		// @ColumnSpec(defaultValue="'2023-01-01'")
		Expression<?> expr = Expressions.template(Object.class, "'2023-01-01'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, java.sql.Date.class, "testField");
		
		assertNotNull(result, "Should parse java.sql.Date from quoted string");
		assertEquals(java.sql.Date.valueOf("2023-01-01"), result);
	}

	@Test
	public void testTemplate_sqlTime_quoted() {
		// @ColumnSpec(defaultValue="'12:30:45'")
		Expression<?> expr = Expressions.template(Object.class, "'12:30:45'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, java.sql.Time.class, "testField");
		
		assertNotNull(result, "Should parse java.sql.Time from quoted string");
		assertEquals(java.sql.Time.valueOf("12:30:45"), result);
	}

	@Test
	public void testTemplate_sqlTimestamp_quoted() {
		// @ColumnSpec(defaultValue="'2023-01-01 12:30:45'")
		Expression<?> expr = Expressions.template(Object.class, "'2023-01-01 12:30:45'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, java.sql.Timestamp.class, "testField");
		
		assertNotNull(result, "Should parse java.sql.Timestamp from quoted string");
		assertEquals(java.sql.Timestamp.valueOf("2023-01-01 12:30:45"), result);
	}

	@Test
	public void testTemplate_utilDate_quoted_timestamp() {
		// @ColumnSpec(defaultValue="'2023-01-01 12:30:45'")
		Expression<?> expr = Expressions.template(Object.class, "'2023-01-01 12:30:45'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, java.util.Date.class, "testField");
		
		assertNotNull(result, "Should parse java.util.Date from timestamp string");
		assertEquals(new java.util.Date(java.sql.Timestamp.valueOf("2023-01-01 12:30:45").getTime()), result);
	}

	@Test
	public void testTemplate_utilDate_quoted_date() {
		// @ColumnSpec(defaultValue="'2023-01-01'") — fallback to Date.valueOf
		Expression<?> expr = Expressions.template(Object.class, "'2023-01-01'");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, java.util.Date.class, "testField");
		
		assertNotNull(result, "Should parse java.util.Date from date string");
		assertEquals(new java.util.Date(java.sql.Date.valueOf("2023-01-01").getTime()), result);
	}

	// ========== SQL Functions (should be skipped) ==========

	@Test
	public void testTemplate_currentTimestamp() {
		// @ColumnSpec(defaultValue="CURRENT_TIMESTAMP")
		Expression<?> expr = Expressions.template(Object.class, "CURRENT_TIMESTAMP");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, java.sql.Timestamp.class, "testField");
		
		assertNull(result, "CURRENT_TIMESTAMP should not be parsed");
	}

	@Test
	public void testTemplate_now() {
		// @ColumnSpec(defaultValue="NOW()")
		Expression<?> expr = Expressions.template(Object.class, "NOW()");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, java.sql.Timestamp.class, "testField");
		
		assertNull(result, "NOW() should not be parsed");
	}

	@Test
	public void testTemplate_uuid() {
		// @ColumnSpec(defaultValue="UUID()")
		Expression<?> expr = Expressions.template(Object.class, "UUID()");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, String.class, "testField");
		
		assertNull(result, "UUID() should not be parsed");
	}

	@Test
	public void testTemplate_functionCall() {
		// @ColumnSpec(defaultValue="SOME_FUNCTION()")
		Expression<?> expr = Expressions.template(Object.class, "SOME_FUNCTION()");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, String.class, "testField");
		
		assertNull(result, "Function calls should not be parsed");
	}

	// ========== Numeric Fallback for Unknown Types ==========

	@Test
	public void testTemplate_numericFallback_integer() {
		// Unquoted numeric value on unknown type → fallback to Integer/Long
		Expression<?> expr = Expressions.template(Object.class, "42");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Object.class, "testField");
		
		assertNotNull(result, "Should fallback to numeric parsing");
		// Fallback returns Integer for values in Integer range
		assertTrue(result instanceof Number, "Should be a Number");
		assertEquals(42, ((Number) result).intValue());
	}

	@Test
	public void testTemplate_numericFallback_double() {
		// Unquoted decimal value on unknown type → fallback to Double
		Expression<?> expr = Expressions.template(Object.class, "3.14");
		Object result = DefaultValueHelper.parseDefaultToJavaValue(expr, 0, Object.class, "testField");
		
		assertNotNull(result, "Should fallback to numeric parsing");
		assertEquals(3.14, result);
		assertTrue(result instanceof Double);
	}
}
