package com.github.xuse.querydsl.sql.dbmeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.github.xuse.querydsl.sql.support.ToStringVisitor2;
import com.querydsl.core.types.ConstraintType;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Templates;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPath;

import lombok.Getter;
import lombok.Setter;


/**
 * 描述一个数据库中的Constraint
 * 
 * @author jiyi
 * 
 *         MS SQLServer系统表 据说和MYSQL差不多：SELECT * FROM
 *         information_schema.TABLE_CONSTRAINTS
 * 
 * 
 *         MySQL系统表: SELECT * FROM information_schema.TABLE_CONSTRAINTS
 *
 *        
 *         Oracle系统表： SELECT * FROM all_CONSTRAINTS Oracle约束的种类 C Check on a
 *         table Column O Read Only on a view P Primary Key R Referential AKA
 *         Foreign Key U Unique Key V Check Option on a view
 */
@Getter
@Setter
public class Constraint {

	/**
	 * 约束的catalog
	 */
	private String catalog;

	/**
	 * 约束所在schema
	 */
	private String schema;

	/**
	 * 约束名
	 */
	private String name;

	/**
	 * 约束所在表的catalog
	 */
	private String tableCatalog;
	
	/**
	 * 约束所在表所在schema
	 */
	private String tableSchema;
	
	/**
	 * 约束所在表名
	 */
	private String tableName;
	
	/**
	 * 约束类型
	 */
	private ConstraintType constraintType;
	
	/**
	 * 检测延迟
	 */
	private boolean deferrable;
	
	/**
	 * 检测延迟
	 */
	private boolean initiallyDeferred;
	
	/**
	 * 约束数据库列
	 */
	private List<String> columnNames = new ArrayList<String>();
	
	/**
	 * 约束的Path列表 
	 */
	private List<Path<?>> paths = new ArrayList<>();
	
	/**
	 * 外键参照表所在schema
	 */
	private String refTableSchema;
	
	/**
	 * 外键参照表
	 */
	private String refTableName;
	
	/**
	 * 外键参照字段列表
	 */
	private List<String> refColumns = new ArrayList<String>();

	/**
	 * 外键更新规则
	 */
	private ForeignKeyAction updateRule;
	
	/**
	 * 外键删除规则
	 */
	private ForeignKeyAction deleteRule;
	
	/**
	 * 外键匹配类型
	 */
	private ForeignKeyMatchType matchType;
	
	/**
	 * 检查约束定义
	 * 
	 * 检查的情况下有效
	 */
	private Expression<Boolean> checkClause;
	
	/**
	 * 约束是否启用
	 */
	private boolean enabled = true;

	/**
	 * 内部使用
	 */
	private String indexQualifier;
	
	/**
	 * 内部使用
	 */
	private String comment;
	
	
	//内部使用，小写的列名，用于比较
	private transient volatile List<String> lowerColumnNames;
	
	public List<String> getLowerColumnNames(){
		if(lowerColumnNames==null) {
			List<String> cNames;
			if(columnNames!=null && !columnNames.isEmpty()) {
				cNames = new ArrayList<>(columnNames.size());
				for(String s:columnNames) {
					cNames.add(StringUtils.lowerCase(s));
				}
			}else {
				cNames = new ArrayList<>(paths.size());
				for(Path<?> path:paths) {
					Path<?> parent=path.getMetadata().getParent();
					if(parent instanceof RelationalPath<?>) {
						ColumnMetadata column=((RelationalPath<?>) parent).getMetadata(path);
						cNames.add(StringUtils.lowerCase(column.getName()));
					}
				}
			}
			return lowerColumnNames = cNames;
		}else {
			return lowerColumnNames;
		}
	}
	
	public void setCheckClause(Expression<Boolean> expr) {
		this.checkClause=expr;
	}
	
	@Override
	public String toString() {
		return "Constraint [name=" + name + ", tableName=" + tableCatalog+":"+tableSchema+":"+tableName + ", type=" + constraintType + ", columns=" + columnNames + ", refTableName=" + refTableName
				+ ", refColumns=" + refColumns + ", enabled=" + enabled + ", check="+checkClause+"]";
	}
	
	public boolean contentEquals(Constraint rhs) {
		ConstraintType type=getConstraintType();
		if(type.isIgnored() || rhs.getConstraintType().isIgnored()) {
			return false;
		}
		if(rhs.getConstraintType()!=type) {
			return false;
		}
		if(type.isColumnList()) {
			List<String> lc=getLowerColumnNames();
			List<String> rc=rhs.getLowerColumnNames();
			return Objects.equals(lc, rc);
		}else if(type.isCheckClause()) {
			Expression<?> l=getCheckClause();
			Expression<?> r=rhs.getCheckClause();
			if(!Objects.equals(l, r)) {
				String s1=l.accept(ToStringVisitor2.DEFAULT, Templates.DEFAULT);
				String s2=r.accept(ToStringVisitor2.DEFAULT, Templates.DEFAULT);
				return Objects.equals(s1,s2);	
			}
			return true;
		}
		return false;
	}
	

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(!(obj instanceof Constraint)) return false;
		Constraint con = (Constraint)obj;
		if(!Objects.equals(this.schema, con.schema))return false;
		if(!Objects.equals(this.name, con.name))return false;
		if(!Objects.equals(this.tableSchema, con.tableSchema))return false;
		if(!Objects.equals(this.tableName, con.tableName))return false;
		if(!Objects.equals(this.constraintType, con.constraintType))return false;
		if(!Objects.equals(this.deferrable, con.deferrable))return false;
		if(!Objects.equals(this.initiallyDeferred, con.initiallyDeferred))return false;
		if(!Arrays.equals(this.columnNames.toArray(), con.columnNames.toArray()))return false;
		if(!Objects.equals(this.refTableName, con.refTableName))return false;
		if(!Arrays.equals(this.refColumns.toArray(), con.refColumns.toArray()))return false;
		if(!Objects.equals(this.updateRule, con.updateRule))return false;
		if(!Objects.equals(this.deleteRule, con.deleteRule))return false;
		if(!Objects.equals(this.matchType, con.matchType))return false;
		if(!Objects.equals(this.enabled, con.enabled))return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = 17;  
		result = result * 31 + name.hashCode();  
		result = result * 31 + tableName.hashCode();  
		return result; 
	}

	public static Constraint valueOf(PrimaryKey<?> primaryKey) {
		if(primaryKey==null) {
			return null;
		}
		Constraint c=new Constraint();
		c.setConstraintType(ConstraintType.PRIMARY_KEY);
		c.setTableName(primaryKey.getEntity().getTableName());
		c.setPaths(new ArrayList<>(primaryKey.getLocalColumns()));
		return c;
	}
}
