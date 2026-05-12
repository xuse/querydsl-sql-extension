package com.github.xuse.querydsl.entity;

import java.util.Map;

import com.github.xuse.querydsl.annotation.query.PathBind;
import com.github.xuse.querydsl.enums.Gender;

import lombok.Data;

@Data
public class FooDTO {
	private int id;
	
	private String code;
	
	private String name;
	
	private String content;
	
	private Gender gender;
	
	private TableDataTypes ext;
	
	private Map<String,String> map;
	
	private int volume;
	
	private int version;
	
	@PathBind("codeType")
	private String codeTypeX;
	
	private java.sql.Date inDay;
}
