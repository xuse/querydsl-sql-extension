package com.github.xuse.querydsl.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Types;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;

/**
 * Unit tests for {@link TypeNames} verifying type registration with
 * precision/scale and unregistered type behavior.
 *
 * Requirements: 8.5, 8.6
 */
@DisplayName("TypeNames - type registration and retrieval")
class TypeNamesTest {

	private TypeNames typeNames;

	@BeforeEach
	void setUp() {
		typeNames = new TypeNames();
	}

	@Nested
	@DisplayName("Type registration with precision/scale")
	class TypeRegistrationWithPrecisionScale {

		@Test
		@DisplayName("Registered type with $l placeholder returns ColumnDef with size replaced")
		void testRegisteredTypeWithLengthPlaceholder() {
			typeNames.put(Types.VARCHAR, "varchar($l)");
			ColumnDef result = typeNames.get(Types.VARCHAR, 255, 0);
			assertNotNull(result);
			assertEquals("varchar(255)", result.getDataType());
			assertEquals(255, result.getColumnSize());
			assertEquals(0, result.getDecimalDigit());
			assertEquals(Types.VARCHAR, result.getJdbcType());
		}

		@Test
		@DisplayName("Registered type with $p and $s placeholders returns ColumnDef with precision and scale replaced")
		void testRegisteredTypeWithPrecisionAndScale() {
			typeNames.put(Types.DECIMAL, "decimal($p,$s)");
			ColumnDef result = typeNames.get(Types.DECIMAL, 10, 2);
			assertNotNull(result);
			assertEquals("decimal(10,2)", result.getDataType());
			assertEquals(10, result.getColumnSize());
			assertEquals(2, result.getDecimalDigit());
			assertEquals(Types.DECIMAL, result.getJdbcType());
		}

		@Test
		@DisplayName("Registered type without placeholders returns template as-is")
		void testRegisteredTypeWithoutPlaceholders() {
			typeNames.put(Types.INTEGER, "int");
			ColumnDef result = typeNames.get(Types.INTEGER, 0, 0);
			assertNotNull(result);
			assertEquals("int", result.getDataType());
			assertEquals(Types.INTEGER, result.getJdbcType());
		}

		@Test
		@DisplayName("Capacity-based registration returns correct type for matching size")
		void testCapacityBasedRegistration() {
			// Register default and capacity-limited types
			typeNames.put(Types.VARCHAR, "text");
			typeNames.put(Types.VARCHAR, 255, "varchar($l)");
			typeNames.put(Types.VARCHAR, 65535, "mediumtext");

			// Size 100 <= 255, should use varchar($l)
			ColumnDef result1 = typeNames.get(Types.VARCHAR, 100, 0);
			assertNotNull(result1);
			assertEquals("varchar(100)", result1.getDataType());

			// Size 1000 <= 65535, should use mediumtext
			ColumnDef result2 = typeNames.get(Types.VARCHAR, 1000, 0);
			assertNotNull(result2);
			assertEquals("mediumtext", result2.getDataType());

			// Size 100000 > 65535, should fall back to default "text"
			ColumnDef result3 = typeNames.get(Types.VARCHAR, 100000, 0);
			assertNotNull(result3);
			assertEquals("text", result3.getDataType());
		}

		@Test
		@DisplayName("get with zero size and scale uses default policy")
		void testGetWithZeroSizeUsesDefault() {
			typeNames.put(Types.TIMESTAMP, "timestamp");
			typeNames.put(Types.TIMESTAMP, 6, "timestamp($l)");

			// size <= 0 should use default
			ColumnDef result = typeNames.get(Types.TIMESTAMP, 0, 0);
			assertNotNull(result);
			assertEquals("timestamp", result.getDataType());
		}
	}

	@Nested
	@DisplayName("Unregistered type handling")
	class UnregisteredTypeHandling {

		@Test
		@DisplayName("Unregistered type code throws IllegalArgumentException")
		void testUnregisteredTypeThrowsException() {
			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
				typeNames.get(9999);
			});
			assertNotNull(ex.getMessage());
			// Message should contain the type code
			assertEquals("No Dialect mapping for JDBC type: 9999", ex.getMessage());
		}

		@Test
		@DisplayName("Unregistered type code with size/scale also throws IllegalArgumentException")
		void testUnregisteredTypeWithSizeThrowsException() {
			assertThrows(IllegalArgumentException.class, () -> {
				typeNames.get(9999, 10, 2);
			});
		}
	}

	@Nested
	@DisplayName("generateDefault produces valid mappings")
	class GenerateDefaultTests {

		@Test
		@DisplayName("Default TypeNames contains standard JDBC type mappings")
		void testGenerateDefaultContainsStandardTypes() {
			TypeNames defaults = TypeNames.generateDefault();
			assertNotNull(defaults);

			// Verify a few standard types are registered
			assertNotNull(defaults.get(Types.VARCHAR));
			assertNotNull(defaults.get(Types.INTEGER));
			assertNotNull(defaults.get(Types.DECIMAL));
			assertNotNull(defaults.get(Types.TIMESTAMP));
			assertNotNull(defaults.get(Types.BOOLEAN));
		}

		@Test
		@DisplayName("Default VARCHAR with size returns varchar(n)")
		void testDefaultVarcharWithSize() {
			TypeNames defaults = TypeNames.generateDefault();
			ColumnDef result = defaults.get(Types.VARCHAR, 128, 0);
			assertNotNull(result);
			assertEquals("varchar(128)", result.getDataType());
			assertEquals(128, result.getColumnSize());
		}

		@Test
		@DisplayName("Default DECIMAL with precision and scale returns decimal(p,s)")
		void testDefaultDecimalWithPrecisionScale() {
			TypeNames defaults = TypeNames.generateDefault();
			ColumnDef result = defaults.get(Types.DECIMAL, 18, 4);
			assertNotNull(result);
			assertEquals("decimal(18,4)", result.getDataType());
			assertEquals(18, result.getColumnSize());
			assertEquals(4, result.getDecimalDigit());
		}
	}
}
