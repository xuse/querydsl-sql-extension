package com.github.xuse.querydsl.sql.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.SQLQueryAlter;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.support.Where.WhereBuilder;
import com.github.xuse.querydsl.util.Exceptions;
import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPath;

public abstract class AbstractCrudRepository<T, ID> implements CRUDRepository<T, ID> {
	@Override
	public T load(ID key) {
		Assert.notNull(key,"key of loading object must not null.");
		RelationalPath<T> t = getPath();
		SQLQueryAlter<T> query = getFactory().selectFrom(t);
		query.where(toPrimaryKeyPredicate(key));
		return query.fetchFirst();
	}

	/*
	 *将泛型对象转换为主键条件。对于复合主键的场景，支持解析List、Pair(2列)、Triple（3列）三种方式包装的复合主键
	 * @param key
	 * @return
	 */
	private Predicate[] toPrimaryKeyPredicate(ID key) {
		RelationalPath<T> t = getPath();
		List<SimpleExpression<?>> ps = getPkColumn().stream().map(e -> (SimpleExpression<?>) e)
				.collect(Collectors.toList());
		if (ps.isEmpty()) {
			throw Exceptions.unsupportedOperation("No primary key column found. {}", t);
		}
		if (ps.size() == 1) {
			return new Predicate[] { ps.get(0).eq(ConstantImpl.create(key)) };
		} else {
			if (key instanceof List) {
				List<?> keys = (List<?>) key;
				if (keys.size() != ps.size()) {
					throw Exceptions.illegalArgument(
							"Input args not match the column number of primary keys. {} [{}]!=[{}]", t, keys.size(),
							ps.size());
				}
				Predicate[] c = new Predicate[ps.size()];
				for (int i = 0; i < ps.size(); i++) {
					c[i] = ps.get(i).eq(ConstantImpl.create(keys.get(i)));
				}
				return c;
			} else if (key instanceof Pair && ps.size() == 2) {
				Pair<?, ?> pair = (Pair<?, ?>) key;
				Predicate[] c = new Predicate[2];
				c[0] = ps.get(0).eq(ConstantImpl.create(pair.getFirst()));
				c[1] = ps.get(1).eq(ConstantImpl.create(pair.getSecond()));
			} else if (key instanceof Triple && ps.size() == 3) {
				Triple<?, ?, ?> triple = (Triple<?, ?, ?>) key;
				Predicate[] c = new Predicate[3];
				c[0] = ps.get(0).eq(ConstantImpl.create(triple.getLeft()));
				c[1] = ps.get(1).eq(ConstantImpl.create(triple.getMiddle()));
				c[2] = ps.get(2).eq(ConstantImpl.create(triple.getRight()));
			}
		}
		throw Exceptions.illegalArgument("input key must be a List, because the table {} has {} primary key columns", t,
				ps.size());
	}

	@Override
	public List<T> findByExample(T t) {
		SQLQueryFactory factory = getFactory();
		ConfigurationEx configuration = factory.getConfiguration();
		SQLQueryAlter<T> query = factory.selectFrom(getPath());
		for (Map.Entry<Path<?>, Object> entry : AdvancedMapper.INSTANCE.createMap(getPath(), t, configuration)
				.entrySet()) {
			SimpleExpression<?> p = (SimpleExpression<?>) entry.getKey();
			query.where(p.eq(ConstantImpl.create(entry.getValue())));
		}
		return query.fetch();
	}

	public List<T> find(Where<T> where) {
		SQLQueryFactory factory = getFactory();
		return factory.selectFrom(getPath()).where(where.conditions).fetch();
	}

	public List<T> find(Consumer<SQLQueryAlter<T>> consumer) {
		SQLQueryFactory factory = getFactory();
		SQLQueryAlter<T> query = factory.selectFrom(getPath());
		consumer.accept(query);
		return query.fetch();
	}

	@Override
	public T load(Where<T> where) {
		SQLQueryFactory factory = getFactory();
		return factory.selectFrom(getPath()).where(where.conditions).fetchOne();
	}

	public int count(Consumer<SQLQueryAlter<T>> consumer) {
		SQLQueryFactory factory = getFactory();
		SQLQueryAlter<T> query = factory.selectFrom(getPath());
		consumer.accept(query);
		return (int) query.fetchCount();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ID insert(T t) {
		SQLQueryFactory factory = getFactory();
		List<? extends Path<?>> columns = getPkColumn();
		SQLInsertClauseAlter insert = factory.insert(getPath()).populate(t);
		if (columns.isEmpty() || columns.size() > 1) {
			insert.execute();
			return null;
		} else {
			ID result = (ID) insert.executeWithKey(columns.get(0));
			return result;
		}
	}

	@Override
	public int insertBatch(List<T> ts) {
		if(ts==null || ts.isEmpty()) {
			return 0;
		}
		if(ts.size()==1) {
			return (int)getFactory().insert(getPath()).populate(ts.get(0)).execute();
		}
		return (int)getFactory().insert(getPath()).populateBatch(ts).execute();
	}

	@Override
	public int delete(ID key) {
		SQLQueryFactory factory = getFactory();
		return (int) factory.delete(getPath()).where(toPrimaryKeyPredicate(key)).execute();
	}

	@Override
	public int delete(Consumer<SQLDeleteClauseAlter> consumer) {
		SQLDeleteClauseAlter delete = getFactory().delete(getPath());
		consumer.accept(delete);
		return (int)delete.execute();
	}

	@Override
	public int deleteByExample(T t) {
		SQLQueryFactory factory = getFactory();
		ConfigurationEx configuration = factory.getConfiguration();
		SQLDeleteClauseAlter query = factory.delete(getPath());
		for (Map.Entry<Path<?>, Object> entry : AdvancedMapper.INSTANCE.createMap(getPath(), t, configuration)
				.entrySet()) {
			SimpleExpression<?> p = (SimpleExpression<?>) entry.getKey();
			query.where(p.eq(ConstantImpl.create(entry.getValue())));
		}
		return (int) query.execute();
	}

	public int delete(Where<T> where) {
		return (int) getFactory().delete(getPath()).where(where.conditions).execute();
	}

	@Override
	public int update(ID key, T t) {
		SQLQueryFactory factory = getFactory();
		return (int) factory.update(getPath()).populate(t).where(toPrimaryKeyPredicate(key)).execute();
	}

	@Override
	public int update(Consumer<SQLUpdateClauseAlter> consumer) {
		SQLUpdateClauseAlter update = getFactory().update(getPath());
		consumer.accept(update);
		return (int) update.execute();
	}

	@Override
	public int count(Where<T> where) {
		return (int) getFactory().selectFrom(getPath()).where(where.conditions).fetchCount();
	}

	public WhereBuilder<T> query() {
		return Where.newBuilder(getPath(), this);
	}

	protected abstract SQLQueryFactory getFactory();

	protected abstract RelationalPath<T> getPath();

	private List<? extends Path<?>> getPkColumn() {
		RelationalPath<T> t = getPath();
		PrimaryKey<T> pk = t.getPrimaryKey();
		if (pk == null) {
			return Collections.emptyList();
		}
		return pk.getLocalColumns();
	}
}
