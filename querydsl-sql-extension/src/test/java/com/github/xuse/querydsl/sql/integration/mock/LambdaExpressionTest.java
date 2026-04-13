package com.github.xuse.querydsl.sql.integration.mock;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.mock.MockedTestBase;
import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.lambda.DateTimeLambdaColumn;
import com.github.xuse.querydsl.lambda.ExprDate;
import com.github.xuse.querydsl.lambda.ExprDateTime;
import com.github.xuse.querydsl.lambda.ExprTime;
import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.lambda.LambdaHelpers;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.lambda.NumberLambdaColumn;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.lambda.StringLambdaColumn;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.TimeExpression;

/**
 * Tests for the lambda expression interfaces in the lambda package.
 */
class LambdaExpressionTest extends MockedTestBase {

        static final StringLambdaColumn<Foo> NAME = Foo::getName;
        static final StringLambdaColumn<Foo> CODE = Foo::getCode;
        static final NumberLambdaColumn<Foo, Integer> ID = Foo::getId;
        static final NumberLambdaColumn<Foo, Integer> VOLUME = Foo::getVolume;
        static final LambdaColumn<Foo, String> CODE_COLUMN = Foo::getCode;

        @BeforeAll
        static void setup() {
                doInit();
        }

        // ==================== ExprComparable ====================

        @Test
        void testComparableAscDesc() {
                OrderSpecifier<String> asc = CODE_COLUMN.asc();
                assertNotNull(asc);
                assertTrue(asc.isAscending());
                assertNotNull(CODE_COLUMN.desc());
        }

        @Test
        void testComparableCoalesce() {
                assertNotNull(CODE_COLUMN.coalesce(NAME.mixin()));
                assertNotNull(CODE_COLUMN.coalesce("default"));
                assertNotNull(CODE_COLUMN.coalesce("a", "b"));
        }

        @Test
        void testComparableIfnullNullif() {
                assertNotNull(CODE_COLUMN.ifnull(NAME.mixin()));
                assertNotNull(CODE_COLUMN.ifnull("fallback"));
                assertNotNull(CODE_COLUMN.nullif("test"));
                assertNotNull(CODE_COLUMN.nullif(NAME.mixin()));
        }

        @Test
        void testComparableAsIsNullCount() {
                assertNotNull(CODE_COLUMN.as("alias1"));
                assertNotNull(CODE_COLUMN.isNull());
                assertNotNull(CODE_COLUMN.isNotNull());
                assertNotNull(CODE_COLUMN.count());
                assertNotNull(CODE_COLUMN.countDistinct());
        }

        @Test
        void testComparableEqNe() {
                assertNotNull(CODE_COLUMN.eq("abc"));
                assertNotNull(CODE_COLUMN.eq(NAME.mixin()));
                assertNotNull(CODE_COLUMN.ne("abc"));
                assertNotNull(CODE_COLUMN.ne(NAME.mixin()));
        }

        @Test
        void testComparableInNotIn() {
                assertNotNull(CODE_COLUMN.in(Arrays.asList("a", "b")));
                assertNotNull(CODE_COLUMN.in("a", "b", "c"));
                assertNotNull(CODE_COLUMN.notIn(Arrays.asList("x", "y")));
                assertNotNull(CODE_COLUMN.notIn("x", "y"));
        }

        @Test
        void testComparableWhenBetween() {
                assertNotNull(CODE_COLUMN.when("test"));
                assertNotNull(CODE_COLUMN.when(NAME.mixin()));
                assertNotNull(CODE_COLUMN.between("a", "z"));
                assertNotNull(CODE_COLUMN.between(NAME.mixin(), CODE.mixin()));
                assertNotNull(CODE_COLUMN.notBetween("a", "z"));
                assertNotNull(CODE_COLUMN.notBetween(NAME.mixin(), CODE.mixin()));
        }

        @Test
        void testComparableGtGoeLtLoe() {
                assertNotNull(CODE_COLUMN.gt("m"));
                assertNotNull(CODE_COLUMN.gt(NAME.mixin()));
                assertNotNull(CODE_COLUMN.goe("m"));
                assertNotNull(CODE_COLUMN.goe(NAME.mixin()));
                assertNotNull(CODE_COLUMN.lt("m"));
                assertNotNull(CODE_COLUMN.lt(NAME.mixin()));
                assertNotNull(CODE_COLUMN.loe("m"));
                assertNotNull(CODE_COLUMN.loe(NAME.mixin()));
        }

        @Test
        void testComparableMinMax() {
                assertNotNull(CODE_COLUMN.min());
                assertNotNull(CODE_COLUMN.max());
        }

        // ==================== ExprString ====================

        @Test
        void testStringAppendConcat() {
                assertNotNull(NAME.append("suffix"));
                assertNotNull(NAME.append(CODE.mixin()));
                assertNotNull(NAME.concat("suffix"));
                assertNotNull(NAME.concat(CODE.mixin()));
        }

        @Test
        void testStringCharAt() {
                assertNotNull(NAME.charAt(0));
                assertNotNull(NAME.charAt(Expressions.constant(1)));
        }

        @Test
        void testStringContains() {
                assertNotNull(NAME.contains("test"));
                assertNotNull(NAME.contains(CODE.mixin()));
                assertNotNull(NAME.containsIgnoreCase("test"));
                assertNotNull(NAME.containsIgnoreCase(CODE.mixin()));
        }

        @Test
        void testStringStartsEndsWith() {
                assertNotNull(NAME.startsWith("pre"));
                assertNotNull(NAME.startsWith(CODE.mixin()));
                assertNotNull(NAME.startsWithIgnoreCase("pre"));
                assertNotNull(NAME.startsWithIgnoreCase(CODE.mixin()));
                assertNotNull(NAME.endsWith("suf"));
                assertNotNull(NAME.endsWith(CODE.mixin()));
                assertNotNull(NAME.endsWithIgnoreCase("suf"));
                assertNotNull(NAME.endsWithIgnoreCase(CODE.mixin()));
        }

        @Test
        void testStringEqualsIgnoreCase() {
                assertNotNull(NAME.equalsIgnoreCase("test"));
                assertNotNull(NAME.equalsIgnoreCase(CODE.mixin()));
                assertNotNull(NAME.notEqualsIgnoreCase("test"));
                assertNotNull(NAME.notEqualsIgnoreCase(CODE.mixin()));
        }

        @Test
        void testStringIndexOf() {
                assertNotNull(NAME.indexOf("a"));
                assertNotNull(NAME.indexOf(CODE.mixin()));
                assertNotNull(NAME.indexOf("a", 2));
                assertNotNull(NAME.indexOf(CODE.mixin(), 2));
        }

        @Test
        void testStringEmptyAndLength() {
                assertNotNull(NAME.isEmpty());
                assertNotNull(NAME.isNotEmpty());
                assertNotNull(NAME.length());
        }

        @Test
        void testStringLike() {
                assertNotNull(NAME.like("%test%"));
                assertNotNull(NAME.like(CODE.mixin()));
                assertNotNull(NAME.likeIgnoreCase("%test%"));
                assertNotNull(NAME.likeIgnoreCase(CODE.mixin()));
                assertNotNull(NAME.like("%test%", '\\'));
                assertNotNull(NAME.like(CODE.mixin(), '\\'));
                assertNotNull(NAME.likeIgnoreCase("%test%", '\\'));
                assertNotNull(NAME.likeIgnoreCase(CODE.mixin(), '\\'));
        }

        @Test
        void testStringNotLike() {
                assertNotNull(NAME.notLike("%test%"));
                assertNotNull(NAME.notLike(CODE.mixin()));
                assertNotNull(NAME.notLike("%test%", '\\'));
                assertNotNull(NAME.notLike(CODE.mixin(), '\\'));
        }

        @Test
        void testStringLocate() {
                assertNotNull(NAME.locate("a"));
                assertNotNull(NAME.locate(CODE.mixin()));
                assertNotNull(NAME.locate("a", 2));
                assertNotNull(NAME.locate("a", Expressions.constant(2)));
                NumberExpression<Integer> start = Expressions.numberPath(Integer.class, "start");
                assertNotNull(NAME.locate(CODE.mixin(), start));
        }

        @Test
        void testStringCaseConversion() {
                assertNotNull(NAME.lower());
                assertNotNull(NAME.upper());
                assertNotNull(NAME.toLowerCase());
                assertNotNull(NAME.toUpperCase());
                assertNotNull(NAME.trim());
        }

        @Test
        void testStringMatches() {
                assertNotNull(NAME.matches(".*test.*"));
                assertNotNull(NAME.matches(CODE.mixin()));
        }

        @Test
        void testStringMinMaxPrepend() {
                assertNotNull(NAME.min());
                assertNotNull(NAME.max());
                assertNotNull(NAME.prepend("prefix"));
                assertNotNull(NAME.prepend(CODE.mixin()));
        }

        @Test
        void testStringSubstring() {
                assertNotNull(NAME.substring(1));
                assertNotNull(NAME.substring(1, 5));
                assertNotNull(NAME.substring(Expressions.constant(1)));
                assertNotNull(NAME.substring(Expressions.constant(1), Expressions.constant(5)));
                assertNotNull(NAME.substring(Expressions.constant(1), 5));
                assertNotNull(NAME.substring(1, Expressions.constant(5)));
        }

        @Test
        void testStringValueAndNullifCoalesce() {
                assertNotNull(NAME.stringValue());
                assertNotNull(NAME.nullif("other"));
                assertNotNull(NAME.nullif(CODE.mixin()));
                assertNotNull(NAME.coalesce(CODE.mixin()));
                assertNotNull(NAME.coalesce("default"));
                assertNotNull(NAME.coalesce("a", "b"));
        }

        // ==================== ExprNumber ====================

        @Test
        void testNumberBasic() {
                assertNotNull(ID.stringValue());
                assertNotNull(ID.abs());
        }

        @Test
        void testNumberArithmetic() {
                assertNotNull(ID.add(VOLUME.mixinNumber()));
                assertNotNull(ID.add(10));
                assertNotNull(ID.subtract(VOLUME.mixinNumber()));
                assertNotNull(ID.subtract(5));
                assertNotNull(ID.multiply(VOLUME.mixinNumber()));
                assertNotNull(ID.multiply(2));
                assertNotNull(ID.divide(VOLUME.mixinNumber()));
                assertNotNull(ID.divide(3));
                assertNotNull(ID.mod(VOLUME.mixinNumber()));
                assertNotNull(ID.mod(7));
        }

        @Test
        void testNumberAggregation() {
                assertNotNull(ID.avg());
                assertNotNull(ID.sum());
        }

        @Test
        void testNumberCast() {
                assertNotNull(ID.byteValue());
                assertNotNull(ID.shortValue());
                assertNotNull(ID.intValue());
                assertNotNull(ID.longValue());
                assertNotNull(ID.floatValue());
                assertNotNull(ID.doubleValue());
                assertNotNull(ID.castToNum(Long.class));
        }

        @Test
        void testNumberMath() {
                assertNotNull(ID.ceil());
                assertNotNull(ID.floor());
                assertNotNull(ID.round());
                assertNotNull(ID.sqrt());
                assertNotNull(ID.negate());
        }

        @Test
        void testNumberGtAndLike() {
                assertNotNull(ID.gt(5));
                assertNotNull(ID.like("1%"));
                assertNotNull(ID.like(NAME.mixin()));
        }

        // ==================== ExprDate (via anonymous impl with Instant) ====================

        @Test
        void testExprDateInterface() {
                // Instant implements Comparable<Instant>, so it satisfies T extends Comparable<T>
                DateExpression<Instant> datePath = Expressions.datePath(Instant.class, "testDate");
                ExprDate<Instant> exprDate = new ExprDate<Instant>() {
                        @Override
                        public DateExpression<Instant> mixin() { return datePath; }
                        @Override
                        public <R, C> R accept(com.querydsl.core.types.Visitor<R, C> v, C context) { return datePath.accept(v, context); }
                        @Override
                        public Class<? extends Instant> getType() { return Instant.class; }
                };

                assertNotNull(exprDate.dayOfMonth());
                assertNotNull(exprDate.dayOfWeek());
                assertNotNull(exprDate.dayOfYear());
                assertNotNull(exprDate.month());
                assertNotNull(exprDate.week());
                assertNotNull(exprDate.year());
                assertNotNull(exprDate.yearMonth());
                assertNotNull(exprDate.yearWeek());
                assertNotNull(exprDate.min());
                assertNotNull(exprDate.max());

                Instant d = Instant.now();
                assertNotNull(exprDate.nullif(d));
                assertNotNull(exprDate.nullif(Expressions.constant(d)));
                assertNotNull(exprDate.coalesce(Expressions.constant(d)));
                assertNotNull(exprDate.coalesce(d));
                assertNotNull(exprDate.coalesce(d, Instant.now()));
        }

        // ==================== ExprDateTime ====================

        @Test
        void testExprDateTimeInterface() {
                DateTimeExpression<Instant> dtPath = Expressions.dateTimePath(Instant.class, "testDateTime");
                ExprDateTime<Instant> exprDt = new ExprDateTime<Instant>() {
                        @Override
                        public DateTimeExpression<Instant> mixin() { return dtPath; }
                        @Override
                        public <R, C> R accept(com.querydsl.core.types.Visitor<R, C> v, C context) { return dtPath.accept(v, context); }
                        @Override
                        public Class<? extends Instant> getType() { return Instant.class; }
                };

                assertNotNull(exprDt.dayOfMonth());
                assertNotNull(exprDt.dayOfWeek());
                assertNotNull(exprDt.dayOfYear());
                assertNotNull(exprDt.hour());
                assertNotNull(exprDt.minute());
                assertNotNull(exprDt.second());
                assertNotNull(exprDt.milliSecond());
                assertNotNull(exprDt.month());
                assertNotNull(exprDt.week());
                assertNotNull(exprDt.year());
                assertNotNull(exprDt.yearMonth());
                assertNotNull(exprDt.yearWeek());
                assertNotNull(exprDt.min());
                assertNotNull(exprDt.max());

                Instant now = Instant.now();
                assertNotNull(exprDt.nullif(now));
                assertNotNull(exprDt.nullif(Expressions.constant(now)));
                assertNotNull(exprDt.coalesce(Expressions.constant(now)));
                assertNotNull(exprDt.coalesce(now));
                assertNotNull(exprDt.coalesce(now, Instant.now()));
        }

        // ==================== ExprTime ====================

        @Test
        void testExprTimeInterface() {
                TimeExpression<Instant> timePath = Expressions.timePath(Instant.class, "testTime");
                ExprTime<Instant> exprTime = new ExprTime<Instant>() {
                        @Override
                        public TimeExpression<Instant> mixin() { return timePath; }
                        @Override
                        public <R, C> R accept(com.querydsl.core.types.Visitor<R, C> v, C context) { return timePath.accept(v, context); }
                        @Override
                        public Class<? extends Instant> getType() { return Instant.class; }
                };

                assertNotNull(exprTime.hour());
                assertNotNull(exprTime.minute());
                assertNotNull(exprTime.second());
                assertNotNull(exprTime.milliSecond());

                Instant t = Instant.now();
                assertNotNull(exprTime.nullif(t));
                assertNotNull(exprTime.nullif(Expressions.constant(t)));
                assertNotNull(exprTime.coalesce(Expressions.constant(t)));
                assertNotNull(exprTime.coalesce(t));
                assertNotNull(exprTime.coalesce(t, Instant.now()));
        }

        // ==================== ExprTemporal ====================

        @Test
        void testTemporalBeforeAfter() {
                DateExpression<Instant> datePath = Expressions.datePath(Instant.class, "testDate2");
                ExprDate<Instant> exprDate = new ExprDate<Instant>() {
                        @Override
                        public DateExpression<Instant> mixin() { return datePath; }
                        @Override
                        public <R, C> R accept(com.querydsl.core.types.Visitor<R, C> v, C context) { return datePath.accept(v, context); }
                        @Override
                        public Class<? extends Instant> getType() { return Instant.class; }
                };

                Instant d = Instant.now();
                assertNotNull(exprDate.after(d));
                assertNotNull(exprDate.after(Expressions.constant(d)));
                assertNotNull(exprDate.before(d));
                assertNotNull(exprDate.before(Expressions.constant(d)));
        }

        // ==================== ExprBoolean ====================

        @Test
        void testBooleanOperations() {
                BooleanExpression condition = NAME.eq("test");
                assertNotNull(condition.and(CODE_COLUMN.eq("abc")));
                assertNotNull(condition.or(CODE_COLUMN.eq("xyz")));
                assertNotNull(condition.not());
        }

        // ==================== LambdaHelpers ====================

        @Test
        void testLambdaHelpers() {
                LambdaHelpers helpers = new LambdaHelpers() {};

                LambdaColumn<Foo, String> col = helpers.column(Foo::getCode);
                assertNotNull(col);
                assertSame(col, helpers.$(col));

                StringLambdaColumn<Foo> strCol = helpers.string(Foo::getName);
                assertNotNull(strCol);
                assertSame(strCol, helpers.s(strCol));

                NumberLambdaColumn<Foo, Integer> numCol = helpers.num(Foo::getId);
                assertNotNull(numCol);
                assertSame(numCol, helpers.n(numCol));

                DateTimeLambdaColumn<Foo, Instant> dtCol = helpers.datetime(Foo::getCreated);
                assertNotNull(dtCol);
                assertSame(dtCol, helpers.dt(dtCol));
        }

        // ==================== LambdaTable ====================

        @Test
        void testLambdaTableBasic() {
                LambdaTable<Foo> table = () -> Foo.class;
                assertNotNull(table.getTableName());
                assertNotNull(table.getColumns());
                assertNotNull(table.getPrimaryKey());
                assertNotNull(table.getSchemaAndTable());
                assertNotNull(table.getType());
                assertNotNull(table.getMetadata());
                assertNotNull(table.getProjection());
                assertNotNull(table.getRoot());
                assertNotNull(table.getAnnotatedElement());
                assertNotNull(table.getComment());
                assertNotNull(table.getBeanCodec());
                assertNotNull(table.getForeignKeys());
                assertNotNull(table.getInverseForeignKeys());
        }

        @Test
        void testLambdaTableForVariable() {
                LambdaTable<Foo> table = () -> Foo.class;
                assertNotNull(table.forVariable("t1"));
        }

        @Test
        void testLambdaTableAccept() {
                LambdaTable<Foo> table = () -> Foo.class;
                Object result = table.accept(new com.querydsl.core.types.Visitor<Object, Void>() {
                        @Override public Object visit(com.querydsl.core.types.Constant<?> expr, Void ctx) { return null; }
                        @Override public Object visit(com.querydsl.core.types.FactoryExpression<?> expr, Void ctx) { return null; }
                        @Override public Object visit(com.querydsl.core.types.Operation<?> expr, Void ctx) { return null; }
                        @Override public Object visit(com.querydsl.core.types.ParamExpression<?> expr, Void ctx) { return null; }
                        @Override public Object visit(com.querydsl.core.types.Path<?> expr, Void ctx) { return "visited"; }
                        @Override public Object visit(com.querydsl.core.types.SubQueryExpression<?> expr, Void ctx) { return null; }
                        @Override public Object visit(com.querydsl.core.types.TemplateExpression<?> expr, Void ctx) { return null; }
                }, null);
                assertEquals("visited", result);
        }

        // ==================== PathCache ====================

        @Test
        void testPathCacheGetAndRegister() {
                assertNotNull(PathCache.get(Foo.class, null));
                assertNotNull(PathCache.get(Foo.class, "alias1"));
                assertSame(PathCache.get(Foo.class, "alias1"), PathCache.get(Foo.class, "alias1"));
        }

        @Test
        void testPathCacheGetPathAsExpr() {
                assertNotNull(PathCache.getPathAsExpr((LambdaColumn<Foo, String>) Foo::getCode));
        }

        // ==================== LambdaColumnBase ====================

        @Test
        void testLambdaColumnBaseMetadata() {
                LambdaColumn<Foo, String> col = Foo::getCode;
                assertNotNull(col.getMetadata());
                assertNotNull(col.getRoot());
                assertNotNull(col.getAnnotatedElement());
                assertNotNull(col.getType());
        }

        @Test
        void testLambdaColumnBaseAccept() {
                LambdaColumn<Foo, String> col = Foo::getCode;
                Object result = col.accept(new com.querydsl.core.types.Visitor<Object, Void>() {
                        @Override public Object visit(com.querydsl.core.types.Constant<?> expr, Void ctx) { return null; }
                        @Override public Object visit(com.querydsl.core.types.FactoryExpression<?> expr, Void ctx) { return null; }
                        @Override public Object visit(com.querydsl.core.types.Operation<?> expr, Void ctx) { return null; }
                        @Override public Object visit(com.querydsl.core.types.ParamExpression<?> expr, Void ctx) { return null; }
                        @Override public Object visit(com.querydsl.core.types.Path<?> expr, Void ctx) { return "col_visited"; }
                        @Override public Object visit(com.querydsl.core.types.SubQueryExpression<?> expr, Void ctx) { return null; }
                        @Override public Object visit(com.querydsl.core.types.TemplateExpression<?> expr, Void ctx) { return null; }
                }, null);
                assertEquals("col_visited", result);
        }
}