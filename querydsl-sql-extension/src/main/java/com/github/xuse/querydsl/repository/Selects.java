package com.github.xuse.querydsl.repository;

import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.querydsl.core.group.QPair;

public class Selects extends ProjectionsAlter{
	public static <K extends Comparable<K>,V extends Comparable<V>> QPair<K,V> pair(LambdaColumn<?,K> expr1,LambdaColumn<?,V> expr2){
    	return new QPair<>(expr1,expr2);
    }
}
