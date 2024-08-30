package com.github.xuse.querydsl.sql;

import static com.github.xuse.querydsl.sql.expression.AbstractMapperSupport.SCENARIO_INSERT;
import static com.github.xuse.querydsl.sql.expression.AbstractMapperSupport.SCENARIO_NORMAL;
import static com.github.xuse.querydsl.sql.expression.AbstractMapperSupport.SCENARIO_UPDATE;

import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.expression.TupleMapper;
import com.querydsl.sql.dml.Mapper;

/**
 * Mapper的组合策略，对象池
 */
@SuppressWarnings("rawtypes")
public final class Mappers {
	
	public static final int SCENARIO_NONE = 0;
	public static final int TYPE_BEAN = 0;
	public static final int NULLS_IGNORED = 0;
	
	
	public static final int NULLS_BIND = 1;
	public static final int TYPE_TUPLE = 2;
	public static final int PRIMARKKEY_IGNORED = 4;
	
	
	
	private static final Mapper[][] MAPPERS=new Mapper[][] {
			//Normal
			new Mapper[] {
					new AdvancedMapper(SCENARIO_NORMAL,false).name("Normal"), //0
					AdvancedMapper.ofNullsBinding(SCENARIO_NORMAL,false).name("Normal, Nulls"),//1
					new TupleMapper(SCENARIO_NORMAL,false).name("Tuple"),//2
					TupleMapper.ofNullsBinding(SCENARIO_NORMAL,false).name("Tuple,Nulls"),//3
					new AdvancedMapper(SCENARIO_NORMAL,true).name("Normal,NoKeys"), //4
					AdvancedMapper.ofNullsBinding(SCENARIO_NORMAL,true).name(" Nulls,NoKeys"),//5
					new TupleMapper(SCENARIO_NORMAL,true).name("Tuple,NoKeys"),//6
					TupleMapper.ofNullsBinding(SCENARIO_NORMAL,true).name("Tuple,Nulls,NoKeys"),//7
			},
			//Insert
			new Mapper[] {
					new AdvancedMapper(SCENARIO_INSERT,false).name("Normal (Insert)"), //0
					AdvancedMapper.ofNullsBinding(SCENARIO_INSERT,false).name("Normal, Nulls (Insert)"),//1
					new TupleMapper(SCENARIO_INSERT,false ).name("Tuple (Insert)"),//2
					TupleMapper.ofNullsBinding(SCENARIO_INSERT,false).name("Tuple,Nulls (Insert)"),//3
					
					new AdvancedMapper(SCENARIO_INSERT,true).name("(Insert),NoKeys"), //4
					AdvancedMapper.ofNullsBinding(SCENARIO_INSERT,true).name(" Nulls (Insert),NoKeys"),//5
					new TupleMapper(SCENARIO_INSERT,true ).name("Tuple (Insert),NoKeys"),//6
					TupleMapper.ofNullsBinding(SCENARIO_INSERT,true).name("Tuple,Nulls (Insert),NoKeys")//7	
			},
			//Update
			new Mapper[] {
					new AdvancedMapper(SCENARIO_UPDATE,false).name("Normal (Update)"),//0
					AdvancedMapper.ofNullsBinding(SCENARIO_UPDATE,false).name("Normal, Nulls (Update)"),//1
					new TupleMapper(SCENARIO_UPDATE,false).name("Tuple (Update)"),//2
					TupleMapper.ofNullsBinding(SCENARIO_UPDATE,false).name("Tuple,Nulls (Update)"),//3	+
					
					new AdvancedMapper(SCENARIO_UPDATE,true).name("(Update),NoKeys"),//4
					AdvancedMapper.ofNullsBinding(SCENARIO_UPDATE,true).name("Nulls (Update),NoKeys"),//5
					new TupleMapper(SCENARIO_UPDATE,true).name("Tuple (Update),NoKeys"),//6
					TupleMapper.ofNullsBinding(SCENARIO_UPDATE,true).name("Tuple,Nulls (Update),NoKeys"),//7			
			},
			//Insert DEFAULT
			new Mapper[] {
					null,
					AdvancedMapper.ofNullsAsDefaultBinding(SCENARIO_INSERT, false).name("Normal, Nulls (Insert DEFAULT"),
					null,
					TupleMapper.ofNullsAsDefaultBinding(SCENARIO_INSERT, false).name("Tuple,Nulls (Insert DEFAULT)")
			}

	};
	
	public static Mapper getNormal(boolean isTuple, boolean nulls) {
		return MAPPERS[SCENARIO_NORMAL][(isTuple ? TYPE_TUPLE : TYPE_BEAN) + (nulls ? NULLS_BIND : NULLS_IGNORED)];
	}
	
	public static Mapper getUpdate(boolean isTuple, boolean nulls) {
		return MAPPERS[SCENARIO_UPDATE][ (isTuple ? TYPE_TUPLE : TYPE_BEAN) | (nulls ? NULLS_BIND : NULLS_IGNORED)];
	}
	
	public static Mapper getNormal(boolean isTuple) {
		return MAPPERS[SCENARIO_NORMAL][(isTuple ? TYPE_TUPLE : TYPE_BEAN)];
	}
	
	public static Mapper get(int scenario, int value) {
		return MAPPERS[scenario][value];
	}
}
