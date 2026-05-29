package com.github.xuse.querydsl.lambda;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.sql.Time;
import java.time.Instant;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.entity.TableDataTypes;
import com.github.xuse.querydsl.mock.MockedTestBase;

/**
 * Unit tests for {@link LambdaHelpers} verifying that helper methods
 * return non-null references identical to the input.
 */
@DisplayName("LambdaHelpers Unit Tests")
class LambdaHelpersTest extends MockedTestBase {

    private final LambdaHelpers helpers = new LambdaHelpers() {};

    @BeforeAll
    static void setUp() {
        doInit();
    }

    // ---- column() ----

    @Test
    @DisplayName("column() returns non-null reference identical to input")
    void testColumnReturnsIdenticalReference() {
        LambdaColumn<Foo, String> ref = Foo::getCode;
        LambdaColumn<Foo, String> result = helpers.column(ref);
        assertNotNull(result, "column() should return non-null");
        assertSame(ref, result, "column() should return the same reference as input");
    }

    // ---- $() alias ----

    @Test
    @DisplayName("$() returns non-null reference identical to input")
    void testDollarReturnsIdenticalReference() {
        LambdaColumn<Foo, String> ref = Foo::getCode;
        LambdaColumn<Foo, String> result = helpers.$(ref);
        assertNotNull(result, "$() should return non-null");
        assertSame(ref, result, "$() should return the same reference as input");
    }

    // ---- string() ----

    @Test
    @DisplayName("string() returns non-null reference identical to input")
    void testStringReturnsIdenticalReference() {
        StringLambdaColumn<Foo> ref = Foo::getName;
        StringLambdaColumn<Foo> result = helpers.string(ref);
        assertNotNull(result, "string() should return non-null");
        assertSame(ref, result, "string() should return the same reference as input");
    }

    // ---- s() alias ----

    @Test
    @DisplayName("s() returns non-null reference identical to input")
    void testSReturnsIdenticalReference() {
        StringLambdaColumn<Foo> ref = Foo::getName;
        StringLambdaColumn<Foo> result = helpers.s(ref);
        assertNotNull(result, "s() should return non-null");
        assertSame(ref, result, "s() should return the same reference as input");
    }

    // ---- num() ----

    @Test
    @DisplayName("num() returns non-null reference identical to input")
    void testNumReturnsIdenticalReference() {
        NumberLambdaColumn<Foo, Integer> ref = Foo::getId;
        NumberLambdaColumn<Foo, Integer> result = helpers.num(ref);
        assertNotNull(result, "num() should return non-null");
        assertSame(ref, result, "num() should return the same reference as input");
    }

    // ---- n() alias ----

    @Test
    @DisplayName("n() returns non-null reference identical to input")
    void testNReturnsIdenticalReference() {
        NumberLambdaColumn<Foo, Integer> ref = Foo::getId;
        NumberLambdaColumn<Foo, Integer> result = helpers.n(ref);
        assertNotNull(result, "n() should return non-null");
        assertSame(ref, result, "n() should return the same reference as input");
    }

    // ---- date() ----

    @Test
    @DisplayName("date() returns non-null reference identical to input")
    void testDateReturnsIdenticalReference() {
        DateLambdaColumn<Foo, java.sql.Date> ref = Foo::getInDay;
        DateLambdaColumn<Foo, java.sql.Date> result = helpers.date(ref);
        assertNotNull(result, "date() should return non-null");
        assertSame(ref, result, "date() should return the same reference as input");
    }

    // ---- time() ----

    @Test
    @DisplayName("time() returns non-null reference identical to input")
    void testTimeReturnsIdenticalReference() {
        TimeLambdaColumn<TableDataTypes, Time> ref = TableDataTypes::getDataTime;
        TimeLambdaColumn<TableDataTypes, Time> result = helpers.time(ref);
        assertNotNull(result, "time() should return non-null");
        assertSame(ref, result, "time() should return the same reference as input");
    }

    // ---- datetime() ----

    @Test
    @DisplayName("datetime() returns non-null reference identical to input")
    void testDatetimeReturnsIdenticalReference() {
        DateTimeLambdaColumn<Foo, Instant> ref = Foo::getCreated;
        DateTimeLambdaColumn<Foo, Instant> result = helpers.datetime(ref);
        assertNotNull(result, "datetime() should return non-null");
        assertSame(ref, result, "datetime() should return the same reference as input");
    }

    // ---- dt() alias ----

    @Test
    @DisplayName("dt() returns non-null reference identical to input")
    void testDtReturnsIdenticalReference() {
        DateTimeLambdaColumn<Foo, Instant> ref = Foo::getCreated;
        DateTimeLambdaColumn<Foo, Instant> result = helpers.dt(ref);
        assertNotNull(result, "dt() should return non-null");
        assertSame(ref, result, "dt() should return the same reference as input");
    }
}
