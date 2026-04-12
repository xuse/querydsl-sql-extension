package com.github.xuse.querydsl.annotation.query;

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
