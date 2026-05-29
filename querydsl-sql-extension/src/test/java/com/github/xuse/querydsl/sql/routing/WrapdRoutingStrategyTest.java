package com.github.xuse.querydsl.sql.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.querydsl.sql.SchemaAndTable;

/**
 * Unit tests for {@link WrapdRoutingStrategy} chaining behavior.
 */
@DisplayName("WrapdRoutingStrategy Unit Tests")
class WrapdRoutingStrategyTest extends MockedTestBase {

	private static ConfigurationEx configuration;

	@BeforeAll
	static void setUp() {
		doInit();
		configuration = factory.getConfiguration();
	}

	// ---- Chaining two strategies ----

	@Test
	@DisplayName("wrap two strategies: applies them in sequence order")
	void testWrapTwoStrategies() {
		RoutingStrategy suffix = TableRouting.suffix("_2024");
		RoutingStrategy prefix = TableRouting.prefix("archive_");

		// Chain: first suffix, then prefix
		RoutingStrategy chained = WrapdRoutingStrategy.wrap(suffix, prefix);
		SchemaAndTable input = new SchemaAndTable("myschema", "orders");
		SchemaAndTable result = chained.getOverride(input, configuration);

		// suffix applied first: "orders" -> "orders_2024"
		// prefix applied second: "orders_2024" -> "archive_orders_2024"
		assertEquals("archive_orders_2024", result.getTable());
	}

	@Test
	@DisplayName("wrap two strategies: reversed order produces different result")
	void testWrapTwoStrategiesReversedOrder() {
		RoutingStrategy suffix = TableRouting.suffix("_2024");
		RoutingStrategy prefix = TableRouting.prefix("archive_");

		// Chain: first prefix, then suffix
		RoutingStrategy chained = WrapdRoutingStrategy.wrap(prefix, suffix);
		SchemaAndTable input = new SchemaAndTable("myschema", "orders");
		SchemaAndTable result = chained.getOverride(input, configuration);

		// prefix applied first: "orders" -> "archive_orders"
		// suffix applied second: "archive_orders" -> "archive_orders_2024"
		assertEquals("archive_orders_2024", result.getTable());
	}

	// ---- Chaining three or more strategies ----

	@Test
	@DisplayName("wrap varargs: three strategies applied in sequence")
	void testWrapThreeStrategies() {
		RoutingStrategy prefix = TableRouting.prefix("pre_");
		RoutingStrategy suffix = TableRouting.suffix("_suf");
		RoutingStrategy replaceKey = TableRouting.replaceKey("order", "item");

		RoutingStrategy chained = WrapdRoutingStrategy.wrap(prefix, suffix, replaceKey);
		SchemaAndTable input = new SchemaAndTable("s", "order_detail");
		SchemaAndTable result = chained.getOverride(input, configuration);

		// prefix: "order_detail" -> "pre_order_detail"
		// suffix: "pre_order_detail" -> "pre_order_detail_suf"
		// replaceKey: "pre_order_detail_suf" -> "pre_item_detail_suf"
		assertEquals("pre_item_detail_suf", result.getTable());
	}

	// ---- Null handling in wrap ----

	@Test
	@DisplayName("wrap(null, strategy): returns the non-null strategy")
	void testWrapFirstNull() {
		RoutingStrategy suffix = TableRouting.suffix("_v2");
		RoutingStrategy result = WrapdRoutingStrategy.wrap(null, suffix);
		assertSame(suffix, result);
	}

	@Test
	@DisplayName("wrap(strategy, null): returns the non-null strategy")
	void testWrapSecondNull() {
		RoutingStrategy prefix = TableRouting.prefix("pre_");
		RoutingStrategy result = WrapdRoutingStrategy.wrap(prefix, null);
		assertSame(prefix, result);
	}

	@Test
	@DisplayName("wrap(null, null): returns DEFAULT strategy")
	void testWrapBothNull() {
		RoutingStrategy result = WrapdRoutingStrategy.wrap(null, null);
		assertSame(RoutingStrategy.DEFAULT, result);
	}

	@Test
	@DisplayName("wrap varargs with all nulls: returns DEFAULT strategy")
	void testWrapVarargsAllNull() {
		RoutingStrategy result = WrapdRoutingStrategy.wrap(null, null, null);
		assertSame(RoutingStrategy.DEFAULT, result);
	}

	@Test
	@DisplayName("wrap varargs with single non-null: returns that strategy directly")
	void testWrapVarargsSingleNonNull() {
		RoutingStrategy suffix = TableRouting.suffix("_x");
		RoutingStrategy result = WrapdRoutingStrategy.wrap(null, suffix, null);
		assertSame(suffix, result);
	}

	@Test
	@DisplayName("wrap varargs with nulls mixed in: only non-null strategies applied")
	void testWrapVarargsNullsMixed() {
		RoutingStrategy prefix = TableRouting.prefix("a_");
		RoutingStrategy suffix = TableRouting.suffix("_b");

		RoutingStrategy chained = WrapdRoutingStrategy.wrap(null, prefix, null, suffix, null);
		SchemaAndTable input = new SchemaAndTable("s", "tbl");
		SchemaAndTable result = chained.getOverride(input, configuration);

		// prefix: "tbl" -> "a_tbl"
		// suffix: "a_tbl" -> "a_tbl_b"
		assertEquals("a_tbl_b", result.getTable());
	}

	// ---- Schema preservation ----

	@Test
	@DisplayName("chained strategies preserve schema name")
	void testSchemaPreserved() {
		RoutingStrategy suffix = TableRouting.suffix("_new");
		RoutingStrategy prefix = TableRouting.prefix("x_");
		RoutingStrategy chained = WrapdRoutingStrategy.wrap(suffix, prefix);

		SchemaAndTable input = new SchemaAndTable("production", "users");
		SchemaAndTable result = chained.getOverride(input, configuration);

		assertEquals("production", result.getSchema());
		assertNotNull(result.getTable());
	}
}
