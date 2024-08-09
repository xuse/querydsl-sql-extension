package com.github.xuse.querydsl.entity;

import java.util.Date;

import com.github.xuse.querydsl.annotation.query.Condition;
import com.github.xuse.querydsl.annotation.query.ConditionBean;
import com.github.xuse.querydsl.annotation.query.Order;
import com.querydsl.core.types.Ops;

/**
 *  满足这样一种场景。有一些固定的组合条件查询。（比如从前端页面传入若干字段）其中一些字段可以为空，即不作为过滤条件。凡是传入有效数值的条件，都要参与查询过滤。
 * 为此，可以定义一个Bean，将查询条件固定下来。通过@Condition注解，配置每个条件的运算操作符。
 * @author jiyi
*/
@ConditionBean(limitField = "limit",offsetField = "offset",isRequireTotalField = "fetchTotal")
public class AvsAuthParams {
	
	@Condition(Ops.STRING_CONTAINS_IC)
	private String authContent;
	
	@Condition(Ops.GT)
	private int authType;
	
	@Condition
	private Integer channelNo;

	@Condition(Ops.BETWEEN)
	private Date[] createTime;
	
	@Condition(value=Ops.GT,path="createTime")
	private Date dateGt;
	
	@Condition(value=Ops.LOE,path="createTime")
	private Date dateLoe;
	
	@Order(sortField = "orderAsc")
	private String order;
	
	private boolean orderAsc;
	
	
	private Integer limit;
	
	private Integer offset;
	
	private boolean fetchTotal;

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

	public boolean isFetchTotal() {
		return fetchTotal;
	}

	public void setFetchTotal(boolean fetchTotal) {
		this.fetchTotal = fetchTotal;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public boolean isOrderAsc() {
		return orderAsc;
	}

	public void setOrderAsc(boolean orderAsc) {
		this.orderAsc = orderAsc;
	}
}
