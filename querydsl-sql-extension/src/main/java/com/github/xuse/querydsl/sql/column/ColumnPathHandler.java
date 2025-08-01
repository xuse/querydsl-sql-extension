package com.github.xuse.querydsl.sql.column;

/**
 * Column的映射相关配置
 * @param <T> The type of target object.
 * @param <Q> The type of target object.
 */
public class ColumnPathHandler<T, Q> extends ColumnBuilderBase<T, ColumnPathHandler<T, Q>> {

	private final Q mixin;

	public ColumnPathHandler(PathMapping p, Q mixin) {
		super(p);
		super.q = this;
		this.mixin = mixin;
	}

	/**
	 *  @return 完成Column的配置，回到原来的Builder链上。
	 */
	public Q build() {
		return mixin;
	}
}
