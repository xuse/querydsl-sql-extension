package com.github.xuse.querydsl.enums;

import java.util.Date;

import com.github.xuse.querydsl.entity.AvsUserAuthority;
import com.github.xuse.querydsl.sql.expression.BeanCodec;

public class TestAbstractPopulator_1 extends BeanCodec {

	@Override
	public Object newInstance(Object[] fields) {
		AvsUserAuthority au=new AvsUserAuthority();
		au.setAuthContent((String)fields[0]);
		au.setAuthType((Integer)fields[1]);
		au.setChannelNo((Integer)fields[2]);
		au.setCreateTime((Date)fields[3]);
		au.setDevId((String)fields[4]);
		au.setId((Integer)fields[5]);
		au.setUpdateTime((Date)fields[6]);
		au.setUserId((String)fields[7]);
		au.setGender((Gender)fields[8]);
		return au;
	}

	@Override
	public Object[] values(Object bean) {
		AvsUserAuthority a=(AvsUserAuthority)bean;
		Object[] obj=new Object[9];
		obj[0]=a.getAuthContent();
		obj[1]=a.getChannelNo();
		return obj;
	}

}
