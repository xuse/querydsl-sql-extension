package com.github.xuse.querydsl.sql.ddl;

import com.querydsl.core.types.DDLOps.AlterColumnOps;
import com.querydsl.core.types.Expression;

/**
 * 描述一个数据库列上的一种变化
 * 
 * @author Administrator
 * 
 */
public class ColumnChange {
	private AlterColumnOps type;
	private Expression<?> from;
	private Expression<?> to;
	
	public static ColumnChange dataType(Expression<?> from, Expression<?> to) {
		ColumnChange c=new ColumnChange(AlterColumnOps.SET_DATATYPE);
		c.setFrom(from);
		c.setTo(to);
		return c;
	}
	
	public static ColumnChange toNull() {
		ColumnChange c=new ColumnChange(AlterColumnOps.SET_NULL);
		return c;
	}
	
	public static ColumnChange toNotNull() {
		ColumnChange c=new ColumnChange(AlterColumnOps.SET_NOTNULL);
		return c;
	}
	
	public static ColumnChange changeDefault(Expression<?> from, Expression<?> to) {
		if(to==null) {
			return new ColumnChange(AlterColumnOps.DROP_DEFAULT);
		}
		ColumnChange c=new  ColumnChange(AlterColumnOps.SET_DEFAULT);
		c.setFrom(from);
		c.setTo(to);
		return c;
	}


	public static ColumnChange comment(Expression<String> from, Expression<String> to) {
		ColumnChange c=new  ColumnChange(AlterColumnOps.SET_COMMENT);
		c.setFrom(from);
		c.setTo(to);
		return c;
	}

	
	/**
	 * 构造
	 * @param type
	 */
	public ColumnChange(AlterColumnOps type) {
		this.type = type;
	}
	/**
	 * 变更种类
	 * @return change枚举，描述变更种类
	 * @see AlterColumnOps
	 */
	public AlterColumnOps getType() {
		return type;
	}
	/**
	 * 获得变更前的描述
	 * @return 变更前描述
	 */
	public Expression<?> getFrom() {
		return from;
	}
	/**
	 * 设置变更前描述
	 * @param from
	 */
	public void setFrom(Expression<?> from) {
		this.from = from;
	}
	/**
	 * 获得变更后描述
	 * @return to
	 */
	public Expression<?> getTo() {
		return to;
	}
	/**
	 *  设置变更后描述
	 * @param to
	 */
	public void setTo(Expression<?> to) {
		this.to = to;
	}
	@Override
	public String toString() {
		return type+"("+from+"->"+to+")";
	}
	
	
}
