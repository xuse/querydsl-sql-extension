package com.github.xuse.querydsl.sql.log;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

import org.jetbrains.annotations.Nullable;

import com.github.xuse.querydsl.util.lang.Primitives;
import com.querydsl.sql.SQLBaseListener;
import com.querydsl.sql.SQLListenerContext;

/**
 * 用于监听错误和慢SQL数量的监听器。
 */
public final class ErrorAndSlowCounter extends SQLBaseListener{
	private String name;
	
	final LongAdder exceptions = new LongAdder();
	final LongAdder integrityViolations = new LongAdder();
	final LongAdder badSqlGrammars = new LongAdder();
	final LongAdder slows = new LongAdder();
	
	
	public ErrorAndSlowCounter(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public long getExceptions() {
		return exceptions.longValue();
	}

	public long getIntegrityViolations() {
		return integrityViolations.longValue();
	}

	public long getBadSqlGrammars() {
		return badSqlGrammars.longValue();
	}

	public long getSlows() {
		return slows.longValue();
	}

	private static final Set<String> BAD_SQL_GRAMMAR_CODES = new HashSet<>(8);

	private static final Set<String> DATA_INTEGRITY_VIOLATION_CODES = new HashSet<>(8);

	private static final Set<String> DATA_ACCESS_RESOURCE_FAILURE_CODES = new HashSet<>(8);

	private static final Set<String> TRANSIENT_DATA_ACCESS_RESOURCE_CODES = new HashSet<>(8);

	private static final Set<String> CONCURRENCY_FAILURE_CODES = new HashSet<>(4);


	static {
		BAD_SQL_GRAMMAR_CODES.add("07");  // Dynamic SQL error
		BAD_SQL_GRAMMAR_CODES.add("21");  // Cardinality violation
		BAD_SQL_GRAMMAR_CODES.add("2A");  // Syntax error direct SQL
		BAD_SQL_GRAMMAR_CODES.add("37");  // Syntax error dynamic SQL
		BAD_SQL_GRAMMAR_CODES.add("42");  // General SQL syntax error
		BAD_SQL_GRAMMAR_CODES.add("65");  // Oracle: unknown identifier

		DATA_INTEGRITY_VIOLATION_CODES.add("01");  // Data truncation
		DATA_INTEGRITY_VIOLATION_CODES.add("02");  // No data found
		DATA_INTEGRITY_VIOLATION_CODES.add("22");  // Value out of range
		DATA_INTEGRITY_VIOLATION_CODES.add("23");  // Integrity constraint violation
		DATA_INTEGRITY_VIOLATION_CODES.add("27");  // Triggered data change violation
		DATA_INTEGRITY_VIOLATION_CODES.add("44");  // With check violation

		DATA_ACCESS_RESOURCE_FAILURE_CODES.add("08");  // Connection exception
		DATA_ACCESS_RESOURCE_FAILURE_CODES.add("53");  // PostgreSQL: insufficient resources (e.g. disk full)
		DATA_ACCESS_RESOURCE_FAILURE_CODES.add("54");  // PostgreSQL: program limit exceeded (e.g. statement too complex)
		DATA_ACCESS_RESOURCE_FAILURE_CODES.add("57");  // DB2: out-of-memory exception / database not started
		DATA_ACCESS_RESOURCE_FAILURE_CODES.add("58");  // DB2: unexpected system error

		TRANSIENT_DATA_ACCESS_RESOURCE_CODES.add("JW");  // Sybase: internal I/O error
		TRANSIENT_DATA_ACCESS_RESOURCE_CODES.add("JZ");  // Sybase: unexpected I/O error
		TRANSIENT_DATA_ACCESS_RESOURCE_CODES.add("S1");  // DB2: communication failure

		CONCURRENCY_FAILURE_CODES.add("40");  // Transaction rollback
		CONCURRENCY_FAILURE_CODES.add("61");  // Oracle: deadlock
	}
	
	@Override
	public void executed(SQLListenerContext context) {
		int important = Primitives.unbox((Integer) context.getData(ContextKeyConstants.IMPORTANT), 0);
		if((important & ContextKeyConstants.SLOW)==1) {
			slows.increment();
		}
	}

	@Override
	public void exception(SQLListenerContext context) {
		Exception e = context.getException();
		exceptions.increment();
		if(e instanceof SQLException) {
			String state = getSqlState((SQLException)e);
			switch(doTranslate(state)) {
			case 1:
				integrityViolations.increment();
				break;
			case 2:
				badSqlGrammars.increment();
				break;
			}
		}
	}
	
	protected int doTranslate(String sqlState) {
		if (sqlState != null && sqlState.length() >= 2) {
			String classCode = sqlState.substring(0, 2);
			if (DATA_INTEGRITY_VIOLATION_CODES.contains(classCode)) {
				return 1;
			} else if (BAD_SQL_GRAMMAR_CODES.contains(classCode)) {
				return 2;
			}
		}
		return 0;
	}

	@Nullable
	private String getSqlState(SQLException ex) {
		String sqlState = ex.getSQLState();
		if (sqlState == null) {
			SQLException nestedEx = ex.getNextException();
			if (nestedEx != null) {
				sqlState = nestedEx.getSQLState();
			}
		}
		return sqlState;
	}
}
