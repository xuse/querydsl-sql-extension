package com.github.xuse.querydsl.entity;

import com.github.xuse.querydsl.annotation.query.ConditionBean;

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
	private String authContent;
}
