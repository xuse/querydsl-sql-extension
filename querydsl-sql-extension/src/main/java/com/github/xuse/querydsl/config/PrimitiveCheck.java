package com.github.xuse.querydsl.config;

public enum PrimitiveCheck {
	/**
	 * <h2>English:</h2> Strictly prevent misuse of primitive types during initialization.
	 * <h2>Chinese:</h2> 初始化时完全杜绝Primitive类型的误用
	 */
	STRICT(true, true),
	/**
	 * <h2>English:</h2> Throw an exception when potential misuse is detected.
	 * <h2>Chinese:</h2> 当用户发生可能误用时抛出异常
	 */
	NORMAL(true, false),
	/**
	 * <h2>English:</h2> No checks or restrictions at all. Not recommended in general; use in specific scenarios.
	 * <h2>Chinese:</h2> 完全不加任何检查和限制，一般不推荐，特定场景使用。
	 */
	FREE(false, false),;

	/**
	 * <h2>English:</h2> Throw exception when a non-nullable column uses a primitive type.
	 * <h2>Chinese:</h2> 针对一个非NULL列使用Primitive类型时抛出异常。
	 */
	public final boolean exceptionIfNonNullColumn;
	/**
	 * <h2>English:</h2> Throw exception when a primitive type field is missing @UnsavedValue annotation during startup check.
	 * <h2>Chinese:</h2> 在启动检测时，发现Primitive类型没有标注UnsavedValue时抛出异常。
	 */
	public final boolean exceptionIfMissUnsavedValue;

	PrimitiveCheck(boolean f1, boolean f2) {
		exceptionIfNonNullColumn = f1;
		exceptionIfMissUnsavedValue = f2;
	}
}
