package com.github.xuse.querydsl.entity;

import java.util.Date;

import javax.annotation.Generated;

import com.github.xuse.querydsl.enums.Gender;

/**
 * Aaa is a Querydsl bean type
 */
@Generated("com.querydsl.codegen.BeanSerializer")
public class Aaa {

	private Date created;

	private Integer id;

	private String name;
	
	private Gender gender;

	private String trantField;

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	@Override
	public String toString() {
		return "created = " + created + ", id = " + id + ", name = " + name;
	}

	public String getTrantField() {
		return trantField;
	}

	public void setTrantField(String trantField) {
		this.trantField = trantField;
	}
}
