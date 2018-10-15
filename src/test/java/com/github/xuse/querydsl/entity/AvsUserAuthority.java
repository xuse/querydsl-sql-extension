package com.github.xuse.querydsl.entity;

import javax.annotation.Generated;

/**
 * AvsUserAuthority is a Querydsl bean type
 */
@Generated("com.querydsl.codegen.BeanSerializer")
public class AvsUserAuthority {

    private String authContent;

    private Integer authType;

    private Integer channelNo;

    private java.sql.Timestamp createTime;

    private String devId;

    private Integer id;

    private java.sql.Timestamp updateTime;

    private String userId;

    public String getAuthContent() {
        return authContent;
    }

    public void setAuthContent(String authContent) {
        this.authContent = authContent;
    }

    public Integer getAuthType() {
        return authType;
    }

    public void setAuthType(Integer authType) {
        this.authType = authType;
    }

    public Integer getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(Integer channelNo) {
        this.channelNo = channelNo;
    }

    public java.sql.Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(java.sql.Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public java.sql.Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(java.sql.Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
         return "authContent = " + authContent + ", authType = " + authType + ", channelNo = " + channelNo + ", createTime = " + createTime + ", devId = " + devId + ", id = " + id + ", updateTime = " + updateTime + ", userId = " + userId;
    }

}

