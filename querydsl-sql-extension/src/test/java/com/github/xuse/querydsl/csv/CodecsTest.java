package com.github.xuse.querydsl.csv;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.init.TableDataInitializer;
import com.github.xuse.querydsl.init.csv.Codecs;
import com.github.xuse.querydsl.util.DateUtils;

public class CodecsTest {
	@Test
	public void testCodecs() {
		String str;

		assertEquals("", Codecs.toString("", String.class));
		assertEquals("", Codecs.fromString("", String.class));
		assertEquals(null, Codecs.fromString(TableDataInitializer.NULL_STRING_ESCAPE, String.class));

		assertEquals("1", str = Codecs.toString(1, Integer.class));
		assertEquals(1, Codecs.fromString(str, Integer.class));
		assertEquals(0, Codecs.fromString(null, Integer.class));
		assertEquals(0, Codecs.fromString("", Integer.class));
		assertEquals(null, Codecs.fromString("null", Integer.class));

		assertEquals("1", str = Codecs.toString((short) 1, Short.class));
		assertEquals((short) 1, Codecs.fromString(str, Short.class));
		assertEquals((short) 0, Codecs.fromString(null, Short.class));
		assertEquals((short) 0, Codecs.fromString("", Short.class));
		assertEquals(null, Codecs.fromString("null", Short.class));

		assertEquals("1", str = Codecs.toString((byte) 1, Byte.class));
		assertEquals((byte) 1, Codecs.fromString(str, Byte.class));
		assertEquals((byte) 0, Codecs.fromString(null, Byte.class));
		assertEquals((byte) 0, Codecs.fromString("", Byte.class));
		assertEquals(null, Codecs.fromString("null", Byte.class));

		assertEquals("1", str = Codecs.toString(1L, Long.class));
		assertEquals(1L, Codecs.fromString(str, Long.class));
		assertEquals(0L, Codecs.fromString(null, Long.class));
		assertEquals(0L, Codecs.fromString("", Long.class));
		assertEquals(null, Codecs.fromString("null", Long.class));

		assertEquals("1.0", str = Codecs.toString(1f, Float.class));
		assertEquals(1f, Codecs.fromString(str, Float.class));
		assertEquals(0f, Codecs.fromString(null, Float.class));
		assertEquals(0f, Codecs.fromString("", Float.class));
		assertEquals(null, Codecs.fromString("null", Float.class));

		assertEquals("1.0", str = Codecs.toString(1d, Double.class));
		assertEquals(1d, Codecs.fromString(str, Double.class));
		assertEquals(0d, Codecs.fromString(null, Double.class));
		assertEquals(0d, Codecs.fromString("", Double.class));
		assertEquals(null, Codecs.fromString("null", Double.class));

		assertEquals("true", str = Codecs.toString(true, Boolean.class));
		assertEquals(true, Codecs.fromString(str, Boolean.class));
		assertEquals(false, Codecs.fromString(null, Boolean.class));
		assertEquals(false, Codecs.fromString("", Boolean.class));
		assertEquals(null, Codecs.fromString("null", Boolean.class));

		assertEquals("" + (char) 1, str = Codecs.toString((char) 1, Character.class));
		assertEquals((char) 1, Codecs.fromString(str, Character.class));
		assertEquals((char) 0, Codecs.fromString(null, Character.class));
		assertEquals((char) 0, Codecs.fromString("", Character.class));
		assertEquals(null, Codecs.fromString("null", Character.class));

		Date d = DateUtils.get(2000, 1, 1, 1, 1, 1);
		assertEquals("946659661000", str = Codecs.toString(d, Date.class));
		assertEquals("", Codecs.toString(null, Date.class));
		assertEquals(d, Codecs.fromString(str, Date.class));
		assertEquals(null, Codecs.fromString("", Date.class));
		assertEquals(null, Codecs.fromString(null, Date.class));

		Timestamp ts = DateUtils.toSqlTimeStamp(d);
		assertEquals("946659661000", str = Codecs.toString(ts, Timestamp.class));
		assertEquals("", Codecs.toString(null, Timestamp.class));
		assertEquals(ts, Codecs.fromString(str, Timestamp.class));
		assertEquals(null, Codecs.fromString("", Timestamp.class));
		assertEquals(null, Codecs.fromString(null, Timestamp.class));

		Instant is = d.toInstant();
		assertEquals("946659661000", str = Codecs.toString(is, Instant.class));
		assertEquals("", Codecs.toString(null, Instant.class));
		assertEquals(is, Codecs.fromString(str, Instant.class));
		assertEquals(null, Codecs.fromString("", Instant.class));
		assertEquals(null, Codecs.fromString(null, Instant.class));

		java.sql.Date sd = new java.sql.Date(d.getTime());
		assertEquals("2000-01-01", str = Codecs.toString(sd, java.sql.Date.class));
		assertEquals("", Codecs.toString(null, java.sql.Date.class));
		assertEquals(DateUtils.getSqlDate(2000, 1, 1), Codecs.fromString(str, java.sql.Date.class));
		assertEquals(null, Codecs.fromString("", java.sql.Date.class));
		assertEquals(null, Codecs.fromString(null, java.sql.Date.class));

		java.sql.Time st = DateUtils.getSqlTime(10, 10, 10, 5);
		assertEquals("36610005", str = Codecs.toString(st, java.sql.Time.class));
		assertEquals("", Codecs.toString(null, java.sql.Time.class));
		assertEquals(st, Codecs.fromString(str, java.sql.Time.class));
		assertEquals(null, Codecs.fromString("", java.sql.Time.class));
		assertEquals(null, Codecs.fromString(null, java.sql.Time.class));

		LocalDate ld = DateUtils.toLocalDate(sd);
		assertEquals("10957", str = Codecs.toString(ld, LocalDate.class));
		assertEquals("", Codecs.toString(null, LocalDate.class));
		assertEquals(ld, Codecs.fromString(str, LocalDate.class));
		assertEquals(null, Codecs.fromString("", LocalDate.class));
		assertEquals(null, Codecs.fromString(null, LocalDate.class));

		LocalTime lt = DateUtils.toLocalTime(st);
		assertEquals("65410000000000", str = Codecs.toString(lt, LocalTime.class));
		assertEquals("", Codecs.toString(null, LocalTime.class));
		assertEquals(lt, Codecs.fromString(str, LocalTime.class));
		assertEquals(null, Codecs.fromString("", LocalTime.class));
		assertEquals(null, Codecs.fromString(null, LocalTime.class));

		LocalDateTime lts = DateUtils.toLocalDateTime(d);
		assertEquals("2000-01-01 01:01:01.000", str = Codecs.toString(lts, LocalDateTime.class));
		assertEquals("", Codecs.toString(null, LocalDateTime.class));
		assertEquals(lts, Codecs.fromString(str, LocalDateTime.class));
		assertEquals(null, Codecs.fromString("", LocalDateTime.class));
		assertEquals(null, Codecs.fromString(null, LocalDateTime.class));

		byte[] bts = "Hello".getBytes();
		assertEquals("SGVsbG8=", str = Codecs.toString(bts, byte[].class));
		assertEquals("", Codecs.toString(null, byte[].class));
		assertArrayEquals(bts, (byte[]) Codecs.fromString(str, byte[].class));
		assertEquals(null, Codecs.fromString("", byte[].class));
		assertEquals(null, Codecs.fromString(null, byte[].class));

		Gender g = Gender.MALE;
		assertEquals("MALE", str = Codecs.toString(g, Gender.class));
		assertEquals("", Codecs.toString(null, Gender.class));
		assertEquals(g, Codecs.fromString(str, Gender.class));
		assertEquals(null, Codecs.fromString("", Gender.class));
		assertEquals(null, Codecs.fromString(null, Gender.class));

		Foo foo = new Foo();
		foo.setCode("Code");
		assertEquals(
				"rO0ABXNyACNjb20uZ2l0aHViLnh1c2UucXVlcnlkc2wuZW50aXR5LkZvb8C/goWKVQVMAgAKSQACaWRJAAZ2b2x1bWVMAARjb2RldAASTGphdmEvbGFuZy9TdHJpbmc7TAAHY29udGVudHEAfgABTAAHY3JlYXRlZHQAE0xqYXZhL3RpbWUvSW5zdGFudDtMAANleHR0ACVMY29tL2dpdGh1Yi94dXNlL3F1ZXJ5ZHNsL2VudGl0eS9BYWE7TAAGZ2VuZGVydAAnTGNvbS9naXRodWIveHVzZS9xdWVyeWRzbC9lbnVtcy9HZW5kZXI7TAADbWFwdAAPTGphdmEvdXRpbC9NYXA7TAAEbmFtZXEAfgABTAAHdXBkYXRlZHQAEExqYXZhL3V0aWwvRGF0ZTt4cAAAAAAAAAAAdAAEQ29kZXBwcHBwcHA=",
				str = Codecs.toString(foo, Foo.class));
		assertEquals("", Codecs.toString(null, Foo.class));
		assertEquals(foo, Codecs.fromString(str, Foo.class));
		assertEquals(null, Codecs.fromString("", Foo.class));
		assertEquals(null, Codecs.fromString(null, Foo.class));
	}

	@Test
	public void testErrors() {
		Aaa aaa = new Aaa();
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			Codecs.toString(aaa, Aaa.class);
		});
	}
}
