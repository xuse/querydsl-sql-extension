package com.github.xuse.querydsl.entity;

import java.util.Date;

import javax.annotation.Generated;

import com.github.xuse.querydsl.annotation.AutoGenerated;
import com.github.xuse.querydsl.annotation.GeneratedType;
import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;

/**
 * Aaa is a Querydsl bean type
 */
public class Aaa {

	@AutoGenerated(GeneratedType.CREATED_TIMESTAMP)
	private Date created;

	private int id;

	private String name;
	
	private Gender gender;

	private String trantField;
	
	private TaskStatus taskStatus;

	@UnsavedValue("-1")
	private int version;
	
	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
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
	
	public TaskStatus getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(TaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}

	@Override
	public String toString() {
		return "Aaa [created=" + created + ", id=" + id + ", name=" + name + ", gender=" + gender + ", taskStatus="
				+ taskStatus + ", version=" + version + "]";
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getTrantField() {
		return trantField;
	}

	public void setTrantField(String trantField) {
		this.trantField = trantField;
	}
}
