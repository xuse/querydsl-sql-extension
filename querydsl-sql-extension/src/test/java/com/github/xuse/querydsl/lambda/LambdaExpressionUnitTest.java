package com.github.xuse.querydsl.lambda;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.TimeExpression;

/**
 * Unit tests for lambda expression interfaces (ExprString, ExprNumber, ExprDate,
 * ExprDateTime, ExprTime) verifying that their default methods build correct
 * QueryDSL expression types.
 *
 * These tests use anonymous implementations backed by QueryDSL Expressions paths,
 * avoiding the need for PathCache initialization or database setup.
 */
@DisplayName("Lambda Expression Unit Tests")
class LambdaExpressionUnitTest {

    // ==================== ExprString ====================

    @Nested
    @DisplayName("ExprString builds StringExpression types")
    class ExprStringTests {

        private final StringExpression stringPath = Expressions.stringPath("testName");
        private final ExprString exprString = new ExprString() {
            @Override
            public StringExpression mixin() {
                return stringPath;
            }

			@Override
			public <R, C> @Nullable R accept(Visitor<R, C> v, @Nullable C context) {
				return stringPath.accept(v, context);
			}

			@Override
			public Class<? extends String> getType() {
				return String.class;
			}
        };

        @Test
        @DisplayName("append returns StringExpression")
        void testAppendReturnsStringExpression() {
            assertInstanceOf(StringExpression.class, exprString.append("suffix"));
            assertInstanceOf(StringExpression.class, exprString.append(Expressions.stringPath("other")));
        }

        @Test
        @DisplayName("concat returns StringExpression")
        void testConcatReturnsStringExpression() {
            assertInstanceOf(StringExpression.class, exprString.concat("suffix"));
            assertInstanceOf(StringExpression.class, exprString.concat(Expressions.stringPath("other")));
        }

        @Test
        @DisplayName("charAt returns SimpleExpression<Character>")
        void testCharAtReturnsSimpleExpression() {
            SimpleExpression<Character> result = exprString.charAt(0);
            assertNotNull(result);
            assertInstanceOf(SimpleExpression.class, result);
        }

        @Test
        @DisplayName("contains returns BooleanExpression")
        void testContainsReturnsBooleanExpression() {
            assertInstanceOf(BooleanExpression.class, exprString.contains("test"));
            assertInstanceOf(BooleanExpression.class, exprString.contains(Expressions.stringPath("other")));
            assertInstanceOf(BooleanExpression.class, exprString.containsIgnoreCase("test"));
            assertInstanceOf(BooleanExpression.class, exprString.containsIgnoreCase(Expressions.stringPath("other")));
        }

        @Test
        @DisplayName("startsWith/endsWith returns BooleanExpression")
        void testStartsEndsWithReturnsBooleanExpression() {
            assertInstanceOf(BooleanExpression.class, exprString.startsWith("pre"));
            assertInstanceOf(BooleanExpression.class, exprString.startsWithIgnoreCase("pre"));
            assertInstanceOf(BooleanExpression.class, exprString.endsWith("suf"));
            assertInstanceOf(BooleanExpression.class, exprString.endsWithIgnoreCase("suf"));
        }

        @Test
        @DisplayName("like returns BooleanExpression")
        void testLikeReturnsBooleanExpression() {
            assertInstanceOf(BooleanExpression.class, exprString.like("%test%"));
            assertInstanceOf(BooleanExpression.class, exprString.like(Expressions.stringPath("pattern")));
            assertInstanceOf(BooleanExpression.class, exprString.likeIgnoreCase("%test%"));
            assertInstanceOf(BooleanExpression.class, exprString.notLike("%test%"));
        }

        @Test
        @DisplayName("indexOf returns NumberExpression<Integer>")
        void testIndexOfReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprString.indexOf("a"));
            assertInstanceOf(NumberExpression.class, exprString.indexOf("a", 2));
        }

        @Test
        @DisplayName("length returns NumberExpression<Integer>")
        void testLengthReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprString.length());
        }

        @Test
        @DisplayName("isEmpty/isNotEmpty returns BooleanExpression")
        void testIsEmptyReturnsBooleanExpression() {
            assertInstanceOf(BooleanExpression.class, exprString.isEmpty());
            assertInstanceOf(BooleanExpression.class, exprString.isNotEmpty());
        }

        @Test
        @DisplayName("lower/upper/trim returns StringExpression")
        void testCaseConversionReturnsStringExpression() {
            assertInstanceOf(StringExpression.class, exprString.lower());
            assertInstanceOf(StringExpression.class, exprString.upper());
            assertInstanceOf(StringExpression.class, exprString.toLowerCase());
            assertInstanceOf(StringExpression.class, exprString.toUpperCase());
            assertInstanceOf(StringExpression.class, exprString.trim());
        }

        @Test
        @DisplayName("substring returns StringExpression")
        void testSubstringReturnsStringExpression() {
            assertInstanceOf(StringExpression.class, exprString.substring(1));
            assertInstanceOf(StringExpression.class, exprString.substring(1, 5));
        }

        @Test
        @DisplayName("prepend returns StringExpression")
        void testPrependReturnsStringExpression() {
            assertInstanceOf(StringExpression.class, exprString.prepend("prefix"));
            assertInstanceOf(StringExpression.class, exprString.prepend(Expressions.stringPath("other")));
        }

        @Test
        @DisplayName("min/max returns StringExpression")
        void testMinMaxReturnsStringExpression() {
            assertInstanceOf(StringExpression.class, exprString.min());
            assertInstanceOf(StringExpression.class, exprString.max());
        }

        @Test
        @DisplayName("stringValue returns StringExpression")
        void testStringValueReturnsStringExpression() {
            assertInstanceOf(StringExpression.class, exprString.stringValue());
        }

        @Test
        @DisplayName("nullif returns StringExpression")
        void testNullifReturnsStringExpression() {
            assertInstanceOf(StringExpression.class, exprString.nullif("other"));
            assertInstanceOf(StringExpression.class, exprString.nullif(Expressions.stringPath("other")));
        }

        @Test
        @DisplayName("coalesce returns StringExpression")
        void testCoalesceReturnsStringExpression() {
            assertInstanceOf(StringExpression.class, exprString.coalesce("default"));
            assertInstanceOf(StringExpression.class, exprString.coalesce(Expressions.stringPath("other")));
            assertInstanceOf(StringExpression.class, exprString.coalesce("a", "b"));
        }

        @Test
        @DisplayName("matches returns BooleanExpression")
        void testMatchesReturnsBooleanExpression() {
            assertInstanceOf(BooleanExpression.class, exprString.matches(".*test.*"));
            assertInstanceOf(BooleanExpression.class, exprString.matches(Expressions.stringPath("regex")));
        }

        @Test
        @DisplayName("equalsIgnoreCase returns BooleanExpression")
        void testEqualsIgnoreCaseReturnsBooleanExpression() {
            assertInstanceOf(BooleanExpression.class, exprString.equalsIgnoreCase("test"));
            assertInstanceOf(BooleanExpression.class, exprString.notEqualsIgnoreCase("test"));
        }

        @Test
        @DisplayName("locate returns NumberExpression<Integer>")
        void testLocateReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprString.locate("a"));
            assertInstanceOf(NumberExpression.class, exprString.locate("a", 2));
        }
    }

    // ==================== ExprNumber ====================

    @Nested
    @DisplayName("ExprNumber builds NumberExpression types")
    class ExprNumberTests {

        private final NumberExpression<Integer> numberPath = Expressions.numberPath(Integer.class, "testId");
        private final ComparableExpression<Integer> comparablePath = Expressions.comparablePath(Integer.class, "testId");
        private final ExprNumber<Integer> exprNumber = new ExprNumber<Integer>() {
            @Override
            public NumberExpression<Integer> mixinNumber() {
                return numberPath;
            }

			@Override
			public ComparableExpression<Integer> mixin() {
				return comparablePath;
			}

			@Override
			public <R, C> @Nullable R accept(Visitor<R, C> v, @Nullable C context) {
				return numberPath.accept(v, context);
			}

			@Override
			public Class<? extends Integer> getType() {
				return Integer.class;
			}
        };

        @Test
        @DisplayName("abs returns NumberExpression")
        void testAbsReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprNumber.abs());
        }

        @Test
        @DisplayName("add returns NumberExpression")
        void testAddReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprNumber.add(10));
            assertInstanceOf(NumberExpression.class, exprNumber.add(Expressions.numberPath(Integer.class, "other")));
        }

        @Test
        @DisplayName("subtract returns NumberExpression")
        void testSubtractReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprNumber.subtract(5));
            assertInstanceOf(NumberExpression.class, exprNumber.subtract(Expressions.numberPath(Integer.class, "other")));
        }

        @Test
        @DisplayName("multiply returns NumberExpression")
        void testMultiplyReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprNumber.multiply(2));
            assertInstanceOf(NumberExpression.class, exprNumber.multiply(Expressions.numberPath(Integer.class, "other")));
        }

        @Test
        @DisplayName("divide returns NumberExpression")
        void testDivideReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprNumber.divide(3));
            assertInstanceOf(NumberExpression.class, exprNumber.divide(Expressions.numberPath(Integer.class, "other")));
        }

        @Test
        @DisplayName("mod returns NumberExpression")
        void testModReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprNumber.mod(7));
            assertInstanceOf(NumberExpression.class, exprNumber.mod(Expressions.numberPath(Integer.class, "other")));
        }

        @Test
        @DisplayName("avg returns NumberExpression<Double>")
        void testAvgReturnsNumberExpression() {
            NumberExpression<Double> result = exprNumber.avg();
            assertInstanceOf(NumberExpression.class, result);
        }

        @Test
        @DisplayName("sum returns NumberExpression")
        void testSumReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprNumber.sum());
        }

        @Test
        @DisplayName("ceil/floor/round returns NumberExpression")
        void testMathReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprNumber.ceil());
            assertInstanceOf(NumberExpression.class, exprNumber.floor());
            assertInstanceOf(NumberExpression.class, exprNumber.round());
        }

        @Test
        @DisplayName("sqrt returns NumberExpression<Double>")
        void testSqrtReturnsNumberExpression() {
            NumberExpression<Double> result = exprNumber.sqrt();
            assertInstanceOf(NumberExpression.class, result);
        }

        @Test
        @DisplayName("negate returns NumberExpression")
        void testNegateReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprNumber.negate());
        }

        @Test
        @DisplayName("castToNum returns NumberExpression of target type")
        void testCastToNumReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprNumber.castToNum(Long.class));
            assertInstanceOf(NumberExpression.class, exprNumber.byteValue());
            assertInstanceOf(NumberExpression.class, exprNumber.shortValue());
            assertInstanceOf(NumberExpression.class, exprNumber.intValue());
            assertInstanceOf(NumberExpression.class, exprNumber.longValue());
            assertInstanceOf(NumberExpression.class, exprNumber.floatValue());
            assertInstanceOf(NumberExpression.class, exprNumber.doubleValue());
        }

        @Test
        @DisplayName("stringValue returns StringExpression")
        void testStringValueReturnsStringExpression() {
            assertInstanceOf(StringExpression.class, exprNumber.stringValue());
        }

        @Test
        @DisplayName("gt returns BooleanExpression")
        void testGtReturnsBooleanExpression() {
            assertInstanceOf(BooleanExpression.class, exprNumber.gt(5));
        }

        @Test
        @DisplayName("like returns BooleanExpression")
        void testLikeReturnsBooleanExpression() {
            assertInstanceOf(BooleanExpression.class, exprNumber.like("1%"));
            assertInstanceOf(BooleanExpression.class, exprNumber.like(Expressions.stringPath("pattern")));
        }
    }

    // ==================== ExprDate ====================

    @Nested
    @DisplayName("ExprDate builds DateExpression types")
    class ExprDateTests {

        private final DateExpression<LocalDate> datePath = Expressions.datePath(LocalDate.class, "testDate");
        private final ExprDate<LocalDate> exprDate = new ExprDate<LocalDate>() {
            @SuppressWarnings("rawtypes")
            @Override
            public DateExpression mixin() {
                return datePath;
            }

            @Override
            public <R, C> R accept(com.querydsl.core.types.Visitor<R, C> v, C context) {
                return datePath.accept(v, context);
            }

            @Override
            public Class<? extends LocalDate> getType() {
                return LocalDate.class;
            }
        };

        @Test
        @DisplayName("dayOfMonth returns NumberExpression<Integer>")
        void testDayOfMonthReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDate.dayOfMonth());
        }

        @Test
        @DisplayName("dayOfWeek returns NumberExpression<Integer>")
        void testDayOfWeekReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDate.dayOfWeek());
        }

        @Test
        @DisplayName("dayOfYear returns NumberExpression<Integer>")
        void testDayOfYearReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDate.dayOfYear());
        }

        @Test
        @DisplayName("month returns NumberExpression<Integer>")
        void testMonthReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDate.month());
        }

        @Test
        @DisplayName("week returns NumberExpression<Integer>")
        void testWeekReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDate.week());
        }

        @Test
        @DisplayName("year returns NumberExpression<Integer>")
        void testYearReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDate.year());
        }

        @Test
        @DisplayName("yearMonth returns NumberExpression<Integer>")
        void testYearMonthReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDate.yearMonth());
        }

        @Test
        @DisplayName("yearWeek returns NumberExpression<Integer>")
        void testYearWeekReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDate.yearWeek());
        }

        @Test
        @DisplayName("min/max returns ComparableExpression")
        void testMinMaxReturnsComparableExpression() {
            assertInstanceOf(ComparableExpression.class, exprDate.min());
            assertInstanceOf(ComparableExpression.class, exprDate.max());
        }

        @Test
        @DisplayName("nullif returns ComparableExpression")
        void testNullifReturnsComparableExpression() {
            LocalDate d = LocalDate.of(2025, 1, 1);
            assertInstanceOf(ComparableExpression.class, exprDate.nullif(d));
            assertInstanceOf(ComparableExpression.class, exprDate.nullif(Expressions.constant(d)));
        }

        @Test
        @DisplayName("coalesce returns ComparableExpression")
        void testCoalesceReturnsComparableExpression() {
            LocalDate d = LocalDate.of(2025, 1, 1);
            assertInstanceOf(ComparableExpression.class, exprDate.coalesce(d));
            assertInstanceOf(ComparableExpression.class, exprDate.coalesce(Expressions.constant(d)));
        }

        @Test
        @DisplayName("mixin() returns DateExpression")
        void testMixinReturnsDateExpression() {
            assertInstanceOf(DateExpression.class, exprDate.mixin());
        }
    }

    // ==================== ExprDateTime ====================

    @Nested
    @DisplayName("ExprDateTime builds DateTimeExpression types")
    class ExprDateTimeTests {

        private final DateTimeExpression<Instant> dateTimePath = Expressions.dateTimePath(Instant.class, "testDateTime");
        private final ExprDateTime<Instant> exprDateTime = new ExprDateTime<Instant>() {
            @SuppressWarnings("rawtypes")
            @Override
            public DateTimeExpression mixin() {
                return dateTimePath;
            }

            @Override
            public <R, C> R accept(com.querydsl.core.types.Visitor<R, C> v, C context) {
                return dateTimePath.accept(v, context);
            }

            @Override
            public Class<? extends Instant> getType() {
                return Instant.class;
            }
        };

        @Test
        @DisplayName("dayOfMonth returns NumberExpression<Integer>")
        void testDayOfMonthReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDateTime.dayOfMonth());
        }

        @Test
        @DisplayName("dayOfWeek returns NumberExpression<Integer>")
        void testDayOfWeekReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDateTime.dayOfWeek());
        }

        @Test
        @DisplayName("dayOfYear returns NumberExpression<Integer>")
        void testDayOfYearReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDateTime.dayOfYear());
        }

        @Test
        @DisplayName("hour returns NumberExpression<Integer>")
        void testHourReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDateTime.hour());
        }

        @Test
        @DisplayName("minute returns NumberExpression<Integer>")
        void testMinuteReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDateTime.minute());
        }

        @Test
        @DisplayName("second returns NumberExpression<Integer>")
        void testSecondReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDateTime.second());
        }

        @Test
        @DisplayName("milliSecond returns NumberExpression<Integer>")
        void testMilliSecondReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDateTime.milliSecond());
        }

        @Test
        @DisplayName("month returns NumberExpression<Integer>")
        void testMonthReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDateTime.month());
        }

        @Test
        @DisplayName("week returns NumberExpression<Integer>")
        void testWeekReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDateTime.week());
        }

        @Test
        @DisplayName("year returns NumberExpression<Integer>")
        void testYearReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDateTime.year());
        }

        @Test
        @DisplayName("yearMonth returns NumberExpression<Integer>")
        void testYearMonthReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDateTime.yearMonth());
        }

        @Test
        @DisplayName("yearWeek returns NumberExpression<Integer>")
        void testYearWeekReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprDateTime.yearWeek());
        }

        @Test
        @DisplayName("min/max returns ComparableExpression")
        void testMinMaxReturnsComparableExpression() {
            assertInstanceOf(ComparableExpression.class, exprDateTime.min());
            assertInstanceOf(ComparableExpression.class, exprDateTime.max());
        }

        @Test
        @DisplayName("nullif returns ComparableExpression")
        void testNullifReturnsComparableExpression() {
            Instant now = Instant.now();
            assertInstanceOf(ComparableExpression.class, exprDateTime.nullif(now));
            assertInstanceOf(ComparableExpression.class, exprDateTime.nullif(Expressions.constant(now)));
        }

        @Test
        @DisplayName("coalesce returns ComparableExpression")
        void testCoalesceReturnsComparableExpression() {
            Instant now = Instant.now();
            assertInstanceOf(ComparableExpression.class, exprDateTime.coalesce(now));
            assertInstanceOf(ComparableExpression.class, exprDateTime.coalesce(Expressions.constant(now)));
        }

        @Test
        @DisplayName("mixin() returns DateTimeExpression")
        void testMixinReturnsDateTimeExpression() {
            assertInstanceOf(DateTimeExpression.class, exprDateTime.mixin());
        }
    }

    // ==================== ExprTime ====================

    @Nested
    @DisplayName("ExprTime builds TimeExpression types")
    class ExprTimeTests {

        private final TimeExpression<LocalTime> timePath = Expressions.timePath(LocalTime.class, "testTime");
        private final ExprTime<LocalTime> exprTime = new ExprTime<LocalTime>() {
            @SuppressWarnings("rawtypes")
            @Override
            public TimeExpression mixin() {
                return timePath;
            }

            @Override
            public <R, C> R accept(com.querydsl.core.types.Visitor<R, C> v, C context) {
                return timePath.accept(v, context);
            }

            @Override
            public Class<? extends LocalTime> getType() {
                return LocalTime.class;
            }
        };

        @Test
        @DisplayName("hour returns NumberExpression<Integer>")
        void testHourReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprTime.hour());
        }

        @Test
        @DisplayName("minute returns NumberExpression<Integer>")
        void testMinuteReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprTime.minute());
        }

        @Test
        @DisplayName("second returns NumberExpression<Integer>")
        void testSecondReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprTime.second());
        }

        @Test
        @DisplayName("milliSecond returns NumberExpression<Integer>")
        void testMilliSecondReturnsNumberExpression() {
            assertInstanceOf(NumberExpression.class, exprTime.milliSecond());
        }

        @Test
        @DisplayName("nullif returns ComparableExpression")
        void testNullifReturnsComparableExpression() {
            LocalTime t = LocalTime.of(10, 30, 0);
            assertInstanceOf(ComparableExpression.class, exprTime.nullif(t));
            assertInstanceOf(ComparableExpression.class, exprTime.nullif(Expressions.constant(t)));
        }

        @Test
        @DisplayName("coalesce returns ComparableExpression")
        void testCoalesceReturnsComparableExpression() {
            LocalTime t = LocalTime.of(10, 30, 0);
            assertInstanceOf(ComparableExpression.class, exprTime.coalesce(t));
            assertInstanceOf(ComparableExpression.class, exprTime.coalesce(Expressions.constant(t)));
        }

        @Test
        @DisplayName("mixin() returns TimeExpression")
        void testMixinReturnsTimeExpression() {
            assertInstanceOf(TimeExpression.class, exprTime.mixin());
        }
    }
}
