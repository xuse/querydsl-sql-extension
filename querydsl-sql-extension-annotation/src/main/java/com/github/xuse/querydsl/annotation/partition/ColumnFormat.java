package com.github.xuse.querydsl.annotation.partition;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * 用于进行时间分区的数据库列的格式。
 * <p>
 * 该列的内容必须符合以下任意一种格式。
 * 正确配置格式后，框架可以自动生成按时间组织的数据库分区。
 */
public enum ColumnFormat {
	/**
	 * 列为时间戳格式。(DATE, DATETIME, TIMESTAMP均可)
	 * 支持按年、按月、按日、按周进行数据分区。
	 */
	TIMESTAMP {
		@Override
		public String generateExpression(Date d) {
			return "'" + DateFormats.DATE_CS.apply(d) + "'";
		}
	},
	
	/**
	 * 
	 * 20230101 这样的数字整型
	 * <p>
	 * 支持按年、按月、按日、按周进行数据分区。
	 */
	NUMBER_YMD {
		@Override
		public String generateExpression(Date d) {
			return DateFormats.DATE_SHORT.apply(d);
		}
	},
	
	/**
	 * '20230101' 这样的字符串
	 * <p>
	 * 支持按年、按月、按日、按周进行数据分区。
	 */
	STRING_YMD {
		@Override
		public String generateExpression(Date d) {
			return "'"+DateFormats.DATE_SHORT.apply(d)+"'";
		}
	},

	/**
	 * 202301 这样的数字整型
	 * <p>
	 * 支持按年、按月进行数据分区
	 */
	NUMBER_YM{
		@Override
		public String generateExpression(Date d) {
			return DateFormats.YEAR_MONTH.apply(d);
		}
	},
	
	/**
	 * '202301' 这样的字符串。
	 * <p>
	 * 支持按年、按月进行数据分区
	 */
	STRING_YM {
		@Override
		public String generateExpression(Date d) {
			return "'"+DateFormats.YEAR_MONTH.apply(d)+"'";
		}
	},
	
	/**
	 * 2023 这样的数字。
	 * <p>
	 * 支持按年进行分区
	 */
	NUMBER_YEAR{
		@Override
		public String generateExpression(Date d) {
			return String.valueOf(getYear(d));
		}
	};

	public abstract String generateExpression(Date d);
	
	private static int getYear(Date d) {
		if (d == null)
			return 0;
		final Calendar c = new GregorianCalendar();
		c.setTime(d);
		return c.get(Calendar.YEAR);
	}
	
}
