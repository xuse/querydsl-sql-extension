package com.github.xuse.querydsl.config;

/**
 * Strategy for handling null values during batch insert operations with populateBatch mode.
 * <p>
 * In batch insert mode ({@code populateBatch}), all columns are explicitly included in the SQL statement.
 * When a bean field is null, the database DEFAULT mechanism is bypassed.
 * This class defines how the framework should handle this situation.
 * <p>
 * <b>Note:</b> If this strategy is {@code null}, the framework will use the traditional addBatch() path
 * (SAFE mode), where null columns are omitted from SQL, allowing database DEFAULT to work naturally.
 * <p>
 * This class is immutable and thread-safe.
 *
 * <h2>中文</h2>
 * populateBatch 模式下批量插入时遇到 null 值的处理策略。
 * <p>
 * 在批量插入模式（{@code populateBatch}）下，所有列都会显式出现在SQL中。
 * 当 Bean 字段为 null 时，数据库 DEFAULT 机制会被绕过。此类定义了框架应如何处理这种情况。
 * <p>
 * <b>注意：</b>如果此策略为 {@code null}，框架将使用传统的 addBatch() 路径（安全模式），
 * null 列会从 SQL 中省略，允许数据库 DEFAULT 自然生效。
 * <p>
 * 此类是不可变的，线程安全。
 */
public final class BatchNullStrategy {

	/**
	 * Default strategy: for NOT NULL columns, use defaultExpression if configured.
	 * Nullable columns are skipped (will receive NULL).
	 * <p>
	 * This is the recommended default for most applications.
	 * <p>
	 * 默认策略：对于 NOT NULL 列，使用配置的 defaultExpression。
	 * 可空列被跳过（将接收 NULL）。
	 * <p>
	 * 这是大多数应用推荐的默认策略。
	 */
	public static final BatchNullStrategy AUTO_DEFAULT = new BatchNullStrategy(
			ColumnStrategy.USE_DEFAULT, ColumnStrategy.SKIP);

	/**
	 * Aggressive fallback strategy: for NOT NULL columns, provide fallback values 
	 * even when no defaultExpression is defined. Nullable columns are skipped.
	 * <p>
	 * Use this when you prioritize "no errors" over "correct defaults".
	 * <p>
	 * 激进的兜底策略：对于 NOT NULL 列，即使没有定义 defaultExpression，也自动提供兜底值。
	 * 可空列被跳过。
	 * <p>
	 * 当你优先考虑"不报错"而非"正确的默认值"时使用此策略。
	 */
	public static final BatchNullStrategy AGGRESSIVE_DEFAULT = new BatchNullStrategy(
			ColumnStrategy.AGGRESSIVE, ColumnStrategy.SKIP);

	private final ColumnStrategy notNullStrategy;
	private final ColumnStrategy nullableStrategy;

	private BatchNullStrategy(ColumnStrategy notNullStrategy, ColumnStrategy nullableStrategy) {
		this.notNullStrategy = notNullStrategy;
		this.nullableStrategy = nullableStrategy;
	}

	/**
	 * Create a new strategy with custom settings for NOT NULL and nullable columns.
	 * <p>
	 * 创建一个自定义 NOT NULL 列和可空列策略的新实例。
	 * 
	 * @param notNullStrategy strategy for NOT NULL columns
	 * @param nullableStrategy strategy for nullable columns
	 * @return a new BatchNullStrategy with the specified settings
	 */
	public static BatchNullStrategy of(ColumnStrategy notNullStrategy, ColumnStrategy nullableStrategy) {
		if (notNullStrategy == null || nullableStrategy == null) {
			throw new IllegalArgumentException("Column strategies cannot be null. Use null BatchNullStrategy for SAFE mode.");
		}
		return new BatchNullStrategy(notNullStrategy, nullableStrategy);
	}

	/**
	 * Create a new strategy with a different setting for NOT NULL columns.
	 * <p>
	 * This method returns a new immutable instance and does not modify the original.
	 * <p>
	 * 创建一个修改 NOT NULL 列策略的新实例。
	 * 此方法返回新的不可变实例，不会修改原实例。
	 * 
	 * @param strategy strategy for NOT NULL columns
	 * @return a new BatchNullStrategy with the specified setting
	 */
	public BatchNullStrategy withNotNullStrategy(ColumnStrategy strategy) {
		if (strategy == null) {
			throw new IllegalArgumentException("Column strategy cannot be null");
		}
		if (this.notNullStrategy == strategy) {
			return this;
		}
		return new BatchNullStrategy(strategy, this.nullableStrategy);
	}

	/**
	 * Create a new strategy with a different setting for nullable columns.
	 * <p>
	 * This method returns a new immutable instance and does not modify the original.
	 * <p>
	 * 创建一个修改可空列策略的新实例。
	 * 此方法返回新的不可变实例，不会修改原实例。
	 * 
	 * @param strategy strategy for nullable columns
	 * @return a new BatchNullStrategy with the specified setting
	 */
	public BatchNullStrategy withNullableStrategy(ColumnStrategy strategy) {
		if (strategy == null) {
			throw new IllegalArgumentException("Column strategy cannot be null");
		}
		if (this.nullableStrategy == strategy) {
			return this;
		}
		return new BatchNullStrategy(this.notNullStrategy, strategy);
	}

	/**
	 * Get the strategy for NOT NULL columns.
	 * <p>
	 * 获取 NOT NULL 列的策略。
	 * 
	 * @return the strategy for NOT NULL columns
	 */
	public ColumnStrategy getNotNullStrategy() {
		return notNullStrategy;
	}

	/**
	 * Get the strategy for nullable columns.
	 * <p>
	 * 获取可空列的策略。
	 * 
	 * @return the strategy for nullable columns
	 */
	public ColumnStrategy getNullableStrategy() {
		return nullableStrategy;
	}

	@Override
	public String toString() {
		return "BatchNullStrategy{notNull=" + notNullStrategy + ", nullable=" + nullableStrategy + "}";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		BatchNullStrategy that = (BatchNullStrategy) obj;
		return notNullStrategy == that.notNullStrategy && nullableStrategy == that.nullableStrategy;
	}

	@Override
	public int hashCode() {
		int result = notNullStrategy != null ? notNullStrategy.hashCode() : 0;
		result = 31 * result + (nullableStrategy != null ? nullableStrategy.hashCode() : 0);
		return result;
	}

	/**
	 * Strategy for handling null values on a specific type of column (NOT NULL or nullable).
	 * <p>
	 * 针对特定类型列（NOT NULL 或可空）的 null 值处理策略。
	 */
	public enum ColumnStrategy {
		/**
		 * Skip processing - the column will receive NULL value.
		 * For NOT NULL columns, this may cause constraint violations.
		 * <p>
		 * 跳过处理 - 列将接收 NULL 值。
		 * 对于 NOT NULL 列，这可能导致约束违反。
		 */
		SKIP,

		/**
		 * Use default value substitution with the configured defaultExpression.
		 * If no defaultExpression is configured, the column will receive NULL (same as SKIP).
		 * <p>
		 * 使用配置的 defaultExpression 应用默认值替换。
		 * 如果没有配置 defaultExpression，列将接收 NULL（等同于 SKIP）。
		 */
		USE_DEFAULT,

		/**
		 * Aggressive fallback: provide a fallback value even when no defaultExpression is defined.
		 * Priority: defaultExpression > aggressive fallback > NULL.
		 * <p>
		 * Fallback values: String→"", numbers→0, boolean→false.
		 * If the zero value is marked as unsaved, tries alternative values (1, true, " ").
		 * <p>
		 * For nullable columns, this behaves the same as USE_DEFAULT (no aggressive fallback).
		 * <p>
		 * 激进兜底：即使没有定义 defaultExpression，也提供兜底值。
		 * 优先级：defaultExpression > 激进兜底 > NULL。
		 * <p>
		 * 兜底值：String→""，数字→0，boolean→false。
		 * 如果零值被标记为 unsaved，尝试替代值（1、true、" "）。
		 * <p>
		 * 对于可空列，此策略等同于 USE_DEFAULT（不提供激进兜底）。
		 */
		AGGRESSIVE
	}
}
