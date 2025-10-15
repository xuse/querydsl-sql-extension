package com.github.xuse.querydsl.util;

public class Holder<T> {
	public volatile T value;
	
	public T get() {
		return value;
	}

	public Holder(){
	}
	
	public Holder(T t){
		value=t;
	}
}
