package com.github.xuse.querydsl.util.lang;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.TypeUtils;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.util.StringUtils;

public class Lambdas {
	private static final Map<Serializable, Pair<Class<?>, String>> LAMBDA_CACHE = new ConcurrentHashMap<>();
	
	private Lambdas() {
	}
	
	public static Pair<Class<?>, String> analysis(Serializable func) {
		return LAMBDA_CACHE.computeIfAbsent(func, Lambdas::analysis0);
	}
	
	private static Pair<Class<?>, String> analysis0(Serializable func) {
		Class<?> funcClz=func.getClass();
		//如果用户不停创建对象并调用此方法，会一直在内存中无法释放。并被用户认为是内存泄漏BUG。
		// 所以此处进行限制，正常情况下只有Lambda类型的对象可以被处理。而lambda正常情况下由代码编译而来，在一个项目中其数量总是有限的。
//		if(funcClz.getName().indexOf("$$Lambda/")==-1) {
//			throw new UnsupportedOperationException("This utilite is use for lambda type only.");
//		}
		ClassLoader cl=Thread.currentThread().getContextClassLoader();
		String clzName;
		String methodName;
		try {
			Method method = funcClz.getDeclaredMethod("writeReplace");
			method.setAccessible(true);
			SerializedLambda o=(SerializedLambda)method.invoke(func);
			clzName=o.getImplClass().replace('/', '.');
			methodName=o.getImplMethodName();			
		}catch(Exception e) {
			throw Exceptions.toRuntime(e);
		}
		try {
			Class<?> clz = cl.loadClass(clzName);
			String fieldName;
			if(TypeUtils.isRecord(clz) || clz.isAnnotation()) {
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
}
