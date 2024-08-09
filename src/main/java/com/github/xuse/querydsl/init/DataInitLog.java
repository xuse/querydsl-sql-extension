package com.github.xuse.querydsl.init;

import java.sql.Types;
import java.util.Date;

import com.github.xuse.querydsl.annotation.UnsavedValue;
import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.annotation.dbdef.Comment;
import com.github.xuse.querydsl.annotation.dbdef.TableSpec;

import lombok.Data;

/**
 * Log the initialization behavior of data. Enabling this feature will have the
 * following effects:
 * 
 * An automatic table will be created in the database to record which database
 * tables have undergone data initialization. After the initialization of a
 * table is completed, a record (disabled=1) will be inserted. When the service
 * starts and finds this record, it will no longer initialize this table again,
 * meaning each table's database initialization will only be executed once. If
 * you need to refresh the initialization data of this table again, you can
 * change the `disabled` field from 1 to 0.
 * <p>
 * 记录数据初始化行为日志。启用该特性后，会有以下效果——
 * <ul>
 * <li>会自动在数据库创建一张表，记录哪些数据库表进行过数据初始化，该表初始化完成后，则插入一条记录（disabled=1），服务启动后发现这条记录对不再对该表进行初始化，即每张表数据库初始化仅执行一次。</li>
 * <li>如果您需要再次刷新这张表的初始化数据，可以将disabled从1修改为0.</li>
 * <li>会创建一条特殊的记录, tableName="*"，当这条记录disabled=1时，所有表都不会进行初始化。.</li>
 * </ul>
 * 
 * @implSpec 加锁:set disabled = 2 where table_name=${table} and disabled=0;
 */
@Data
@TableSpec(name = "querydsl_auto_init_data_log", primaryKeys = { "tableName" })
@Comment("informations about data initialize by querydsl-sql-extenstion")
public class DataInitLog {

	/**
	 * 可以标记每张表是否允许初始化。无记录表示可以初始化。
	 * 
	 * tableName="*" records="0"
	 * 
	 */
	@ColumnSpec(name = "table_name", type = Types.VARCHAR, nullable = false, size = 128, defaultValue = "")
	@Comment("table name. * is a global option to disable data init feature")
	private String tableName;

	/**
	 * <ul>
	 * <li>0: Data initialization not performed</li>
	 * <li>1: Initialization completed, no further action needed</li>
	 * <li>2: Data initialization in progress</li>
	 * </ul>
	 * <ul>
	 * <li>0：未进行数据初始化</li>
	 * <li>1: 已进行初始化，无需再做</li>
	 * <li>2: 正在进行数据初始化。</li>
	 * </ul>
	 */
	@ColumnSpec(name = "is_disabled", type = Types.TINYINT, unsigned = true, nullable = false, defaultValue = "0")
	@UnsavedValue(UnsavedValue.MinusNumber)
	@Comment("1-Data init on this table is disabled. 0 - data init enabled.")
	private int disabled;

	@ColumnSpec(name = "init_records", type = Types.SMALLINT, unsigned = true, nullable = false, defaultValue = "0")
	@UnsavedValue(UnsavedValue.MinusNumber)
	@Comment("records saved on the last write process.")
	private int records;

	@ColumnSpec(name = "last_init_time", type = Types.TIMESTAMP)
	private Date lastInitTime;

	@ColumnSpec(name = "last_init_user", type = Types.VARCHAR, size = 128)
	private String lastInitUser;

	@ColumnSpec(name = "last_init_result", type = Types.VARCHAR, size = 400)
	private String lastInitResult;
}