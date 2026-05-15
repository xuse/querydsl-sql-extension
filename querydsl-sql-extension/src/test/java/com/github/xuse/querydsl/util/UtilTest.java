package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Util} reflection utilities.
 */
class UtilTest {

	// Test class hierarchy
	interface TestInterface {
		default String defaultMethod() { return "default"; }
	}

	static class Parent implements TestInterface {
		private String parentField;
		protected int parentInt;
		public String getParentField() { return parentField; }
	}

	static class Child extends Parent {
		private String childField;
		private List<String> genericField;
		public String getChildField() { return childField; }
		public List<String> getGenericField() { return genericField; }
	}

	@Test
	void testFindField_byName() {
		Field f = Util.findField(Child.class, "childField");
		assertNotNull(f);
		assertEquals("childField", f.getName());
	}

	@Test
	void testFindField_inherited() {
		Field f = Util.findField(Child.class, "parentField");
		assertNotNull(f);
		assertEquals("parentField", f.getName());
	}

	@Test
	void testFindField_byNameAndType() {
		Field f = Util.findField(Child.class, "parentInt", int.class);
		assertNotNull(f);
	}

	@Test
	void testFindField_notFound() {
		Field f = Util.findField(Child.class, "nonExistent");
		assertNull(f);
	}

	@Test
	void testFindMethod_byName() {
		Method m = Util.findMethod(Child.class, "getChildField");
		assertNotNull(m);
	}

	@Test
	void testFindMethod_inherited() {
		Method m = Util.findMethod(Child.class, "getParentField");
		assertNotNull(m);
	}

	@Test
	void testFindMethod_withParams() {
		Method m = Util.findMethod(String.class, "substring", int.class);
		assertNotNull(m);
	}

	@Test
	void testFindMethod_notFound() {
		Method m = Util.findMethod(Child.class, "nonExistentMethod");
		assertNull(m);
	}

	@Test
	void testGetDeclaredFields() {
		Field[] fields = Util.getDeclaredFields(Child.class);
		assertNotNull(fields);
		assertTrue(fields.length >= 2);
	}

	@Test
	void testGetDeclaredFields_empty() {
		// Object class has no declared fields
		Field[] fields = Util.getDeclaredFields(Runnable.class);
		assertNotNull(fields);
		assertEquals(0, fields.length);
	}

	@Test
	void testGetDeclaredMethods() {
		Method[] methods = Util.getDeclaredMethods(Child.class);
		assertNotNull(methods);
		assertTrue(methods.length >= 2);
	}

	@Test
	void testGetDeclaredMethods_withDefaultMethods() {
		// getDeclaredMethods includes interface default methods
		Method[] methods = Util.getDeclaredMethods(Child.class);
		assertNotNull(methods);
		// Should have at least the declared methods of Child
		assertTrue(methods.length >= 2);
	}

	@Test
	void testGetField() {
		Child child = new Child();
		Field f = Util.findField(Child.class, "childField");
		f.setAccessible(true);
		// Initially null
		Object value = Util.getField(f, child);
		assertNull(value);
	}

	@Test
	void testInvokeMethod() {
		Child child = new Child();
		Method m = Util.findMethod(Child.class, "getChildField");
		Object result = Util.invokeMethod(m, child);
		assertNull(result); // field is null
	}

	@Test
	void testGetMethodType() {
		Type type = Util.getMethodType(Child.class, "getGenericField");
		assertNotNull(type);
		assertTrue(type.toString().contains("List"));
	}

	@Test
	void testGetMethodType_notFound() {
		Type type = Util.getMethodType(Child.class, "nonExistent");
		assertNull(type);
	}

	@Test
	void testGetFieldType() {
		Type type = Util.getFieldType(Child.class, "genericField");
		assertNotNull(type);
		assertTrue(type.toString().contains("List"));
	}

	@Test
	void testGetFieldType_notFound() {
		Type type = Util.getFieldType(Child.class, "nonExistent");
		assertNull(type);
	}

	@Test
	void testGetAllInterfacesForClassAsSet() {
		Set<Class<?>> interfaces = Util.getAllInterfacesForClassAsSet(Child.class);
		assertNotNull(interfaces);
		assertTrue(interfaces.contains(TestInterface.class));
	}

	@Test
	void testGetAllInterfacesForClassAsSet_interface() {
		Set<Class<?>> interfaces = Util.getAllInterfacesForClassAsSet(TestInterface.class);
		assertEquals(1, interfaces.size());
		assertTrue(interfaces.contains(TestInterface.class));
	}
}
