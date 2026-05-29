package com.github.xuse.querydsl.lambda;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.querydsl.core.types.Path;

/**
 * Unit tests for {@link PathCache} verifying caching behavior.
 */
@DisplayName("PathCache Unit Tests")
class PathCacheTest extends MockedTestBase {

    @BeforeAll
    static void setUp() {
        doInit();
    }

    // ---- PathCache.get caching ----

    @Test
    @DisplayName("get with same class and variable returns same object instance")
    void testGetReturnsSameInstanceForSameClassAndVariable() {
        RelationalPathEx<Foo> first = PathCache.get(Foo.class, null);
        RelationalPathEx<Foo> second = PathCache.get(Foo.class, null);
        assertNotNull(first);
        assertSame(first, second, "PathCache.get should return the same cached instance for same class and variable");
    }

    @Test
    @DisplayName("get with same class and named variable returns same instance")
    void testGetReturnsSameInstanceForNamedVariable() {
        RelationalPathEx<Foo> first = PathCache.get(Foo.class, "alias1");
        RelationalPathEx<Foo> second = PathCache.get(Foo.class, "alias1");
        assertNotNull(first);
        assertSame(first, second, "PathCache.get should return the same cached instance for same class and named variable");
    }

    // ---- PathCache.getPath with LambdaColumn ----

    @Test
    @DisplayName("getPath with LambdaColumn returns non-null Path; second call returns same instance")
    void testGetPathWithLambdaColumnReturnsCachedPath() {
        StringLambdaColumn<Foo> nameRef = Foo::getName;
        Path<String> first = PathCache.getPath(nameRef);
        assertNotNull(first, "PathCache.getPath should return a non-null Path");

        Path<String> second = PathCache.getPath(nameRef);
        assertSame(first, second, "PathCache.getPath should return the same cached Path instance on subsequent calls");
    }

    @Test
    @DisplayName("getPath with NumberLambdaColumn returns non-null Path; second call returns same instance")
    void testGetPathWithNumberLambdaColumnReturnsCachedPath() {
        NumberLambdaColumn<Foo, Integer> idRef = Foo::getId;
        Path<Integer> first = PathCache.getPath(idRef);
        assertNotNull(first, "PathCache.getPath should return a non-null Path for NumberLambdaColumn");

        Path<Integer> second = PathCache.getPath(idRef);
        assertSame(first, second, "PathCache.getPath should return the same cached Path instance for NumberLambdaColumn");
    }
}
