package com.github.xuse.querydsl.entity;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Instant;
import java.util.Date;

import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.annotation.InitializeData;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * Aaa is a Querydsl bean type
 */
@Getter
@Setter
@InitializeData(value="table_aaa",mergeKeys = {"name"})
public class TableDataTypes {

	private Instant created;

	private int id;

	private String name;
	
	private Gender gender;
	
	private TaskStatus taskStatus;
	
	private String trantField;
	
	private Gender genderWithChar;
	
	private int dataInt;
	
	private float dataFloat;
	
	private double dataDouble;
	
	private short dataShort;
	
	private long dataBigint;
	
	private BigDecimal dataDecimal;
	
	@UnsavedValue(UnsavedValue.NullOrEmpty)
	private boolean dataBool;
	
	@UnsavedValue(UnsavedValue.NullOrEmpty)
	private boolean dataBit;
	
	private Date dataDate;
	
	private Time dataTime;
	
	private Date dateTimestamp;
	
	private String dataText;
	
	private String dataLongText;
	
	private byte[] dateBinary;
	
	private byte[] dateVarBinary;
	

	@UnsavedValue("-1")
	private int version;
	
	@Override
	public String toString() {
		return "Aaa [created=" + created + ", id=" + id + ", name=" + name + ", gender=" + gender + ", taskStatus="
				+ taskStatus + ", version=" + version + "]";
	}
}
