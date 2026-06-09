package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for IOUtils IO utility methods: copy, toString, and closeQuietly.
 * Validates: Requirements 9.5
 */
@DisplayName("IOUtils IO Sub-Package Tests")
class IoSubPackageTest {

    @Nested
    @DisplayName("IOUtils.copy - byte stream")
    class CopyByteStream {

        @Test
        @DisplayName("copy transfers all bytes from input to output")
        void testCopyTransfersAllBytes() {
            byte[] source = "Hello, IOUtils copy test with some content!".getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream in = new ByteArrayInputStream(source);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int copied = IOUtils.copy(in, out, 1024);

            assertEquals(source.length, copied);
            assertArrayEquals(source, out.toByteArray());
        }

        @Test
        @DisplayName("copy transfers all bytes with small buffer size")
        void testCopyWithSmallBuffer() {
            byte[] source = "Testing with a very small buffer size to ensure multiple reads".getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream in = new ByteArrayInputStream(source);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int copied = IOUtils.copy(in, out, 4);

            assertEquals(source.length, copied);
            assertArrayEquals(source, out.toByteArray());
        }

        @Test
        @DisplayName("copy handles empty input stream")
        void testCopyEmptyStream() {
            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int copied = IOUtils.copy(in, out, 1024);

            assertEquals(0, copied);
            assertEquals(0, out.toByteArray().length);
        }

        @Test
        @DisplayName("copy transfers large content correctly")
        void testCopyLargeContent() {
            byte[] source = new byte[10_000];
            for (int i = 0; i < source.length; i++) {
                source[i] = (byte) (i % 256);
            }
            ByteArrayInputStream in = new ByteArrayInputStream(source);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int copied = IOUtils.copy(in, out, 512);

            assertEquals(source.length, copied);
            assertArrayEquals(source, out.toByteArray());
        }
    }

    @Nested
    @DisplayName("IOUtils.copy - char stream")
    class CopyCharStream {

        @Test
        @DisplayName("copy transfers all characters from reader to writer")
        void testCopyReaderToWriter() {
            String content = "Hello, character stream copy test!";
            StringReader reader = new StringReader(content);
            StringWriter writer = new StringWriter();

            IOUtils.copy(reader, writer, 1024);

            assertEquals(content, writer.toString());
        }

        @Test
        @DisplayName("copy with zero buffer size defaults to 1024")
        void testCopyWithZeroBufferSize() {
            String content = "Testing default buffer size behavior";
            StringReader reader = new StringReader(content);
            StringWriter writer = new StringWriter();

            IOUtils.copy(reader, writer, 0);

            assertEquals(content, writer.toString());
        }

        @Test
        @DisplayName("copy handles empty reader")
        void testCopyEmptyReader() {
            StringReader reader = new StringReader("");
            StringWriter writer = new StringWriter();

            IOUtils.copy(reader, writer, 1024);

            assertEquals("", writer.toString());
        }
    }

    @Nested
    @DisplayName("IOUtils.toString")
    class ToStringTests {

        @Test
        @DisplayName("toString reads complete content from Reader")
        void testToStringReadsCompleteContent() {
            String expected = "Complete content to read from a reader";
            StringReader reader = new StringReader(expected);

            String result = IOUtils.toString(reader);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("toString reads multi-line content")
        void testToStringMultiLineContent() {
            String expected = "Line 1\nLine 2\nLine 3\nEnd";
            StringReader reader = new StringReader(expected);

            String result = IOUtils.toString(reader);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("toString returns null for null reader")
        void testToStringNullReader() {
            String result = IOUtils.toString(null);

            assertNull(result);
        }

        @Test
        @DisplayName("toString reads complete content from InputStream")
        void testToStringFromInputStream() {
            String expected = "Content from an InputStream";
            InputStream in = new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8));

            String result = IOUtils.toString(in, StandardCharsets.UTF_8);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("toString reads large content completely")
        void testToStringLargeContent() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5000; i++) {
                sb.append("Line ").append(i).append('\n');
            }
            String expected = sb.toString();
            StringReader reader = new StringReader(expected);

            String result = IOUtils.toString(reader);

            assertEquals(expected, result);
        }
    }

    @Nested
    @DisplayName("IOUtils.closeQuietly")
    class CloseQuietlyTests {

        @Test
        @DisplayName("closeQuietly does not throw on null")
        void testCloseQuietlyNull() {
            assertDoesNotThrow(() -> IOUtils.closeQuietly(null));
        }

        @Test
        @DisplayName("closeQuietly does not throw on failed close")
        void testCloseQuietlyFailedClose() {
            Closeable failingCloseable = () -> {
                throw new IOException("Simulated close failure");
            };

            assertDoesNotThrow(() -> IOUtils.closeQuietly(failingCloseable));
        }

        @Test
        @DisplayName("closeQuietly invokes close on valid resource")
        void testCloseQuietlyInvokesClose() {
            boolean[] closed = {false};
            Closeable resource = () -> closed[0] = true;

            IOUtils.closeQuietly(resource);

            assertEquals(true, closed[0]);
        }
    }
}
