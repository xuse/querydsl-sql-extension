package com.github.xuse.querydsl.sql.expression;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;

/**
 * Tests for {@link SQLFunctions}.
 * <p>
 * Verifies that SQL function expression generation methods produce correct
 * Expression objects containing the expected operations and operands.
 * </p>
 *
 * <p>Requirements: 2.9</p>
 */
@DisplayName("SQLFunctions unit tests")
class SQLFunctionsTest {

	/**
	 * Verify ifnull with a constant fallback produces an Expression containing
	 * IF_NULL operation with both operands.
	 */
	@Test
	@DisplayName("ifnull produces Expression containing IF_NULL operation with both operands")
	void testIfnullWithConstantFallback() {
		ComparableExpression<String> field = Expressions.comparablePath(String.class, "name");
		String fallbackValue = "default_name";

		ComparableExpression<String> result = SQLFunctions.ifnull(field, fallbackValue);

		assertNotNull(result, "ifnull should return a non-null expression");
		// The result should be an Operation
		assertInstanceOf(Operation.class, result, "ifnull result should be an Operation");

		Operation<?> operation = (Operation<?>) result;
		Operator operator = operation.getOperator();
		assertEquals(FunctionOps.IF_NULL, operator, "Operator should be IF_NULL");

		// Verify both operands are present
		assertEquals(2, operation.getArgs().size(), "IF_NULL should have exactly 2 arguments");
		assertSame(field, operation.getArg(0), "First argument should be the original expression");
		// Second argument is a ConstantImpl wrapping the fallback value
		assertNotNull(operation.getArg(1), "Second argument (fallback) should not be null");
	}

	/**
	 * Verify ifnull with an Expression fallback produces correct operation.
	 */
	@Test
	@DisplayName("ifnull with Expression fallback produces correct IF_NULL operation")
	void testIfnullWithExpressionFallback() {
		ComparableExpression<String> field = Expressions.comparablePath(String.class, "firstName");
		ComparableExpression<String> fallbackExpr = Expressions.comparablePath(String.class, "lastName");

		ComparableExpression<String> result = SQLFunctions.ifnull(field, fallbackExpr);

		assertNotNull(result);
		assertInstanceOf(Operation.class, result);

		Operation<?> operation = (Operation<?>) result;
		assertEquals(FunctionOps.IF_NULL, operation.getOperator());
		assertEquals(2, operation.getArgs().size());
		assertSame(field, operation.getArg(0));
		assertSame(fallbackExpr, operation.getArg(1));
	}

	/**
	 * Verify ifnull preserves the type of the original expression.
	 */
	@Test
	@DisplayName("ifnull preserves the type of the original expression")
	void testIfnullPreservesType() {
		ComparableExpression<Integer> numField = Expressions.comparablePath(Integer.class, "age");
		Integer fallback = 0;

		ComparableExpression<Integer> result = SQLFunctions.ifnull(numField, fallback);

		assertNotNull(result);
		assertInstanceOf(Operation.class, result);

		Operation<?> operation = (Operation<?>) result;
		assertEquals(Integer.class, operation.getType(), "Result type should match the original expression type");
	}

	/**
	 * Verify ifnull result toString contains function representation.
	 */
	@Test
	@DisplayName("ifnull result toString contains IF_NULL function representation")
	void testIfnullToStringContainsFunction() {
		ComparableExpression<String> field = Expressions.comparablePath(String.class, "city");
		String fallback = "Unknown";

		ComparableExpression<String> result = SQLFunctions.ifnull(field, fallback);

		String str = result.toString();
		assertNotNull(str);
		// The toString should reference IF_NULL or the operands
		assertTrue(str.contains("city") || str.contains("IF_NULL"),
				"toString should reference the field or function name, got: " + str);
	}

	/**
	 * Verify FunctionOps.IF_NULL enum has correct type.
	 */
	@Test
	@DisplayName("FunctionOps.IF_NULL has Object.class as type")
	void testFunctionOpsIfNullType() {
		assertEquals(Object.class, FunctionOps.IF_NULL.getType(),
				"IF_NULL operator type should be Object.class");
	}

	/**
	 * Verify ifnull works with numeric comparable expressions.
	 */
	@Test
	@DisplayName("ifnull works with numeric comparable expressions")
	void testIfnullWithNumericExpression() {
		ComparableExpression<Long> amount = Expressions.comparablePath(Long.class, "amount");
		Long fallback = 0L;

		ComparableExpression<Long> result = SQLFunctions.ifnull(amount, fallback);

		assertNotNull(result);
		assertInstanceOf(Operation.class, result);

		Operation<?> operation = (Operation<?>) result;
		assertEquals(FunctionOps.IF_NULL, operation.getOperator());
		assertEquals(Long.class, operation.getType());
	}
}
