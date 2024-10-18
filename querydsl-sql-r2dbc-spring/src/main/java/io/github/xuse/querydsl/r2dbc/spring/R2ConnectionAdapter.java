package io.github.xuse.querydsl.r2dbc.spring;

import java.time.Duration;

import org.reactivestreams.Publisher;

import io.r2dbc.spi.Batch;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionMetadata;
import io.r2dbc.spi.IsolationLevel;
import io.r2dbc.spi.Statement;
import io.r2dbc.spi.TransactionDefinition;
import io.r2dbc.spi.ValidationDepth;

public abstract class R2ConnectionAdapter implements Connection{
	protected final Connection wrapped;

	public R2ConnectionAdapter(Connection r2) {
		this.wrapped = r2;
	}

	@Override
	public Publisher<Void> beginTransaction() {
		return wrapped.beginTransaction();
	}

	@Override
	public Publisher<Void> beginTransaction(TransactionDefinition definition) {
		return wrapped.beginTransaction(definition);
	}

	@Override
	public Publisher<Void> commitTransaction() {
		return wrapped.commitTransaction();
	}

	@Override
	public Batch createBatch() {
		return wrapped.createBatch();
	}

	@Override
	public Publisher<Void> createSavepoint(String name) {
		return wrapped.createSavepoint(name);
	}

	@Override
	public Statement createStatement(String sql) {
		return wrapped.createStatement(sql);
	}

	@Override
	public boolean isAutoCommit() {
		return wrapped.isAutoCommit();
	}

	@Override
	public ConnectionMetadata getMetadata() {
		return wrapped.getMetadata();
	}

	@Override
	public IsolationLevel getTransactionIsolationLevel() {
		return wrapped.getTransactionIsolationLevel();
	}

	@Override
	public Publisher<Void> releaseSavepoint(String name) {
		return wrapped.releaseSavepoint(name);
	}

	@Override
	public Publisher<Void> rollbackTransaction() {
		return wrapped.rollbackTransaction();
	}

	@Override
	public Publisher<Void> rollbackTransactionToSavepoint(String name) {
		return wrapped.rollbackTransactionToSavepoint(name);
	}

	@Override
	public Publisher<Void> setAutoCommit(boolean autoCommit) {
		return wrapped.setAutoCommit(autoCommit);
	}

	@Override
	public Publisher<Void> setLockWaitTimeout(Duration timeout) {
		return wrapped.setLockWaitTimeout(timeout);
	}

	@Override
	public Publisher<Void> setStatementTimeout(Duration timeout) {
		return wrapped.setStatementTimeout(timeout);
	}

	@Override
	public Publisher<Void> setTransactionIsolationLevel(IsolationLevel isolationLevel) {
		return wrapped.setTransactionIsolationLevel(isolationLevel);
	}

	@Override
	public Publisher<Boolean> validate(ValidationDepth depth) {
		return wrapped.validate(depth);
	}
}
