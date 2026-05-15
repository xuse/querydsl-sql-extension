package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.util.io.UnicodeReader;

/**
 * Tests for UnicodeReader: BOM detection for UTF-8, UTF-16LE, UTF-16BE, and no-BOM fallback.
 */
class UnicodeReaderTest {

	/** UTF-8 BOM (EF BB BF) should be detected and skipped. */
	@Test
	void testUtf8Bom() throws IOException {
		byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
		byte[] text = "hello".getBytes(StandardCharsets.UTF_8);
		byte[] data = concat(bom, text);
		try (UnicodeReader reader = new UnicodeReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8)) {
			char[] buf = new char[100];
			int len = reader.read(buf, 0, buf.length);
			assertEquals("hello", new String(buf, 0, len));
			assertTrue(reader.getEncoding().toUpperCase().contains("UTF"));
		}
	}

	/** UTF-16LE BOM (FF FE) should be detected. */
	@Test
	void testUtf16LeBom() throws IOException {
		byte[] bom = {(byte) 0xFF, (byte) 0xFE};
		byte[] text = "AB".getBytes(StandardCharsets.UTF_16LE);
		byte[] data = concat(bom, text);
		try (UnicodeReader reader = new UnicodeReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8)) {
			char[] buf = new char[100];
			int len = reader.read(buf, 0, buf.length);
			assertEquals("AB", new String(buf, 0, len));
		}
	}

	/** UTF-16BE BOM (FE FF) should be detected. */
	@Test
	void testUtf16BeBom() throws IOException {
		byte[] bom = {(byte) 0xFE, (byte) 0xFF};
		byte[] text = "CD".getBytes(StandardCharsets.UTF_16BE);
		byte[] data = concat(bom, text);
		try (UnicodeReader reader = new UnicodeReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8)) {
			char[] buf = new char[100];
			int len = reader.read(buf, 0, buf.length);
			assertEquals("CD", new String(buf, 0, len));
		}
	}

	/** No BOM: should fall back to default encoding. */
	@Test
	void testNoBom() throws IOException {
		byte[] text = "plain text".getBytes(StandardCharsets.UTF_8);
		try (UnicodeReader reader = new UnicodeReader(new ByteArrayInputStream(text), StandardCharsets.UTF_8)) {
			assertEquals(StandardCharsets.UTF_8, reader.getDefaultEncoding());
			assertNull(reader.getEncoding()); // not initialized yet
			char[] buf = new char[100];
			int len = reader.read(buf, 0, buf.length);
			assertEquals("plain text", new String(buf, 0, len));
			assertNotNull(reader.getEncoding()); // now initialized
		}
	}

	/** No BOM with null default encoding: should use system default. */
	@Test
	void testNoBomNullDefault() throws IOException {
		byte[] text = "test".getBytes(StandardCharsets.UTF_8);
		try (UnicodeReader reader = new UnicodeReader(new ByteArrayInputStream(text), null)) {
			assertNull(reader.getDefaultEncoding());
			char[] buf = new char[100];
			int len = reader.read(buf, 0, buf.length);
			assertEquals("test", new String(buf, 0, len));
		}
	}

	/** Close without reading should still work (init is called in close). */
	@Test
	void testCloseWithoutRead() throws IOException {
		byte[] text = "data".getBytes(StandardCharsets.UTF_8);
		UnicodeReader reader = new UnicodeReader(new ByteArrayInputStream(text), StandardCharsets.UTF_8);
		reader.close(); // should not throw
	}

	private static byte[] concat(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}
}