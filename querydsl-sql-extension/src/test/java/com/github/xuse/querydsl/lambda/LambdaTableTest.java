package com.github.xuse.querydsl.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.querydsl.core.types.Path;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.SchemaAndTable;

/**
 * Unit tests for {@link LambdaTable} verifying delegation methods
 * return correct values from the underlying RelationalPathEx.
 */
@DisplayName("LambdaTable Unit Tests")
class LambdaTableTest extends MockedTestBase {

    private static final LambdaTable<Foo> TABLE = () -> Foo.class;

    @BeforeAll
    static void setUp() {
        doInit();
    }

    // ---- getTableName ----

    @Test
    @DisplayName("getTableName returns non-empty string")
    void testGetTableNameReturnsNonEmptyString() {
        String tableName = TABLE.getTableName();
        assertNotNull(tableName, "getTableName should return non-null");
        assertFalse(tableName.isEmpty(), "getTableName should return non-empty string");
    }

    // ---- getColumns ----

    @Test
    @DisplayName("getColumns returns non-empty list")
    void testGetColumnsReturnsNonEmptyList() {
    	List<Path<?>>  columns = TABLE.getColumns();
        assertNotNull(columns, "getColumns should return non-null");
        assertFalse(columns.isEmpty(), "getColumns should return non-empty list");
    }

    // ---- getPrimaryKey ----

    @Test
    @DisplayName("getPrimaryKey returns non-null")
    void testGetPrimaryKeyReturnsNonNull() {
    	PrimaryKey primaryKey = TABLE.getPrimaryKey();
        assertNotNull(primaryKey, "getPrimaryKey should return non-null for entity with @TableSpec(primaryKeys)");
    }

    // ---- getType ----

    @Test
    @DisplayName("getType matches bean class")
    void testGetTypeMatchesBeanClass() {
        Class<? extends Foo> type = TABLE.getType();
        assertNotNull(type, "getType should return non-null");
        assertEquals(Foo.class, type, "getType should match the bean class");
    }

    // ---- get() supplier method ----

    @Test
    @DisplayName("get() supplier returns the bean class")
    void testSupplierGetReturnsBeanClass() {
        Class<Foo> beanClass = TABLE.get();
        assertNotNull(beanClass, "get() should return non-null");
        assertEquals(Foo.class, beanClass, "get() should return the bean class");
    }

    // ---- getSchemaAndTable ----

    @Test
    @DisplayName("getSchemaAndTable returns non-null with table name")
    void testGetSchemaAndTableReturnsNonNull() {
    	SchemaAndTable schemaAndTable = TABLE.getSchemaAndTable();
        assertNotNull(schemaAndTable, "getSchemaAndTable should return non-null");
        assertNotNull(schemaAndTable.getTable(), "getSchemaAndTable.getTable() should return non-null");
        assertFalse(schemaAndTable.getTable().isEmpty(), "getSchemaAndTable.getTable() should be non-empty");
    }
}
