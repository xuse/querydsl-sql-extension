package com.github.xuse.querydsl.r2dbc.core;

import reactor.core.publisher.Mono;

public interface R2DMLClause<C extends R2DMLClause<C>> {

    /**
     * Execute the clause and return the amount of affected rows
     *
     * @return amount of affected rows
     */
    Mono<Long> execute();
}
