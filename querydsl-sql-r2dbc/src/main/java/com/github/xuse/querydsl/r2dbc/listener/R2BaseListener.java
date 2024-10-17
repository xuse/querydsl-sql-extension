package com.github.xuse.querydsl.r2dbc.listener;

import com.querydsl.sql.SQLListener;

import io.r2dbc.spi.Connection;

public interface R2BaseListener extends SQLListener {
	/**
	 * Called at the start of a query. Most context parameters are empty at this
	 * stage
	 *
	 * @param context a context object that is progressively filled out as the query
	 *                executes
	 */
	default void start(R2ListenerContext context) {
	}

	/**
	 * Called at the start of SQL rendering.
	 *
	 * @param context a context object that is progressively filled out as the query
	 *                executes
	 */
	default void preRender(R2ListenerContext context) {
	}

	/**
	 * Called at the end of SQL rendering. The sql context value will not be
	 * available
	 *
	 * @param context a context object that is progressively filled out as the query
	 *                executes
	 */
	default void rendered(R2ListenerContext context) {
	}

	/**
	 * Called at the start of {@link java.sql.PreparedStatement} preparation.
	 *
	 * @param context a context object that is progressively filled out as the query
	 *                executes
	 */
	default void prePrepare(R2ListenerContext context) {
	}

	/**
	 * Called at the end of {@link java.sql.PreparedStatement} preparation.
	 *
	 * @param context a context object that is progressively filled out as the query
	 *                executes
	 */
	default void prepared(R2ListenerContext context) {
	}

	/**
	 * Called at the start of {@link java.sql.PreparedStatement} execution.
	 *
	 * @param context a context object that is progressively filled out as the query
	 *                executes
	 */
	default void preExecute(R2ListenerContext context) {
	}

	/**
	 * Called at the end of {@link java.sql.PreparedStatement} execution.
	 *
	 * @param context a context object that is progressively filled out as the query
	 *                executes
	 */
	default void executed(R2ListenerContext context) {
	}

	/**
	 * Called if an exception happens during query building and execution. The
	 * context exception values will now be available indicating the exception that
	 * occurred.
	 *
	 * @param context a context object that is progressively filled out as the query
	 *                executes
	 */
	default void exception(R2ListenerContext context) {
	}

	/**
	 * Called at the end of a query.
	 *
	 * @param context a context object that is progressively filled out as the query
	 *                executes
	 */
	default void end(R2ListenerContext context) {
	}
	
	default void close(Connection conn) {
	}
	
}
