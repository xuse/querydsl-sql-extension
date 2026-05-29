package com.github.xuse.querydsl.sql.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SchemaAndTable;

/**
 * Unit tests for {@link TableRouting} static factory methods and routing behavior.
 */
@DisplayName("TableRouting Unit Tests")
class TableRoutingTest extends MockedTestBase {

	private static ConfigurationEx configuration;
	private static RelationalPath<?> fooPath;

	@BeforeAll
	static void setUp() {
		doInit();
		configuration = factory.getConfiguration();
		fooPath = PathCache.get(Foo.class, null);
	}

	// ---- Suffix-based routing ----

	@Test
	@DisplayName("suffix routing: getOverride returns table + suffix")
	void testSuffixRouting() {
		TableRouting routing = TableRouting.suffix("_2024");
		SchemaAndTable input = new SchemaAndTable("myschema", "orders");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("orders_2024", result.getTable());
	}

	@Test
	@DisplayName("suffix routing: empty suffix returns original table name")
	void testSuffixRoutingEmpty() {
		TableRouting routing = TableRouting.suffix("");
		SchemaAndTable input = new SchemaAndTable("myschema", "orders");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("orders", result.getTable());
	}

	@Test
	@DisplayName("suffix routing: whitespace-only suffix is trimmed to empty")
	void testSuffixRoutingWhitespace() {
		TableRouting routing = TableRouting.suffix("   ");
		SchemaAndTable input = new SchemaAndTable("myschema", "orders");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("orders", result.getTable());
	}

	// ---- Prefix-based routing ----

	@Test
	@DisplayName("prefix routing: getOverride returns prefix + table")
	void testPrefixRouting() {
		TableRouting routing = TableRouting.prefix("archive_");
		SchemaAndTable input = new SchemaAndTable("myschema", "orders");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("archive_orders", result.getTable());
	}

	@Test
	@DisplayName("prefix routing: empty prefix returns original table name")
	void testPrefixRoutingEmpty() {
		TableRouting routing = TableRouting.prefix("");
		SchemaAndTable input = new SchemaAndTable("myschema", "orders");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("orders", result.getTable());
	}

	@Test
	@DisplayName("prefix routing: whitespace-only prefix is trimmed to empty")
	void testPrefixRoutingWhitespace() {
		TableRouting routing = TableRouting.prefix("  ");
		SchemaAndTable input = new SchemaAndTable("myschema", "orders");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("orders", result.getTable());
	}

	// ---- Rename routing ----

	@Test
	@DisplayName("rename routing: getOverride returns replacement name regardless of original")
	void testRenameRouting() {
		TableRouting routing = TableRouting.rename("new_orders");
		SchemaAndTable input = new SchemaAndTable("myschema", "old_orders");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("new_orders", result.getTable());
	}

	@Test
	@DisplayName("rename routing: works with any original table name")
	void testRenameRoutingDifferentInput() {
		TableRouting routing = TableRouting.rename("target_table");
		SchemaAndTable input1 = new SchemaAndTable("s", "table_a");
		SchemaAndTable input2 = new SchemaAndTable("s", "table_b");
		assertEquals("target_table", routing.getOverride(input1, configuration).getTable());
		assertEquals("target_table", routing.getOverride(input2, configuration).getTable());
	}

	// ---- ReplaceKey routing ----

	@Test
	@DisplayName("replaceKey routing: getOverride replaces matching substring")
	void testReplaceKeyRouting() {
		TableRouting routing = TableRouting.replaceKey("order", "archive_order");
		SchemaAndTable input = new SchemaAndTable("myschema", "order_detail");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("archive_order_detail", result.getTable());
	}

	@Test
	@DisplayName("replaceKey routing: no match leaves table name unchanged")
	void testReplaceKeyRoutingNoMatch() {
		TableRouting routing = TableRouting.replaceKey("xyz", "abc");
		SchemaAndTable input = new SchemaAndTable("myschema", "orders");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("orders", result.getTable());
	}

	@Test
	@DisplayName("replaceKey routing: replaces all occurrences of the key")
	void testReplaceKeyRoutingMultipleOccurrences() {
		TableRouting routing = TableRouting.replaceKey("a", "b");
		SchemaAndTable input = new SchemaAndTable("myschema", "banana");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("bbnbnb", result.getTable());
	}

	// ---- No routing registered (builder with unmatched table) ----

	@Test
	@DisplayName("no routing registered: returns original table name unchanged")
	void testNoRoutingReturnsOriginal() {
		TableRouting routing = TableRouting.builder()
				.suffix(fooPath, "_archive")
				.build();
		// Use a table name that is NOT registered in the builder
		SchemaAndTable input = new SchemaAndTable("myschema", "unregistered_table");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("unregistered_table", result.getTable());
	}

	@Test
	@DisplayName("no routing registered: builder with no entries returns original table")
	void testEmptyBuilderReturnsOriginal() {
		TableRouting routing = TableRouting.builder().build();
		SchemaAndTable input = new SchemaAndTable("myschema", "any_table");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("any_table", result.getTable());
	}

	// ---- Schema preservation ----

	@Test
	@DisplayName("routing preserves schema name")
	void testSchemaPreserved() {
		TableRouting routing = TableRouting.suffix("_v2");
		SchemaAndTable input = new SchemaAndTable("production", "users");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("production", result.getSchema());
		assertEquals("users_v2", result.getTable());
	}

	@Test
	@DisplayName("routing result is not null")
	void testResultNotNull() {
		TableRouting routing = TableRouting.rename("target");
		SchemaAndTable input = new SchemaAndTable(null, "source");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertNotNull(result);
		assertEquals("target", result.getTable());
	}
}
