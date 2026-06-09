package com.github.xuse.querydsl.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SizeParser} verifying MYSQL_TIMESTAMP and MYSQL_TIME
 * size parsing behavior.
 *
 * Requirements: 8.7
 */
@DisplayName("SizeParser - size specification parsing")
class SizeParserTest {

	@Nested
	@DisplayName("MYSQL_TIMESTAMP")
	class MysqlTimestampTests {

		@Test
		@DisplayName("size 26 yields 6 (26 - 20 = 6, since size > 19)")
		void testSize26Yields6() {
			assertEquals(6, SizeParser.MYSQL_TIMESTAMP.size(26, 0));
		}

		@Test
		@DisplayName("size 19 yields 0 (19 - 19 = 0, since size <= 19)")
		void testSize19Yields0() {
			assertEquals(0, SizeParser.MYSQL_TIMESTAMP.size(19, 0));
		}

		@Test
		@DisplayName("size 22 yields 2 (22 - 20 = 2, since size > 19)")
		void testSize22Yields2() {
			assertEquals(2, SizeParser.MYSQL_TIMESTAMP.size(22, 0));
		}

		@Test
		@DisplayName("size 20 yields 0 (20 - 20 = 0, since size > 19)")
		void testSize20Yields0() {
			assertEquals(0, SizeParser.MYSQL_TIMESTAMP.size(20, 0));
		}

		@Test
		@DisplayName("size 23 yields 3 (23 - 20 = 3, since size > 19)")
		void testSize23Yields3() {
			assertEquals(3, SizeParser.MYSQL_TIMESTAMP.size(23, 0));
		}
	}

	@Nested
	@DisplayName("MYSQL_TIME")
	class MysqlTimeTests {

		@Test
		@DisplayName("size 15 yields 6 (15 - 9 = 6, since size > 9)")
		void testSize15Yields6() {
			assertEquals(6, SizeParser.MYSQL_TIME.size(15, 0));
		}

		@Test
		@DisplayName("size 8 yields 0 (8 - 8 = 0, since size <= 9)")
		void testSize8Yields0() {
			assertEquals(0, SizeParser.MYSQL_TIME.size(8, 0));
		}

		@Test
		@DisplayName("size 10 yields 1 (10 - 9 = 1, since size > 9)")
		void testSize10Yields1() {
			assertEquals(1, SizeParser.MYSQL_TIME.size(10, 0));
		}

		@Test
		@DisplayName("size 9 yields 1 (9 - 8 = 1, since size <= 9)")
		void testSize9Yields1() {
			assertEquals(1, SizeParser.MYSQL_TIME.size(9, 0));
		}
	}

	@Nested
	@DisplayName("DEFAULT SizeParser")
	class DefaultParserTests {

		@Test
		@DisplayName("DEFAULT returns size unchanged")
		void testDefaultReturnsSizeUnchanged() {
			assertEquals(100, SizeParser.DEFAULT.size(100, 5));
			assertEquals(0, SizeParser.DEFAULT.size(0, 0));
		}

		@Test
		@DisplayName("DEFAULT returns digits unchanged")
		void testDefaultReturnsDigitsUnchanged() {
			assertEquals(5, SizeParser.DEFAULT.digits(100, 5));
			assertEquals(0, SizeParser.DEFAULT.digits(0, 0));
		}
	}

	@Nested
	@DisplayName("TIME_DIGIT_AS_SIZE")
	class TimeDigitAsSizeTests {

		@Test
		@DisplayName("TIME_DIGIT_AS_SIZE returns digits as size")
		void testReturnsDigitsAsSize() {
			assertEquals(6, SizeParser.TIME_DIGIT_AS_SIZE.size(0, 6));
			assertEquals(3, SizeParser.TIME_DIGIT_AS_SIZE.size(100, 3));
			assertEquals(0, SizeParser.TIME_DIGIT_AS_SIZE.size(19, 0));
		}
	}
}
