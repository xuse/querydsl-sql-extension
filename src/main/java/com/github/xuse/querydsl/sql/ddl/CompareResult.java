package com.github.xuse.querydsl.sql.ddl;

import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.querydsl.core.types.Operator;
import lombok.Data;

import java.util.*;

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
	 * 表备注等修改
	 */
	private Map<Operator,String> otherChange = new HashMap<>();

	public CompareResult ofAddSingleColumn(ColumnMapping c) {
		CompareResult cr = new CompareResult();
		cr.setAddColumns(Collections.singletonList(c));
		return cr;
	}

	public CompareResult ofDropSingleColumn(String c) {
		CompareResult cr = new CompareResult();
		cr.setDropColumns(Collections.singletonList(c));
		return cr;
	}

	public CompareResult ofSingleChangeColumn(ColumnModification c) {
		CompareResult cr = new CompareResult();
		cr.setChangeColumns(Collections.singletonList(c));
		return cr;
	}

	public CompareResult ofAddSingleConstraint(Constraint c) {
		CompareResult cr = new CompareResult();
		cr.setAddConstraints(Collections.singletonList(c));
		return cr;
	}

	public CompareResult ofDropSingleConstraint(Constraint c) {
		CompareResult cr = new CompareResult();
		cr.setDropConstraints(Collections.singletonList(c));
		return cr;
	}
	
	public void addDropConstraints(List<Constraint> toDrop) {
		this.dropConstraints.addAll(toDrop);
	}
	
	public void addCreateConstraints(List<Constraint> toCreate) {
		this.addConstraints.addAll(toCreate);
	}

	public void addOtherChange(Operator operator, String change) {
		this.otherChange.put(operator, change);
		
	}
	public void setTableCommentChange(String changeTo) {
		this.otherChange.put(DDLOps.COMMENT_ON_COLUMN, changeTo);
	}
	
	public void setTableCollation(String changeTo) {
		this.otherChange.put(DDLOps.COLLATE, changeTo);
	}
	
	public boolean hasOtherChange(Operator op) {
		return this.otherChange.containsKey(op);
	}
 }