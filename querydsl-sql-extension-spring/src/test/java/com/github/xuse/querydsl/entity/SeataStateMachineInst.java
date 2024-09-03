package com.github.xuse.querydsl.entity;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * SeataStateMachineInst is a Querydsl bean type
 */
@Getter
@Setter
public class SeataStateMachineInst {

    private String businessKey;

    private String compensationStatus;

    private String endParams;

    private byte[] excep;

    private Date gmtEnd;

    private Date gmtStarted;

    private Date gmtUpdated;

    private String id;

    private Integer isRunning;

    private String machineId;

    private String parentId;

    private String startParams;

    private String status;

    private String tenantId;

    @Override
    public String toString() {
         return "businessKey = " + businessKey + ", compensationStatus = " + compensationStatus + ", endParams = " + endParams + ", excep = " + excep + ", gmtEnd = " + gmtEnd + ", gmtStarted = " + gmtStarted + ", gmtUpdated = " + gmtUpdated + ", id = " + id + ", isRunning = " + isRunning + ", machineId = " + machineId + ", parentId = " + parentId + ", startParams = " + startParams + ", status = " + status + ", tenantId = " + tenantId;
    }

}

