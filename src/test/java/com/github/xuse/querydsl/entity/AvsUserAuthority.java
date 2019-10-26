package com.github.xuse.querydsl.entity;

import java.util.Date;
import java.util.Map;

import javax.annotation.Generated;

import com.github.xuse.querydsl.annotation.CustomType;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.types.EnumByCodeType;
import com.github.xuse.querydsl.types.JSONObjectType;

/**
 * AvsUserAuthority is a Querydsl bean type
 */
@Generated("com.querydsl.codegen.BeanSerializer")
public class AvsUserAuthority {

    private String authContent;

    private Integer authType;

    private Integer channelNo;

    private Date createTime;

    private String devId;

   // @CustomType(StringAsBigIntType.class)
    private int id;

    private Date updateTime;

    private String userId;
    
    @CustomType(EnumByCodeType.class)
	private Gender gender;
	
	@CustomType(JSONObjectType.class)
	private Map<String,String> map;

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}
	
	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}

	@Override
    public String toString() {
         return "authContent = " + authContent + ", authType = " + authType + ", channelNo = " + channelNo + ", createTime = " + createTime + ", devId = " + devId + ", id = " + id + ", updateTime = " + updateTime + ", userId = " + userId;
    }

}

