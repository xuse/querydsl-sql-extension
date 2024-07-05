package com.github.xuse.querydsl.sql.test.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.github.xuse.querydsl.entity.CaAsset;
import com.github.xuse.querydsl.entity.QCaAsset;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.support.AbstractCrudRepository;
import com.querydsl.sql.RelationalPath;

@Repository
public class CaAssetRepository extends AbstractCrudRepository<CaAsset, Integer>{
	@Autowired
	private SQLQueryFactory factory;
	
	@Override
	protected SQLQueryFactory getFactory() {
		return factory;
	}

	@Override
	protected RelationalPath<CaAsset> getPath() {
		return QCaAsset.caAsset;
	}
}
