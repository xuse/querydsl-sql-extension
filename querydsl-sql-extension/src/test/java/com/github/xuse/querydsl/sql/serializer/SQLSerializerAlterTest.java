package com.github.xuse.querydsl.sql.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.sql.routing.TableRouting;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.TemplatesAccessor;

/**
 * Unit tests for {@link SQLSerializerAlter} and {@link TemplatesAccessor}.
 * These classes are in com.querydsl.sql package for access to package-private members.
 */
class SQLSerializerAlterTest extends MockedTestBase {

	private static ConfigurationEx configuration;
	private static RelationalPath<?> fooPath;

	@BeforeAll
	static void setUp() {
		doInit();
		configuration = factory.getConfiguration();
		fooPath = PathCache.get(Foo.class, null);
	}

	@Test
	void testBasicConstruction() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		assertNotNull(serializer);
		assertEquals(RoutingStrategy.DEFAULT, serializer.getRouting());
	}

	@Test
	void testConstructionWithOptions() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true, false, true);
		assertNotNull(serializer);
	}

	@Test
	void testSetRouting() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		RoutingStrategy routing = TableRouting.suffix("_2024");
		serializer.setRouting(routing);
		assertEquals(routing, serializer.getRouting());
	}

	@Test
	void testSetRoutingNull() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		serializer.setRouting(null);
		// Should keep default when null is passed
		assertEquals(RoutingStrategy.DEFAULT, serializer.getRouting());
	}

	@Test
	void testVisitPath() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		serializer.serializeAction("SELECT * FROM ", fooPath, new Object[0]);
		String sql = serializer.toString();
		assertNotNull(sql);
		assertTrue(sql.contains("foo"), "Should contain table name 'foo': " + sql);
	}

	@Test
	void testVisitConstantCollection() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, false);
		serializer.visitConstant(Arrays.asList(1, 2, 3));
		String result = serializer.toString();
		assertNotNull(result);
		assertTrue(result.contains("("), "Should have opening bracket");
		assertTrue(result.contains(")"), "Should have closing bracket");
	}

	@Test
	void testVisitConstantObjectArray() {
		// When objectArrayAsCollection is enabled, Object[] should be treated as collection without brackets
		boolean original = configuration.isObjectArrayAsCollection();
		configuration.setObjectArrayAsCollection(true);
		try {
			SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, false);
			serializer.visitConstant(new Object[] { "a", "b", "c" });
			String result = serializer.toString();
			assertNotNull(result);
			// Should NOT have brackets when objectArrayAsCollection is true
			assertTrue(!result.contains("(") || !result.contains(")"),
					"Should not have brackets for Object[] with objectArrayAsCollection=true: " + result);
		} finally {
			configuration.setObjectArrayAsCollection(original);
		}
	}

	@Test
	void testVisitConstantSingleValue() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, false);
		serializer.visitConstant(42);
		String result = serializer.toString();
		assertNotNull(result);
	}

	@Test
	void testVisitConstantWithLiterals() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, false, true, false);
		serializer.visitConstant("hello");
		String result = serializer.toString();
		assertNotNull(result);
	}

	@Test
	void testVisitConstantCollectionWithLiterals() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, false, true, false);
		serializer.visitConstant(Arrays.asList("a", "b"));
		String result = serializer.toString();
		assertNotNull(result);
	}

	@Test
	void testSerializeAction() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		serializer.serializeAction(fooPath, "DROP TABLE ");
		String sql = serializer.toString();
		assertNotNull(sql);
		assertTrue(sql.contains("DROP TABLE"), "Should contain DROP TABLE: " + sql);
		assertTrue(sql.contains("foo"), "Should contain table name: " + sql);
	}

	@Test
	void testSerializeActionWithExpressions() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		Expression<?> expr = Expressions.template(String.class, "COMMENT 'test'");
		List<Expression<?>> exprs = Collections.singletonList(expr);
		serializer.serializeAction("ALTER TABLE ", fooPath, exprs);
		String sql = serializer.toString();
		assertNotNull(sql);
		assertTrue(sql.contains("ALTER TABLE"), "Should contain ALTER TABLE: " + sql);
	}

	@Test
	void testHandleTemplate() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		serializer.handle("SELECT {0} FROM {1}", Expressions.constant("col1"), Expressions.constant("table1"));
		String sql = serializer.toString();
		assertNotNull(sql);
	}

	@Test
	@SuppressWarnings("unchecked")
	void testHandleValueList() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, false);
		List<Path<?>> columns = (List<Path<?>>) (List<?>) fooPath.getColumns().subList(0, 2);
		List<Expression<?>> values = Arrays.asList(
				Expressions.constant(1),
				Expressions.constant("test"));
		serializer.handleValueList(values, columns);
		String sql = serializer.toString();
		assertNotNull(sql);
	}

	@Test
	void testSerializeWithRouting() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		serializer.setRouting(TableRouting.suffix("_archive"));
		serializer.serializeAction("SELECT * FROM ", fooPath, new Object[0]);
		String sql = serializer.toString();
		assertNotNull(sql);
		assertTrue(sql.contains("foo_archive"), "Should contain routed table name: " + sql);
	}

	// --- TemplatesAccessor tests ---

	@Test
	void testTemplatesAccessorSetAutoIncrement() {
		String original = configuration.getTemplates().getAutoIncrement();
		try {
			TemplatesAccessor.setAutoIncrement(configuration.getTemplates().getOriginal(), " GENERATED_ID");
			String autoInc = configuration.getTemplates().getAutoIncrement();
			assertNotNull(autoInc);
			assertTrue(autoInc.contains("GENERATED_ID"), "Should have set auto increment string");
		} finally {
			// Restore original
			TemplatesAccessor.setAutoIncrement(configuration.getTemplates().getOriginal(), original);
		}
	}
}
