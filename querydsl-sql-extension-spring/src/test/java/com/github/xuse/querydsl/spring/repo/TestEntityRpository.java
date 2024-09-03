package com.github.xuse.querydsl.spring.repo;

import org.springframework.stereotype.Repository;

import com.github.xuse.querydsl.spring.entity.TestEntity;
import com.github.xuse.querydsl.repository.GenericRepository;


@Repository
public class TestEntityRpository extends GenericRepository<TestEntity, Long>{
}
