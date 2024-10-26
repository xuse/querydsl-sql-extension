package com.github.xuse.querydsl.r2dbc.core;

import java.time.Duration;

import org.jetbrains.annotations.NotNull;
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
	public @NotNull Publisher<Void> beginTransaction() {
		return wrapped.beginTransaction();
	}

	@Override
	public @NotNull Publisher<Void> beginTransaction(@NotNull TransactionDefinition definition) {
		return wrapped.beginTransaction(definition);
	}

	@Override
	public @NotNull Publisher<Void> commitTransaction() {
		return wrapped.commitTransaction();
	}

	@Override
	public @NotNull Batch createBatch() {
		return wrapped.createBatch();
	}

	@Override
	public @NotNull Publisher<Void> createSavepoint(@NotNull String name) {
		return wrapped.createSavepoint(name);
	}

	@Override
	public @NotNull Statement createStatement(@NotNull String sql) {
		return wrapped.createStatement(sql);
	}

	@Override
	public boolean isAutoCommit() {
		return wrapped.isAutoCommit();
	}

	@Override
	public @NotNull ConnectionMetadata getMetadata() {
		return wrapped.getMetadata();
	}

	@Override
	public @NotNull IsolationLevel getTransactionIsolationLevel() {
		return wrapped.getTransactionIsolationLevel();
	}

	@Override
	public @NotNull Publisher<Void> releaseSavepoint(@NotNull String name) {
		return wrapped.releaseSavepoint(name);
	}

	@Override
	public @NotNull Publisher<Void> rollbackTransaction() {
		return wrapped.rollbackTransaction();
	}

	@Override
	public @NotNull Publisher<Void> rollbackTransactionToSavepoint(@NotNull String name) {
		return wrapped.rollbackTransactionToSavepoint(name);
	}

	@Override
	public @NotNull Publisher<Void> setAutoCommit(boolean autoCommit) {
		return wrapped.setAutoCommit(autoCommit);
	}

	@Override
	public @NotNull Publisher<Void> setLockWaitTimeout(@NotNull Duration timeout) {
		return wrapped.setLockWaitTimeout(timeout);
	}

	@Override
	public @NotNull Publisher<Void> setStatementTimeout(@NotNull Duration timeout) {
		return wrapped.setStatementTimeout(timeout);
	}

	@Override
	public @NotNull Publisher<Void> setTransactionIsolationLevel(@NotNull IsolationLevel isolationLevel) {
		return wrapped.setTransactionIsolationLevel(isolationLevel);
	}

	@Override
	public @NotNull Publisher<Boolean> validate(@NotNull ValidationDepth depth) {
		return wrapped.validate(depth);
	}
}
