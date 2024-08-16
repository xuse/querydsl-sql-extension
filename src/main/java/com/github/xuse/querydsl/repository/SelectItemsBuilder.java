package com.github.xuse.querydsl.repository;

public class SelectItemsBuilder<T, Chain extends QueryWrapper<T, Chain>> {
	private final Chain typedChain;

	public SelectItemsBuilder(Chain chain) {
		this.typedChain = chain;
	}

	public Chain build() {
		return typedChain;
	}
}
