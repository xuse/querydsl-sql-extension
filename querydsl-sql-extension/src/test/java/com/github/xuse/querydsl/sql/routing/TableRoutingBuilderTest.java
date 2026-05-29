package com.github.xuse.querydsl.sql.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
 * Unit tests for {@link TableRoutingBuilder} construction and routing behavior.
 */
@DisplayName("TableRoutingBuilder Unit Tests")
class TableRoutingBuilderTest extends MockedTestBase {

	private static ConfigurationEx configuration;
	private static RelationalPath<?> fooPath;

	@BeforeAll
	static void setUp() {
		doInit();
		configuration = factory.getConfiguration();
		fooPath = PathCache.get(Foo.class, null);
	}

	// ---- Suffix routing per table ----

	@Test
	@DisplayName("built routing applies suffix strategy to matching table")
	void testSuffixRoutingForMatchingTable() {
		TableRouting routing = TableRouting.builder()
				.suffix(fooPath, "_archive")
				.build();

		// fooPath table name is "ca_foo"
		SchemaAndTable input = new SchemaAndTable("myschema", "ca_foo");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("ca_foo_archive", result.getTable());
	}

	@Test
	@DisplayName("built routing applies suffix case-insensitively")
	void testSuffixRoutingCaseInsensitive() {
		TableRouting routing = TableRouting.builder()
				.suffix(fooPath, "_2024")
				.build();

		// Use uppercase table name - should still match (case-insensitive lookup)
		// Note: configurationEx.getOverride() may normalize the table name to lowercase
		SchemaAndTable input = new SchemaAndTable("myschema", "CA_FOO");
		SchemaAndTable result = routing.getOverride(input, configuration);
		// The function applies to the table name after configuration override processing
		assertEquals("ca_foo_2024", result.getTable());
	}

	// ---- Prefix routing per table ----

	@Test
	@DisplayName("built routing applies prefix strategy to matching table")
	void testPrefixRoutingForMatchingTable() {
		TableRouting routing = TableRouting.builder()
				.prefix(fooPath, "bak_")
				.build();

		SchemaAndTable input = new SchemaAndTable("myschema", "ca_foo");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("bak_ca_foo", result.getTable());
	}

	@Test
	@DisplayName("built routing applies prefix case-insensitively")
	void testPrefixRoutingCaseInsensitive() {
		TableRouting routing = TableRouting.builder()
				.prefix(fooPath, "old_")
				.build();

		// Mixed-case input still matches because lookup is case-insensitive
		// configurationEx.getOverride() normalizes table name to lowercase
		SchemaAndTable input = new SchemaAndTable("myschema", "Ca_Foo");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("old_ca_foo", result.getTable());
	}

	// ---- Non-matching tables left unchanged ----

	@Test
	@DisplayName("non-matching table is left unchanged")
	void testNonMatchingTableUnchanged() {
		TableRouting routing = TableRouting.builder()
				.suffix(fooPath, "_archive")
				.build();

		SchemaAndTable input = new SchemaAndTable("myschema", "other_table");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("other_table", result.getTable());
	}

	@Test
	@DisplayName("empty builder leaves all tables unchanged")
	void testEmptyBuilderLeavesAllUnchanged() {
		TableRouting routing = TableRouting.builder().build();

		SchemaAndTable input = new SchemaAndTable("myschema", "any_table");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("any_table", result.getTable());
	}

	@Test
	@DisplayName("null table name is handled gracefully")
	void testNullTableNameHandled() {
		TableRouting routing = TableRouting.builder()
				.suffix(fooPath, "_x")
				.build();

		SchemaAndTable input = new SchemaAndTable("myschema", null);
		SchemaAndTable result = routing.getOverride(input, configuration);
		// adjustTable returns null when table is null
		assertEquals(null, result.getTable());
	}

	// ---- Multiple tables with different strategies ----

	@Test
	@DisplayName("builder with suffix then prefix on same table: last one wins")
	void testLastStrategyWinsForSameTable() {
		TableRouting routing = TableRouting.builder()
				.suffix(fooPath, "_old")
				.prefix(fooPath, "new_")
				.build();

		SchemaAndTable input = new SchemaAndTable("s", "ca_foo");
		SchemaAndTable result = routing.getOverride(input, configuration);
		// prefix was registered last, so it overwrites the suffix entry
		assertEquals("new_ca_foo", result.getTable());
	}

	// ---- Whitespace/null suffix and prefix handling ----

	@Test
	@DisplayName("suffix with null value is ignored (no routing applied)")
	void testSuffixNullIgnored() {
		TableRouting routing = TableRouting.builder()
				.suffix(fooPath, null)
				.build();

		SchemaAndTable input = new SchemaAndTable("s", "ca_foo");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("ca_foo", result.getTable());
	}

	@Test
	@DisplayName("prefix with whitespace-only value is ignored (no routing applied)")
	void testPrefixWhitespaceIgnored() {
		TableRouting routing = TableRouting.builder()
				.prefix(fooPath, "   ")
				.build();

		SchemaAndTable input = new SchemaAndTable("s", "ca_foo");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("ca_foo", result.getTable());
	}

	// ---- Schema preservation ----

	@Test
	@DisplayName("built routing preserves schema name")
	void testSchemaPreserved() {
		TableRouting routing = TableRouting.builder()
				.suffix(fooPath, "_v2")
				.build();

		SchemaAndTable input = new SchemaAndTable("production", "ca_foo");
		SchemaAndTable result = routing.getOverride(input, configuration);
		assertEquals("production", result.getSchema());
		assertEquals("ca_foo_v2", result.getTable());
	}
}
