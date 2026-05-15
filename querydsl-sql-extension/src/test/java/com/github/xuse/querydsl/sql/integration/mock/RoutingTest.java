package com.github.xuse.querydsl.sql.integration.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.sql.routing.TableRouting;
import com.github.xuse.querydsl.sql.routing.TableRoutingBuilder;
import com.github.xuse.querydsl.sql.routing.WrapdRoutingStrategy;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SchemaAndTable;

class RoutingTest extends MockedTestBase {

        private static ConfigurationEx configuration;
        private static RelationalPath<?> fooPath;

        @BeforeAll
        static void setUp() {
                doInit();
                configuration = factory.getConfiguration();
                fooPath = PathCache.get(Foo.class, null);
        }

        // ---- TableRouting.suffix ----

        @Test
        void testSuffix() {
                TableRouting routing = TableRouting.suffix("_2024");
                SchemaAndTable input = new SchemaAndTable("schema", "table_name");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("table_name_2024", result.getTable());
        }

        @Test
        void testSuffixEmptyString() {
                TableRouting routing = TableRouting.suffix("");
                SchemaAndTable input = new SchemaAndTable("schema", "table_name");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("table_name", result.getTable());
        }

        // ---- TableRouting.prefix ----

        @Test
        void testPrefix() {
                TableRouting routing = TableRouting.prefix("pre_");
                SchemaAndTable input = new SchemaAndTable("schema", "table_name");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("pre_table_name", result.getTable());
        }

        @Test
        void testPrefixEmptyString() {
                TableRouting routing = TableRouting.prefix("");
                SchemaAndTable input = new SchemaAndTable("schema", "table_name");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("table_name", result.getTable());
        }

        // ---- TableRouting.rename ----

        @Test
        void testRename() {
                TableRouting routing = TableRouting.rename("new_table");
                SchemaAndTable input = new SchemaAndTable("schema", "table_name");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("new_table", result.getTable());
        }

        // ---- TableRouting.replaceKey ----

        @Test
        void testReplaceKey() {
                TableRouting routing = TableRouting.replaceKey("foo", "bar");
                SchemaAndTable input = new SchemaAndTable("schema", "foo_table");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("bar_table", result.getTable());
        }

        @Test
        void testReplaceKeyNoMatch() {
                TableRouting routing = TableRouting.replaceKey("xyz", "bar");
                SchemaAndTable input = new SchemaAndTable("schema", "table_name");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("table_name", result.getTable());
        }

        // ---- TableRoutingBuilder with specific tables ----

        @Test
        void testBuilderSuffix() {
                TableRouting routing = TableRouting.builder()
                                .suffix(fooPath, "_archive")
                                .build();
                // Foo's table name is "ca_foo"
                SchemaAndTable input = new SchemaAndTable("schema", "ca_foo");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("ca_foo_archive", result.getTable());
        }

        @Test
        void testBuilderPrefix() {
                TableRouting routing = TableRouting.builder()
                                .prefix(fooPath, "bak_")
                                .build();
                SchemaAndTable input = new SchemaAndTable("schema", "ca_foo");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("bak_ca_foo", result.getTable());
        }

        @Test
        void testBuilderUnmatchedTable() {
                TableRouting routing = TableRouting.builder()
                                .suffix(fooPath, "_archive")
                                .build();
                // A table not registered in the builder should pass through unchanged
                SchemaAndTable input = new SchemaAndTable("schema", "other_table");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("other_table", result.getTable());
        }

        @Test
        void testBuilderNullSuffix() {
                // null suffix should be a no-op (trimToNull returns null)
                TableRouting routing = TableRouting.builder()
                                .suffix(fooPath, null)
                                .build();
                SchemaAndTable input = new SchemaAndTable("schema", "ca_foo");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("ca_foo", result.getTable());
        }

        @Test
        void testBuilderNullPrefix() {
                // null prefix should be a no-op (trimToNull returns null)
                TableRouting routing = TableRouting.builder()
                                .prefix(fooPath, null)
                                .build();
                SchemaAndTable input = new SchemaAndTable("schema", "ca_foo");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("ca_foo", result.getTable());
        }

        @Test
        void testBuilderBlankSuffix() {
                // blank (whitespace-only) suffix should be a no-op
                TableRouting routing = TableRouting.builder()
                                .suffix(fooPath, "   ")
                                .build();
                SchemaAndTable input = new SchemaAndTable("schema", "ca_foo");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("ca_foo", result.getTable());
        }

        // ---- WrapdRoutingStrategy.wrap(a, b) ----

        @Test
        void testWrapBothNull() {
                RoutingStrategy result = WrapdRoutingStrategy.wrap(null, null);
                assertSame(RoutingStrategy.DEFAULT, result);
        }

        @Test
        void testWrapFirstNull() {
                RoutingStrategy b = TableRouting.suffix("_b");
                RoutingStrategy result = WrapdRoutingStrategy.wrap(null, b);
                assertSame(b, result);
        }

        @Test
        void testWrapSecondNull() {
                RoutingStrategy a = TableRouting.prefix("a_");
                RoutingStrategy result = WrapdRoutingStrategy.wrap(a, null);
                assertSame(a, result);
        }

        @Test
        void testWrapBothNonNull() {
                RoutingStrategy a = TableRouting.prefix("pre_");
                RoutingStrategy b = TableRouting.suffix("_suf");
                RoutingStrategy chained = WrapdRoutingStrategy.wrap(a, b);
                assertInstanceOf(WrapdRoutingStrategy.class, chained);

                SchemaAndTable input = new SchemaAndTable("schema", "table_name");
                SchemaAndTable result = chained.getOverride(input, configuration);
                // prefix applied first, then suffix
                assertEquals("pre_table_name_suf", result.getTable());
        }

        // ---- WrapdRoutingStrategy.wrap(RoutingStrategy...) ----

        @Test
        void testWrapVarargAllNull() {
                RoutingStrategy result = WrapdRoutingStrategy.wrap(null, null, null);
                assertSame(RoutingStrategy.DEFAULT, result);
        }

        @Test
        void testWrapVarargSingleNonNull() {
                RoutingStrategy a = TableRouting.suffix("_x");
                RoutingStrategy result = WrapdRoutingStrategy.wrap(null, a, null);
                assertSame(a, result);
        }

        @Test
        void testWrapVarargMultipleNonNull() {
                RoutingStrategy a = TableRouting.prefix("p_");
                RoutingStrategy b = TableRouting.suffix("_s");
                RoutingStrategy c = TableRouting.replaceKey("p_", "q_");
                RoutingStrategy chained = WrapdRoutingStrategy.wrap(null, a, null, b, c);
                assertInstanceOf(WrapdRoutingStrategy.class, chained);

                SchemaAndTable input = new SchemaAndTable("schema", "table");
                SchemaAndTable result = chained.getOverride(input, configuration);
                // a: "p_table", b: "p_table_s", c: "q_table_s"
                assertEquals("q_table_s", result.getTable());
        }

        // ---- RoutingStrategy.DEFAULT ----

        @Test
        void testDefaultStrategy() {
                assertNotNull(RoutingStrategy.DEFAULT);
                SchemaAndTable input = new SchemaAndTable("schema", "table_name");
                SchemaAndTable result = RoutingStrategy.DEFAULT.getOverride(input, configuration);
                // DEFAULT delegates to configurationEx.getOverride, which returns the table as-is
                assertNotNull(result);
                assertEquals("table_name", result.getTable());
        }

        // ---- Schema preservation ----

        @Test
        void testSchemaPreservedBySuffix() {
                TableRouting routing = TableRouting.suffix("_2024");
                SchemaAndTable input = new SchemaAndTable("my_schema", "my_table");
                SchemaAndTable result = routing.getOverride(input, configuration);
                assertEquals("my_schema", result.getSchema());
                assertEquals("my_table_2024", result.getTable());
        }

        // ---- TableRouting.builder() returns a builder ----

        @Test
        void testBuilderReturnsInstance() {
                TableRoutingBuilder builder = TableRouting.builder();
                assertNotNull(builder);
                TableRouting routing = builder.build();
                assertNotNull(routing);
        }
}