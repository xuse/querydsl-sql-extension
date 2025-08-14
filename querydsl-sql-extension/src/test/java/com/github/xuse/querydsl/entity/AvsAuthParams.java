package com.github.xuse.querydsl.entity;

import java.util.Date;
import java.util.List;

import com.github.xuse.querydsl.annotation.query.Condition;
import com.github.xuse.querydsl.annotation.query.ConditionBean;
import com.github.xuse.querydsl.annotation.query.IntCase;
import com.github.xuse.querydsl.annotation.query.Order;
import com.github.xuse.querydsl.annotation.query.When;
import com.querydsl.core.types.Ops;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *  满足这样一种场景。有一些固定的组合条件查询。（比如从前端页面传入若干字段）其中一些字段可以为空，即不作为过滤条件。凡是传入有效数值的条件，都要参与查询过滤。
 * 为此，可以定义一个Bean，将查询条件固定下来。通过@Condition注解，配置每个条件的运算操作符。
 * @author jiyi
*/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ConditionBean(limitField = "limit",offsetField = "offset",isRequireTotalField = "fetchTotal")
public class AvsAuthParams {
	
	@Condition(Ops.STRING_CONTAINS_IC)
	private String authContent;
	
	@Condition(Ops.GT)
	private Integer authType;

	@Condition(value=Ops.GOE,path="authType")
	private Integer authTypeGoe;
	
	@Condition(value=Ops.LT,path="authType")
	private Integer authTypeLt;
	
	@Condition(value=Ops.LOE,path="authType")
	private Integer authTypeLoe;
	
	@Condition(value=Ops.EQ,ignoreUnsavedValue = false)
	@Builder.Default
	private String devId = "";

	@Condition(value=Ops.GT,path="devId")
	private String devIdGt;
	
	@Condition(value=Ops.GOE,path="devId")
	private String devIdGoe;
	
	@Condition(value=Ops.LT,path="devId")
	private String devIdLt;
	
	@Condition(value=Ops.LOE,path="devId")
	private String devIdLoe;
	
	@Condition
	private Integer channelNo;

	@Condition(Ops.BETWEEN)
	private Date[] createTime;
	
	@Condition(value=Ops.BETWEEN, path="createTime")
	private List<Date> createTime2;
	
	@Condition(value=Ops.GT,path="createTime")
	private Date dateGt;
	
	@Condition(value=Ops.LOE,path="createTime")
	private Date dateLoe;
	
	@Condition(value=Ops.IN,path="id")
	private List<Integer> ids;
	
	@Condition(value=Ops.IN,path="id")
	private int[] ids2;
	
	@Order(sortField = "orderAsc")
	private String order;
	
	private boolean orderAsc;
	
	private Integer limit;
	
	private Integer offset;
	
	private boolean fetchTotal;
	
	@Condition(value=Ops.STARTS_WITH,path="devId")
	private String devIdStartWith;
	
	@Condition(value=Ops.ENDS_WITH,path="devId")
	private String devIdEndWith;
	
	@Condition(value=Ops.STARTS_WITH_IC,path="devId")
	private String devIdStartWithIC;
	
	@Condition(value=Ops.ENDS_WITH_IC,path="devId")
	private String devIdEndWithIC;
	
	@Condition(value=Ops.LIKE,path="devId")
	private String devIdLike;
	
	@Condition(value=Ops.LIKE_IC,path="devId")
	private String devIdLikeIC;
	
	@Condition(value=Ops.IS_NULL,path="devId")
	private boolean devIdIsNull;
	
	@Condition(value=Ops.IS_NOT_NULL,path="devId")
	private boolean devIdIsNotNull;
	
	@Condition(value=Ops.STRING_CONTAINS,path="devId",otherPaths = {"userId","authContent"})
	private String mixField;
	
	@When(path="devId",ignoreIfNoMatchCase = false,forInt = {
			@IntCase(is=1,ops = Ops.ENDS_WITH, value = "123"),
			@IntCase(is=2,ops = Ops.ENDS_WITH, value = "456"),
			@IntCase(is=3,ops = Ops.ENDS_WITH, value = "456")
	})
	private Integer caseType;
}
