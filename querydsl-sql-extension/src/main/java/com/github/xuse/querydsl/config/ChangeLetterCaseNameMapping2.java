package com.github.xuse.querydsl.config;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.querydsl.sql.SchemaAndTable;
import com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase;
import com.querydsl.sql.namemapping.NameMapping;

public abstract class ChangeLetterCaseNameMapping2 implements NameMapping {
	Locale locale;

	/**
	 * Constructor.
	 * 
	 * @param locale The locale that is used for the letter-case conversion.
	 */
	private ChangeLetterCaseNameMapping2(Locale locale) {
		this.locale = Objects.requireNonNull(locale);
	}

	/**
	 * 重写 getColumnOverride 方法以提供列覆盖逻辑
	 * 此方法用于获取指定表中列的覆盖名称如果列没有被覆盖，则返回空的 Optional
	 * 
	 * @param key    表的模式和表名，用于定位特定的数据库表
	 * @param column 列的原始名称，可能需要根据某些规则进行转换或覆盖
	 * @return 返回一个 Optional 对象，其中可能包含列的覆盖名称如果列没有被覆盖，则返回 Optional.empty()
	 */
	@Override
	public Optional<String> getColumnOverride(SchemaAndTable key, String column) {
		return Optional.ofNullable(targetCaseOrNull(column));
	}

	@Override
	public Optional<SchemaAndTable> getOverride(SchemaAndTable key) {
		return Optional.of(new SchemaAndTable(targetCaseOrNull(key.getSchema()), targetCaseOrNull(key.getTable())));
	}

	abstract String targetCaseOrNull(String schema);

	private static final class UPPER extends ChangeLetterCaseNameMapping2 {
		UPPER(Locale locale) {
			super(locale);
		}

		@Override
		String targetCaseOrNull(String schema) {
			return schema == null ? null : schema.toUpperCase(locale);
		}
	}

	private static final class LOWER extends ChangeLetterCaseNameMapping2 {
		LOWER(Locale locale) {
			super(locale);
		}

		@Override
		String targetCaseOrNull(String schema) {
			return schema == null ? null : schema.toLowerCase(locale);
		}
	}

	public static NameMapping valueOf(com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase letterCase,
			Locale locale) {
		if (letterCase == LetterCase.UPPER) {
			return new UPPER(locale);
		} else {
			return new LOWER(locale);
		}
	}

}
