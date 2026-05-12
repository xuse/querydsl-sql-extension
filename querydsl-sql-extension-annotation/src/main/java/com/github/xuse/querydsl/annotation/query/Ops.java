package com.github.xuse.querydsl.annotation.query;

/**
 * <h2>English:</h2>
 * Enumerates the comparison operators available for {@link Condition}, {@link StringCase},
 * {@link BoolCase}, and {@link IntCase} annotations. Each constant corresponds to a SQL
 * predicate pattern applied to the target column.
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * @Condition(Ops.STRING_CONTAINS)
 * private String name;  // → WHERE name LIKE '%value%'
 *
 * @Condition(Ops.BETWEEN)
 * private String dateRange;  // → WHERE col BETWEEN val1 AND val2 (comma-separated)
 * }</pre>
 *
 * <h2>Chinese:</h2>
 * 枚举 {@link Condition}、{@link StringCase}、{@link BoolCase} 和 {@link IntCase}
 * 注解可用的比较运算符。每个常量对应一种作用于目标列的 SQL 谓词模式。
 *
 * @see Condition#value()
 * @see StringCase#ops()
 * @see BoolCase#ops()
 * @see IntCase#ops()
 */
public enum Ops {
	/**
	 * A equals expr.
	 */
	EQ,
	/**
	 * A in (expr1, expr2, ...)
	 */
	IN,
	/**
	 * A BETWEEN expr1 AND expr2
	 */
	BETWEEN,
	/**
	 * A or B
	 */
	OR,
	/**
	 * A is null
	 */
	IS_NULL,
	/**
	 * A is not null
	 */
	IS_NOT_NULL,
	/**
	 * A < expr
	 */
	LT,
	/**
	 * A > expr
	 */
	GT,
	/**
	 * A < = expr
	 */
	LOE,
	/**
	 * A >= expr
	 */
	GOE,
	/**
	 * A LIKE 'string%' ESCAPE '/'
	 */
	STARTS_WITH,
	/**
	 * A LIKE 'string%' ESCAPE '/' (Ignoring case)
	 */
	STARTS_WITH_IC,
	/**
	 * A LIKE '%string' ESCAPE '/'
	 */
	ENDS_WITH,
	/**
	 * A LIKE '%string' ESCAPE '/' (Ignoring case)
	 */
	ENDS_WITH_IC,
	/**
	 * A LIKE '%string%'
	 */
	STRING_CONTAINS,
	/**
	 * A LIKE '%string%' ESCAPE '/'(Ignoring case)
	 */
	STRING_CONTAINS_IC,
	
	/**
	 * A LIKE 'string' (Without escape)
	 * @implNote
	 * Be aware of <strong>SQL INJECTION RISK</strong> 
	 */
	LIKE,
	/**
	 * A LIKE 'string' (Without escape, ignoring case)
	 * @implNote
	 * Be aware of <strong>SQL INJECTION RISK</strong>
	 */
	LIKE_IC
}
