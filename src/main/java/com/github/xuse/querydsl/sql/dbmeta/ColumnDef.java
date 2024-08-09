/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.xuse.querydsl.sql.dbmeta;

import java.sql.Types;

import com.github.xuse.querydsl.util.StringUtils;

import lombok.Setter;


/**
 * 描述一个列对象
 *
 */
@Setter
public class ColumnDef{
	
	private String tableCat;
	
	private String tableSchema;
	/**
	 * 表名
	 */
	private String tableName;
	
	/**
	 * 顺序, start at 1
	 */
	private int ordinal;
	/**
	 * 列名
	 */
	private String columnName;
	/**
	 * 备注
	 */
	private String remarks;
	/**
	 * 列的数据类型
	 */
	private String dataType;
	/**
	 * 列的数据类型：SQL常量
	 * java.sql.Types中的常量之一
	 */
	private int jdbcType;
	/**
	 * 列宽(p)
	 */
	private int columnSize;
	/**
	 * 十进制数位(s)
	 */
	private int decimalDigit;
	/**
	 * 允许null
	 */
	private boolean nullable;
	/**
	 * 列的缺省值
	 */
	private String columnDef;

	/**
	 * 似乎没用
	 */
	private boolean generated;
	
	/**
	 * 评估
	 */
	private boolean autoIncrement;
	
	
	private int charOctetLength;
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder(columnName);
		sb.append(" ").append(this.dataType);
		if(!this.nullable){
			sb.append(" not null");
		}
		if(autoIncrement){
			sb.append(" autoIncrement");
		}
		return sb.toString();
	}

	public String getDataTypeString() {
		StringBuilder sb=new StringBuilder();
		int type=this.getJdbcType();
		if(type==Types.CLOB || type==Types.BLOB || type==Types.DATE || type==Types.TIME) {
			sb.append(this.dataType);
		}else {
			sb.append(this.dataType).append('(').append(this.columnSize).append(')');
		}
		if(this.isNullable()) {
			sb.append(" NULL");
		}else{
			sb.append(" NOT NULL");
		}
		boolean isString=type==Types.CHAR || type==Types.VARCHAR || type==Types.NVARCHAR || type==Types.CLOB;
		boolean isDate=type==Types.TIME || type==Types.DATE || type==Types.TIMESTAMP;
		if(this.columnDef!=null && isString) {
			sb.append(" DEFAULT '").append(columnDef).append('\'');
		}else if(StringUtils.isNotEmpty(this.columnDef)) {
			if(isDate) {
				sb.append(" DEFAULT '").append(columnDef).append('\'');	
			}else {
				sb.append(" DEFAULT ").append(columnDef);
			}
		}
		if(StringUtils.isNotBlank(this.remarks)){
			sb.append(" COMMENT '").append(this.remarks).append('\'');
		}
		return sb.toString();
	}

	public String getTableCat() {
		return tableCat;
	}

	public String getTableSchema() {
		return tableSchema;
	}

	public String getTableName() {
		return tableName;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getRemarks() {
		return remarks;
	}

	public String getDataType() {
		return dataType;
	}

	public int getJdbcType() {
		return jdbcType;
	}

	public int getColumnSize() {
		return columnSize;
	}

	public int getDecimalDigit() {
		return decimalDigit;
	}

	public boolean isNullable() {
		return nullable;
	}

	public String getColumnDef() {
		return columnDef;
	}

	public boolean isGenerated() {
		return generated;
	}

	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public int getCharOctetLength() {
		return charOctetLength;
	}
}
