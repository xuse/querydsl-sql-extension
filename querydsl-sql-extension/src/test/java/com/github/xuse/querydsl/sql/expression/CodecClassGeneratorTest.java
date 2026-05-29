package com.github.xuse.querydsl.sql.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CodecClassGenerator} — verifies ASM-generated BeanCodec
 * correctness for simple POJOs, inherited beans, converter-annotated fields,
 * and null handling.
 *
 * <p>Requirements: 2.1, 2.2</p>
 */
@DisplayName("CodecClassGenerator unit tests")
class CodecClassGeneratorTest {

	// ==================== Test POJOs (no Lombok, explicit getters/setters) ====================

	/**
	 * Simple POJO with 4 fields: primitive int, primitive double, String, and Integer (reference).
	 */
	public static class SimpleBean {
		private int id;
		private double score;
		private String name;
		private Integer count;

		public SimpleBean() {}

		public int getId() { return id; }
		public void setId(int id) { this.id = id; }
		public double getScore() { return score; }
		public void setScore(double score) { this.score = score; }
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public Integer getCount() { return count; }
		public void setCount(Integer count) { this.count = count; }
	}

	/**
	 * Parent bean with 2 fields.
	 */
	public static class ParentBean {
		private int parentId;
		private String parentName;

		public ParentBean() {}

		public int getParentId() { return parentId; }
		public void setParentId(int parentId) { this.parentId = parentId; }
		public String getParentName() { return parentName; }
		public void setParentName(String parentName) { this.parentName = parentName; }
	}

	/**
	 * Child bean extending ParentBean with 2 additional fields.
	 */
	public static class ChildBean extends ParentBean {
		private String childField;
		private long childValue;

		public ChildBean() {}

		public String getChildField() { return childField; }
		public void setChildField(String childField) { this.childField = childField; }
		public long getChildValue() { return childValue; }
		public void setChildValue(long childValue) { this.childValue = childValue; }
	}

	/**
	 * Bean with a field whose type differs from the binding type (simulates converter scenario).
	 * The field is Long but the binding type will be Integer (simulating a numeric type conversion).
	 */
	public static class ConverterBean {
		private int id;
		private Long convertedField;
		private String normalField;

		public ConverterBean() {}

		public int getId() { return id; }
		public void setId(int id) { this.id = id; }
		public Long getConvertedField() { return convertedField; }
		public void setConvertedField(Long convertedField) { this.convertedField = convertedField; }
		public String getNormalField() { return normalField; }
		public void setNormalField(String normalField) { this.normalField = normalField; }
	}

	// ==================== Tests ====================

	/**
	 * Test codec generation for a simple POJO with 4 fields (primitive and reference types).
	 * Verifies newInstance(values) followed by values(bean) returns array equal to original input.
	 */
	@Test
	@DisplayName("Generate codec for simple POJO with primitive and reference types")
	void testGenerateCodecForSimpleBean() {
		BeanCodec codec = getCodecForClass(SimpleBean.class, "id", "score", "name", "count");

		Object[] input = {42, 3.14, "hello", 100};
		Object bean = codec.newInstance(input);

		assertNotNull(bean);
		assertInstanceOf(SimpleBean.class, bean);

		SimpleBean sb = (SimpleBean) bean;
		assertEquals(42, sb.getId());
		assertEquals(3.14, sb.getScore());
		assertEquals("hello", sb.getName());
		assertEquals(100, sb.getCount());

		// Round-trip: values(bean) should return equivalent array
		Object[] output = codec.values(bean);
		assertNotNull(output);
		assertEquals(input.length, output.length);
		assertEquals(42, output[0]);
		assertEquals(3.14, output[1]);
		assertEquals("hello", output[2]);
		assertEquals(100, output[3]);
	}

	/**
	 * Test codec generation for inherited bean classes (parent + subclass fields).
	 * Verifies that both parent and child fields are accessible via the generated codec.
	 */
	@Test
	@DisplayName("Generate codec for inherited bean with parent and child fields")
	void testGenerateCodecForInheritedBean() {
		BeanCodec codec = getCodecForClass(ChildBean.class,
				"parentId", "parentName", "childField", "childValue");

		Object[] input = {10, "parent", "child", 999L};
		Object bean = codec.newInstance(input);

		assertNotNull(bean);
		assertInstanceOf(ChildBean.class, bean);

		ChildBean cb = (ChildBean) bean;
		assertEquals(10, cb.getParentId());
		assertEquals("parent", cb.getParentName());
		assertEquals("child", cb.getChildField());
		assertEquals(999L, cb.getChildValue());

		// Round-trip verification
		Object[] output = codec.values(bean);
		assertEquals(4, output.length);
		assertEquals(10, output[0]);
		assertEquals("parent", output[1]);
		assertEquals("child", output[2]);
		assertEquals(999L, output[3]);
	}

	/**
	 * Test codec with converter-annotated fields (binding type differs from field type).
	 * When the binding type is Integer but the field type is Long, the codec should
	 * handle the numeric type conversion via the ASM-generated tryTypeConvert logic.
	 */
	@Test
	@DisplayName("Generate codec with converter-annotated fields (numeric type conversion)")
	void testGenerateCodecWithConverterFields() {
		// Create a binding where 'convertedField' has binding type Integer
		// but the actual field type is Long. The codec handles Number-to-Number conversion.
		List<String> fieldNames = Arrays.asList("id", "convertedField", "normalField");
		Map<String, Class<?>> typeMap = new HashMap<>();
		typeMap.put("id", int.class);
		typeMap.put("convertedField", Integer.class); // binding type differs from field type (Long)
		typeMap.put("normalField", String.class);

		BindingProvider bindings = createBindingProvider(fieldNames, typeMap);
		BeanCodec codec = BeanCodecManager.getInstance().getCodec(ConverterBean.class, bindings);

		assertNotNull(codec);

		// The codec should convert Integer value to Long for the convertedField
		// The ASM tryTypeConvert handles Integer -> Long via Number.longValue()
		Object[] input = {1, 42, "normal"};
		Object bean = codec.newInstance(input);

		assertNotNull(bean);
		assertInstanceOf(ConverterBean.class, bean);

		ConverterBean cb = (ConverterBean) bean;
		assertEquals(1, cb.getId());
		// The Integer 42 should be converted to Long 42L via Number.longValue()
		assertEquals(42L, cb.getConvertedField());
		assertEquals("normal", cb.getNormalField());
	}

	/**
	 * Test null field handling in generated codec.
	 * Reference type fields should accept null; primitive fields with null cause NPE.
	 */
	@Test
	@DisplayName("Codec handles null values for reference type fields")
	void testCodecHandlesNullFields() {
		BeanCodec codec = getCodecForClass(SimpleBean.class, "id", "score", "name", "count");

		// Set reference fields to null, primitives to valid values
		Object[] input = {0, 0.0, null, null};
		Object bean = codec.newInstance(input);

		assertNotNull(bean);
		SimpleBean sb = (SimpleBean) bean;
		assertEquals(0, sb.getId());
		assertEquals(0.0, sb.getScore());
		assertNull(sb.getName());
		assertNull(sb.getCount());

		// Round-trip with nulls
		Object[] output = codec.values(bean);
		assertEquals(0, output[0]);
		assertEquals(0.0, output[1]);
		assertNull(output[2]);
		assertNull(output[3]);
	}

	/**
	 * Test that null on a primitive field causes NullPointerException during unboxing.
	 */
	@Test
	@DisplayName("Codec throws NPE when null is passed to primitive field")
	void testCodecNullOnPrimitiveFieldThrowsNPE() {
		BeanCodec codec = getCodecForClass(SimpleBean.class, "id", "score", "name", "count");

		// null for primitive int field 'id' should cause NPE
		Object[] input = {null, 0.0, "test", 1};
		assertThrows(NullPointerException.class, () -> codec.newInstance(input));
	}

	/**
	 * Verify generated codec get/set operations produce correct values.
	 * Tests the sets() method which assigns values to an existing bean.
	 */
	@Test
	@DisplayName("Codec sets() assigns values to existing bean correctly")
	void testCodecSetsOperation() {
		BeanCodec codec = getCodecForClass(SimpleBean.class, "id", "score", "name", "count");

		SimpleBean bean = new SimpleBean();
		bean.setId(1);
		bean.setName("original");

		Object[] newValues = {99, 2.71, "updated", 50};
		codec.sets(newValues, bean);

		assertEquals(99, bean.getId());
		assertEquals(2.71, bean.getScore());
		assertEquals("updated", bean.getName());
		assertEquals(50, bean.getCount());
	}

	/**
	 * Verify generated codec copy() operation copies fields between beans.
	 */
	@Test
	@DisplayName("Codec copy() copies fields between beans of same type")
	void testCodecCopyOperation() {
		BeanCodec codec = getCodecForClass(SimpleBean.class, "id", "score", "name", "count");

		SimpleBean source = new SimpleBean();
		source.setId(7);
		source.setScore(9.8);
		source.setName("source");
		source.setCount(33);

		SimpleBean target = new SimpleBean();
		codec.copy(source, target);

		assertEquals(7, target.getId());
		assertEquals(9.8, target.getScore());
		assertEquals("source", target.getName());
		assertEquals(33, target.getCount());
	}

	// ==================== Helper methods ====================

	/**
	 * Create a BeanCodec for the given class with specified field names.
	 * Uses BeanCodecManager which internally invokes CodecClassGenerator.
	 */
	private BeanCodec getCodecForClass(Class<?> clazz, String... fields) {
		List<String> fieldNames = new ArrayList<>();
		Map<String, Class<?>> typeMap = new HashMap<>();
		for (String name : fields) {
			fieldNames.add(name);
			try {
				typeMap.put(name, clazz.getDeclaredField(name).getType());
			} catch (NoSuchFieldException e) {
				// Field might be in superclass
				try {
					typeMap.put(name, findFieldInHierarchy(clazz, name).getType());
				} catch (NoSuchFieldException ex) {
					throw new RuntimeException("Field not found: " + name, ex);
				}
			}
		}

		BindingProvider bindings = createBindingProvider(fieldNames, typeMap);
		return BeanCodecManager.getInstance().getCodec(clazz, bindings);
	}

	private java.lang.reflect.Field findFieldInHierarchy(Class<?> clazz, String name) throws NoSuchFieldException {
		Class<?> current = clazz;
		while (current != null) {
			try {
				return current.getDeclaredField(name);
			} catch (NoSuchFieldException e) {
				current = current.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}

	private BindingProvider createBindingProvider(List<String> fieldNames, Map<String, Class<?>> typeMap) {
		return new BindingProvider() {
			@Override
			public List<String> fieldNames() {
				return fieldNames;
			}

			@Override
			public int size() {
				return fieldNames.size();
			}

			@Override
			public List<String> names(Map<String, FieldProperty> fieldOrder) {
				return fieldNames;
			}

			@Override
			public Class<?> getType(String name, FieldProperty property) {
				return typeMap.get(name);
			}
		};
	}
}
