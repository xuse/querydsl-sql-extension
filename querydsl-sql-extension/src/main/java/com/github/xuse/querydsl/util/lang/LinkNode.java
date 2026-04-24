package com.github.xuse.querydsl.util.lang;

import java.io.Serializable;

/**
 * 用于构造单向链表的类
 * @param <T>
 */
public class LinkNode<T> implements Serializable{
	LinkNode<T> next;
	
	T value;
	
	public LinkNode() {
	}
	
	public LinkNode(T t){
		this.value=t;
	}
}
