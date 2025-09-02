package com.github.xuse.querydsl.util.lang;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.TypeUtils;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.util.StringUtils;

public class Lambdas {
	private Lambdas() {
	}
	
	public static Pair<Class<?>, String> analysis(Serializable func) {
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
