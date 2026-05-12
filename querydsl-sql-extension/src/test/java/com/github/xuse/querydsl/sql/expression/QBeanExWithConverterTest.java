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
 * Tests for {@link QBeanExWithConverter} and {@link ConvertMapper}.
 * <p>
 * Verifies:
 * <ul>
 *   <li>Basic field mapping by same name</li>
 *   <li>{@code @PathBind} annotation for field name remapping</li>
 *   <li>Built-in type conversion (int → String via BuiltinConverters)</li>
 *   <li>Unmapped fields preserve DTO default values</li>
 *   <li>Null handling on primitive vs reference type fields</li>
 *   <li>ConvertMapper for Insert/Update with field name remapping</li>
 * </ul>
 */
class QBeanExWithConverterTest {

	private final RelationalPathEx<Foo> fooPath = PathCache.get(Foo.class, null);

	// ========== QBeanExWithConverter (Select) tests ==========

	/**
	 * Verify getArgs() contains only columns matching DTO fields,
	 * and includes codeType (mapped via @PathBind on codeTypeX).
	 */
	@Test
	void testGetArgsContainsMappedColumns() {
		QBeanExWithConverter<FooDTO> projection = createProjection();
		List<Expression<?>> args = projection.getArgs();

		assertNotNull(args);
		assertFalse(args.isEmpty());

		// codeType column should be present (mapped to codeTypeX via @PathBind)
		assertTrue(hasColumn(args, "codeType"), "codeType should be in getArgs() via @PathBind");
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
	 * Verifies @PathBind mapping and int→String built-in conversion.
	 * Foo.codeType is int, FooDTO.codeTypeX is String.
	 */
	@Test
	void testNewInstanceWithPathBindAndTypeConversion() {
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
		// codeType(int 7) → codeTypeX(String "7") via built-in converter
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
		// codeType is int, value is 0, converter int→String gives "0"
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

	// ========== ConvertMapper (Insert/Update) tests ==========

	/**
	 * Test ConvertMapper creates correct path-value map from DTO.
	 * Verifies @PathBind name remapping: codeTypeX → codeType column.
	 */
	@Test
	void testConvertMapperBasicMapping() {
		ConvertMapper mapper = ConvertMapper.of(FooDTO.class);

		FooDTO dto = new FooDTO();
		dto.setId(1);
		dto.setCode("ABC");
		dto.setName("Test");
		dto.setVolume(50);
		dto.setVersion(2);
		dto.setCodeTypeX("99");

		Map<Path<?>, Object> map = mapper.createMap(fooPath, dto);

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
	 * Test ConvertMapper skips null values.
	 */
	@Test
	void testConvertMapperSkipsNullValues() {
		ConvertMapper mapper = ConvertMapper.of(FooDTO.class);

		FooDTO dto = new FooDTO();
		dto.setId(1);
		dto.setCode("ABC");
		// name is null, should not appear in map

		Map<Path<?>, Object> map = mapper.createMap(fooPath, dto);

		Path<?> namePath = fooPath.getColumn("name");
		assertFalse(map.containsKey(namePath), "null fields should not be in the map");
	}

	/**
	 * Test ConvertMapper skips fields not present in the target table.
	 * FooDTO fields that don't exist in Foo table should be ignored.
	 */
	@Test
	void testConvertMapperSkipsUnmatchedFields() {
		ConvertMapper mapper = ConvertMapper.of(FooDTO.class);

		FooDTO dto = new FooDTO();
		dto.setId(1);
		dto.setCode("ABC");

		Map<Path<?>, Object> map = mapper.createMap(fooPath, dto);

		// The map should only contain paths that exist in fooPath
		for (Path<?> key : map.keySet()) {
			assertNotNull(fooPath.getColumn(key.getMetadata().getName()),
					"All keys in map should be valid table columns");
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
