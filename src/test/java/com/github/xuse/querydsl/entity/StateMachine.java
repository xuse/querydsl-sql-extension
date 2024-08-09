package com.github.xuse.querydsl.entity;

import java.util.Date;

import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.annotation.dbdef.Comment;
import com.github.xuse.querydsl.annotation.dbdef.Key;
import com.github.xuse.querydsl.annotation.dbdef.TableSpec;

import lombok.Data;

/**
 * SeataStateMachineInst is a Querydsl bean type
 */
@Data
@TableSpec(
		name="state_machine",
		keys = @Key(path={"businessKey","tenantId"}),
		primaryKeys = "id"
)
@Comment("测试尽可能少的注解完成访问")
public class StateMachine {

	@ColumnSpec
    private String businessKey;

	@ColumnSpec
    private String compensationStatus;

	@ColumnSpec
    private String endParams;

	@ColumnSpec
    private byte[] excep;

	@ColumnSpec
    private Date gmtEnd;

	@ColumnSpec
    private Date gmtStarted;

	@ColumnSpec
    private Date gmtUpdated;

	@ColumnSpec
    private String id;

	@ColumnSpec
    private Integer isRunning;

	@ColumnSpec
    private String machineId;

	@ColumnSpec
    private String parentId;

	@ColumnSpec
    private String startParams;

	@ColumnSpec
    private String status;

	@ColumnSpec
    private String tenantId;

    @Override
    public String toString() {
         return "businessKey = " + businessKey + ", compensationStatus = " + compensationStatus + ", endParams = " + endParams + ", excep = " + excep + ", gmtEnd = " + gmtEnd + ", gmtStarted = " + gmtStarted + ", gmtUpdated = " + gmtUpdated + ", id = " + id + ", isRunning = " + isRunning + ", machineId = " + machineId + ", parentId = " + parentId + ", startParams = " + startParams + ", status = " + status + ", tenantId = " + tenantId;
    }

}

