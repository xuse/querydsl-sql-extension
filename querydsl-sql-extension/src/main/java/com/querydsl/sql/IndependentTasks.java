package com.querydsl.sql;

import java.util.List;

import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.querydsl.core.types.Expression;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class IndependentTasks {
	private List<Constraint> constraints;
	private List<Expression<?>> comments;

}
