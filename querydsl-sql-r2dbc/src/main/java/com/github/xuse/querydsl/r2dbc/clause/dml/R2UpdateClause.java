package com.github.xuse.querydsl.r2dbc.clause.dml;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.clause.AbstractR2UpdateClause;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.sql.RelationalPath;

import io.r2dbc.spi.ConnectionFactory;

public class R2UpdateClause extends AbstractR2UpdateClause<R2UpdateClause>{
	public R2UpdateClause(ConnectionFactory connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration, entity);
		// TODO Auto-generated constructor stub
	}

	@Override
	public R2UpdateClause set(List<? extends Path<?>> paths, List<?> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> R2UpdateClause set(Path<T> path, @Nullable T value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> R2UpdateClause set(Path<T> path, Expression<? extends T> expression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> R2UpdateClause setNull(Path<T> path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long execute() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public R2UpdateClause where(Predicate... o) {
		// TODO Auto-generated method stub
		return null;
	}

}
