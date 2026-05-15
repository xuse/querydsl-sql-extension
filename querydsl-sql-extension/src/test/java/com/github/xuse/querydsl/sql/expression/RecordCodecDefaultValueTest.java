package com.github.xuse.querydsl.sql.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.util.IOUtils;
import com.github.xuse.querydsl.util.lang.JDKEnvironment;

/**
 * Tests for {@link CodecClassGenerator#pushDefaultValueOnStack} correctness.
 * <p>
 * This test verifies that when a Record codec is generated with only a subset of fields
 * bound (partial projection), the unbound primitive fields receive correct default values
 * (0, 0L, 0.0, 0.0f, false, (byte)0, '\0', (short)0) and unbound reference fields receive null.
 * <p>
 * The bug (now fixed) was that {@code pushDefaultValueOnStack} used the field's <b>name</b>
 * instead of the field's <b>type name</b> to determine which JVM constant instruction to emit.
 * This caused incorrect bytecode when a field name's hash ({@code name.length() + name.charAt(0)})
 * collided with a different primitive type's hash.
 * <p>
 * Requires JDK 16+ (Record support).
 */
public class RecordCodecDefaultValueTest {

	private static final String RECORD_CLASS_NAME = "io.github.xuse.demo.RecordAllPrimitives";

	/**
	 * Test that a Record codec with only 'stringField' bound correctly initializes
	 * all primitive fields to their default values.
	 */
	@Test
	public void testRecordPartialBinding_allPrimitivesDefault() {
		Assumptions.assumeTrue(JDKEnvironment.JVM_VERSION >= 16, "Requires JDK 16+ for Record support");

		Class<?> recordClz = loadRecordClass();
		assertNotNull(recordClz, "RecordAllPrimitives.classdata should be loadable");

		// Create a codec with only 'stringField' bound — all primitive fields will use defaults
		BeanCodec codec = createPartialCodec(recordClz, "stringField");

		// newInstance with only the bound field value
		Object instance = codec.newInstance(new Object[]{"hello"});
		assertNotNull(instance);

		// Verify all fields via accessor methods (Record components)
		assertEquals(0, invokeGetter(instance, "intField"), "int default should be 0");
		assertEquals(0L, invokeGetter(instance, "longField"), "long default should be 0L");
		assertEquals(0.0, invokeGetter(instance, "doubleField"), "double default should be 0.0");
		assertEquals(0.0f, invokeGetter(instance, "floatField"), "float default should be 0.0f");
		assertFalse((Boolean) invokeGetter(instance, "booleanField"), "boolean default should be false");
		assertEquals((byte) 0, invokeGetter(instance, "byteField"), "byte default should be 0");
		assertEquals('\0', invokeGetter(instance, "charField"), "char default should be '\\0'");
		assertEquals((short) 0, invokeGetter(instance, "shortField"), "short default should be 0");
		assertEquals("hello", invokeGetter(instance, "stringField"), "stringField should be 'hello'");
	}

	/**
	 * Test that binding only primitive fields works correctly — the unbound String field
	 * should be null.
	 */
	@Test
	public void testRecordPartialBinding_referenceFieldDefault() {
		Assumptions.assumeTrue(JDKEnvironment.JVM_VERSION >= 16, "Requires JDK 16+ for Record support");

		Class<?> recordClz = loadRecordClass();
		assertNotNull(recordClz);

		// Bind only 'intField' — stringField should default to null
		BeanCodec codec = createPartialCodec(recordClz, "intField");

		Object instance = codec.newInstance(new Object[]{42});
		assertNotNull(instance);

		assertEquals(42, invokeGetter(instance, "intField"));
		assertNull(invokeGetter(instance, "stringField"), "unbound String field should be null");
		assertEquals(0L, invokeGetter(instance, "longField"));
		assertEquals(0.0, invokeGetter(instance, "doubleField"));
		assertEquals(0.0f, invokeGetter(instance, "floatField"));
	}

	/**
	 * Test binding multiple primitive fields of different types simultaneously.
	 * Unbound fields should still get correct defaults.
	 */
	@Test
	public void testRecordPartialBinding_multiplePrimitives() {
		Assumptions.assumeTrue(JDKEnvironment.JVM_VERSION >= 16, "Requires JDK 16+ for Record support");

		Class<?> recordClz = loadRecordClass();
		assertNotNull(recordClz);

		// Bind longField and doubleField — others should default
		BeanCodec codec = createPartialCodec(recordClz, "longField", "doubleField");

		Object instance = codec.newInstance(new Object[]{123456789L, 3.14});
		assertNotNull(instance);

		assertEquals(123456789L, invokeGetter(instance, "longField"));
		assertEquals(3.14, invokeGetter(instance, "doubleField"));
		assertEquals(0, invokeGetter(instance, "intField"), "unbound int should be 0");
		assertEquals(0.0f, invokeGetter(instance, "floatField"), "unbound float should be 0.0f");
		assertFalse((Boolean) invokeGetter(instance, "booleanField"), "unbound boolean should be false");
		assertNull(invokeGetter(instance, "stringField"), "unbound String should be null");
	}

	/**
	 * Test binding all fields — no defaults should be needed.
	 */
	@Test
	public void testRecordFullBinding() {
		Assumptions.assumeTrue(JDKEnvironment.JVM_VERSION >= 16, "Requires JDK 16+ for Record support");

		Class<?> recordClz = loadRecordClass();
		assertNotNull(recordClz);

		// Bind all fields
		BeanCodec codec = createPartialCodec(recordClz,
				"intField", "longField", "doubleField", "floatField",
				"booleanField", "byteField", "charField", "shortField", "stringField");

		Object instance = codec.newInstance(new Object[]{
				1, 2L, 3.0, 4.0f, true, (byte) 5, 'A', (short) 6, "test"
		});
		assertNotNull(instance);

		assertEquals(1, invokeGetter(instance, "intField"));
		assertEquals(2L, invokeGetter(instance, "longField"));
		assertEquals(3.0, invokeGetter(instance, "doubleField"));
		assertEquals(4.0f, invokeGetter(instance, "floatField"));
		assertEquals(true, invokeGetter(instance, "booleanField"));
		assertEquals((byte) 5, invokeGetter(instance, "byteField"));
		assertEquals('A', invokeGetter(instance, "charField"));
		assertEquals((short) 6, invokeGetter(instance, "shortField"));
		assertEquals("test", invokeGetter(instance, "stringField"));
	}

	// ==================== Helper methods ====================

	/**
	 * Load the Record class into the same ClassLoaderAccessor used by BeanCodecManager,
	 * so that the generated codec classes can reference it via NEW instruction.
	 */
	private Class<?> loadRecordClass() {
		URL clzUrl = getClass().getResource("/RecordAllPrimitives.classdata");
		if (clzUrl == null) {
			return null;
		}
		byte[] classBytes = IOUtils.toByteArray(clzUrl);
		// Use the same ClassLoaderAccessor that BeanCodecManager uses for codec generation.
		// This ensures the generated codec class can resolve the Record class at runtime.
		ClassLoaderAccessor cl = getBeanCodecManagerClassLoader();
		try {
			return cl.loadClass(RECORD_CLASS_NAME);
		} catch (ClassNotFoundException e) {
			// Not yet defined — define it now
			return cl.defineClz(RECORD_CLASS_NAME, classBytes);
		}
	}

	private ClassLoaderAccessor getBeanCodecManagerClassLoader() {
		try {
			Field clField = BeanCodecManager.class.getDeclaredField("cl");
			clField.setAccessible(true);
			return (ClassLoaderAccessor) clField.get(BeanCodecManager.getInstance());
		} catch (Exception e) {
			throw new RuntimeException("Cannot access BeanCodecManager.cl field", e);
		}
	}

	/**
	 * Create a BeanCodec for the given Record class with only the specified fields bound.
	 * This simulates a partial projection query where not all Record fields are selected.
	 */
	private BeanCodec createPartialCodec(Class<?> recordClz, String... boundFields) {
		// Build a BindingProvider that only includes the specified fields
		List<String> fieldNames = new ArrayList<>();
		Map<String, Class<?>> typeMap = new HashMap<>();
		for (String name : boundFields) {
			fieldNames.add(name);
			try {
				typeMap.put(name, recordClz.getDeclaredField(name).getType());
			} catch (NoSuchFieldException e) {
				throw new RuntimeException("Field not found: " + name, e);
			}
		}

		BindingProvider bindings = new BindingProvider() {
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

		return BeanCodecManager.getInstance().getCodec(recordClz, bindings);
	}

	private Object invokeGetter(Object instance, String fieldName) {
		try {
			Method getter = instance.getClass().getDeclaredMethod(fieldName);
			return getter.invoke(instance);
		} catch (Exception e) {
			throw new RuntimeException("Failed to invoke getter for: " + fieldName, e);
		}
	}
}
