package com.github.xuse.querydsl.entity;

import java.util.Date;

import com.github.xuse.querydsl.annotation.Condition;
import com.github.xuse.querydsl.sql.ConditionBean;
import com.querydsl.core.types.Ops;

//@Builder
public class AvsAuthParams implements ConditionBean {
	@Condition(Ops.STARTS_WITH)
	private String authContent;
	@Condition(Ops.GT)
	private int authType;
	@Condition
	private Integer channelNo;

	@Condition(Ops.BETWEEN)
	private Date[] createTime;

	public String getAuthContent() {
		return authContent;
	}

	public void setAuthContent(String authContent) {
		this.authContent = authContent;
	}

	public int getAuthType() {
		return authType;
	}

	public void setAuthType(int authType) {
		this.authType = authType;
	}

	public Integer getChannelNo() {
		return channelNo;
	}

	public void setChannelNo(Integer channelNo) {
		this.channelNo = channelNo;
	}

	public Date[] getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date[] createTime) {
		this.createTime = createTime;
	}

}
