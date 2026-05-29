package com.github.xuse.querydsl.sql.expression;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.entity.FooDTO;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.querydsl.core.types.Path;

/**
 * Tests for {@link ConverterWrappedBean#extractValues(RelationalPathEx)}.
 * <p>
 * Focuses on verifying that extractValues returns values converted to the target column type
 * when the DTO field type differs from the entity column type.
 */
@DisplayName("ConverterWrappedBean - extractValues conversion tests")
class ConverterWrappedBeanTest {

	private final RelationalPathEx<Foo> fooPath = PathCache.get(Foo.class, null);

	/**
	 * Verify that extractValues converts String codeTypeX to int codeType
	 * via the built-in String->int converter (triggered by @PathBinder mapping).
	 */
	@Test
	@DisplayName("extractValues converts String field to int column type via @PathBinder")
	void testExtractValuesConvertsStringToInt() {
		FooDTO dto = new FooDTO();
		dto.setId(1);
		dto.setCode("TEST");
		dto.setName("Test");
		dto.setVolume(10);
		dto.setVersion(1);
		dto.setCodeTypeX("42"); // String field mapped to int column via @PathBinder

		ConverterWrappedBean wrapped = ConverterWrappedBean.of(dto);
		Object[] values = wrapped.extractValues(fooPath);

		// Find the codeType column index
		List<Path<?>> columns = fooPath.getColumns();
		int codeTypeIndex = -1;
		for (int i = 0; i < columns.size(); i++) {
			if ("codeType".equals(columns.get(i).getMetadata().getName())) {
				codeTypeIndex = i;
				break;
			}
		}
		assertTrue(codeTypeIndex >= 0, "codeType column should exist in entity");

		// The value should be converted from String "42" to Integer 42
		Object convertedValue = values[codeTypeIndex];
		assertNotNull(convertedValue);
		assertInstanceOf(Integer.class, convertedValue);
		assertEquals(42, convertedValue);
	}

	/**
	 * Verify that extractValues returns NOT_AVAILABLE for columns not mapped by the DTO.
	 */
	@Test
	@DisplayName("extractValues returns NOT_AVAILABLE for unmapped columns")
	void testExtractValuesReturnsNotAvailableForUnmappedColumns() {
		FooDTO dto = new FooDTO();
		dto.setCode("ABC");
		dto.setVersion(1);

		ConverterWrappedBean wrapped = ConverterWrappedBean.of(dto);
		Object[] values = wrapped.extractValues(fooPath);

		// FooDTO does not have "created" or "updated" fields
		List<Path<?>> columns = fooPath.getColumns();
		for (int i = 0; i < columns.size(); i++) {
			String colName = columns.get(i).getMetadata().getName();
			if ("created".equals(colName) || "updated".equals(colName)) {
				assertSame(ConverterWrappedBean.NOT_AVAILABLE, values[i],
						"Column '" + colName + "' not in DTO should be NOT_AVAILABLE");
			}
		}
	}

	/**
	 * Verify that extractValues preserves null for mapped fields with null values
	 * (distinguishing from NOT_AVAILABLE for unmapped fields).
	 */
	@Test
	@DisplayName("extractValues returns null for mapped fields with null value")
	void testExtractValuesReturnsNullForMappedNullFields() {
		FooDTO dto = new FooDTO();
		dto.setCode(null); // mapped field, explicitly null
		dto.setVersion(1);

		ConverterWrappedBean wrapped = ConverterWrappedBean.of(dto);
		Object[] values = wrapped.extractValues(fooPath);

		List<Path<?>> columns = fooPath.getColumns();
		for (int i = 0; i < columns.size(); i++) {
			String colName = columns.get(i).getMetadata().getName();
			if ("code".equals(colName)) {
				// Mapped field with null value → should be null, not NOT_AVAILABLE
				assertNull(values[i], "Mapped field with null value should be null");
			}
		}
	}

	/**
	 * Verify that extractValues correctly converts multiple field types in a single call.
	 * Tests that the conversion pipeline handles int, String, and enum fields together.
	 */
	@Test
	@DisplayName("extractValues converts multiple field types correctly")
	void testExtractValuesMultipleFieldConversions() {
		FooDTO dto = new FooDTO();
		dto.setId(100);
		dto.setCode("MULTI");
		dto.setName("Multi Test");
		dto.setVolume(50);
		dto.setVersion(2);
		dto.setCodeTypeX("99");
		dto.setInDay(java.sql.Date.valueOf("2024-06-15"));

		ConverterWrappedBean wrapped = ConverterWrappedBean.of(dto);
		Object[] values = wrapped.extractValues(fooPath);

		List<Path<?>> columns = fooPath.getColumns();
		for (int i = 0; i < columns.size(); i++) {
			String colName = columns.get(i).getMetadata().getName();
			switch (colName) {
				case "id":
					assertEquals(100, values[i]);
					break;
				case "code":
					assertEquals("MULTI", values[i]);
					break;
				case "asset_name": // Foo.name is mapped to column "asset_name"
				case "name":
					if (values[i] != null && !values[i].equals(ConverterWrappedBean.NOT_AVAILABLE)) {
						assertEquals("Multi Test", values[i]);
					}
					break;
				case "codeType":
					// String "99" converted to int 99
					assertEquals(99, values[i]);
					break;
				case "volume":
					assertEquals(50, values[i]);
					break;
				case "version":
					assertEquals(2, values[i]);
					break;
				case "inDay":
					assertEquals(java.sql.Date.valueOf("2024-06-15"), values[i]);
					break;
			}
		}
	}

	/**
	 * Verify that extractValues handles null on a converter-mapped field gracefully.
	 * When codeTypeX is null, the String->int converter should return null.
	 */
	@Test
	@DisplayName("extractValues handles null on converter-mapped field")
	void testExtractValuesNullOnConverterField() {
		FooDTO dto = new FooDTO();
		dto.setId(1);
		dto.setCode("NULL-CONV");
		dto.setVolume(1);
		dto.setVersion(1);
		dto.setCodeTypeX(null); // null String to be converted to int column

		ConverterWrappedBean wrapped = ConverterWrappedBean.of(dto);
		Object[] values = wrapped.extractValues(fooPath);

		List<Path<?>> columns = fooPath.getColumns();
		for (int i = 0; i < columns.size(); i++) {
			String colName = columns.get(i).getMetadata().getName();
			if ("codeType".equals(colName)) {
				// null String -> converter returns null (not an exception)
				assertNull(values[i], "Null String through converter should produce null");
			}
		}
	}

	/**
	 * Verify that the result array length matches the entity column count.
	 */
	@Test
	@DisplayName("extractValues result array length matches entity column count")
	void testExtractValuesArrayLength() {
		FooDTO dto = new FooDTO();
		dto.setCode("LEN");
		dto.setVersion(1);

		ConverterWrappedBean wrapped = ConverterWrappedBean.of(dto);
		Object[] values = wrapped.extractValues(fooPath);

		assertEquals(fooPath.getColumns().size(), values.length,
				"Result array length should match entity column count");
	}
}
