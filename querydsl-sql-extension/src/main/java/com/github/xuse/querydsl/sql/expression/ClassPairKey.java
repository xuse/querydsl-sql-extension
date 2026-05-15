package com.github.xuse.querydsl.sql.expression;

/**
 * A cache key composed of two Class references (e.g. dtoType + entityType).
 * Used by {@link ProjectionsAlter} and {@link ConverterWrappedBean} for mapping caches.
 */
final class ClassPairKey {
	private final Class<?> first;
	private final Class<?> second;
	private final int hash;

	ClassPairKey(Class<?> first, Class<?> second) {
		this.first = first;
		this.second = second;
		this.hash = first.hashCode() * 31 + second.hashCode();
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClassPairKey) {
			ClassPairKey other = (ClassPairKey) obj;
			return this.first == other.first && this.second == other.second;
		}
		return false;
	}
}
