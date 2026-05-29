package com.github.xuse.querydsl.sql.partitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.annotation.partition.Partition;

/**
 * Unit tests for {@link PartitionDef}.
 * Validates: Requirements 6.5
 */
@DisplayName("PartitionDef Unit Tests")
class PartitionDefTest {

	@Nested
	@DisplayName("Basic accessor methods")
	class BasicAccessors {

		@Test
		@DisplayName("name() returns the provided name")
		void testNameReturnsProvidedName() {
			PartitionDef def = new PartitionDef("p_2024", "2024-01-01", "2025-01-01");
			assertEquals("p_2024", def.name());
		}

		@Test
		@DisplayName("from() returns the provided from value")
		void testFromReturnsProvidedFrom() {
			PartitionDef def = new PartitionDef("p_2024", "2024-01-01", "2025-01-01");
			assertEquals("2024-01-01", def.from());
		}

		@Test
		@DisplayName("value() returns the provided value string")
		void testValueReturnsProvidedValue() {
			PartitionDef def = new PartitionDef("p_2024", "2024-01-01", "2025-01-01");
			assertEquals("2025-01-01", def.value());
		}

		@Test
		@DisplayName("annotationType() returns Partition annotation class")
		void testAnnotationTypeReturnsPartitionClass() {
			PartitionDef def = new PartitionDef("p1", null, "100");
			Class<? extends Annotation> annotationType = def.annotationType();

			assertNotNull(annotationType);
			assertEquals(Partition.class, annotationType);
		}
	}

	@Nested
	@DisplayName("Null and empty value handling")
	class NullHandling {

		@Test
		@DisplayName("from() returns null when constructed with null from")
		void testFromReturnsNullWhenNull() {
			PartitionDef def = new PartitionDef("p_active", null, "'A','B','C'");
			assertNull(def.from());
		}

		@Test
		@DisplayName("value() returns empty string when constructed with empty value")
		void testValueReturnsEmptyString() {
			PartitionDef def = new PartitionDef("p_empty", null, "");
			assertEquals("", def.value());
		}

		@Test
		@DisplayName("name() returns empty string when constructed with empty name")
		void testNameReturnsEmptyString() {
			PartitionDef def = new PartitionDef("", "start", "end");
			assertEquals("", def.name());
		}
	}

	@Nested
	@DisplayName("Various partition definition scenarios")
	class Scenarios {

		@Test
		@DisplayName("List partition: name and value for list-style partition")
		void testListPartitionDef() {
			PartitionDef def = new PartitionDef("p_north", null, "'N','NE','NW'");

			assertEquals("p_north", def.name());
			assertNull(def.from());
			assertEquals("'N','NE','NW'", def.value());
			assertEquals(Partition.class, def.annotationType());
		}

		@Test
		@DisplayName("Range partition: name, from, and value for range-style partition")
		void testRangePartitionDef() {
			PartitionDef def = new PartitionDef("p_q1", "2024-01-01", "2024-04-01");

			assertEquals("p_q1", def.name());
			assertEquals("2024-01-01", def.from());
			assertEquals("2024-04-01", def.value());
			assertEquals(Partition.class, def.annotationType());
		}

		@Test
		@DisplayName("MAXVALUE partition: value is MAXVALUE string")
		void testMaxValuePartitionDef() {
			PartitionDef def = new PartitionDef("pmax", "2025-01-01", "MAXVALUE");

			assertEquals("pmax", def.name());
			assertEquals("2025-01-01", def.from());
			assertEquals("MAXVALUE", def.value());
		}
	}
}
