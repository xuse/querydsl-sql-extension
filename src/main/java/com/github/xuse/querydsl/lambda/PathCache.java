package com.github.xuse.querydsl.lambda;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.util.Exceptions;
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
	private static final Map<Class<?>, RelationalPathEx<?>> TABLE_CACHE = new ConcurrentHashMap<>();
	private static final Map<LambdaColumn, Path<?>> COLUMN_CACHE = new ConcurrentHashMap<>();
	
	
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
	public static <B, T extends Comparable<T>> Path<T> getPath(LambdaColumn<B, T> func) {
		try {
			Path<T> p= (Path<T>) COLUMN_CACHE.computeIfAbsent(func, PathCache::generate);
			return p;
		}catch(Exception e) {
			throw Exceptions.toRuntime(e);
		}
	}

	public static <T> RelationalPathEx<T> getPath(LambdaTable<T> func) {
		return get(func.get());
	}

	@SuppressWarnings("unchecked")
	public static <T> RelationalPathEx<T> get(Class<T> beanType){
		RelationalPathEx<T> result= (RelationalPathEx<T>) TABLE_CACHE.computeIfAbsent(beanType, PathCache::generate);
		return result;
	}
	
	protected static <T> RelationalPathExImpl<T> generate(Class<T> beanType){
		log.info("Generate dynamic table path for {}",beanType);
		return RelationalPathExImpl.valueOf(beanType);
	}
	
	@SuppressWarnings("unchecked")
	protected static <B, T extends Comparable<T>> Path<T> generate(LambdaColumn<B, T> func) {
		Pair<String,String> pair;
		try {
			Method method = func.getClass().getDeclaredMethod("writeReplace");
			method.setAccessible(true);
			SerializedLambda o=(SerializedLambda)method.invoke(func);
			pair=Pair.of(o.getImplClass(), o.getImplMethodName());
		}catch(Exception e) {
			throw Exceptions.toRuntime(e);
		}
		ClassLoader cl=Thread.currentThread().getContextClassLoader();
		String clzName=pair.getFirst().replace('/', '.');
		String methodName=pair.getSecond();
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
			RelationalPathEx path = get(clz);
			Path<T> p=path.getColumn(fieldName);
			log.info("Generate column path for {}::{} from {}", clzName, methodName, path.getClass());
			return p;
		}catch(Exception e) {
			throw Exceptions.illegalState("path generate:{}.{} error.", clzName,methodName, e);
		}
	}
	
	public static boolean register(RelationalPathEx<?> tablePath) {
		RelationalPathEx<?> previous = TABLE_CACHE.putIfAbsent(tablePath.getType(), tablePath);
		return previous == null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> RelationalPathExImpl<T> compute(Class<? extends T> clz,Supplier<RelationalPathExImpl<T>> supplier) {
		return (RelationalPathExImpl<T>) TABLE_CACHE.computeIfAbsent(clz, (t)->supplier.get());
	}
}

