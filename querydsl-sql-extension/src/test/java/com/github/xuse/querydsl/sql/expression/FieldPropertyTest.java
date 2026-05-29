package com.github.xuse.querydsl.sql.expression;

import static org.junit.jupiter.api.Assertions.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FieldProperty} and {@link FieldCollector}.
 * <p>
 * Verifies that FieldCollector scans all accessible getter/setter pairs
 * across class hierarchy, and that FieldProperty correctly exposes
 * field metadata.
 * </p>
 *
 * <p>Requirements: 2.7, 2.8</p>
 */
@DisplayName("FieldProperty and FieldCollector unit tests")
class FieldPropertyTest {

	// ==================== Test POJOs ====================

	public static class BaseEntity {
		private int baseId;
		private String baseName;

		public BaseEntity() {}

		public int getBaseId() { return baseId; }
		public void setBaseId(int baseId) { this.baseId = baseId; }
		public String getBaseName() { return baseName; }
		public void setBaseName(String baseName) { this.baseName = baseName; }
	}

	public static class ExtendedEntity extends BaseEntity {
		private String extField;
		private double extValue;

		public ExtendedEntity() {}

		public String getExtField() { return extField; }
		public void setExtField(String extField) { this.extField = extField; }
		public double getExtValue() { return extValue; }
		public void setExtValue(double extValue) { this.extValue = extValue; }
	}

	public static class ThreeLevelChild extends ExtendedEntity {
		private long deepField;

		public ThreeLevelChild() {}

		public long getDeepField() { return deepField; }
		public void setDeepField(long deepField) { this.deepField = deepField; }
	}

	public static class GetterOnlyBean {
		private String readOnly;

		public GetterOnlyBean() {}

		public String getReadOnly() { return readOnly; }
		// No setter
	}

	// ==================== FieldCollector Tests ====================

	@Nested
	@DisplayName("FieldCollector behavior")
	class FieldCollectorTests {

		@Test
		@DisplayName("fieldNames returns wildcard list ['*']")
		void testFieldNamesReturnsWildcard() {
			FieldCollector collector = new FieldCollector();
			List<String> names = collector.fieldNames();

			assertNotNull(names);
			assertEquals(1, names.size());
			assertEquals("*", names.get(0));
			assertSame(FieldCollector.ALL_FIELDS, names);
		}

		@Test
		@DisplayName("size returns default estimate of 8")
		void testSizeReturnsDefaultEstimate() {
			FieldCollector collector = new FieldCollector();
			assertEquals(8, collector.size());
		}

		@Test
		@DisplayName("names returns all keys from fieldOrder map")
		void testNamesReturnsAllFieldOrderKeys() {
			FieldCollector collector = new FieldCollector();

			Map<String, FieldProperty> fieldOrder = new HashMap<>();
			fieldOrder.put("id", createFieldProperty(BaseEntity.class, "baseId"));
			fieldOrder.put("name", createFieldProperty(BaseEntity.class, "baseName"));

			List<String> result = collector.names(fieldOrder);
			assertNotNull(result);
			assertEquals(2, result.size());
			assertTrue(result.contains("id"));
			assertTrue(result.contains("name"));
		}

		@Test
		@DisplayName("FieldCollector scans all accessible getter/setter pairs across class hierarchy")
		void testFieldCollectorScansHierarchy() {
			// Use BeanCodecManager with FieldCollector (getCodec with single arg)
			// to verify all fields from hierarchy are collected
			BeanCodec codec = BeanCodecManager.getInstance().getCodec(ExtendedEntity.class);

			assertNotNull(codec);
			Property[] fields = codec.getFields();
			assertNotNull(fields);

			// ExtendedEntity has 2 own fields + 2 from BaseEntity = 4 total
			// Introspector should find: baseId, baseName, extField, extValue
			List<String> fieldNames = new ArrayList<>();
			for (Property f : fields) {
				if (f.getName() != null) {
					fieldNames.add(f.getName());
				}
			}

			assertTrue(fieldNames.contains("baseId"), "Should contain parent field 'baseId'");
			assertTrue(fieldNames.contains("baseName"), "Should contain parent field 'baseName'");
			assertTrue(fieldNames.contains("extField"), "Should contain own field 'extField'");
			assertTrue(fieldNames.contains("extValue"), "Should contain own field 'extValue'");
			assertEquals(4, fieldNames.size(), "Should have exactly 4 fields from hierarchy");
		}

		@Test
		@DisplayName("FieldCollector scans three-level hierarchy correctly")
		void testFieldCollectorScansThreeLevelHierarchy() {
			BeanCodec codec = BeanCodecManager.getInstance().getCodec(ThreeLevelChild.class);

			assertNotNull(codec);
			Property[] fields = codec.getFields();
			assertNotNull(fields);

			List<String> fieldNames = new ArrayList<>();
			for (Property f : fields) {
				if (f.getName() != null) {
					fieldNames.add(f.getName());
				}
			}

			// ThreeLevelChild: deepField + ExtendedEntity: extField, extValue + BaseEntity: baseId, baseName = 5
			assertTrue(fieldNames.contains("baseId"), "Should contain grandparent field 'baseId'");
			assertTrue(fieldNames.contains("baseName"), "Should contain grandparent field 'baseName'");
			assertTrue(fieldNames.contains("extField"), "Should contain parent field 'extField'");
			assertTrue(fieldNames.contains("extValue"), "Should contain parent field 'extValue'");
			assertTrue(fieldNames.contains("deepField"), "Should contain own field 'deepField'");
			assertEquals(5, fieldNames.size(), "Should have exactly 5 fields from three-level hierarchy");
		}

		@Test
		@DisplayName("getType returns field type when field is present")
		void testGetTypeReturnsFieldType() {
			FieldCollector collector = new FieldCollector();
			FieldProperty prop = createFieldProperty(BaseEntity.class, "baseId");

			Class<?> type = collector.getType("baseId", prop);
			assertEquals(int.class, type);
		}

		@Test
		@DisplayName("getType returns getter return type when field is null")
		void testGetTypeReturnsGetterReturnTypeWhenFieldNull() throws Exception {
			FieldCollector collector = new FieldCollector();
			Method getter = BaseEntity.class.getMethod("getBaseId");
			FieldProperty prop = new FieldProperty(getter, null, null);

			Class<?> type = collector.getType("baseId", prop);
			assertEquals(int.class, type);
		}

		@Test
		@DisplayName("getType returns Object.class when property is null")
		void testGetTypeReturnsObjectClassWhenPropertyNull() {
			FieldCollector collector = new FieldCollector();
			Class<?> type = collector.getType("unknown", null);
			assertEquals(Object.class, type);
		}
	}

	// ==================== FieldProperty Tests ====================

	@Nested
	@DisplayName("FieldProperty behavior")
	class FieldPropertyTests {

		@Test
		@DisplayName("FieldProperty exposes getter, setter, and field correctly")
		void testFieldPropertyAccessors() {
			FieldProperty prop = createFieldProperty(BaseEntity.class, "baseId");

			assertNotNull(prop.getField());
			assertNotNull(prop.getGetter());
			assertNotNull(prop.getSetter());
			assertEquals("baseId", prop.getField().getName());
		}

		@Test
		@DisplayName("FieldProperty getName returns field name")
		void testGetNameReturnsFieldName() {
			FieldProperty prop = createFieldProperty(BaseEntity.class, "baseName");
			assertEquals("baseName", prop.getName());
		}

		@Test
		@DisplayName("FieldProperty getType returns field declared type")
		void testGetTypeReturnsFieldDeclaredType() {
			FieldProperty intProp = createFieldProperty(BaseEntity.class, "baseId");
			assertEquals(int.class, intProp.getType());

			FieldProperty stringProp = createFieldProperty(BaseEntity.class, "baseName");
			assertEquals(String.class, stringProp.getType());
		}

		@Test
		@DisplayName("FieldProperty bindingType can be set and retrieved")
		void testBindingTypeSetAndGet() {
			FieldProperty prop = createFieldProperty(BaseEntity.class, "baseId");
			assertNull(prop.getBindingType());

			prop.setBindingType(Integer.class);
			assertEquals(Integer.class, prop.getBindingType());
		}

		@Test
		@DisplayName("FieldProperty toString contains class and field info")
		void testToStringContainsInfo() {
			FieldProperty prop = createFieldProperty(BaseEntity.class, "baseId");
			String str = prop.toString();

			assertNotNull(str);
			assertFalse(str.isEmpty());
			assertTrue(str.contains("BaseEntity"));
			assertTrue(str.contains("baseId") || str.contains("getBaseId") || str.contains("setBaseId"));
		}

		@Test
		@DisplayName("FieldProperty with null field, getter, setter returns empty toString")
		void testToStringWithAllNullReturnsEmpty() {
			FieldProperty prop = new FieldProperty(null, null, null);
			assertEquals("", prop.toString());
		}

		@Test
		@DisplayName("FieldProperty getModifiers returns field modifiers")
		void testGetModifiersReturnsFieldModifiers() {
			FieldProperty prop = createFieldProperty(BaseEntity.class, "baseId");
			int modifiers = prop.getModifiers();
			// private field should have PRIVATE modifier
			assertTrue(java.lang.reflect.Modifier.isPrivate(modifiers));
		}

		@Test
		@DisplayName("FieldProperty getGenericType returns field generic type")
		void testGetGenericType() {
			FieldProperty prop = createFieldProperty(BaseEntity.class, "baseName");
			assertNotNull(prop.getGenericType());
			assertEquals(String.class, prop.getGenericType());
		}
	}

	// ==================== Helper methods ====================

	/**
	 * Create a FieldProperty by introspecting the given class for the named field.
	 */
	private FieldProperty createFieldProperty(Class<?> clazz, String fieldName) {
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor pd : props) {
				if (pd.getName().equals(fieldName)) {
					Field field = findField(clazz, fieldName);
					return new FieldProperty(pd.getReadMethod(), pd.getWriteMethod(), field);
				}
			}
			throw new RuntimeException("Property not found: " + fieldName + " in " + clazz.getName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Field findField(Class<?> clazz, String name) {
		Class<?> current = clazz;
		while (current != null) {
			try {
				return current.getDeclaredField(name);
			} catch (NoSuchFieldException e) {
				current = current.getSuperclass();
			}
		}
		return null;
	}
}
