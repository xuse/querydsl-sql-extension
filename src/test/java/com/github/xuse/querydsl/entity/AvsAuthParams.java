package com.github.xuse.querydsl.entity;

import java.util.Date;

import com.github.xuse.querydsl.annotation.Condition;
import com.github.xuse.querydsl.annotation.ConditionBean;
import com.querydsl.core.types.Ops;

@ConditionBean(additional = {"dateGt","dateLoe"},limitField = "limit",offsetField = "offset")
public class AvsAuthParams {
	@Condition(Ops.STARTS_WITH)
	private String authContent;
	@Condition(Ops.GT)
	private int authType;
	@Condition
	private Integer channelNo;

	@Condition(Ops.BETWEEN)
	private Date[] createTime;
	
	private Integer limit;
	
	private Integer offset;
	
	@Condition(value=Ops.GT,name="createTime")
	private Date dateGt;
	
	@Condition(value=Ops.LOE,name="createTime")
	private Date dateLoe;
	

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

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public Date getDateGt() {
		return dateGt;
	}

	public void setDateGt(Date dateGt) {
		this.dateGt = dateGt;
	}

	public Date getDateLoe() {
		return dateLoe;
	}

	public void setDateLoe(Date dateLoe) {
		this.dateLoe = dateLoe;
	}
}
