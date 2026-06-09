package com.github.xuse.querydsl.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;

/**
 * Unit tests for {@link ConfigurationEx} verifying construction,
 * default property values, and null-input validation.
 *
 * Requirements: 11.1, 11.2
 */
@DisplayName("ConfigurationEx - construction and default properties")
class ConfigurationExTest {

	@Nested
	@DisplayName("Construction with valid SQLTemplates")
	class ValidConstruction {

		@Test
		@DisplayName("H2Templates produces non-null Configuration and SQLTemplatesEx")
		void testConstructionWithH2Templates() {
			SQLTemplates templates = H2Templates.builder().build();
			ConfigurationEx config = new ConfigurationEx(templates);

			assertNotNull(config.get(), "Underlying Configuration should be non-null");
			assertNotNull(config.getTemplates(), "getTemplates() should return non-null SQLTemplatesEx");
		}

		@Test
		@DisplayName("MySQLTemplates produces non-null Configuration and SQLTemplatesEx")
		void testConstructionWithMySQLTemplates() {
			SQLTemplates templates = MySQLTemplates.builder().build();
			ConfigurationEx config = new ConfigurationEx(templates);

			assertNotNull(config.get(), "Underlying Configuration should be non-null");
			SQLTemplatesEx templatesEx = config.getTemplates();
			assertNotNull(templatesEx, "getTemplates() should return non-null SQLTemplatesEx");
		}
	}

	@Nested
	@DisplayName("Default property values")
	class DefaultProperties {

		private final ConfigurationEx config = new ConfigurationEx(H2Templates.builder().build());

		@Test
		@DisplayName("slowSqlWarnMillis defaults to 10000")
		void testDefaultSlowSqlWarnMillis() {
			assertEquals(10000L, config.getSlowSqlWarnMillis());
		}

		@Test
		@DisplayName("defaultQueryTimeout defaults to 0")
		void testDefaultQueryTimeout() {
			assertEquals(0, config.getDefaultQueryTimeout());
		}

		@Test
		@DisplayName("allowTableDropAndCreate defaults to false")
		void testDefaultAllowTableDropAndCreate() {
			assertFalse(config.isAllowTableDropAndCreate());
		}
	}

	@Nested
	@DisplayName("Null SQLTemplates handling")
	class NullTemplates {

		@Test
		@DisplayName("null SQLTemplates throws NullPointerException")
		void testNullTemplatesThrowsException() {
			assertThrows(NullPointerException.class, () -> new ConfigurationEx(null));
		}
	}
}
