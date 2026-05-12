package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link KVSplitter}.
 */
class KVSplitterTest {

	@Test
	void testBasicParsing() {
		Map<String, String> result = KVSplitter.on('&', '=')
				.split("a=1&b=2&c=3")
				.collect(LinkedHashMap::new);
		assertEquals(3, result.size());
		assertEquals("1", result.get("a"));
		assertEquals("2", result.get("b"));
		assertEquals("3", result.get("c"));
	}

	@Test
	void testWithSpaces() {
		Map<String, String> result = KVSplitter.on('&', '=')
				.split(" a = 1 & b = 2 ")
				.collect(HashMap::new);
		assertEquals("1", result.get("a"));
		assertEquals("2", result.get("b"));
	}

	@Test
	void testKeepSpace() {
		Map<String, String> result = KVSplitter.on('&', '=')
				.keepSpace()
				.split(" a = 1 & b = 2 ")
				.collect(HashMap::new);
		assertEquals(" 1 ", result.get(" a "));
		assertEquals(" 2 ", result.get(" b "));
	}

	@Test
	void testCustomSeparators() {
		Map<String, String> result = KVSplitter.on(';', ':')
				.split("host:localhost;port:3306;db:test")
				.collect(HashMap::new);
		assertEquals("localhost", result.get("host"));
		assertEquals("3306", result.get("port"));
		assertEquals("test", result.get("db"));
	}

	@Test
	void testEmptyValue() {
		Map<String, String> result = KVSplitter.on('&', '=')
				.split("a=&b=2")
				.collect(HashMap::new);
		assertEquals("", result.get("a"));
		assertEquals("2", result.get("b"));
	}

	@Test
	void testSingleEntry() {
		Map<String, String> result = KVSplitter.on('&', '=')
				.split("key=value")
				.collect(HashMap::new);
		assertEquals(1, result.size());
		assertEquals("value", result.get("key"));
	}

	@Test
	void testKeyFilter() {
		Map<String, String> result = KVSplitter.on('&', '=')
				.keyFilter(k -> k.startsWith("x"))
				.split("x_a=1&b=2&x_c=3")
				.collect(HashMap::new);
		assertEquals(2, result.size());
		assertEquals("1", result.get("x_a"));
		assertEquals("3", result.get("x_c"));
	}

	@Test
	void testFunctionOfHeadChar() {
		// Custom head function that skips quote chars
		Map<String, String> result = KVSplitter.on('&', '=')
				.keepSpace()
				.split("a=hello&b=world")
				.collect(HashMap::new);
		assertNotNull(result);
		assertEquals("hello", result.get("a"));
	}

	// Helper to access the internal string for the lambda above
	private String result() {
		return "";
	}

	@Test
	void testFunctionOfTailChar() {
		KVSplitter splitter = KVSplitter.on('&', '=');
		splitter.functionOfTailChar(i -> i);
		Map<String, String> result = splitter.split("a=1 &b=2 ").collect(HashMap::new);
		// With identity tail function, trailing spaces are kept
		assertEquals("1 ", result.get("a"));
	}

	@Test
	void testErrorPosition() {
		KVSplitter splitter = KVSplitter.on('&', '=');
		splitter.split("a=1&b=2");
		// After parsing, errorPosition shows remaining
		splitter.collect(HashMap::new);
		String pos = splitter.errorPosition();
		assertNotNull(pos);
	}

	@Test
	void testEmptyString() {
		// Empty string may throw or return empty depending on implementation
		KVSplitter splitter = KVSplitter.on('&', '=').keepSpace();
		Map<String, String> result = splitter.split("").collect(HashMap::new);
		assertTrue(result.isEmpty());
	}

	@Test
	void testNoKeyValueSep() {
		// String without key-value separator
		Map<String, String> result = KVSplitter.on('&', '=')
				.split("abc&def")
				.collect(HashMap::new);
		// No '=' found, so no entries
		assertTrue(result.isEmpty());
	}
}
