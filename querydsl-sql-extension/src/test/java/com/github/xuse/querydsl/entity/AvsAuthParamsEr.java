package com.github.xuse.querydsl.entity;

import com.github.xuse.querydsl.annotation.query.Condition;
import com.github.xuse.querydsl.annotation.query.ConditionBean;
import com.github.xuse.querydsl.annotation.query.Ops;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ConditionBean()
public class AvsAuthParamsEr {
	@Condition(Ops.BETWEEN)
	private String authContent;
}
