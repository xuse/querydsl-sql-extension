package com.github.xuse.querydsl.init;

/**
 * 指定扫描到的实体后的行为
 */
public enum DataInitBehavior {
	
	/**
	 * 不执行数据初始化
	 */
	NONE(0),
	/**
	 * 仅对新创建的表执行数据初始化
	 */
	FOR_CREATED_TABLE_ONLY(1),
	
	/**
	 * 仅对创建和发生修改的表进行数据初始化
	 */
	FOR_MODIFIED_TABLE(2),
	
	/**
	 * 对所有扫描到的表进行数据初始化
	 */
	FOR_ALL_TABLE(5);
	
	public final int code;
	
	DataInitBehavior(int code){
		this.code=code;
	}
	
}
