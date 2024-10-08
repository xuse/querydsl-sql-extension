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
     * @param locale The locale that is used for the letter-case conversion.
     */
    private ChangeLetterCaseNameMapping2(Locale locale) {
        this.locale = Objects.requireNonNull(locale);
    }

    @Override
    public Optional<String> getColumnOverride(SchemaAndTable key, String column) {
        return Optional.ofNullable(targetCaseOrNull(column));
    }

    @Override
    public Optional<SchemaAndTable> getOverride(SchemaAndTable key) {
        return Optional.of(new SchemaAndTable(targetCaseOrNull(key.getSchema()), targetCaseOrNull(key.getTable())));
    }

    abstract String targetCaseOrNull(String schema);

	private static final class UPPER extends ChangeLetterCaseNameMapping2{
		UPPER(Locale locale){
			super(locale);
		}
		
		@Override
		String targetCaseOrNull(String schema) {
			return schema==null? null: schema.toUpperCase(locale);
		}
    }
	
	private static final class LOWER extends ChangeLetterCaseNameMapping2{
		LOWER(Locale locale){
			super(locale);
		}
		
		@Override
		String targetCaseOrNull(String schema) {
			return schema==null? null: schema.toLowerCase(locale);
		}
    }

	public static NameMapping valueOf(com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase letterCase,
			Locale locale) {
		if(letterCase==LetterCase.UPPER) {
			return new UPPER(locale);
		}else {
			return new LOWER(locale);
		}
	}

}
