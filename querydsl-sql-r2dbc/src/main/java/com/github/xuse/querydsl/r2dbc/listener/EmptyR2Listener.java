package com.github.xuse.querydsl.r2dbc.listener;

import java.util.List;
import java.util.Map;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.dml.SQLInsertBatch;
import com.querydsl.sql.dml.SQLMergeBatch;
import com.querydsl.sql.dml.SQLUpdateBatch;

public class EmptyR2Listener implements R2BaseListener{

	@Override
	public void notifyQuery(QueryMetadata md) {
	}

	@Override
	public void notifyDelete(RelationalPath<?> entity, QueryMetadata md) {
	}

	@Override
	public void notifyDeletes(RelationalPath<?> entity, List<QueryMetadata> batches) {
	}

	@Override
	public void notifyMerge(RelationalPath<?> entity, QueryMetadata md, List<Path<?>> keys, List<Path<?>> columns,
			List<Expression<?>> values, SubQueryExpression<?> subQuery) {
	}

	@Override
	public void notifyMerges(RelationalPath<?> entity, QueryMetadata md, List<SQLMergeBatch> batches) {
	}

	@Override
	public void notifyInsert(RelationalPath<?> entity, QueryMetadata md, List<Path<?>> columns,
			List<Expression<?>> values, SubQueryExpression<?> subQuery) {
	}

	@Override
	public void notifyInserts(RelationalPath<?> entity, QueryMetadata md, List<SQLInsertBatch> batches) {
	}

	@Override
	public void notifyUpdate(RelationalPath<?> entity, QueryMetadata md, Map<Path<?>, Expression<?>> updates) {
	}

	@Override
	public void notifyUpdates(RelationalPath<?> entity, List<SQLUpdateBatch> batches) {
	}
}
