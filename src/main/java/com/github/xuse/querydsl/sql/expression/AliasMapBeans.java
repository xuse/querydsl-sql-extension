package com.github.xuse.querydsl.sql.expression;

import java.util.Map;

public class AliasMapBeans {

    private final Map<String, ?> beans;

    public AliasMapBeans(Map<String, ?> beans) {
        this.beans = beans;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path) {
        return (T) beans.get(path);
    }

}