package com.github.xuse.querydsl.sql;

import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.expression.TupleMapper;
import com.querydsl.sql.dml.Mapper;

/**
 * 总共12种Mapper策略组合
 */
@SuppressWarnings("rawtypes")
public final class Mappers {
	
	public static final int SCENARIO_NONE = 0;
	
	public static final int SCENARIO_INSERT = 4;

	public static final int SCENARIO_UPDATE = 8;
	
	public static final int SCENARIO_INSERT_NULL_WITH_DEFAULT = 12;
	
	public static final int TYPE_BEAN = 0;
	
	public static final int TYPE_TUPLE = 2;
	
	public static final int NULLS_IGNORED = 0;
	
	public static final int NULLS_BIND = 1;
	
	
	private static final Mapper[] MAPPERS=new Mapper[] {
			new AdvancedMapper(SCENARIO_NONE).name("Normal"),
			AdvancedMapper.ofNullsBinding(SCENARIO_NONE).name("Normal, Nulls"),
			new TupleMapper(SCENARIO_NONE).name("Tuple"),
			TupleMapper.ofNullsBinding(SCENARIO_NONE).name("Tuple,Nulls"),
			
			new AdvancedMapper(SCENARIO_INSERT).name("Normal (Insert)"),
			AdvancedMapper.ofNullsBinding(SCENARIO_INSERT).name("Normal, Nulls (Insert)"),
			new TupleMapper(SCENARIO_INSERT).name("Tuple (Insert)"),
			TupleMapper.ofNullsBinding(SCENARIO_INSERT).name("Tuple,Nulls (Insert)"),
			
			new AdvancedMapper(SCENARIO_UPDATE).name("Normal (Update)"),
			AdvancedMapper.ofNullsBinding(SCENARIO_UPDATE).name("Normal, Nulls (Update)"),
			new TupleMapper(SCENARIO_UPDATE).name("Tuple (Update)"),
			TupleMapper.ofNullsBinding(SCENARIO_UPDATE).name("Tuple,Nulls (Update)"),
			
			null,
			AdvancedMapper.ofNullsAsDefaultBinding(SCENARIO_INSERT).name("Normal, Nulls (Insert DEFAULT"),
			null,
			TupleMapper.ofNullsAsDefaultBinding(SCENARIO_INSERT).name("Tuple,Nulls (Insert DEFAULT)")
	};
	
	public static Mapper getNormal(boolean isTuple, boolean nulls) {
		return MAPPERS[(isTuple ? TYPE_TUPLE : TYPE_BEAN) + (nulls ? NULLS_BIND : NULLS_IGNORED)];
	}
	
	public static Mapper getUpdate(boolean isTuple, boolean nulls) {
		return MAPPERS[SCENARIO_UPDATE | (isTuple ? TYPE_TUPLE : TYPE_BEAN) | (nulls ? NULLS_BIND : NULLS_IGNORED)];
	}
	
	public static Mapper getNormal(boolean isTuple) {
		return MAPPERS[(isTuple ? TYPE_TUPLE : TYPE_BEAN)];
	}
	
	public static Mapper get(int value) {
		return MAPPERS[value];
	}
}
