package com.github.xuse.querydsl.sql.ddl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.querydsl.core.types.DDLOps;
import com.querydsl.core.types.Operator;

import lombok.Data;

@Data
public class CompareResult {

	public boolean isEmpty() {
		return dropColumns.isEmpty() && changeColumns.isEmpty() && addColumns.isEmpty() && addConstraints.isEmpty()
				&& dropConstraints.isEmpty() && otherChange.isEmpty();
	}

	private List<Constraint> addConstraints = new ArrayList<>();

	private List<Constraint> dropConstraints = new ArrayList<>();

	private List<String> dropColumns = Collections.emptyList();

	private List<ColumnModification> changeColumns = Collections.emptyList();

	private Collection<ColumnMapping> addColumns = Collections.emptyList();
	
	/**
	 * 表备注修改
	 */
	private Map<Operator,String> otherChange = new HashMap<>();

	public CompareResult ofAddSingleColumn(ColumnMapping c) {
		CompareResult cr = new CompareResult();
		cr.setAddColumns(Arrays.asList(c));
		return cr;
	}

	public CompareResult ofDropSingleColumn(String c) {
		CompareResult cr = new CompareResult();
		cr.setDropColumns(Arrays.asList(c));
		return cr;
	}

	public CompareResult ofSingleChangeColumn(ColumnModification c) {
		CompareResult cr = new CompareResult();
		cr.setChangeColumns(Arrays.asList(c));
		return cr;
	}

	public CompareResult ofAddSingleConstraint(Constraint c) {
		CompareResult cr = new CompareResult();
		cr.setAddConstraints(Arrays.asList(c));
		return cr;
	}

	public CompareResult ofDropSingleConstraint(Constraint c) {
		CompareResult cr = new CompareResult();
		cr.setDropConstraints(Arrays.asList(c));
		return cr;
	}
	
	public void addDropConstraints(List<Constraint> toDrop) {
		this.dropConstraints.addAll(toDrop);
	}
	
	public void addCreateConstraints(List<Constraint> toCreate) {
		this.addConstraints.addAll(toCreate);
	}

	public void setTableCommentChange(String changeTo) {
		this.otherChange.put(DDLOps.COMMENT, changeTo);
	}
	
	public void setTableCollation(String changeTo) {
		this.otherChange.put(DDLOps.COLLATE, changeTo);
	}
	
	public boolean hasOtherChange(Operator op) {
		return this.otherChange.containsKey(op);
	}
 }