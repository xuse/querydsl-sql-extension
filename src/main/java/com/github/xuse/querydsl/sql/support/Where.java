package com.github.xuse.querydsl.sql.support;

import java.util.ArrayList;
import java.util.List;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.sql.RelationalPath;

public class Where<T> {
	final Path<T> table;
	final Predicate[] conditions;
	

	public Where(Path<T> table, Predicate[] conditions) {
		this.table = table;
		this.conditions = conditions;
	}

	public static <T> WhereBuilder<T> newBuilder(RelationalPath<T> table) {
		return new WhereBuilder<>(table,null);
	}
	
	static <T> WhereBuilder<T> newBuilder(RelationalPath<T> table,AbstractCrudRepository<T,?> repo) {
		return new WhereBuilder<>(table,repo);
	}

	public static class WhereBuilder<T> {
		final RelationalPath<T> table;
		final List<Predicate> conditions = new ArrayList<>();
		final AbstractCrudRepository<T,?> repository;

		WhereBuilder(RelationalPath<T> table,AbstractCrudRepository<T,?> repository) {
			this.table = table;
			this.repository=repository;
		}

		public WhereBuilder<T> where(Predicate... predicates) {
			for (Predicate p : predicates) {
				if (p != null) {
					conditions.add(p);
				}
			}
			return this;
		}

		public Where<T> build() {
			return new Where<T>(table, condition());
		}
		
		private Predicate[] condition() {
			return conditions.toArray(new Predicate[conditions.size()]);
		}

		public List<T> fetch(){
			if(repository!=null) {
				return repository.find(build());
			}else {
				throw new IllegalArgumentException();
			}
		}
		
		public int count() {
			if(repository!=null) {
				return repository.count(build());
			}else {
				throw new IllegalArgumentException();
			}
		}
		
		public T fetchFirst() {
			if(repository!=null) {
				return repository.getFactory().selectFrom(table).where(condition()).fetchFirst();
			}else {
				throw new IllegalArgumentException();
			}
		}
		
		public int update(T t) {
			if(repository!=null) {
				return (int)repository.getFactory().update(table).where(condition()).populate(t).execute();
			}else {
				throw new IllegalArgumentException();
			}
		}
		
		public T load() {
			if(repository!=null) {
				return repository.load(build());
			}else {
				throw new IllegalArgumentException();
			}
		}
		
		public int delete() {
			if(repository!=null) {
				return (int)repository.getFactory().delete(table).where(condition()).execute();
			}else {
				throw new IllegalArgumentException();
			}	
		}
	}
}
