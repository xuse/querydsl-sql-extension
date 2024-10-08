package com.github.xuse.querydsl.lambda;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.NoReadLockHashMap;
import com.github.xuse.querydsl.util.TypeUtils;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("rawtypes")
@Slf4j
public class PathCache {
	private static final Map<Class<?>, TablePathHolder> TABLE_CACHE = new ConcurrentHashMap<>();
	private static final Map<LambdaColumnBase, Path<?>> COLUMN_CACHE = new ConcurrentHashMap<>();
	
	static class TablePathHolder{
		@SuppressWarnings("unused")
		private final Class<?> beanType;
		private final RelationalPathEx<?> defaultPath;
		private final String defaultPathVariable;
		private final Map<String,RelationalPathEx<?>> data=new NoReadLockHashMap<>(12);

		TablePathHolder(Class<?> beanType) {
			this.beanType = beanType;
			log.info("Generate dynamic table path for {}", beanType);
			this.defaultPath = RelationalPathExImpl.valueOf(beanType, null);
			this.defaultPathVariable = defaultPath.getMetadata().getName();
		}
		
		TablePathHolder(Class<?> beanType, RelationalPathEx<?> path) {
			this.beanType = beanType;
			this.defaultPath = path;
			this.defaultPathVariable = defaultPath.getMetadata().getName();
		}

		public RelationalPathEx<?> get(String variable) {
			if(variable==null || variable.isEmpty() || defaultPathVariable.equals(variable)) {
				return defaultPath;
			}
			return data.computeIfAbsent(variable, this::generate);
		}

		private RelationalPathEx<?> generate(String variable) {
			return ((RelationalPathBaseEx) defaultPath).copyTo(variable);
		}

		/**
		 * @param tablePath tablePath
		 * @return true表示新增成功，false表示已有
		 */
		public boolean add(RelationalPathEx<?> tablePath) {
			String variable = tablePath.getMetadata().getName();
			if(defaultPathVariable.equals(variable)) {
				return false;
			}
			return data.put(variable, tablePath) == null;
		}

		@SuppressWarnings("unchecked")
		public <T> RelationalPathEx<T> computeIfAbsent(String variable, Supplier<RelationalPathExImpl<T>> supplier) {
			RelationalPathEx<?> result;
			if (variable == null || variable.isEmpty() || defaultPathVariable.equals(variable)) {
				result = defaultPath;
			} else {
				result = data.computeIfAbsent(variable, e -> supplier.get());
			}
			return (RelationalPathEx<T>) result;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <B, T extends Comparable<T>> ComparableExpression<T> getPathAsExpr(LambdaColumn<B, T> func) {
		Path<T> path=getPath(func);
		if(path instanceof ComparableExpression){
			return (ComparableExpression<T>)path;
		}else {
			return Expressions.asComparable(path);
		}
	}

	@SuppressWarnings("unchecked")
	public static <B, T> Path<T> getPath(LambdaColumnBase<B, T> func) {
		try {
			Path<T> p = (Path<T>) COLUMN_CACHE.computeIfAbsent(func, PathCache::generate);
			return p;
		}catch(Exception e) {
			throw Exceptions.toRuntime(e);
		}
	}

	public static <T> RelationalPathEx<T> getPath(LambdaTable<T> func, String variable) {
		return get(func.get(), variable);
	}

	@SuppressWarnings("unchecked")
	public static <T> RelationalPathEx<T> get(Class<T> clazz,String variable){
		return (RelationalPathEx<T>) TABLE_CACHE.computeIfAbsent(clazz, TablePathHolder::new).get(variable);
	}
	
	@SuppressWarnings("unchecked")
	protected static <B, T> Path<T> generate(LambdaColumnBase<B, T> func) {
		Pair<Class<?>, String> pair = analysis(func);
		RelationalPathEx path = get(pair.getFirst(), null);
		Class<?> clazz = pair.getFirst();
		String fieldName = pair.getSecond();
		Path<T> p = path.getColumn(fieldName);
		log.info("Generate column path for {}::{} from {}", clazz.getName(), fieldName, path.getClass());
		return p;
	}
	
	public static Pair<Class<?>, String> analysis(LambdaColumnBase<?, ?> func) {
		ClassLoader cl=Thread.currentThread().getContextClassLoader();
		String clzName;
		String methodName;
		try {
			Method method = func.getClass().getDeclaredMethod("writeReplace");
			method.setAccessible(true);
			SerializedLambda o=(SerializedLambda)method.invoke(func);
			clzName=o.getImplClass().replace('/', '.');
			methodName=o.getImplMethodName();			
		}catch(Exception e) {
			throw Exceptions.toRuntime(e);
		}
		try {
			Class<?> clz = cl.loadClass(clzName);
			boolean isRecord = TypeUtils.isRecord(clz);
			String fieldName;
			if(isRecord) {
				fieldName = methodName;
			}else {
				if (methodName.startsWith("get")) {
					fieldName = methodName.substring(3);
				} else if (methodName.startsWith("is")) {
					fieldName = methodName.substring(2);
				} else {
					throw Exceptions.illegalArgument(
							"Method should started with 'get' or 'is', {}.{} is invalid.", clzName, methodName);
				}	
			}
			fieldName = StringUtils.uncapitalize(fieldName);
			return Pair.of(clz, fieldName);
		}catch(Exception e) {
			throw Exceptions.illegalState("path generate:{}.{} error.", clzName,methodName, e);
		}
	}

	public static boolean register(RelationalPathEx<?> tablePath) {
		TablePathHolder holder = TABLE_CACHE.computeIfAbsent(tablePath.getType(), clz -> new TablePathHolder(clz, tablePath));
		return holder.add(tablePath);
	}
	
	public static <T> RelationalPathEx<T> compute(Class<? extends T> clz, String variabe, Supplier<RelationalPathExImpl<T>> supplier) {
		TablePathHolder holder=TABLE_CACHE.computeIfAbsent(clz, TablePathHolder::new);
		return holder.computeIfAbsent(variabe,supplier);
	}
}


