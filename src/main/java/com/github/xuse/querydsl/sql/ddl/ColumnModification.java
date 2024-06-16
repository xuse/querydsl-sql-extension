package com.github.xuse.querydsl.sql.ddl;

import java.util.Arrays;
import java.util.List;

import com.github.xuse.querydsl.sql.column.AbstractColumnMetadataEx;
import com.github.xuse.querydsl.sql.column.ColumnMetadataEx;
import com.querydsl.core.types.DDLOps.AlterColumnOps;
import com.querydsl.core.types.Path;


/**
 * 描述在一个数据库列上，实际的模型和框架中的模型之间的差异
 */
public final class ColumnModification {
	private ColumnMetadataEx from;
	private ColumnMetadataEx newColumn;
	private List<ColumnChange> changes;
	private Path<?> path;

	/**
	 * 构造
	 * 
	 * @param beforeChange
	 *            变更前的列定义
	 * @param changes
	 *            对比出来的变化
	 * @param changeTo
	 *            变更后的列类型
	 */
	public ColumnModification(Path<?> path,ColumnMetadataEx beforeChange, List<ColumnChange> changes, ColumnMetadataEx changeTo) {
		this.path=path;
		this.from = beforeChange;
		this.changes = changes;
		this.newColumn = changeTo;
	}

	public ColumnModification ofSingleChange(ColumnChange change) {
		return new ColumnModification(path, from, Arrays.asList(change),newColumn);
	}
	
	@Override
	public String toString() {
		return from.toString();
	}

	/**
	 * 得到数据库中（实际）的列定义
	 * 
	 * @return 数据库中的列定义
	 */
	public ColumnMetadataEx getFrom() {
		return from;
	}

	/**
	 * 获得数据库列上的数据类型变更
	 * 
	 * @return 在一个列上的变更列表（比如Default值变化、null/not null变化、数据类型变化、数据长度变化等）
	 * @see ColumnChange
	 */
	public List<ColumnChange> getChanges() {
		return changes;
	}

	/**
	 * 得到模型会将数据库列更改为类型
	 * 
	 * @return 更改后的类型
	 * @see AbstractColumnMetadataEx
	 */
	public ColumnMetadataEx getNewColumn() {
		return newColumn;
	}

	/**
	 * 返回列名
	 * 
	 * @return 列名
	 */
	public String getColumnName() {
		return from.getColumn().getName();
	}

	public boolean hasChange(AlterColumnOps type) {
		if (changes != null) {
			for (ColumnChange cc : changes) {
				if (cc.getType() == type) {
					return true;
				}
			}
		}
		return false;
	}

	public Path<?> getPath() {
		return path;
	}
}
