package com.github.xuse.querydsl.sql.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLListenerContext;

/**
 * Unit tests for {@link QueryDSLSQLListener} exceptionLogLevel feature.
 */
@ExtendWith(MockitoExtension.class)
class QueryDSLSQLListenerTest {

	@Mock
	private SQLListenerContext context;

	@Test
	void testDefaultExceptionLogLevel_isError() {
		// Verify constant values
		assertEquals(0, QueryDSLSQLListener.LOG_LEVEL_ERROR);
		assertEquals(1, QueryDSLSQLListener.LOG_LEVEL_WARN);
	}

	@Test
	void testSetExceptionLogLevel_fluentReturn() {
		QueryDSLSQLListener listener = new QueryDSLSQLListener();
		QueryDSLSQLListener result = listener.setExceptionLogLevel(QueryDSLSQLListener.LOG_LEVEL_WARN);
		assertSame(listener, result, "setExceptionLogLevel should return this for fluent API");
	}

	@Test
	void testException_defaultLevel_logsError() {
		QueryDSLSQLListener listener = new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_COMPACT);
		// default is ERROR

		SQLBindings bindings = new SQLBindings("SELECT 1 FROM dual", Collections.emptyList());
		when(context.getAllSQLBindings()).thenReturn(Arrays.asList(bindings));
		when(context.getException()).thenReturn(new RuntimeException("default level test"));

		String output = captureStdout(() -> listener.exception(context));
		assertTrue(output.contains("[ERROR]"), "Default level should log at ERROR, got: " + output);
		assertTrue(output.contains("SELECT 1 FROM dual"), "Log should contain the SQL statement");
	}

	@Test
	void testException_withErrorLevel_logsError() {
		QueryDSLSQLListener listener = new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_FULL);
		listener.setExceptionLogLevel(QueryDSLSQLListener.LOG_LEVEL_ERROR);

		SQLBindings bindings = new SQLBindings("INSERT INTO t VALUES(?)", Collections.singletonList("val"));
		when(context.getAllSQLBindings()).thenReturn(Arrays.asList(bindings));
		when(context.getException()).thenReturn(new RuntimeException("error level test"));

		String output = captureStdout(() -> listener.exception(context));
		assertTrue(output.contains("[ERROR]"), "Should log at ERROR level, got: " + output);
	}

	@Test
	void testException_withWarnLevel_logsWarn() {
		QueryDSLSQLListener listener = new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_DEBUG);
		listener.setExceptionLogLevel(QueryDSLSQLListener.LOG_LEVEL_WARN);

		SQLBindings bindings = new SQLBindings("UPDATE t SET x=1", Collections.emptyList());
		when(context.getAllSQLBindings()).thenReturn(Arrays.asList(bindings));
		when(context.getException()).thenReturn(new RuntimeException("warn level test"));

		String output = captureStdout(() -> listener.exception(context));
		assertTrue(output.contains("[WARN]"), "Should log at WARN level, got: " + output);
	}

	@Test
	void testException_switchFromErrorToWarn() {
		QueryDSLSQLListener listener = new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_COMPACT);

		SQLBindings bindings = new SQLBindings("DELETE FROM t WHERE id=?", Collections.singletonList(42));
		when(context.getAllSQLBindings()).thenReturn(Arrays.asList(bindings));
		when(context.getException()).thenReturn(new RuntimeException("switch test"));

		// First call with default ERROR
		String output1 = captureStdout(() -> listener.exception(context));
		assertTrue(output1.contains("[ERROR]"), "Before switch should be ERROR");

		// Switch to WARN
		listener.setExceptionLogLevel(QueryDSLSQLListener.LOG_LEVEL_WARN);
		String output2 = captureStdout(() -> listener.exception(context));
		assertTrue(output2.contains("[WARN]"), "After switch should be WARN");
	}

	@Test
	void testException_allFormats_workWithBothLevels() {
		int[] formats = {QueryDSLSQLListener.FORMAT_COMPACT, QueryDSLSQLListener.FORMAT_FULL, QueryDSLSQLListener.FORMAT_DEBUG};
		for (int format : formats) {
			QueryDSLSQLListener listener = new QueryDSLSQLListener(format);

			when(context.getAllSQLBindings()).thenReturn(Collections.emptyList());
			when(context.getException()).thenReturn(new RuntimeException("format " + format));

			// ERROR level
			listener.setExceptionLogLevel(QueryDSLSQLListener.LOG_LEVEL_ERROR);
			listener.exception(context); // should not throw

			// WARN level
			listener.setExceptionLogLevel(QueryDSLSQLListener.LOG_LEVEL_WARN);
			listener.exception(context); // should not throw
		}
	}

	private String captureStdout(Runnable action) {
		PrintStream original = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut(new PrintStream(baos));
		try {
			action.run();
		} finally {
			System.setOut(original);
		}
		return baos.toString();
	}
}
