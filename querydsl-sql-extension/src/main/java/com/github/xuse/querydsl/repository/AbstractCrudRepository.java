package com.github.xuse.querydsl.repository;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.annotation.query.BoolCase;
import com.github.xuse.querydsl.annotation.query.Condition;
import com.github.xuse.querydsl.annotation.query.ConditionBean;
import com.github.xuse.querydsl.annotation.query.IntCase;
import com.github.xuse.querydsl.annotation.query.Order;
import com.github.xuse.querydsl.annotation.query.StringCase;
import com.github.xuse.querydsl.annotation.query.When;
import com.github.xuse.querydsl.init.csv.Codecs;
import com.github.xuse.querydsl.lambda.LambdaColumnBase;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.SQLQueryAlter;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.column.UnsavedValuePredicateFactory;
import com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.github.xuse.querydsl.sql.expression.BeanCodecManager;
import com.github.xuse.querydsl.sql.expression.Property;
import com.github.xuse.querydsl.util.ArrayUtils;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPath;

public abstract class AbstractCrudRepository<T, ID> implements CRUDRepository<T, ID> {
	@Override
	public T load(ID key) {
		Assert.notNull(key, "key of loading object must not null.");
		RelationalPath<T> t = getPath();
		SQLQueryAlter<T> query = getFactory().selectFrom(t);
		query.where(toPrimaryKeyPredicate(key));
		return query.fetchFirst();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> loadBatch(List<ID> keys) {
		RelationalPath<T> t = getPath();
		SQLQueryAlter<T> query = getFactory().selectFrom(t);
		 List<? extends Path<?>> columns=getPkColumn();
		if(columns.size()>1) {
			throw Exceptions.unsupportedOperation("using batch load on Complex primary keys on {}.", t);
		}
		if(columns.isEmpty()) {
			throw Exceptions.unsupportedOperation("no primary key on {}.", t);
		}
		@SuppressWarnings("rawtypes")
		SimpleExpression p=(SimpleExpression)columns.get(0);
		query.where(p.in(keys));
		return query.fetch();
	}

	@Override
	public int countByExample(T example) {
		SQLQueryFactory factory = getFactory();
		SQLQueryAlter<T> query = factory.selectFrom(getPath());
		for (Map.Entry<Path<?>, Object> entry : DEFAULT.createMap(getPath(), example).entrySet()) {
			SimpleExpression<?> p = (SimpleExpression<?>) entry.getKey();
			query.where(p.eq(ConstantImpl.create(entry.getValue())));
		}
		return (int) query.fetchCount();
	}

	@Override
	public List<T> findByExample(T example) {
		SQLQueryFactory factory = getFactory();
		SQLQueryAlter<T> query = factory.selectFrom(getPath());
		for (Map.Entry<Path<?>, Object> entry : DEFAULT.createMap(getPath(), example).entrySet()) {
			SimpleExpression<?> p = (SimpleExpression<?>) entry.getKey();
			query.where(p.eq(ConstantImpl.create(entry.getValue())));
		}
		return query.fetch();
	}

	public List<T> find(Consumer<SQLQueryAlter<T>> consumer) {
		SQLQueryFactory factory = getFactory();
		SQLQueryAlter<T> query = factory.selectFrom(getPath());
		consumer.accept(query);
		return query.fetch();
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
		if (columns.size() != 1) {
			insert.execute();
			return null;
		} else {
			ID result = (ID) insert.executeWithKey(columns.get(0));
			return result;
		}
	}

	@Override
	public int insertBatch(List<T> ts) {
		if (ts == null || ts.isEmpty()) {
			return 0;
		}
		SQLInsertClauseAlter insert = getFactory().insert(getPath());
		if (ts.size() == 1) {
			insert.populate(ts.get(0));
		}else {
			insert.populateBatch(ts);
		}
		return (int) insert.execute();
	}
	
	@Override
	public int insertBatch(List<T> ts, boolean selective) {
		if (ts == null || ts.isEmpty()) {
			return 0;
		}
		SQLInsertClauseAlter insert = getFactory().insert(getPath()).writeNulls(!selective);
		if (ts.size() == 1) {
			insert.populate(ts.get(0));
		} else {
			insert.populateBatch(ts);
		}
		return (int) insert.execute();
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
		return (int) delete.execute();
	}

	private static final AdvancedMapper DEFAULT = new AdvancedMapper(0,false);

	@Override
	public int deleteByExample(T t) {
		SQLQueryFactory factory = getFactory();
		SQLDeleteClauseAlter query = factory.delete(getPath());
		for (Map.Entry<Path<?>, Object> entry : DEFAULT.createMap(getPath(), t).entrySet()) {
			SimpleExpression<?> p = (SimpleExpression<?>) entry.getKey();
			query.where(p.eq(ConstantImpl.create(entry.getValue())));
		}
		return (int) query.execute();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int updateByKeys(T obj, Path<?>... bizKeys) {
		if (bizKeys.length == 0) {
			throw new IllegalArgumentException("please input where paths.");
		}
		SQLQueryFactory factory = getFactory();
		Map<Path<?>, Object> map = DEFAULT.createMap(getPath(), obj);
		SQLUpdateClauseAlter update = factory.update(getPath());
		for (Path<?> key : bizKeys) {
			Object value = map.remove(key);
			SimpleExpression<?> p = (SimpleExpression<?>) key;
			if (value != null) {
				update.where(p.eq(ConstantImpl.create(value)));
			} else {
				//如果指定的key当中有无效或null值。此时是否将其作为查询条件？正常情况是需要的，但是也要考虑特殊场景
				//对于primitive字段也可以用isNull.因为定义为无效的primitive字段在数据库中是无意义的，理论上数据库中不应当出现无效值，因此使用isNull也就意味着查不到该数值。
				update.where(p.isNull());
			}
		}
		for (Map.Entry<Path<?>, Object> entry : map.entrySet()) {
			Path path = entry.getKey();
			update.set(path, entry.getValue());
		}
		return (int) update.execute();
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

	public QueryExecutor<T,T> query() {
		RelationalPath<T> path=getPath();
		return new QueryExecutor<>(path,new DefaultQueryMetadata(),this);
	}

	/**
	 * @return Needs the SQLQueryFactory
	 */
	protected abstract SQLQueryFactory getFactory();

	/**
	 * @return The RelationalPath(path of the table).
	 */
	protected abstract RelationalPath<T> getPath();

	private List<? extends Path<?>> getPkColumn() {
		RelationalPath<T> t = getPath();
		PrimaryKey<T> pk = t.getPrimaryKey();
		if (pk == null) {
			return Collections.emptyList();
		}
		return pk.getLocalColumns();
	}
	
	/**
	 * Fetch count for a condition bean
	 * @param conditionBean
	 * @return count
	 */
	public final int countByCondition(Object conditionBean) {
		Class<?> clz = conditionBean.getClass();
		ConditionBean cb = clz.getAnnotation(ConditionBean.class);
		if (cb == null) {
			throw new IllegalArgumentException("Condition bean must annotated with @ConditionBean");
		}
		SQLQueryAlter<T> select = createSelectQuery(conditionBean,cb,null);
		return (int)select.fetchCount();
	}
	
	public final Pair<Integer,List<T>> findByCondition(Object conditionBean, int limit, int offset) {
		Class<?> clz = conditionBean.getClass();
		ConditionBean cb = clz.getAnnotation(ConditionBean.class);
		if (cb == null) {
			throw new IllegalArgumentException("Condition bean must annotated with @ConditionBean");
		}
		Params p=new Params();
		SQLQueryAlter<T> select = createSelectQuery(conditionBean,cb,p);
		if(limit>0) {
			select.limit(limit);
		}else if(p.limit !=null && p.limit.intValue()>0){
			select.limit(p.limit.intValue());
		}
		if(offset>0) {
			select.offset(offset);
		}else if(p.offset!=null && p.offset.intValue()>0) {
			select.offset(p.offset.intValue());
		}
		if(p.fetchTotal==null || p.fetchTotal) {
			QueryResults<T> results=select.fetchResults();
			return new Pair<Integer,List<T>>((int)results.getTotal(),results.getResults());
		}
		return Pair.of(-1, select.fetch());
	}
	
	static class Params{
		Number limit;
		Number offset;
		Boolean fetchTotal;
	}

	@SuppressWarnings("deprecation")
	private SQLQueryAlter<T> createSelectQuery(Object conditionBean,ConditionBean cb, Params params) {
		RelationalPath<T> beanPath= getPath();
		SQLQueryAlter<T> select= getFactory().selectFrom(beanPath);
		RelationalPathEx<T> beanPathEx=null;
		if(beanPath instanceof RelationalPathEx) {
			beanPathEx=(RelationalPathEx<T>)beanPath;
		}
		
		BeanCodec codec = BeanCodecManager.getInstance().getCodec(conditionBean.getClass());
		Property[] fields = codec.getFields();
		Object[] values = codec.values(conditionBean);
		Map<String, Path<?>> bindings = new HashMap<>();
		for (Path<?> p : beanPath.getColumns()) {
			bindings.put(p.getMetadata().getName(), p);
		}
		for (int i = 0; i < fields.length; i++) {
			Property field = fields[i];
			Object value = values[i];
			String fieldName=field.getName();
			if (fieldName.equals(cb.limitField())) {
				if(params!=null){
					params.limit = (Number) value; 
				}
				continue;
			}
			if (fieldName.equals(cb.offsetField())) {
				if(params!=null){
					params.offset = (Number) value; 
				}
				continue;
			}
			if(fieldName.equals(cb.isRequireTotalField())) {
				if(params!=null){
					params.fetchTotal=(Boolean)value;
				}
				continue;
			}
			Condition condition = field.getAnnotation(Condition.class);
			When when=field.getAnnotation(When.class);
			Order order=field.getAnnotation(Order.class);
			if(ArrayUtils.countNonNull(condition,when,order)>1) {
				throw Exceptions.illegalArgument("These annotation (@Condition @When @Order) must appear once on a field. {}", field);
			}
			if(order!=null && value!=null) {
				List<Pair<Path<?>, Boolean>> orders = processOrder(value, order, fields, values, bindings);
				for (Pair<Path<?>, Boolean> pair : orders) {
					ComparableExpressionBase<?> expr = (ComparableExpressionBase<?>) pair.getFirst();
					select.orderBy(pair.getSecond() ? expr.asc() : expr.desc());
				}
				continue;
			}
			if (condition != null) {
				Predicate where = null;
				if (condition.otherPaths().length > 0) {
					for (String op : condition.otherPaths()) {
						if (StringUtils.isEmpty(op)) {
							continue;
						}
						Path<?> path = bindings.get(op);
						if (path == null) {
							throw Exceptions.illegalArgument("Not found path {} in bean {}", op, beanPath);
						}
						if (condition.ignoreUnsavedValue() && isUnsaved(value, beanPathEx, path, condition.value())) {
						}else {
							where = appendOr(where, toPredicate(value, path, condition.value(),field.getName()));
						}
					}
				}
				{
					String pathName = condition.path();
					if (StringUtils.isEmpty(pathName)) {
						pathName = field.getName();
					}
					Path<?> path = bindings.get(pathName);
					if (path == null) {
						throw Exceptions.illegalArgument("Not found path {} in bean {}", pathName, beanPath);
					}
					if (condition.ignoreUnsavedValue() && isUnsaved(value, beanPathEx,path,condition.value())) {
					}else {
						where = appendOr(where, toPredicate(value, path, condition.value(),field.getName()));				
					}
					select.where(where);
				}
			}
			if(when!=null) {
				Class<?> fieldType=field.getType();
				if(fieldType.isPrimitive()) {
					throw Exceptions.illegalArgument("@When must not on a field of primitive type. {}", field);
				}
				String pathName = when.path();
				if (StringUtils.isEmpty(pathName)) {
					pathName = field.getName();
				}
				Path<?> path = bindings.get(pathName);
				if(value!=null) {
					Pair<Ops,Object> matchExpr;
					if(field.getType()==Boolean.class) {
						matchExpr = getMatchExpr(value, when.forBool(),path.getType());
					} else if (field.getType() == Integer.class) {
						matchExpr = getMatchExpr(value, when.forInt(),path.getType());
					} else if (field.getType() == String.class) {
						matchExpr = getMatchExpr(value, when.value(),path.getType());
					}else {
						throw Exceptions.illegalArgument("@When only supports field in these types('Integer','Boolean','String'), the field {}", field);
					}
					if(matchExpr==null) {
						if(!when.ignoreIfNoMatchCase()) {
							throw Exceptions.illegalArgument("@When has no match case for value {}, on the field {}", value, field);	
						}
					}else {
						Predicate where;
						if(matchExpr.getFirst()==null) {
							where=Expressions.booleanTemplate((String)matchExpr.getSecond());	
						}else {
							where = toPredicate(matchExpr.getSecond(), path, matchExpr.getFirst(),field.getName());
						}
						select.where(where);
					}
				}
			}
		}
		return select;
	}

	private Pair<Ops, Object> getMatchExpr(Object value, StringCase[] cases,Class<?> pathType) {
		String v=String.valueOf(value);
		for(StringCase c:cases) {
			if(v.equals(c.is())) {
				return toConditionExpr(c.ops(),c.value(),pathType,c.expression());
			}
		}
		return null;
	}

	private Pair<Ops, Object> getMatchExpr(Object value, IntCase[] cases,Class<?> pathType) {
		int v = ((Number) value).intValue();
		for(IntCase c:cases) {
			if(v==c.is()) {
				return toConditionExpr(c.ops(),c.value(),pathType,c.expression());
			}
		}
		return null;
	}

	private Pair<Ops, Object> getMatchExpr(Object value, BoolCase[] cases,Class<?> pathType) {
		boolean v = ((Boolean) value).booleanValue();
		for(BoolCase c:cases) {
			if(v==c.is()) {
				return toConditionExpr(c.ops(),c.value(),pathType,c.expression());
			}
		}
		return null;
	}
	
	private Pair<Ops, Object> toConditionExpr(Ops ops, String value, Class<?> pathType, String expression) {
		if(StringUtils.isNotBlank(expression)) {
			return Pair.of(null, expression);
		}
		if(ops==Ops.IN || ops==Ops.BETWEEN) {
			//to array
			String[] strs=StringUtils.split(expression,',');
			Object[] v=new Object[strs.length];
			for(int i=0;i<strs.length;i++) {
				v[i] = Codecs.fromString(strs[i], pathType);
			}
			return Pair.of(ops, v);
		}else {
			Object v=Codecs.fromString(value, pathType);
			return Pair.of(ops, v);
		}
	}

	private Predicate appendOr(Predicate p1, Predicate p2) {
		if(p1==null) {
			return p2;
		}
		if(p2==null) {
			return p1;
		}
		return  Expressions.booleanOperation(Ops.OR, p1, p2);
	}

	private boolean isUnsaved(Object value,RelationalPathEx<T> beanPathEx,Path<?> path,Ops op) {
		java.util.function.Predicate<Object> tester;
		if (beanPathEx != null) {
			ColumnMapping cm = beanPathEx.getColumnMetadata(path);
			tester = cm::isUnsavedValue;
		} else {
			tester = UnsavedValuePredicateFactory.Null;
		}
		return isUnsavedValue(tester, value, op); 
	}

	private List<Pair<Path<?>,Boolean>> processOrder(Object value, Order order,Property[] fields,Object[] values,Map<String, Path<?>> bindings) {
		String fieldNames=String.valueOf(value);
		if(StringUtils.isBlank(fieldNames)) {
			return Collections.emptyList();
		}
		Object sortValue = null;
		if(StringUtils.isNotEmpty(order.sortField())) {
			for(int i=0;i<fields.length;i++) {
				Property f = fields[i];
				if(f.getName().equalsIgnoreCase(order.sortField())) {
					sortValue = values[i];
					break;
				}
			}	
		}
		boolean asc = toAscDesc(sortValue);
		List<Pair<Path<?>,Boolean>> result=new ArrayList<>();
		for(String pathName:StringUtils.split(fieldNames,',')) {
			//混合Expr模式
			if(order.orderExpr()) {
				int space=pathName.indexOf(' ');
				if(space>0) {
					sortValue = pathName.substring(space+1).trim();
					pathName = pathName.substring(0, space).trim();
					asc = toAscDesc(sortValue);
				}
			}
			Path<?> path=bindings.get(pathName);
			if (path == null) {
				throw Exceptions.illegalArgument("Not found path {}", fieldNames);
			}
			result.add(Pair.of(path, asc));
		}
		return result;
	}

	private boolean toAscDesc(Object sortValue) {
		if(sortValue instanceof Boolean) {
			return (Boolean)sortValue;
		}
		if(sortValue instanceof String) {
			return "DESC".equalsIgnoreCase((String)sortValue);
		}
		return true;
	}

	@Override
	public <R> List<R> find(QueryWrapper<T, R, ?> wrapper) {
		QueryExecutor<T,R> executor=new QueryExecutor<>(wrapper, this);
		return executor.fetch();
	}

	@Override
	public  <R> Pair<Integer, List<R>> findAndCount(QueryWrapper<T,R, ?> wrapper) {
		QueryExecutor<T,R> executor=new QueryExecutor<>(wrapper, this);
		return executor.findAndCount();
	}
	
	@Override
	public <R> Pair<Integer, List<R>> findAndCount(QueryWrapper<T, R, ?> wrapper, int limit, int offset) {
		if(limit>0) {
			wrapper.limit(limit);
		}
		if(offset>0) {
			wrapper.offset(offset);
		}
		return findAndCount(wrapper);
	}

	@Override
	public int delete(QueryWrapper<T,?, ?> wrapper) {
		QueryExecutor<T,?> executor=new QueryExecutor<>(wrapper, this);
		return executor.delete();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int deleteBatch(Collection<ID> keys) {
		RelationalPath<T> t = getPath();
		SQLDeleteClauseAlter query = getFactory().delete(t);
		 List<? extends Path<?>> columns=getPkColumn();
		if(columns.size()>1) {
			throw Exceptions.unsupportedOperation("using batch delete on Complex primary keys on {}.", t);
		}
		if(columns.isEmpty()) {
			throw Exceptions.unsupportedOperation("no primary key on {}.", t);
		}
		@SuppressWarnings("rawtypes")
		SimpleExpression p=(SimpleExpression)columns.get(0);
		return (int)query.where(p.in(keys)).execute();
	}

	@Override
	public int deleteBy(Predicate... predicates) {
		RelationalPath<T> t = getPath();
		return (int)getFactory().delete(t).where(predicates).execute();
	}

	@Override
	public int update(T t, QueryWrapper<T,T, ?> wrapper) {
		QueryExecutor<T,T> executor=new QueryExecutor<>(wrapper, this);
		return executor.update(t);
	}

	@Override
	public int count(QueryWrapper<T,?, ?> wrapper) {
		QueryExecutor<T,?> executor=new QueryExecutor<>(wrapper, this);
		return executor.count();
	}

	@Override
	public <P> List<T> listBy(Path<P> path, Collection<P> ids) {
		RelationalPath<T> entity = getPath();
		SimpleExpression<P> expr=toSafePath(path);
		return getFactory().selectFrom(entity).where(expr.in(ids)).fetch();
	}

	@Override
	public List<T> list(Predicate... p) {
		RelationalPath<T> entity = getPath();
		return getFactory().selectFrom(entity).where(p).fetch();
	}

	@Override
	public List<T> list(Predicate p, int limit, int offset, OrderSpecifier<? extends Comparable<?>> order) {
		RelationalPath<T> entity = getPath();
		SQLQueryAlter<T> query = getFactory().selectFrom(entity).where(p);
		if(limit>0) {
			query.limit(limit);
		}
		if(offset>0) {
			query.offset(offset);
		}
		if(order!=null) {
			query.orderBy(order);
		}
		return query.fetch();
	}

	@Override
	public <P> T loadBy(Path<P> path, P param) {
		RelationalPath<T> entity = getPath();
		SimpleExpression<P> expr = toSafePath(path);
		return getFactory().selectFrom(entity).where(expr.eq(param)).fetchFirst();
	}

	@Override
	public <P> T getBy(Path<P> path, P param) {
		RelationalPath<T> entity = getPath();
		SimpleExpression<P> expr = toSafePath(path);
		return getFactory().selectFrom(entity).where(expr.eq(param)).fetchOne();
	}
	
	@Override
	public T loadBy(Predicate... p) {
		RelationalPath<T> entity = getPath();
		return getFactory().selectFrom(entity).where(p).fetchFirst();
	}
	
	@Override
	public T loadBy(Predicate p, OrderSpecifier<? extends Comparable<?>> order) {
		RelationalPath<T> entity = getPath();
		SQLQueryAlter<T> sql = getFactory().selectFrom(entity).where(p);
		if (order != null) {
			sql.orderBy(order);
		}
		return sql.fetchFirst();
	}

	@Override
	public T getBy(Predicate p) {
		RelationalPath<T> entity = getPath();
		return getFactory().selectFrom(entity).where(p).fetchOne();
	}

	private boolean isUnsavedValue(java.util.function.Predicate<Object> cm, Object value, Ops operator) {
		if (operator == Ops.IN || operator == Ops.BETWEEN) {
			if (value == null) {
				return true;
			}
			if (value instanceof Collection) {
				for (Object e : (Collection<?>) value) {
					if (cm.test(e)) {
						return true;
					}
				}
				return false;
			}
			if (value instanceof Object[]) {
				for (Object e : (Object[]) value) {
					if (cm.test(e)) {
						return true;
					}
				}
			}
			return false;
		} else if(operator==Ops.IS_NULL || operator==Ops.IS_NOT_NULL) {
			//Only if input value is TRUE or '1', the condition will take effect.
			return !(Boolean.TRUE.equals(value) || "1".equals(value));
		} else {
			return cm.test(value);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Predicate toPredicate(Object value, Path<?> path, Ops operator, String fieldName) {
		try {
		if (operator == Ops.IN) {
			if (value == null || elements(value)<1) {
				return null;
			}
		} else if (operator == Ops.BETWEEN) {
			int paramCount=elements(value);
			if (value == null || paramCount==0) {
				return null;
			}
			if (paramCount != 2) {
				throw new IllegalArgumentException("Invalid param, the value for between condition must be 2 elements.");
			}
		}
		switch (operator) {
		case IN: {
			SimpleExpression exp = (SimpleExpression) path;
			if (value instanceof Collection<?>) {
				return exp.in((Collection<?>) value);
			} else if (value.getClass().isArray()) {
				return exp.in(Arrays.asList(ArrayUtils.toWrapped(value)));
			}
			break;
		}
		case EQ: {
			SimpleExpression exp = (SimpleExpression) path;
			if (value == null) {
				return exp.isNull();
			} else {
				return exp.eq(value);
			}
		}
		case LT: {
			if (path instanceof NumberExpression) {
				NumberExpression exp = (NumberExpression) path;
				return exp.lt((Number) value);
			} else if (value != null) {
				ComparableExpression exp = (ComparableExpression) path;
				return exp.lt((Comparable) value);
			}
			break;
		}
		case LOE: {
			if (path instanceof NumberExpression) {
				NumberExpression exp = (NumberExpression) path;
				return exp.loe((Number) value);
			} else if (value != null) {
				ComparableExpression exp = (ComparableExpression) path;
				return exp.loe((Comparable) value);
			}
			break;
		}
		case GT: {
			if (path instanceof NumberExpression) {
				NumberExpression exp = (NumberExpression) path;
				return exp.gt((Number) value);
			} else if (value != null) {
				ComparableExpression exp = (ComparableExpression) path;
				return exp.gt((Comparable) value);
			}
			break;
		}
		case GOE: {
			if (path instanceof NumberExpression) {
				NumberExpression exp = (NumberExpression) path;
				return exp.goe((Number) value);
			} else if (value != null) {
				ComparableExpression exp = (ComparableExpression) path;
				return exp.goe((Comparable) value);
			}
			break;
		}
		case BETWEEN: {
			ComparableExpression exp = (ComparableExpression) path;
			if (value instanceof Collection<?>) {
				List<? extends Comparable> list = toList((Collection<Comparable>) value);
				return exp.between(list.get(0), list.get(1));
			} else if (value instanceof Object[]) {
				Object[] binValues = (Object[]) value;
				return exp.between((Comparable) binValues[0], (Comparable) binValues[1]);
			}
			break;
		}
		case STARTS_WITH: {
			StringExpression exp = (StringExpression) path;
			return exp.startsWith(String.valueOf(value));
		}
		case STARTS_WITH_IC: {
			StringExpression exp = (StringExpression) path;
			return exp.startsWithIgnoreCase(String.valueOf(value));
		}
		case ENDS_WITH: {
			StringExpression exp = (StringExpression) path;
			return exp.endsWith(String.valueOf(value));
		}
		case ENDS_WITH_IC: {
			StringExpression exp = (StringExpression) path;
			return exp.endsWithIgnoreCase(String.valueOf(value));
		}
		case STRING_CONTAINS: {
			String str;
			if(value==null || (str=value.toString()).isEmpty()) {
				return null;
			}
			StringExpression exp = (StringExpression) path;
			return exp.contains(str);
		}
		case STRING_CONTAINS_IC: {
			String str;
			if(value==null || (str=value.toString()).isEmpty()) {
				return null;
			}
			StringExpression exp = (StringExpression) path;
			return exp.containsIgnoreCase(str);
		}
		case LIKE: {
			StringExpression exp = (StringExpression) path;
			return exp.like(String.valueOf(value));
		}
		case LIKE_IC: {
			StringExpression exp = (StringExpression) path;
			return exp.likeIgnoreCase(String.valueOf(value));
		}
		case IS_NULL: {
			SimpleExpression exp = (SimpleExpression) path;
			return exp.isNull();
		}
		case IS_NOT_NULL: {
			SimpleExpression exp = (SimpleExpression) path;
			return exp.isNotNull();
		}
		default:
		}
		}catch(Exception ex) {
			throw Exceptions.illegalArgument("Setting condition [{}] on path {} error", fieldName, path,ex);
		}
		throw new UnsupportedOperationException("Ops." + operator + " is not supported on field " + path);
	}

	@SuppressWarnings({ "rawtypes" })
	private int elements(Object value) {
		if (value instanceof Collection) {
			return ((Collection) value).size();
		}
		if(value.getClass().isArray()) {
			return Array.getLength(value);
		}
		return -1;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<? extends Comparable> toList(Collection<? extends Comparable> value) {
		if (value instanceof List) {
			return (List<Comparable>) value;
		} else {
			return new ArrayList<>(value);
		}
	}
	
	/*
	 *将泛型对象转换为主键条件。对于复合主键的场景，支持解析java.util.List、com.mysema.commons.lang.Pair(2列)三种方式包装的复合主键
	 * @param key
	 * @return
	 */
	private Predicate[] toPrimaryKeyPredicate(ID key) {
		RelationalPath<T> t = getPath();
		List<SimpleExpression<?>> ps = getPkColumn().stream().map(e -> (SimpleExpression<?>) e).collect(Collectors.toList());
		if (ps.isEmpty()) {
			throw Exceptions.unsupportedOperation("No primary key column found. {}", t);
		}
		if (ps.size() == 1) {
			return new Predicate[] { ps.get(0).eq(ConstantImpl.create(key)) };
		} else {
			if (key instanceof List) {
				List<?> keys = (List<?>) key;
				if (keys.size() != ps.size()) {
					throw Exceptions.illegalArgument("Input args not match the column number of primary keys. {} [{}]!=[{}]", t, keys.size(), ps.size());
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
				return c;
			}
		}
		throw Exceptions.illegalArgument("input key must be a List, because the table {} has {} primary key columns", t, ps.size());
	}
	
	@SuppressWarnings("unchecked")
	private <P> SimpleExpression<P> toSafePath(Path<P> p){
		if(p instanceof LambdaColumnBase) {
			return (SimpleExpression<P>)PathCache.getPath((LambdaColumnBase<?,P>)p);
		}
		return (SimpleExpression<P>)p;
	}
}
