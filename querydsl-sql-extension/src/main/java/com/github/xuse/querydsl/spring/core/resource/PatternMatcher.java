package com.github.xuse.querydsl.spring.core.resource;

public interface PatternMatcher {

	boolean match(String pattern, String source);

	boolean matchStart(String fullPattern, String string);

	boolean isPattern(String substring);
}
