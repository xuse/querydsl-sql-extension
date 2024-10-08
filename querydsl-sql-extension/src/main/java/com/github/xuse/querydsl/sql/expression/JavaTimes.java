package com.github.xuse.querydsl.sql.expression;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.Temporal;

import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.DateTimeOperation;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.TimeExpression;

public class JavaTimes {
	/**
	 * Create an expression representing the current date as a DateExpression
	 * instance
	 *
	 * @return current date
	 */
	public static DateExpression<LocalDate> currentDate() {
		return Expressions.dateOperation(LocalDate.class, Ops.DateTimeOps.CURRENT_DATE);
	}

	/**
	 * Create an expression representing the current time instant as a
	 * DateTimeExpression instance
	 *
	 * @return current timestamp
	 */
	public static DateTimeOperation<Instant> currentTimestamp() {
		return Expressions.dateTimeOperation(Instant.class, Ops.DateTimeOps.CURRENT_TIMESTAMP);
	}

	/**
	 * Create an expression representing the current time instant as a
	 * DateTimeExpression instance
	 * @param <T> type of the Temporal
	 * @param clz type
	 * @return current timestamp
	 */
	public static <T extends Temporal & Comparable<T>> DateTimeOperation<T> currentTimestamp(Class<T> clz) {
		return Expressions.dateTimeOperation(clz, Ops.DateTimeOps.CURRENT_TIMESTAMP);
	}

	/**
	 * Create an expression representing the current time as a TimeExpression
	 * instance
	 *
	 * @return current time
	 */
	public static TimeExpression<LocalTime> currentTime() {
		return Expressions.timeOperation(LocalTime.class, Ops.DateTimeOps.CURRENT_TIME);
	}

}
