package com.github.xuse.querydsl.sql.expression;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ReflectCodec} — verifies that the reflection-based codec
 * produces identical results to the ASM-generated BeanCodec for newInstance,
 * values, sets, and copy operations.
 *
 * <p>Requirements: 2.11</p>
 */
@DisplayName("ReflectCodec unit tests")
class ReflectCodecTest {

	// ==================== Test POJOs ====================

	public static class SampleBean {
		private int id;
		private String name;
		private Double score;
		private long timestamp;

		public SampleBean() {}

		public int getId() { return id; }
		public void setId(int id) { this.id = id; }
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public Double getScore() { return score; }
		public void setScore(Double score) { this.score = score; }
		public long getTimestamp() { return timestamp; }
		public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
	}

	public static class ParentBean {
		private int parentId;
		private String parentName;

		public ParentBean() {}

		public int getParentId() { return parentId; }
		public void setParentId(int parentId) { this.parentId = parentId; }
		public String getParentName() { return parentName; }
		public void setParentName(String parentName) { this.parentName = parentName; }
	}

	public static class ChildBean extends ParentBean {
		private String childField;
		private long childValue;

		public ChildBean() {}

		public String getChildField() { return childField; }
		public void setChildField(String childField) { this.childField = childField; }
		public long getChildValue() { return childValue; }
		public void setChildValue(long childValue) { this.childValue = childValue; }
	}

	// ==================== Tests: Equivalence with ASM codec ====================

	@Nested
	@DisplayName("Equivalence with ASM-generated BeanCodec")
	class EquivalenceTests {

		@Test
		@DisplayName("newInstance produces identical bean to ASM codec")
		void testNewInstanceEquivalence() {
			String[] fields = {"id", "name", "score", "timestamp"};
			BeanCodec asmCodec = getAsmCodec(SampleBean.class, fields);
			ReflectCodec reflectCodec = createReflectCodec(SampleBean.class, fields);

			Object[] input = {42, "hello", 3.14, 1000L};

			Object asmBean = asmCodec.newInstance(input);
			Object reflectBean = reflectCodec.newInstance(input);

			assertNotNull(asmBean);
			assertNotNull(reflectBean);
			assertInstanceOf(SampleBean.class, asmBean);
			assertInstanceOf(SampleBean.class, reflectBean);

			SampleBean asmResult = (SampleBean) asmBean;
			SampleBean reflectResult = (SampleBean) reflectBean;

			assertEquals(asmResult.getId(), reflectResult.getId());
			assertEquals(asmResult.getName(), reflectResult.getName());
			assertEquals(asmResult.getScore(), reflectResult.getScore());
			assertEquals(asmResult.getTimestamp(), reflectResult.getTimestamp());
		}

		@Test
		@DisplayName("values produces identical array to ASM codec")
		void testValuesEquivalence() {
			String[] fields = {"id", "name", "score", "timestamp"};
			BeanCodec asmCodec = getAsmCodec(SampleBean.class, fields);
			ReflectCodec reflectCodec = createReflectCodec(SampleBean.class, fields);

			SampleBean bean = new SampleBean();
			bean.setId(99);
			bean.setName("world");
			bean.setScore(2.71);
			bean.setTimestamp(5000L);

			Object[] asmValues = asmCodec.values(bean);
			Object[] reflectValues = reflectCodec.values(bean);

			assertArrayEquals(asmValues, reflectValues);
		}

		@Test
		@DisplayName("sets produces identical bean state to ASM codec")
		void testSetsEquivalence() {
			String[] fields = {"id", "name", "score", "timestamp"};
			BeanCodec asmCodec = getAsmCodec(SampleBean.class, fields);
			ReflectCodec reflectCodec = createReflectCodec(SampleBean.class, fields);

			Object[] newValues = {77, "updated", 1.41, 9999L};

			SampleBean asmBean = new SampleBean();
			SampleBean reflectBean = new SampleBean();

			asmCodec.sets(newValues, asmBean);
			reflectCodec.sets(newValues, reflectBean);

			assertEquals(asmBean.getId(), reflectBean.getId());
			assertEquals(asmBean.getName(), reflectBean.getName());
			assertEquals(asmBean.getScore(), reflectBean.getScore());
			assertEquals(asmBean.getTimestamp(), reflectBean.getTimestamp());
		}

		@Test
		@DisplayName("copy produces identical target bean to ASM codec")
		void testCopyEquivalence() {
			String[] fields = {"id", "name", "score", "timestamp"};
			BeanCodec asmCodec = getAsmCodec(SampleBean.class, fields);
			ReflectCodec reflectCodec = createReflectCodec(SampleBean.class, fields);

			SampleBean source = new SampleBean();
			source.setId(55);
			source.setName("source");
			source.setScore(6.28);
			source.setTimestamp(12345L);

			SampleBean asmTarget = new SampleBean();
			SampleBean reflectTarget = new SampleBean();

			asmCodec.copy(source, asmTarget);
			reflectCodec.copy(source, reflectTarget);

			assertEquals(asmTarget.getId(), reflectTarget.getId());
			assertEquals(asmTarget.getName(), reflectTarget.getName());
			assertEquals(asmTarget.getScore(), reflectTarget.getScore());
			assertEquals(asmTarget.getTimestamp(), reflectTarget.getTimestamp());
		}

		@Test
		@DisplayName("Inherited bean: newInstance and values produce identical results")
		void testInheritedBeanEquivalence() {
			String[] fields = {"parentId", "parentName", "childField", "childValue"};
			BeanCodec asmCodec = getAsmCodec(ChildBean.class, fields);
			ReflectCodec reflectCodec = createReflectCodec(ChildBean.class, fields);

			Object[] input = {10, "parent", "child", 999L};

			Object asmBean = asmCodec.newInstance(input);
			Object reflectBean = reflectCodec.newInstance(input);

			// Verify values() equivalence
			Object[] asmValues = asmCodec.values(asmBean);
			Object[] reflectValues = reflectCodec.values(reflectBean);
			assertArrayEquals(asmValues, reflectValues);
		}
	}

	// ==================== Tests: ReflectCodec standalone operations ====================

	@Nested
	@DisplayName("ReflectCodec standalone operations")
	class StandaloneTests {

		@Test
		@DisplayName("newInstance creates bean with correct field values")
		void testNewInstance() {
			String[] fields = {"id", "name", "score", "timestamp"};
			ReflectCodec codec = createReflectCodec(SampleBean.class, fields);

			Object[] input = {1, "test", 9.99, 2000L};
			Object bean = codec.newInstance(input);

			assertNotNull(bean);
			assertInstanceOf(SampleBean.class, bean);

			SampleBean sb = (SampleBean) bean;
			assertEquals(1, sb.getId());
			assertEquals("test", sb.getName());
			assertEquals(9.99, sb.getScore());
			assertEquals(2000L, sb.getTimestamp());
		}

		@Test
		@DisplayName("values extracts all field values in correct order")
		void testValues() {
			String[] fields = {"id", "name", "score", "timestamp"};
			ReflectCodec codec = createReflectCodec(SampleBean.class, fields);

			SampleBean bean = new SampleBean();
			bean.setId(5);
			bean.setName("extract");
			bean.setScore(4.0);
			bean.setTimestamp(3000L);

			Object[] values = codec.values(bean);
			assertEquals(4, values.length);
			assertEquals(5, values[0]);
			assertEquals("extract", values[1]);
			assertEquals(4.0, values[2]);
			assertEquals(3000L, values[3]);
		}

		@Test
		@DisplayName("sets assigns values to existing bean")
		void testSets() {
			String[] fields = {"id", "name", "score", "timestamp"};
			ReflectCodec codec = createReflectCodec(SampleBean.class, fields);

			SampleBean bean = new SampleBean();
			Object[] values = {88, "assigned", 7.77, 4000L};
			codec.sets(values, bean);

			assertEquals(88, bean.getId());
			assertEquals("assigned", bean.getName());
			assertEquals(7.77, bean.getScore());
			assertEquals(4000L, bean.getTimestamp());
		}

		@Test
		@DisplayName("copy transfers all fields from source to target")
		void testCopy() {
			String[] fields = {"id", "name", "score", "timestamp"};
			ReflectCodec codec = createReflectCodec(SampleBean.class, fields);

			SampleBean source = new SampleBean();
			source.setId(11);
			source.setName("copied");
			source.setScore(1.23);
			source.setTimestamp(6000L);

			SampleBean target = new SampleBean();
			codec.copy(source, target);

			assertEquals(11, target.getId());
			assertEquals("copied", target.getName());
			assertEquals(1.23, target.getScore());
			assertEquals(6000L, target.getTimestamp());
		}

		@Test
		@DisplayName("newInstance handles null reference type fields")
		void testNewInstanceWithNulls() {
			String[] fields = {"id", "name", "score", "timestamp"};
			ReflectCodec codec = createReflectCodec(SampleBean.class, fields);

			Object[] input = {0, null, null, 0L};
			Object bean = codec.newInstance(input);

			SampleBean sb = (SampleBean) bean;
			assertEquals(0, sb.getId());
			assertNull(sb.getName());
			assertNull(sb.getScore());
			assertEquals(0L, sb.getTimestamp());
		}

		@Test
		@DisplayName("Round-trip: newInstance then values returns equivalent array")
		void testRoundTrip() {
			String[] fields = {"id", "name", "score", "timestamp"};
			ReflectCodec codec = createReflectCodec(SampleBean.class, fields);

			Object[] input = {33, "roundtrip", 5.55, 7777L};
			Object bean = codec.newInstance(input);
			Object[] output = codec.values(bean);

			assertArrayEquals(input, output);
		}
	}

	// ==================== Helper methods ====================

	/**
	 * Get the ASM-generated BeanCodec via BeanCodecManager for comparison.
	 */
	private BeanCodec getAsmCodec(Class<?> clazz, String... fieldNames) {
		List<String> names = Arrays.asList(fieldNames);
		Map<String, Class<?>> typeMap = buildTypeMap(clazz, fieldNames);
		BindingProvider bindings = createBindingProvider(names, typeMap);
		return BeanCodecManager.getInstance().getCodec(clazz, bindings);
	}

	/**
	 * Create a ReflectCodec with the same field ordering as the ASM codec would use.
	 */
	private ReflectCodec createReflectCodec(Class<?> clazz, String... fieldNames) {
		List<FieldProperty> properties = collectFieldProperties(clazz, fieldNames);
		return new ReflectCodec(clazz, properties);
	}

	/**
	 * Collect FieldProperty instances for the given class and field names,
	 * mimicking the same logic used by BeanCodecDefaultProvider.initMethods.
	 */
	private List<FieldProperty> collectFieldProperties(Class<?> clazz, String... fieldNames) {
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
			Map<String, FieldProperty> maps = new HashMap<>();
			for (PropertyDescriptor p : props) {
				if (p.getReadMethod() == null || p.getReadMethod().getDeclaringClass() == Object.class) {
					continue;
				}
				String name = p.getName();
				Method getter = p.getReadMethod();
				Method setter = p.getWriteMethod();
				Field field = findFieldInHierarchy(clazz, name);
				maps.put(name, new FieldProperty(getter, setter, field));
			}

			List<FieldProperty> result = new ArrayList<>();
			for (String name : fieldNames) {
				FieldProperty prop = maps.get(name);
				if (prop != null) {
					result.add(prop);
				} else {
					result.add(new FieldProperty(null, null, null));
				}
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, Class<?>> buildTypeMap(Class<?> clazz, String... fieldNames) {
		Map<String, Class<?>> typeMap = new HashMap<>();
		for (String name : fieldNames) {
			Field field = findFieldInHierarchy(clazz, name);
			if (field != null) {
				typeMap.put(name, field.getType());
			}
		}
		return typeMap;
	}

	private Field findFieldInHierarchy(Class<?> clazz, String name) {
		Class<?> current = clazz;
		while (current != null && current != Object.class) {
			try {
				return current.getDeclaredField(name);
			} catch (NoSuchFieldException e) {
				current = current.getSuperclass();
			}
		}
		return null;
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
