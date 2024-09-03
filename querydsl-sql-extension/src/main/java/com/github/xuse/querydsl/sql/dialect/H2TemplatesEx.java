package com.github.xuse.querydsl.sql.dialect;

import java.sql.Types;

import com.github.xuse.querydsl.sql.ddl.ConstraintType;
import com.github.xuse.querydsl.sql.ddl.DDLOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.Basic;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.SQLTemplates;

public class H2TemplatesEx extends DefaultSQLTemplatesEx {

	public H2TemplatesEx(SQLTemplates templates) {
		super(templates);
		typeNames.put(Types.TINYINT, "smallint").type(Types.SMALLINT);
	}

	@Override
	public boolean supportCreateInTableDefinition(ConstraintType type) {
		return !(type == ConstraintType.KEY || type == ConstraintType.HASH);
	}

	@Override
	public void init(SQLTemplates templates) {
		SQLTemplatesEx.initDefaultDDLTemplate(templates);
		add(templates, DDLOps.COLUMN_ALLOW_NULL, "");
		add(templates, DDLOps.CHARSET, "{0}");
		add(templates, DDLOps.COLLATE, "{0}");
		add(templates, DDLOps.UNSIGNED, "{0}");
		add(templates, Basic.SELECT_VALUES, "values {0}");

		add(templates, ConstraintType.UNIQUE, "CONSTRAINT {1} UNIQUE {2}");
	}

}
