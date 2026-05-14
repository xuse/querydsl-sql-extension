package com.github.xuse.querydsl.sql.expression;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.entity.FooDTO;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;

/**
 * Tests for {@link QBeanExWithConverter} and {@link ConverterWrappedBean}.
 * <p>
 * Verifies:
 * <ul>
 *   <li>Basic field mapping by same name</li>
 *   <li>{@code @PathBinder} annotation for field name remapping</li>
 *   <li>Built-in type conversion (int to String via BuiltinConverters)</li>
 *   <li>Unmapped fields preserve DTO default values</li>
 *   <li>Null handling on primitive vs reference type fields</li>
 *   <li>ConverterWrappedBean for Insert/Update with field name remapping</li>
 * </ul>
 */
class QBeanExWithConverterTest {

	private final RelationalPathEx<Foo> fooPath = PathCache.get(Foo.class, null);

	// ========== QBeanExWithConverter (Select) tests ==========

	/**
	 * Verify getArgs() contains only columns matching DTO fields,
	 * and includes codeType (mapped via @PathBinder on codeTypeX).
	 */
	@Test
	void testGetArgsContainsMappedColumns() {
		QBeanExWithConverter<FooDTO> projection = createProjection();
		List<Expression<?>> args = projection.getArgs();

		assertNotNull(args);
		assertFalse(args.isEmpty());

		// codeType column should be present (mapped to codeTypeX via @PathBinder)
		assertTrue(hasColumn(args, "codeType"), "codeType should be in getArgs() via @PathBinder");
		// id, code, name etc. should be present
		assertTrue(hasColumn(args, "id"));
		assertTrue(hasColumn(args, "code"));
		assertTrue(hasColumn(args, "name"));
	}

	/**
	 * Verify getArgs() does NOT contain columns that have no matching DTO field.
	 * Foo has "created" and "updated" fields, but FooDTO does not.
	 */
	@Test
	void testGetArgsExcludesUnmappedColumns() {
		QBeanExWithConverter<FooDTO> projection = createProjection();
		List<Expression<?>> args = projection.getArgs();

		assertFalse(hasColumn(args, "created"), "created should not be in args (not in FooDTO)");
		assertFalse(hasColumn(args, "updated"), "updated should not be in args (not in FooDTO)");
	}

	/**
	 * Test newInstance with normal values.
	 * Verifies @PathBinder mapping and int to String built-in conversion.
	 * Foo.codeType is int, FooDTO.codeTypeX is String.
	 */
	@Test
	void testNewInstanceWithPathBinderAndTypeConversion() {
		QBeanExWithConverter<FooDTO> projection = createProjection();
		Object[] values = buildValues(projection);

		FooDTO dto = projection.newInstance(values);

		assertNotNull(dto);
		assertEquals(42, dto.getId());
		assertEquals("TEST_CODE", dto.getCode());
		assertEquals("Test Name", dto.getName());
		assertEquals("Some content", dto.getContent());
		assertEquals(Gender.MALE, dto.getGender());
		assertEquals(100, dto.getVolume());
		assertEquals(1, dto.getVersion());
		// codeType(int 7) to codeTypeX(String "7") via built-in converter
		assertEquals("7", dto.getCodeTypeX());
	}

	/**
	 * Test that null on a reference type field (String, Map, etc.) works fine.
	 */
	@Test
	void testNullOnReferenceFields() {
		QBeanExWithConverter<FooDTO> projection = createProjection();
		List<Expression<?>> args = projection.getArgs();
		Object[] values = new Object[args.size()];

		// Only set primitive int fields to avoid NPE on unboxing
		for (int i = 0; i < args.size(); i++) {
			Path<?> path = (Path<?>) args.get(i);
			if (path.getType() == int.class || path.getType() == Integer.class) {
				values[i] = 0;
			}
		}

		FooDTO dto = projection.newInstance(values);
		assertNotNull(dto);
		assertNull(dto.getCode());
		assertNull(dto.getName());
		assertNull(dto.getContent());
		assertNull(dto.getGender());
		assertNull(dto.getExt());
		assertNull(dto.getMap());
		assertNull(dto.getInDay());
		// codeType is int, value is 0, converter int to String gives "0"
		assertEquals("0", dto.getCodeTypeX());
	}

	/**
	 * Test that null passed to a primitive int field causes NullPointerException.
	 * This is consistent with QBeanEx behavior.
	 */
	@Test
	void testNullOnPrimitiveFieldThrowsNPE() {
		QBeanExWithConverter<FooDTO> projection = createProjection();
		Object[] values = new Object[projection.getArgs().size()];
		// All values are null, primitive int fields will NPE during unboxing
		assertThrows(NullPointerException.class, () -> projection.newInstance(values));
	}

	// ========== ConverterWrappedBean (Insert/Update) tests ==========

	/**
	 * Test ConverterWrappedBean + AdvancedMapper creates correct path-value map from DTO.
	 * Verifies @PathBinder name remapping: codeTypeX to codeType column.
	 */
	@Test
	void testConverterWrappedBeanBasicMapping() {
		AdvancedMapper mapper = new AdvancedMapper(1, false); // SCENARIO_INSERT

		FooDTO dto = new FooDTO();
		dto.setId(1);
		dto.setCode("ABC");
		dto.setName("Test");
		dto.setVolume(50);
		dto.setVersion(2);
		dto.setCodeTypeX("99");

		ConverterWrappedBean wrapped = ConverterWrappedBean.of(dto);
		Map<Path<?>, Object> map = mapper.createMap(fooPath, wrapped);

		assertNotNull(map);
		assertFalse(map.isEmpty());

		// Verify codeTypeX value is mapped to codeType path
		Path<?> codeTypePath = fooPath.getColumn("codeType");
		assertNotNull(codeTypePath);
		// Without writeConverter, the String "99" is passed as-is
		assertTrue(map.containsKey(codeTypePath), "codeType path should be in the map");
		assertEquals("99", map.get(codeTypePath));

		// Verify normal fields are mapped correctly
		Path<?> codePath = fooPath.getColumn("code");
		assertEquals("ABC", map.get(codePath));

		Path<?> namePath = fooPath.getColumn("name");
		assertEquals("Test", map.get(namePath));
	}

	/**
	 * Test that ConverterWrappedBean skips fields not present in the target table.
	 */
	@Test
	void testConverterWrappedBeanSkipsUnmatchedFields() {
		AdvancedMapper mapper = new AdvancedMapper(1, false);

		FooDTO dto = new FooDTO();
		dto.setId(1);
		dto.setCode("ABC");

		ConverterWrappedBean wrapped = ConverterWrappedBean.of(dto);
		Map<Path<?>, Object> map = mapper.createMap(fooPath, wrapped);

		// The map should only contain paths that exist in fooPath
		for (Path<?> key : map.keySet()) {
			assertNotNull(fooPath.getColumn(key.getMetadata().getName()),
					"All keys in map should be valid table columns");
		}
	}

	// ========== toValueExtractor (Batch Insert) tests ==========

	/**
	 * Test toValueExtractor produces values in effective column order (a subset of entity columns).
	 * Simulates the BatchProcessor scenario where only certain columns participate in the SQL.
	 */
	@Test
	void testToValueExtractorEffectiveColumnOrder() {
		FooDTO dto = new FooDTO();
		dto.setId(1);
		dto.setCode("BATCH-01");
		dto.setName("Batch Test");
		dto.setVolume(77);
		dto.setVersion(3);
		dto.setCodeTypeX("55");

		ConverterWrappedBean wrapped = ConverterWrappedBean.of(dto);

		// Simulate effective column order: a subset of entity columns (as BatchProcessor would produce)
		List<Path<?>> allColumns = fooPath.getColumns();
		// Pick a few columns in original order: code, name, volume, codeType
		java.util.List<Path<?>> effectivePaths = new java.util.ArrayList<>();
		for (Path<?> col : allColumns) {
			String name = col.getMetadata().getName();
			if ("code".equals(name) || "name".equals(name) || "volume".equals(name) || "codeType".equals(name)) {
				effectivePaths.add(col);
			}
		}

		ValueExtractor extractor = wrapped.toValueExtractor(fooPath, effectivePaths);

		// Extract values for the wrapped bean
		Object[] values = extractor.values(wrapped);
		assertEquals(effectivePaths.size(), values.length);

		// Verify values are in effective column order
		for (int i = 0; i < effectivePaths.size(); i++) {
			String colName = effectivePaths.get(i).getMetadata().getName();
			switch (colName) {
				case "code": assertEquals("BATCH-01", values[i]); break;
				case "name": assertEquals("Batch Test", values[i]); break;
				case "volume": assertEquals(77, values[i]); break;
				case "codeType": assertEquals("55", values[i]); break; // @PathBinder remapped from codeTypeX
			}
		}
	}

	/**
	 * Test toValueExtractor handles unmapped columns (DTO has no field for that entity column).
	 * In the effective column order output, unmapped columns produce null (the slot is null).
	 * Note: this differs from extractValues() which produces NOT_AVAILABLE for unmapped columns.
	 */
	@Test
	void testToValueExtractorUnmappedColumnsAreNull() {
		FooDTO dto = new FooDTO();
		dto.setCode("X");

		ConverterWrappedBean wrapped = ConverterWrappedBean.of(dto);

		// Include a column that DTO doesn't map to (e.g. "created" — FooDTO has no created field)
		List<Path<?>> allColumns = fooPath.getColumns();
		java.util.List<Path<?>> effectivePaths = new java.util.ArrayList<>();
		for (Path<?> col : allColumns) {
			String name = col.getMetadata().getName();
			if ("code".equals(name) || "created".equals(name)) {
				effectivePaths.add(col);
			}
		}

		ValueExtractor extractor = wrapped.toValueExtractor(fooPath, effectivePaths);
		Object[] values = extractor.values(wrapped);

		for (int i = 0; i < effectivePaths.size(); i++) {
			String colName = effectivePaths.get(i).getMetadata().getName();
			if ("code".equals(colName)) {
				assertEquals("X", values[i]);
			} else if ("created".equals(colName)) {
				assertNull(values[i], "Unmapped column in effective order should produce null");
			}
		}
	}

	/**
	 * Test that toValueExtractor caches the column index mapping (same effectivePaths instance).
	 */
	@Test
	void testToValueExtractorCaching() {
		FooDTO dto1 = new FooDTO();
		dto1.setCode("A");
		dto1.setVolume(1);

		FooDTO dto2 = new FooDTO();
		dto2.setCode("B");
		dto2.setVolume(2);

		ConverterWrappedBean wrapped1 = ConverterWrappedBean.of(dto1);

		List<Path<?>> allColumns = fooPath.getColumns();
		java.util.List<Path<?>> effectivePaths = new java.util.ArrayList<>();
		for (Path<?> col : allColumns) {
			String name = col.getMetadata().getName();
			if ("code".equals(name) || "volume".equals(name)) {
				effectivePaths.add(col);
			}
		}

		// Create extractor once, use for multiple beans
		ValueExtractor extractor = wrapped1.toValueExtractor(fooPath, effectivePaths);

		Object[] values1 = extractor.values(ConverterWrappedBean.of(dto1));
		Object[] values2 = extractor.values(ConverterWrappedBean.of(dto2));

		// Verify different beans produce different values
		assertEquals("A", values1[0]);
		assertEquals(1, values1[1]);
		assertEquals("B", values2[0]);
		assertEquals(2, values2[1]);
	}

	/**
	 * Test hasPathBinder detection.
	 */
	@Test
	void testHasPathBinder() {
		// FooDTO has @PathBinder on codeTypeX
		assertTrue(ConverterWrappedBean.hasPathBinder(FooDTO.class));
		// Foo entity does not have @PathBinder
		assertFalse(ConverterWrappedBean.hasPathBinder(Foo.class));
	}

	// ========== NOT_AVAILABLE sentinel tests ==========

	/**
	 * Test that extractValues produces NOT_AVAILABLE for columns the DTO does not map.
	 * This allows AdvancedMapper to skip those columns, letting database DEFAULT take effect.
	 */
	@Test
	void testExtractValuesProducesNotAvailableForUnmappedColumns() {
		FooDTO dto = new FooDTO();
		dto.setCode("ABC");
		dto.setName("Test");

		ConverterWrappedBean wrapped = ConverterWrappedBean.of(dto);
		Object[] values = wrapped.extractValues(fooPath);

		// FooDTO does not have "created" or "updated" fields — those positions should be NOT_AVAILABLE
		List<Path<?>> columns = fooPath.getColumns();
		for (int i = 0; i < columns.size(); i++) {
			String colName = columns.get(i).getMetadata().getName();
			if ("created".equals(colName) || "updated".equals(colName)) {
				assertSame(ConverterWrappedBean.NOT_AVAILABLE, values[i],
						"Column '" + colName + "' not mapped by DTO should be NOT_AVAILABLE");
			}
		}

		// FooDTO does have "code" and "name" — those should NOT be NOT_AVAILABLE
		for (int i = 0; i < columns.size(); i++) {
			String colName = columns.get(i).getMetadata().getName();
			if ("code".equals(colName)) {
				assertEquals("ABC", values[i]);
			} else if ("name".equals(colName)) {
				assertEquals("Test", values[i]);
			}
		}
	}

	/**
	 * Test that AdvancedMapper skips NOT_AVAILABLE columns when processing ConverterWrappedBean.
	 * The resulting map should not contain paths for unmapped DTO columns.
	 */
	@Test
	void testAdvancedMapperSkipsNotAvailableColumns() {
		AdvancedMapper mapper = new AdvancedMapper(1, false); // SCENARIO_INSERT

		FooDTO dto = new FooDTO();
		dto.setCode("SKIP-TEST");
		dto.setName("Only mapped");
		dto.setVolume(10);
		dto.setVersion(1);

		ConverterWrappedBean wrapped = ConverterWrappedBean.of(dto);
		Map<Path<?>, Object> map = mapper.createMap(fooPath, wrapped);

		// "created" and "updated" are not in FooDTO — they should NOT appear in the map
		Path<?> createdPath = fooPath.getColumn("created");
		Path<?> updatedPath = fooPath.getColumn("updated");
		assertFalse(map.containsKey(createdPath),
				"Unmapped column 'created' should not be in the map (database DEFAULT should apply)");
		assertFalse(map.containsKey(updatedPath),
				"Unmapped column 'updated' should not be in the map (database DEFAULT should apply)");

		// Mapped columns should be present
		Path<?> codePath = fooPath.getColumn("code");
		Path<?> namePath = fooPath.getColumn("name");
		assertEquals("SKIP-TEST", map.get(codePath));
		assertEquals("Only mapped", map.get(namePath));
	}

	/**
	 * Test that null values from mapped DTO fields are distinguishable from NOT_AVAILABLE.
	 * In extractValues(): mapped null → null, unmapped → NOT_AVAILABLE.
	 * In createMap(): NOT_AVAILABLE columns are excluded; null mapped fields go through
	 * normal null strategy (may or may not appear depending on mapper configuration).
	 */
	@Test
	void testNullMappedFieldVsNotAvailable() {
		FooDTO dto = new FooDTO();
		dto.setCode("NULL-TEST");
		dto.setGender(null); // mapped field, explicitly null
		dto.setVersion(1);

		ConverterWrappedBean wrapped = ConverterWrappedBean.of(dto);
		Object[] values = wrapped.extractValues(fooPath);

		List<Path<?>> columns = fooPath.getColumns();
		for (int i = 0; i < columns.size(); i++) {
			String colName = columns.get(i).getMetadata().getName();
			if ("gender".equals(colName)) {
				// Mapped field with null value → should be null (not NOT_AVAILABLE)
				assertNull(values[i], "Mapped field with null value should be null, not NOT_AVAILABLE");
			} else if ("created".equals(colName)) {
				// Unmapped field → should be NOT_AVAILABLE
				assertSame(ConverterWrappedBean.NOT_AVAILABLE, values[i],
						"Unmapped column should be NOT_AVAILABLE");
			}
		}
	}

	// ========== Helper methods ==========

	private QBeanExWithConverter<FooDTO> createProjection() {
		Map<String, Expression<?>> bindings = new LinkedHashMap<>();
		for (Path<?> p : fooPath.getColumns()) {
			bindings.put(p.getMetadata().getName(), p);
		}
		return new QBeanExWithConverter<>(FooDTO.class, bindings);
	}

	private Object[] buildValues(QBeanExWithConverter<FooDTO> projection) {
		List<Expression<?>> args = projection.getArgs();
		Object[] values = new Object[args.size()];
		for (int i = 0; i < args.size(); i++) {
			Path<?> path = (Path<?>) args.get(i);
			values[i] = testValueFor(path.getMetadata().getName());
		}
		return values;
	}

	private Object testValueFor(String fieldName) {
		switch (fieldName) {
			case "id": return 42;
			case "code": return "TEST_CODE";
			case "name": return "Test Name";
			case "content": return "Some content";
			case "gender": return Gender.MALE;
			case "ext": return null;
			case "map": return null;
			case "volume": return 100;
			case "version": return 1;
			case "codeType": return 7;
			case "inDay": return Date.valueOf("2024-01-15");
			default: return null;
		}
	}

	private boolean hasColumn(List<Expression<?>> args, String name) {
		return args.stream()
				.filter(e -> e instanceof Path)
				.anyMatch(e -> name.equals(((Path<?>) e).getMetadata().getName()));
	}
}
