package com.github.xuse.querydsl.spring.core.resource;

public interface ResourcePatternResolver {

	String CLASSPATH_ALL_URL_PREFIX = "classpath*:";

	Resource[] getResources(String locationPattern);
}
