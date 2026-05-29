package com.github.xuse.querydsl.sql.expression;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

/**
 * Tests for {@link QBeanEx}.
 * <p>
 * Verifies that newInstance creates a bean with field values matching
 * the argument array in binding order.
 * </p>
 *
 * <p>Requirements: 2.7</p>
 */
@DisplayName("QBeanEx unit tests")
class QBeanExTest {

	// ==================== Test POJOs ====================

	public static class PersonBean {
		private int id;
		private String name;
		private String email;

		public PersonBean() {}

		public int getId() { return id; }
		public void setId(int id) { this.id = id; }
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }
	}

	public static class AddressBean {
		private String street;
		private String city;
		private Integer zipCode;

		public AddressBean() {}

		public String getStreet() { return street; }
		public void setStreet(String street) { this.street = street; }
		public String getCity() { return city; }
		public void setCity(String city) { this.city = city; }
		public Integer getZipCode() { return zipCode; }
		public void setZipCode(Integer zipCode) { this.zipCode = zipCode; }
	}

	public static class ParentEntity {
		private int parentId;
		private String parentName;

		public ParentEntity() {}

		public int getParentId() { return parentId; }
		public void setParentId(int parentId) { this.parentId = parentId; }
		public String getParentName() { return parentName; }
		public void setParentName(String parentName) { this.parentName = parentName; }
	}

	public static class ChildEntity extends ParentEntity {
		private String childField;
		private long childValue;

		public ChildEntity() {}

		public String getChildField() { return childField; }
		public void setChildField(String childField) { this.childField = childField; }
		public long getChildValue() { return childValue; }
		public void setChildValue(long childValue) { this.childValue = childValue; }
	}

	// ==================== Tests ====================

	/**
	 * Verify newInstance creates bean with field values matching argument array in binding order.
	 */
	@Test
	@DisplayName("newInstance creates bean with field values matching argument array in binding order")
	void testNewInstanceCreatesCorrectBean() {
		// Build bindings: id, name, email
		Map<String, Expression<?>> bindings = new LinkedHashMap<>();
		bindings.put("id", createNumberPath(Integer.class, "id"));
		bindings.put("name", createStringPath("name"));
		bindings.put("email", createStringPath("email"));

		QBeanEx<PersonBean> qBean = new QBeanEx<>(PersonBean.class, bindings);

		Object[] values = {42, "Alice", "alice@example.com"};
		PersonBean result = qBean.newInstance(values);

		assertNotNull(result);
		assertEquals(42, result.getId());
		assertEquals("Alice", result.getName());
		assertEquals("alice@example.com", result.getEmail());
	}

	/**
	 * Verify newInstance with different binding order produces correct mapping.
	 */
	@Test
	@DisplayName("newInstance respects binding order for field assignment")
	void testNewInstanceRespectsBindingOrder() {
		// Bind in order: email, name, id (different from field declaration order)
		Map<String, Expression<?>> bindings = new LinkedHashMap<>();
		bindings.put("email", createStringPath("email"));
		bindings.put("name", createStringPath("name"));
		bindings.put("id", createNumberPath(Integer.class, "id"));

		QBeanEx<PersonBean> qBean = new QBeanEx<>(PersonBean.class, bindings);

		// Values in binding order: email, name, id
		Object[] values = {"bob@test.com", "Bob", 99};
		PersonBean result = qBean.newInstance(values);

		assertNotNull(result);
		assertEquals(99, result.getId());
		assertEquals("Bob", result.getName());
		assertEquals("bob@test.com", result.getEmail());
	}

	/**
	 * Verify newInstance handles null values for reference type fields.
	 */
	@Test
	@DisplayName("newInstance handles null values for reference type fields")
	void testNewInstanceWithNullReferenceFields() {
		Map<String, Expression<?>> bindings = new LinkedHashMap<>();
		bindings.put("street", createStringPath("street"));
		bindings.put("city", createStringPath("city"));
		bindings.put("zipCode", createNumberPath(Integer.class, "zipCode"));

		QBeanEx<AddressBean> qBean = new QBeanEx<>(AddressBean.class, bindings);

		Object[] values = {null, "Springfield", null};
		AddressBean result = qBean.newInstance(values);

		assertNotNull(result);
		assertNull(result.getStreet());
		assertEquals("Springfield", result.getCity());
		assertNull(result.getZipCode());
	}

	/**
	 * Verify newInstance works with inherited bean (parent + child fields).
	 */
	@Test
	@DisplayName("newInstance works with inherited bean mapping parent and child fields")
	void testNewInstanceWithInheritedBean() {
		Map<String, Expression<?>> bindings = new LinkedHashMap<>();
		bindings.put("parentId", createNumberPath(Integer.class, "parentId"));
		bindings.put("parentName", createStringPath("parentName"));
		bindings.put("childField", createStringPath("childField"));
		bindings.put("childValue", createNumberPath(Long.class, "childValue"));

		QBeanEx<ChildEntity> qBean = new QBeanEx<>(ChildEntity.class, bindings);

		Object[] values = {10, "ParentName", "ChildData", 500L};
		ChildEntity result = qBean.newInstance(values);

		assertNotNull(result);
		assertEquals(10, result.getParentId());
		assertEquals("ParentName", result.getParentName());
		assertEquals("ChildData", result.getChildField());
		assertEquals(500L, result.getChildValue());
	}

	/**
	 * Verify getArgs returns the expressions in binding order.
	 */
	@Test
	@DisplayName("getArgs returns expressions in binding order")
	void testGetArgsReturnsExpressionsInBindingOrder() {
		Map<String, Expression<?>> bindings = new LinkedHashMap<>();
		Expression<?> idExpr = createNumberPath(Integer.class, "id");
		Expression<?> nameExpr = createStringPath("name");
		Expression<?> emailExpr = createStringPath("email");
		bindings.put("id", idExpr);
		bindings.put("name", nameExpr);
		bindings.put("email", emailExpr);

		QBeanEx<PersonBean> qBean = new QBeanEx<>(PersonBean.class, bindings);

		List<Expression<?>> args = qBean.getArgs();
		assertNotNull(args);
		assertEquals(3, args.size());
		assertSame(idExpr, args.get(0));
		assertSame(nameExpr, args.get(1));
		assertSame(emailExpr, args.get(2));
	}

	/**
	 * Verify getBeanCodec returns a non-null codec that can perform round-trip.
	 */
	@Test
	@DisplayName("getBeanCodec returns functional codec for round-trip operations")
	void testGetBeanCodecRoundTrip() {
		Map<String, Expression<?>> bindings = new LinkedHashMap<>();
		bindings.put("id", createNumberPath(Integer.class, "id"));
		bindings.put("name", createStringPath("name"));
		bindings.put("email", createStringPath("email"));

		QBeanEx<PersonBean> qBean = new QBeanEx<>(PersonBean.class, bindings);
		BeanCodec codec = qBean.getBeanCodec();

		assertNotNull(codec);

		Object[] input = {7, "Charlie", "charlie@test.com"};
		Object bean = codec.newInstance(input);
		Object[] output = codec.values(bean);

		assertEquals(input.length, output.length);
		assertEquals(7, output[0]);
		assertEquals("Charlie", output[1]);
		assertEquals("charlie@test.com", output[2]);
	}

	// ==================== Helper methods ====================

	private StringPath createStringPath(String name) {
		return Expressions.stringPath(name);
	}

	private <T extends Number & Comparable<?>> NumberPath<T> createNumberPath(Class<T> type, String name) {
		return Expressions.numberPath(type, name);
	}
}
