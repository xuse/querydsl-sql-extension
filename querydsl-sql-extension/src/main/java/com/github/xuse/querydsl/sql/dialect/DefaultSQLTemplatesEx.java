package com.github.xuse.querydsl.sql.dialect;


import java.util.HashSet;
import java.util.Set;

import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.ddl.ConstraintType;
import com.github.xuse.querydsl.util.Assert;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.namemapping.ChangeLetterCaseNameMapping.LetterCase;

public class DefaultSQLTemplatesEx implements SQLTemplatesEx {
	protected final TypeNames typeNames = TypeNames.generateDefault();
	
	protected final SQLTemplates template;
	
	protected final Set<Operator> unsupports=new HashSet<>();
	
	protected boolean bacthToBulk;
	
	public DefaultSQLTemplatesEx(SQLTemplates template) {
		Assert.notNull(template);
		this.template=template;
	}
	
	protected void addUnsupports(Operator... ops) {
		for(Operator op:ops) {
			if(op!=null) {
				unsupports.add(op);
			}
		}
	}

	@Override
	public boolean supportCreateInTableDefinition(ConstraintType type) {
		return !type.isIndex();
	}

	public LetterCase getDefaultLetterCase() {
		return LetterCase.UPPER;
	}
	
	public ColumnDef getColumnDataType(int sqlTypes, int size, int scale) {
		return typeNames.get(sqlTypes, size, scale);
	}

	@Override
	public boolean notSupports(Operator op) {
		return template.getTemplate(op)==null || unsupports.contains(op);
	}

	@Override
	public SQLTemplates getOriginal() {
		return template;
	}

	@Override
	public boolean isBatchToBulkInDefault() {
		return bacthToBulk;
	}
}
