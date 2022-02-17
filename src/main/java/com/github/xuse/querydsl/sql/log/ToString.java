package com.github.xuse.querydsl.sql.log;

abstract class ToString<T> {
	abstract void append(T t,StringBuilder sb);
}
