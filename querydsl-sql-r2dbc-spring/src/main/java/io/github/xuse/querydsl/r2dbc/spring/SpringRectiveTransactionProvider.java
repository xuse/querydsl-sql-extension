package io.github.xuse.querydsl.r2dbc.spring;

import org.springframework.r2dbc.connection.ConnectionFactoryUtils;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.reactive.TransactionContextManager;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import reactor.core.publisher.Mono;

public class SpringRectiveTransactionProvider implements ConnectionFactory {
	
	private final ConnectionFactory transactionManagedFactory;
	
	/**
	 * @param connectionPool the ConnectionFactory in Spring tx manager.
	 */
	public SpringRectiveTransactionProvider(ConnectionFactory connectionPool) {
		this.transactionManagedFactory = connectionPool;
	}

	@Override
	public Mono<Connection> create() {
		ConnectionFactory key=this.transactionManagedFactory;
		return TransactionContextManager.currentContext()
				.flatMap((e)->ConnectionFactoryUtils.doGetConnection(key))
				.onErrorResume(NoTransactionException.class,this::unmanagedConnection);
	}
	
	@SuppressWarnings("unchecked")	
	private Mono<Connection> unmanagedConnection(NoTransactionException ex){
		Mono<Connection> conn=(Mono<Connection>) transactionManagedFactory.create();
		return conn.map(c->new UnmanagedR2Connection(c));
	}

	@Override
	public ConnectionFactoryMetadata getMetadata() {
		return transactionManagedFactory.getMetadata();
	}
}
