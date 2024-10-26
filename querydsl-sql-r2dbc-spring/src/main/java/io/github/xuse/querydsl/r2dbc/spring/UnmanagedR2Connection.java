package io.github.xuse.querydsl.r2dbc.spring;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import com.github.xuse.querydsl.r2dbc.core.R2ConnectionAdapter;

import io.r2dbc.spi.Connection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class UnmanagedR2Connection extends R2ConnectionAdapter {
	public UnmanagedR2Connection(Connection r2) {
		super(r2);
		log.debug("UnmanagedR2Connection created, {}", wrapped);
	}

	@Override
	public @NotNull Publisher<Void> close() {
		log.debug("UnmanagedR2Connection closing, {}", wrapped);
		return wrapped.close();
	}
}
