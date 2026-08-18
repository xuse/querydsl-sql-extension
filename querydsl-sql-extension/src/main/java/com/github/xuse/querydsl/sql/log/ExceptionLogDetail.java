package com.github.xuse.querydsl.sql.log;

/**
 * SQL 异常日志的输出详细度。用于控制 {@link QueryDSLSQLListener} 在 SQL 执行失败时打印多少内容。
 * <p>
 * 典型场景：业务方使用 insert 并捕获主键冲突来控制流程，这类异常属于预期内情况，
 * 不希望框架每次都打印完整堆栈淹没业务日志。此时可将该语句设置为
 * {@link #BRIEF} 或 {@link #NONE}，而不影响其它语句的错误日志。
 * <p>
 * Verbosity of SQL exception logging. Allows expected exceptions (e.g. duplicate key
 * used for flow control) to be logged briefly or not at all, without muting other statements.
 *
 * @author Joey
 */
public enum ExceptionLogDetail {

	/**
	 * 打印 SQL、绑定参数以及完整异常堆栈。框架默认行为。
	 * <p>
	 * Print SQL, bindings and the full exception stack trace. This is the default.
	 */
	FULL_STACK,

	/**
	 * 仅打印 SQL、绑定参数以及异常的类名和 message，不输出堆栈。
	 * <p>
	 * Print SQL, bindings and the exception class name plus message, without the stack trace.
	 */
	BRIEF,

	/**
	 * 完全不打印异常日志。异常本身仍会正常抛出，只是不再由框架记录。
	 * <p>
	 * Log nothing. The exception is still thrown as usual, it is merely not logged by the framework.
	 */
	NONE;
}
