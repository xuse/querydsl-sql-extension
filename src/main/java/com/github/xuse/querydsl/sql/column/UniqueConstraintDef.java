package com.github.xuse.querydsl.sql.column;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.UniqueConstraint;

import com.github.xuse.querydsl.sql.IRelationPathEx;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.ConstraintType;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.SQLTemplates;


/**
 * 定义一个唯一约束
 * @author Jiyi
 *
 */
public class UniqueConstraintDef {
	public String name;

	public String[] columnNames;

	public UniqueConstraintDef(UniqueConstraint unique) {
		this.name=unique.name();
		this.columnNames=unique.columnNames();
	}
	
	/**
	 * 构造单列的唯一约束
	 * @param name
	 * @param columnName
	 */
	public UniqueConstraintDef(String name, String columnName) {
		this.name=name;
		this.columnNames=new String[] {columnName};
	}

	public String name() {
		return name;
	}

	public String[] columnNames() {
		return columnNames;
	}
	
	public List<String> getColumnNames(IRelationPathEx meta, SQLTemplates dialect) {
		List<String> columns=new ArrayList<String>(columnNames.length);
		for(int i=0;i<columnNames.length;i++){
//			String name=columnNames[i];
//			for(String s: StringUtils.split(name, ',')){//为了容错，这个很有可能配错
//				ColumnMapping column=meta.getColumnMetadata(s);
//				if(column==null){
//					throw new NoSuchElementException("Field not found in entity "+meta.getName()+": "+s);
//				}else{
//					columns.add(column.getColumnName(dialect, true));
//				}
//			}
		}
		return columns;
	}
	
	public Constraint toConstraint(String tableName, IRelationPathEx meta, SQLTemplates dialect){
		Constraint con = new Constraint();
		con.setName(name);
		con.setTableName(tableName);
		con.setColumns(getColumnNames(meta, dialect));
		con.setType(ConstraintType.U);
		return con;
	}

	public static boolean check(UniqueConstraint unique, Class<?> typeName) {
		if(unique==null || unique.columnNames().length==0) {
			return false;
		}
		for(String s: unique.columnNames()) {
			if(StringUtils.isBlank(s)) {
				throw new PersistenceException(Exceptions.format("The {} has a invalid @UniqueConstraint defnition.", typeName)); 
			}
		}
		return true;
	}
}
