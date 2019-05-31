package com.github.xuse.querydsl.entity;

import java.util.Date;

import javax.annotation.Generated;

/**
 * Aaa is a Querydsl bean type
 */
@Generated("com.querydsl.codegen.BeanSerializer")
public class Aaa {

    private Date created;

    private Integer id;

    private String name;

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

    @Override
    public String toString() {
         return "created = " + created + ", id = " + id + ", name = " + name;
    }

}

