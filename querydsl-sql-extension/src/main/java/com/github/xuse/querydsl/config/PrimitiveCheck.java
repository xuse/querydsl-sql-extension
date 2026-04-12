package com.github.xuse.querydsl.config;

public enum PrimitiveCheck {
	/**
	 * 初始化时完全杜绝Primitive类型的误用
	 */
	STRICT(true, true),
	/**
	 * 当用户发生可能误用时抛出异常
	 */
	NORMAL(true, false),
	/**
	 * 完全不加任何检查和限制，一般不推荐，特定场景使用。
	 */
	FREE(false, false),;

	// 针对一个非NULL列使用Primitive类型时抛出异常。
	public final boolean exceptionIfNonNullColumn;
	// 在启动检测时，发现Primitive类型没有标注UnsavedValue时抛出异常。
	public final boolean exceptionIfMissUnsavedValue;

	PrimitiveCheck(boolean f1, boolean f2) {
		exceptionIfNonNullColumn = f1;
		exceptionIfMissUnsavedValue = f2;
	}
}
