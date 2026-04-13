package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.util.lang.Annotations;
import com.github.xuse.querydsl.util.lang.Enums;
import com.github.xuse.querydsl.util.lang.Lambdas;
import com.mysema.commons.lang.Pair;

/**
 * Tests for Annotations (dynamic annotation proxy), Enums utilities, and Lambdas analysis.
 */
class AnnotationsTest {

	@Retention(RetentionPolicy.RUNTIME)
	@interface SampleAnnotation {
		String value() default "";
		int count() default 0;
		String[] tags() default {};
	}

	enum Color { RED, GREEN, BLUE }

	// ==================== Annotations.Builder ====================

	/** Build a dynamic annotation and verify attribute values. */
	@Test
	void testBuildAnnotation() {
		SampleAnnotation ann = Annotations.builder(SampleAnnotation.class)
				.set("value", "hello")
				.set("count", 42)
				.set("tags", new String[]{"a", "b"})
				.build();
		assertNotNull(ann);
		assertEquals("hello", ann.value());
		assertEquals(42, ann.count());
		assertArrayEquals(new String[]{"a", "b"}, ann.tags());
	}

	/** Verify annotationType() returns the correct class. */
	@Test
	void testAnnotationType() {
		SampleAnnotation ann = Annotations.builder(SampleAnnotation.class)
				.set("value", "test")
				.build();
		assertEquals(SampleAnnotation.class, ann.annotationType());
	}

	/** Verify toString() contains the annotation type name. */
	@Test
	void testAnnotationToString() {
		SampleAnnotation ann = Annotations.builder(SampleAnnotation.class)
				.set("value", "x")
				.build();
		String str = ann.toString();
		assertNotNull(str);
		assertTrue(str.contains("SampleAnnotation"));
	}

	/** Verify hashCode() is consistent. */
	@Test
	void testAnnotationHashCode() {
		SampleAnnotation a1 = Annotations.builder(SampleAnnotation.class).set("value", "a").build();
		SampleAnnotation a2 = Annotations.builder(SampleAnnotation.class).set("value", "a").build();
		assertEquals(a1.hashCode(), a2.hashCode());
	}

	/** Verify equals() for same and different values. */
	@Test
	void testAnnotationEquals() {
		SampleAnnotation a1 = Annotations.builder(SampleAnnotation.class).set("value", "a").set("count", 1).build();
		SampleAnnotation a2 = Annotations.builder(SampleAnnotation.class).set("value", "a").set("count", 1).build();
		SampleAnnotation a3 = Annotations.builder(SampleAnnotation.class).set("value", "b").set("count", 2).build();
		assertEquals(a1, a2);
		assertNotEquals(a1, a3);
		assertNotEquals(a1, null);
		assertNotEquals(a1, "not an annotation");
	}

	/** Verify array attribute cloning (returned arrays should be independent copies). */
	@Test
	void testAnnotationArrayClone() {
		String[] tags = {"x", "y"};
		SampleAnnotation ann = Annotations.builder(SampleAnnotation.class).set("tags", tags).build();
		String[] result = ann.tags();
		assertArrayEquals(tags, result);
		// Modify returned array should not affect the annotation
		result[0] = "modified";
		assertArrayEquals(new String[]{"x", "y"}, ann.tags());
	}

	// ==================== Enums ====================

	/** valueOf with default value. */
	@Test
	void testEnumValueOfWithDefault() {
		assertEquals(Color.RED, Enums.valueOf(Color.class, "RED", Color.BLUE));
		assertEquals(Color.BLUE, Enums.valueOf(Color.class, "INVALID", Color.BLUE));
	}

	/** valueOf by ordinal. */
	@Test
	void testEnumValueOfByOrdinal() {
		assertEquals(Color.GREEN, Enums.valueOf(Color.class, 1));
		assertThrows(IllegalArgumentException.class, () -> Enums.valueOf(Color.class, 99));
	}

	/** valueOf by Integer ordinal with default. */
	@Test
	void testEnumValueOfByIntegerOrdinal() {
		assertEquals(Color.BLUE, Enums.valueOf(Color.class, 2, Color.RED));
		assertEquals(Color.RED, Enums.valueOf(Color.class, (Integer) null, Color.RED));
		assertEquals(Color.RED, Enums.valueOf(Color.class, 99, Color.RED));
	}

	/** valueOf with exception message. */
	@Test
	void testEnumValueOfWithException() {
		assertEquals(Color.RED, Enums.valueOf(Color.class, "RED", "Invalid: {}"));
		assertThrows(IllegalArgumentException.class, () -> Enums.valueOf(Color.class, "BAD", "Invalid: {}", "BAD"));
	}

	/** valuesMap returns a name-to-enum map. */
	@Test
	void testEnumValuesMap() {
		Map<String, Color> map = Enums.valuesMap(Color.class);
		assertEquals(3, map.size());
		assertEquals(Color.RED, map.get("RED"));
	}

	/** getByCode with a custom key function. */
	@Test
	void testEnumGetByCode() {
		Color result = Enums.getByCode(Color.class, Color::ordinal, 2);
		assertEquals(Color.BLUE, result);
		assertNull(Enums.getByCode(Color.class, Color::ordinal, 99));
	}

	// ==================== Lambdas ====================

	/** Lambda analysis should extract class and field name from a method reference. */
	@Test
	void testLambdaAnalysis() {
		com.github.xuse.querydsl.lambda.StringLambdaColumn<com.github.xuse.querydsl.entity.Foo> col =
				com.github.xuse.querydsl.entity.Foo::getName;
		Pair<Class<?>, String> result = Lambdas.analysis(col);
		assertEquals(com.github.xuse.querydsl.entity.Foo.class, result.getFirst());
		assertEquals("name", result.getSecond());
	}
}