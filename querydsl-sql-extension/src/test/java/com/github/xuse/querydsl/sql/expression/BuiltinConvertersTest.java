package com.github.xuse.querydsl.sql.expression;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BuiltinConverters}.
 * <p>
 * Verifies that all registered type pairs return non-null converters from {@code find()}
 * and that those converters convert sample values without throwing exceptions.
 */
@DisplayName("BuiltinConverters - type conversion registry tests")
class BuiltinConvertersTest {

	@Nested
	@DisplayName("String ↔ numeric conversions")
	class StringNumericConversions {

		@Test
		@DisplayName("String -> int converter exists and converts correctly")
		void testStringToInt() {
			Function converter = BuiltinConverters.find(String.class, int.class);
			assertNotNull(converter, "String -> int converter should be registered");
			assertEquals(42, converter.apply("42"));
		}

		@Test
		@DisplayName("String -> Integer converter exists and converts correctly")
		void testStringToInteger() {
			Function converter = BuiltinConverters.find(String.class, Integer.class);
			assertNotNull(converter, "String -> Integer converter should be registered");
			assertEquals(Integer.valueOf(123), converter.apply("123"));
		}

		@Test
		@DisplayName("String -> long converter exists and converts correctly")
		void testStringToLong() {
			Function converter = BuiltinConverters.find(String.class, long.class);
			assertNotNull(converter, "String -> long converter should be registered");
			assertEquals(999999999L, converter.apply("999999999"));
		}

		@Test
		@DisplayName("String -> Long converter exists and converts correctly")
		void testStringToLongBoxed() {
			Function converter = BuiltinConverters.find(String.class, Long.class);
			assertNotNull(converter, "String -> Long converter should be registered");
			assertEquals(Long.valueOf(100L), converter.apply("100"));
		}

		@Test
		@DisplayName("String -> double converter exists and converts correctly")
		void testStringToDouble() {
			Function converter = BuiltinConverters.find(String.class, double.class);
			assertNotNull(converter, "String -> double converter should be registered");
			assertEquals(3.14, converter.apply("3.14"));
		}

		@Test
		@DisplayName("String -> float converter exists and converts correctly")
		void testStringToFloat() {
			Function converter = BuiltinConverters.find(String.class, float.class);
			assertNotNull(converter, "String -> float converter should be registered");
			assertEquals(2.5f, (float) converter.apply("2.5"), 0.001f);
		}

		@Test
		@DisplayName("String -> short converter exists and converts correctly")
		void testStringToShort() {
			Function converter = BuiltinConverters.find(String.class, short.class);
			assertNotNull(converter, "String -> short converter should be registered");
			assertEquals((short) 10, converter.apply("10"));
		}

		@Test
		@DisplayName("String -> boolean converter exists and converts correctly")
		void testStringToBoolean() {
			Function converter = BuiltinConverters.find(String.class, boolean.class);
			assertNotNull(converter, "String -> boolean converter should be registered");
			assertEquals(true, converter.apply("true"));
			assertEquals(false, converter.apply("false"));
		}

		@Test
		@DisplayName("int -> String converter exists and converts correctly")
		void testIntToString() {
			Function converter = BuiltinConverters.find(int.class, String.class);
			assertNotNull(converter, "int -> String converter should be registered");
			assertEquals("42", converter.apply(42));
		}

		@Test
		@DisplayName("Integer -> String converter exists and converts correctly")
		void testIntegerToString() {
			Function converter = BuiltinConverters.find(Integer.class, String.class);
			assertNotNull(converter, "Integer -> String converter should be registered");
			assertEquals("100", converter.apply(100));
		}

		@Test
		@DisplayName("long -> String converter exists and converts correctly")
		void testLongToString() {
			Function converter = BuiltinConverters.find(long.class, String.class);
			assertNotNull(converter, "long -> String converter should be registered");
			assertEquals("999", converter.apply(999L));
		}

		@Test
		@DisplayName("Double -> String converter exists and converts correctly")
		void testDoubleToString() {
			Function converter = BuiltinConverters.find(Double.class, String.class);
			assertNotNull(converter, "Double -> String converter should be registered");
			assertEquals("3.14", converter.apply(3.14));
		}

		@Test
		@DisplayName("String -> numeric converters handle null gracefully")
		void testStringToNumericNullHandling() {
			Function strToInt = BuiltinConverters.find(String.class, int.class);
			Function strToLong = BuiltinConverters.find(String.class, Long.class);
			Function strToDouble = BuiltinConverters.find(String.class, double.class);

			assertNull(strToInt.apply(null));
			assertNull(strToLong.apply(null));
			assertNull(strToDouble.apply(null));
		}

		@Test
		@DisplayName("Boxed numeric -> String converters handle null gracefully")
		void testNumericToStringNullHandling() {
			Function intToStr = BuiltinConverters.find(Integer.class, String.class);
			Function longToStr = BuiltinConverters.find(Long.class, String.class);
			Function doubleToStr = BuiltinConverters.find(Double.class, String.class);

			assertNull(intToStr.apply(null));
			assertNull(longToStr.apply(null));
			assertNull(doubleToStr.apply(null));
		}
	}

	@Nested
	@DisplayName("Numeric ↔ numeric conversions")
	class NumericNumericConversions {

		@Test
		@DisplayName("Integer -> long converter exists and converts correctly")
		void testIntegerToLong() {
			Function converter = BuiltinConverters.find(Integer.class, long.class);
			assertNotNull(converter, "Integer -> long converter should be registered");
			assertEquals(42L, converter.apply(42));
		}

		@Test
		@DisplayName("Long -> int converter exists and converts correctly")
		void testLongToInt() {
			Function converter = BuiltinConverters.find(Long.class, int.class);
			assertNotNull(converter, "Long -> int converter should be registered");
			assertEquals(100, converter.apply(100L));
		}

		@Test
		@DisplayName("Integer -> double converter exists and converts correctly")
		void testIntegerToDouble() {
			Function converter = BuiltinConverters.find(Integer.class, double.class);
			assertNotNull(converter, "Integer -> double converter should be registered");
			assertEquals(42.0, converter.apply(42));
		}

		@Test
		@DisplayName("Double -> int converter exists and converts correctly")
		void testDoubleToInt() {
			Function converter = BuiltinConverters.find(Double.class, int.class);
			assertNotNull(converter, "Double -> int converter should be registered");
			assertEquals(3, converter.apply(3.99));
		}

		@Test
		@DisplayName("Integer -> float converter exists and converts correctly")
		void testIntegerToFloat() {
			Function converter = BuiltinConverters.find(Integer.class, float.class);
			assertNotNull(converter, "Integer -> float converter should be registered");
			assertEquals(42.0f, (float) converter.apply(42), 0.001f);
		}

		@Test
		@DisplayName("Long -> double converter exists and converts correctly")
		void testLongToDouble() {
			Function converter = BuiltinConverters.find(Long.class, double.class);
			assertNotNull(converter, "Long -> double converter should be registered");
			assertEquals(1000.0, converter.apply(1000L));
		}

		@Test
		@DisplayName("Integer -> short converter exists and converts correctly")
		void testIntegerToShort() {
			Function converter = BuiltinConverters.find(Integer.class, short.class);
			assertNotNull(converter, "Integer -> short converter should be registered");
			assertEquals((short) 5, converter.apply(5));
		}

		@Test
		@DisplayName("Short -> int converter exists and converts correctly")
		void testShortToInt() {
			Function converter = BuiltinConverters.find(Short.class, int.class);
			assertNotNull(converter, "Short -> int converter should be registered");
			assertEquals(7, converter.apply((short) 7));
		}

		@Test
		@DisplayName("Float -> double converter exists and converts correctly")
		void testFloatToDouble() {
			Function converter = BuiltinConverters.find(Float.class, double.class);
			assertNotNull(converter, "Float -> double converter should be registered");
			double result = (double) converter.apply(1.5f);
			assertEquals(1.5, result, 0.001);
		}

		@Test
		@DisplayName("Boxed numeric -> numeric converters handle null gracefully")
		void testNumericToNumericNullHandling() {
			Function intToLong = BuiltinConverters.find(Integer.class, long.class);
			Function longToInt = BuiltinConverters.find(Long.class, int.class);
			Function intToDouble = BuiltinConverters.find(Integer.class, double.class);

			assertNull(intToLong.apply(null));
			assertNull(longToInt.apply(null));
			assertNull(intToDouble.apply(null));
		}
	}

	@Nested
	@DisplayName("String ↔ Date conversions")
	class StringDateConversions {

		@Test
		@DisplayName("String -> java.sql.Date converter exists and converts correctly")
		void testStringToSqlDate() {
			Function converter = BuiltinConverters.find(String.class, java.sql.Date.class);
			assertNotNull(converter, "String -> java.sql.Date converter should be registered");
			Date result = (Date) converter.apply("2024-01-15");
			assertNotNull(result);
			assertEquals(Date.valueOf("2024-01-15"), result);
		}

		@Test
		@DisplayName("java.sql.Date -> String converter exists and converts correctly")
		void testSqlDateToString() {
			Function converter = BuiltinConverters.find(java.sql.Date.class, String.class);
			assertNotNull(converter, "java.sql.Date -> String converter should be registered");
			String result = (String) converter.apply(Date.valueOf("2024-06-30"));
			assertNotNull(result);
			assertEquals("2024-06-30", result);
		}

		@Test
		@DisplayName("String -> java.sql.Timestamp converter exists and converts correctly")
		void testStringToTimestamp() {
			Function converter = BuiltinConverters.find(String.class, java.sql.Timestamp.class);
			assertNotNull(converter, "String -> java.sql.Timestamp converter should be registered");
			Timestamp result = (Timestamp) converter.apply("2024-01-15 10:30:00");
			assertNotNull(result);
			assertEquals(Timestamp.valueOf("2024-01-15 10:30:00"), result);
		}

		@Test
		@DisplayName("java.sql.Timestamp -> String converter exists and converts correctly")
		void testTimestampToString() {
			Function converter = BuiltinConverters.find(java.sql.Timestamp.class, String.class);
			assertNotNull(converter, "java.sql.Timestamp -> String converter should be registered");
			Timestamp ts = Timestamp.valueOf("2024-01-15 10:30:00");
			String result = (String) converter.apply(ts);
			assertNotNull(result);
			assertTrue(result.contains("2024-01-15"));
		}

		@Test
		@DisplayName("String -> java.sql.Time converter exists and converts correctly")
		void testStringToTime() {
			Function converter = BuiltinConverters.find(String.class, java.sql.Time.class);
			assertNotNull(converter, "String -> java.sql.Time converter should be registered");
			Time result = (Time) converter.apply("14:30:00");
			assertNotNull(result);
			assertEquals(Time.valueOf("14:30:00"), result);
		}

		@Test
		@DisplayName("java.sql.Time -> String converter exists and converts correctly")
		void testTimeToString() {
			Function converter = BuiltinConverters.find(java.sql.Time.class, String.class);
			assertNotNull(converter, "java.sql.Time -> String converter should be registered");
			String result = (String) converter.apply(Time.valueOf("08:15:30"));
			assertNotNull(result);
			assertEquals("08:15:30", result);
		}

		@Test
		@DisplayName("String -> java.util.Date converter exists and converts correctly")
		void testStringToUtilDate() {
			Function converter = BuiltinConverters.find(String.class, java.util.Date.class);
			assertNotNull(converter, "String -> java.util.Date converter should be registered");
			java.util.Date result = (java.util.Date) converter.apply("2024-01-15 00:00:00");
			assertNotNull(result);
		}

		@Test
		@DisplayName("String -> Date converters handle null gracefully")
		void testStringToDateNullHandling() {
			Function strToDate = BuiltinConverters.find(String.class, java.sql.Date.class);
			Function strToTs = BuiltinConverters.find(String.class, java.sql.Timestamp.class);
			Function strToTime = BuiltinConverters.find(String.class, java.sql.Time.class);

			assertNull(strToDate.apply(null));
			assertNull(strToTs.apply(null));
			assertNull(strToTime.apply(null));
		}
	}

	@Nested
	@DisplayName("long ↔ Date conversions")
	class LongDateConversions {

		@Test
		@DisplayName("Long -> java.sql.Date converter exists and converts correctly")
		void testLongToSqlDate() {
			Function converter = BuiltinConverters.find(Long.class, java.sql.Date.class);
			assertNotNull(converter, "Long -> java.sql.Date converter should be registered");
			long millis = Date.valueOf("2024-01-15").getTime();
			Date result = (Date) converter.apply(millis);
			assertNotNull(result);
			assertEquals(Date.valueOf("2024-01-15"), result);
		}

		@Test
		@DisplayName("long -> java.sql.Date converter exists and converts correctly")
		void testPrimitiveLongToSqlDate() {
			Function converter = BuiltinConverters.find(long.class, java.sql.Date.class);
			assertNotNull(converter, "long -> java.sql.Date converter should be registered");
			long millis = Date.valueOf("2024-06-30").getTime();
			Date result = (Date) converter.apply(millis);
			assertNotNull(result);
			assertEquals(Date.valueOf("2024-06-30"), result);
		}

		@Test
		@DisplayName("java.sql.Date -> Long converter exists and converts correctly")
		void testSqlDateToLong() {
			Function converter = BuiltinConverters.find(java.sql.Date.class, Long.class);
			assertNotNull(converter, "java.sql.Date -> Long converter should be registered");
			Date date = Date.valueOf("2024-01-15");
			Long result = (Long) converter.apply(date);
			assertNotNull(result);
			assertEquals(date.getTime(), result.longValue());
		}

		@Test
		@DisplayName("java.sql.Date -> long converter exists and converts correctly")
		void testSqlDateToPrimitiveLong() {
			Function converter = BuiltinConverters.find(java.sql.Date.class, long.class);
			assertNotNull(converter, "java.sql.Date -> long converter should be registered");
			Date date = Date.valueOf("2024-03-20");
			Long result = (Long) converter.apply(date);
			assertNotNull(result);
			assertEquals(date.getTime(), result.longValue());
		}

		@Test
		@DisplayName("Long -> java.sql.Timestamp converter exists and converts correctly")
		void testLongToTimestamp() {
			Function converter = BuiltinConverters.find(Long.class, java.sql.Timestamp.class);
			assertNotNull(converter, "Long -> java.sql.Timestamp converter should be registered");
			long millis = System.currentTimeMillis();
			Timestamp result = (Timestamp) converter.apply(millis);
			assertNotNull(result);
			assertEquals(millis, result.getTime());
		}

		@Test
		@DisplayName("java.sql.Timestamp -> Long converter exists and converts correctly")
		void testTimestampToLong() {
			Function converter = BuiltinConverters.find(java.sql.Timestamp.class, Long.class);
			assertNotNull(converter, "java.sql.Timestamp -> Long converter should be registered");
			Timestamp ts = new Timestamp(System.currentTimeMillis());
			Long result = (Long) converter.apply(ts);
			assertNotNull(result);
			assertEquals(ts.getTime(), result.longValue());
		}

		@Test
		@DisplayName("Long -> java.util.Date converter exists and converts correctly")
		void testLongToUtilDate() {
			Function converter = BuiltinConverters.find(Long.class, java.util.Date.class);
			assertNotNull(converter, "Long -> java.util.Date converter should be registered");
			long millis = System.currentTimeMillis();
			java.util.Date result = (java.util.Date) converter.apply(millis);
			assertNotNull(result);
			assertEquals(millis, result.getTime());
		}

		@Test
		@DisplayName("java.util.Date -> Long converter exists and converts correctly")
		void testUtilDateToLong() {
			Function converter = BuiltinConverters.find(java.util.Date.class, Long.class);
			assertNotNull(converter, "java.util.Date -> Long converter should be registered");
			java.util.Date date = new java.util.Date();
			Long result = (Long) converter.apply(date);
			assertNotNull(result);
			assertEquals(date.getTime(), result.longValue());
		}

		@Test
		@DisplayName("Long -> Date converters handle null gracefully")
		void testLongToDateNullHandling() {
			Function longToDate = BuiltinConverters.find(Long.class, java.sql.Date.class);
			Function longToTs = BuiltinConverters.find(Long.class, java.sql.Timestamp.class);
			Function longToUtil = BuiltinConverters.find(Long.class, java.util.Date.class);

			assertNull(longToDate.apply(null));
			assertNull(longToTs.apply(null));
			assertNull(longToUtil.apply(null));
		}

		@Test
		@DisplayName("Date -> long converters handle null gracefully")
		void testDateToLongNullHandling() {
			Function dateToLong = BuiltinConverters.find(java.sql.Date.class, Long.class);
			Function tsToLong = BuiltinConverters.find(java.sql.Timestamp.class, Long.class);
			Function utilToLong = BuiltinConverters.find(java.util.Date.class, Long.class);

			assertNull(dateToLong.apply(null));
			assertNull(tsToLong.apply(null));
			assertNull(utilToLong.apply(null));
		}
	}

	@Nested
	@DisplayName("find() returns null for unregistered pairs")
	class UnregisteredPairs {

		@Test
		@DisplayName("find returns null for unregistered type pair")
		void testFindReturnsNullForUnregistered() {
			// No built-in converter for java.util.List -> String
			Function converter = BuiltinConverters.find(java.util.List.class, String.class);
			assertNull(converter, "Unregistered type pair should return null");
		}

		@Test
		@DisplayName("find returns null for same type (identity is handled elsewhere)")
		void testFindReturnsNullForSameType() {
			// Same type conversion is handled by resolveConverter, not find
			Function converter = BuiltinConverters.find(String.class, String.class);
			assertNull(converter, "Same type should return null from find (identity handled elsewhere)");
		}
	}
}
