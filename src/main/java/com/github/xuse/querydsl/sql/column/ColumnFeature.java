package com.github.xuse.querydsl.sql.column;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.core.types.dsl.Expressions;

public enum ColumnFeature {
	AUTO_INCREMENT{
		@Override
		public Expression<?> get(SQLTemplatesEx dialectEx) {
			return Expressions.simpleTemplate(Object.class, dialectEx.getAutoIncrement());
		}
	},
	PRIMARY_KEY{
		@Override
		public Expression<?> get(SQLTemplatesEx dialectEx) {
			return Expressions.simpleTemplate(Object.class, "PRIMARY KEY");
		}
	},
	;
	public abstract Expression<?> get(SQLTemplatesEx dialectEx);
}
