package com.github.xuse.querydsl.spring.core.resource;

import java.io.IOException;

public interface ResourcePatternResolver {

	String CLASSPATH_ALL_URL_PREFIX = "classpath*:";

	Resource[] getResources(String locationPattern) throws IOException;
}
