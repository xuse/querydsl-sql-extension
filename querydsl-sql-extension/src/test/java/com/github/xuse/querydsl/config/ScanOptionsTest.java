package com.github.xuse.querydsl.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.entity.CaAsset;
import com.github.xuse.querydsl.init.ScanOptions;

/**
 * Unit tests for {@link ScanOptions}.
 * Validates: Requirements 11.3
 */
@DisplayName("ScanOptions Unit Tests")
class ScanOptionsTest {

	@Nested
	@DisplayName("Annotation Filters")
	class AnnotationFilters {

		@Test
		@DisplayName("withAnnotation sets the annotation filter correctly")
		void testWithAnnotation() {
			ScanOptions options = new ScanOptions();
			assertNull(options.getWithAnnotation(), "Default withAnnotation should be null");

			ScanOptions result = options.withAnnotation(Deprecated.class);

			// Fluent API returns same instance
			assertEquals(options, result);
			assertEquals(Deprecated.class, options.getWithAnnotation());
		}

		@Test
		@DisplayName("withoutAnnotation sets the exclusion annotation filter correctly")
		void testWithoutAnnotation() {
			ScanOptions options = new ScanOptions();
			assertNull(options.getWithoutAnnotation(), "Default withoutAnnotation should be null");

			ScanOptions result = options.withoutAnnotation(Deprecated.class);

			// Fluent API returns same instance
			assertEquals(options, result);
			assertEquals(Deprecated.class, options.getWithoutAnnotation());
		}

		@Test
		@DisplayName("withAnnotation and withoutAnnotation can be set together")
		void testBothAnnotationFilters() {
			ScanOptions options = new ScanOptions()
					.withAnnotation(TestIncludeAnnotation.class)
					.withoutAnnotation(TestExcludeAnnotation.class);

			assertEquals(TestIncludeAnnotation.class, options.getWithAnnotation());
			assertEquals(TestExcludeAnnotation.class, options.getWithoutAnnotation());
		}

		@Test
		@DisplayName("withAnnotation can be overwritten with a different annotation")
		void testWithAnnotationOverwrite() {
			ScanOptions options = new ScanOptions()
					.withAnnotation(Deprecated.class)
					.withAnnotation(TestIncludeAnnotation.class);

			assertEquals(TestIncludeAnnotation.class, options.getWithAnnotation());
		}

		@Test
		@DisplayName("withoutAnnotation can be overwritten with a different annotation")
		void testWithoutAnnotationOverwrite() {
			ScanOptions options = new ScanOptions()
					.withoutAnnotation(Deprecated.class)
					.withoutAnnotation(TestExcludeAnnotation.class);

			assertEquals(TestExcludeAnnotation.class, options.getWithoutAnnotation());
		}

		@Test
		@DisplayName("withAnnotation accepts null to clear the filter")
		void testWithAnnotationNull() {
			ScanOptions options = new ScanOptions()
					.withAnnotation(Deprecated.class)
					.withAnnotation(null);

			assertNull(options.getWithAnnotation());
		}

		@Test
		@DisplayName("withoutAnnotation accepts null to clear the filter")
		void testWithoutAnnotationNull() {
			ScanOptions options = new ScanOptions()
					.withoutAnnotation(Deprecated.class)
					.withoutAnnotation(null);

			assertNull(options.getWithoutAnnotation());
		}
	}

	@Nested
	@DisplayName("initTaskJustOn Whitelist")
	class InitTaskWhitelist {

		@Test
		@DisplayName("initTaskJustOn populates whitelist with specified entity classes")
		void testInitTaskJustOnPopulatesWhitelist() {
			ScanOptions options = new ScanOptions();
			assertTrue(options.getInitTaskWhiteList().isEmpty(), "Default whitelist should be empty");

			options.initTaskJustOn(Foo.class, CaAsset.class);

			assertEquals(2, options.getInitTaskWhiteList().size());
			assertTrue(options.getInitTaskWhiteList().contains(Foo.class));
			assertTrue(options.getInitTaskWhiteList().contains(CaAsset.class));
		}

		@Test
		@DisplayName("initTaskJustOn with single class adds exactly one entry")
		void testInitTaskJustOnSingleClass() {
			ScanOptions options = new ScanOptions();

			options.initTaskJustOn(Foo.class);

			assertEquals(1, options.getInitTaskWhiteList().size());
			assertTrue(options.getInitTaskWhiteList().contains(Foo.class));
		}

		@Test
		@DisplayName("initTaskJustOn called multiple times accumulates classes")
		void testInitTaskJustOnAccumulates() {
			ScanOptions options = new ScanOptions();

			options.initTaskJustOn(Foo.class);
			options.initTaskJustOn(CaAsset.class);

			assertEquals(2, options.getInitTaskWhiteList().size());
			assertTrue(options.getInitTaskWhiteList().contains(Foo.class));
			assertTrue(options.getInitTaskWhiteList().contains(CaAsset.class));
		}

		@Test
		@DisplayName("initTaskJustOn with duplicate class does not add duplicates (Set behavior)")
		void testInitTaskJustOnNoDuplicates() {
			ScanOptions options = new ScanOptions();

			options.initTaskJustOn(Foo.class, Foo.class);

			assertEquals(1, options.getInitTaskWhiteList().size());
		}

		@Test
		@DisplayName("deprecated setInitTableWhiteList also populates whitelist")
		@SuppressWarnings("deprecation")
		void testDeprecatedSetInitTableWhiteList() {
			ScanOptions options = new ScanOptions();

			options.setInitTableWhiteList(Foo.class, CaAsset.class);

			assertEquals(2, options.getInitTaskWhiteList().size());
			assertTrue(options.getInitTaskWhiteList().contains(Foo.class));
			assertTrue(options.getInitTaskWhiteList().contains(CaAsset.class));
		}

		@Test
		@DisplayName("whitelist is accessible via getInitTaskWhiteList and is mutable set")
		void testWhitelistAccessible() {
			ScanOptions options = new ScanOptions();
			options.initTaskJustOn(Foo.class);

			assertNotNull(options.getInitTaskWhiteList());
			assertEquals(1, options.getInitTaskWhiteList().size());
		}
	}

	@Nested
	@DisplayName("Combined Configuration")
	class CombinedConfiguration {

		@Test
		@DisplayName("ScanOptions supports fluent chaining of all options")
		void testFluentChaining() {
			ScanOptions options = new ScanOptions()
					.withAnnotation(TestIncludeAnnotation.class)
					.withoutAnnotation(TestExcludeAnnotation.class)
					.canCreateMissingTable(true)
					.canAlterExistTable(false);

			assertEquals(TestIncludeAnnotation.class, options.getWithAnnotation());
			assertEquals(TestExcludeAnnotation.class, options.getWithoutAnnotation());
			assertTrue(options.isCreateMissingTable());
			assertEquals(false, options.isAlterExistTable());
		}

		@Test
		@DisplayName("initTaskJustOn combined with annotation filters")
		void testWhitelistWithAnnotationFilters() {
			ScanOptions options = new ScanOptions()
					.withAnnotation(TestIncludeAnnotation.class)
					.initTaskJustOn(Foo.class, CaAsset.class);

			assertEquals(TestIncludeAnnotation.class, options.getWithAnnotation());
			assertEquals(2, options.getInitTaskWhiteList().size());
		}
	}

	// Test annotations used for filter verification
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface TestIncludeAnnotation {
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface TestExcludeAnnotation {
	}
}
