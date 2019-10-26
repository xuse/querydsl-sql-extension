package com.github.xuse.querydsl.entity;

import java.util.Date;
import java.util.Map;

import com.github.xuse.querydsl.enums.Gender;

public class CaAsset {
	private int id;
	private String code;
	private String name;
	private Date created;
	private Date updated;
	private Aaa ext;
	private Gender gender;
	private Map<String,String> map;

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Aaa getExt() {
		return ext;
	}

	public void setExt(Aaa ext) {
		this.ext = ext;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}
}
