package com.github.xuse.querydsl.sql.dialect;

import java.sql.Types;

import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.querydsl.core.types.ConstraintType;
import com.querydsl.core.types.DDLOps;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.core.types.DDLOps.Basic;
import com.querydsl.sql.SQLTemplates;

public class H2SQLTemplatesEx extends DefaultSQLTemplatesEx {

	public H2SQLTemplatesEx(SQLTemplates templates) {
		super(templates);
		typeNames.put(Types.TINYINT, "smallint").type(Types.SMALLINT);
	}

	public ColumnDef getColumnDataType(int sqlTypes, int size, int scale) {
		return typeNames.get(sqlTypes, size, scale);
	}

	@Override
	public boolean supportCreateInTableDefinition(ConstraintType type) {
		return !(type == ConstraintType.KEY || type == ConstraintType.HASH);
	}

	@Override
	public void init(SQLTemplates templates) {
		SQLTemplatesEx.initDefaultDDLTemplate(templates);
		add(templates, DDLOps.COLUMN_ALLOW_NULL, "");
		add(templates, DDLOps.COMMENT, "{0}");
		add(templates, DDLOps.CHARSET, "{0}");
		add(templates, DDLOps.COLLATE, "{0}");
		add(templates, DDLOps.UNSIGNED, "{0}");
		add(templates, Basic.SELECT_VALUES, "values {0}");

		add(templates, ConstraintType.UNIQUE, "CONSTRAINT {1} UNIQUE {2}");
	}

}
