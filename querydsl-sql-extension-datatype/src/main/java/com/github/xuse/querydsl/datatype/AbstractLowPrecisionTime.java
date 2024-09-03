package com.github.xuse.querydsl.datatype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.github.xuse.querydsl.util.DateFormats;
import com.github.xuse.querydsl.util.DateFormats.TLDateFormat;
import com.querydsl.sql.types.AbstractType;

public abstract class AbstractLowPrecisionTime extends AbstractType<Date> {
	/**
	 * 精度。
	 */
	public final long scale;

	private final long offset;

	private final long offsetDivScale;
	

	public AbstractLowPrecisionTime(int type, long scale, long offset) {
		super(type);
		this.scale = scale;
		this.offset = offset;
		this.offsetDivScale = offset / scale;
	}

	@Override
	public Class<Date> getReturnedClass() {
		return Date.class;
	}

	@Override
	public Date getValue(ResultSet rs, int startIndex) throws SQLException {
		int n = rs.getInt(startIndex);
		return rs.wasNull() ? null : toDate(n);
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, Date value) throws SQLException {
		st.setInt(startIndex, toInt(value));
	}

	protected final int toInt(Date value) {
		/*
		 * 必须这样计算，因为负数除法会截断小数，相当于向上取整。而正数触发则相反是向下取整。
		 * 如果先减再除会导致负数场合下取整方式不一致。
		 */
		int min = (int) ((value.getTime()) / scale - offsetDivScale);
		return min;
	}

	protected final Date toDate(int n) {
		return new Date(n * scale + offset);
	}

	public Date truncateAs(Date d) {
		return toDate(toInt(d));
	}

	public Date getMinValue() {
		return toDate(getMinNum());
	};

	public Date getMaxValue() {
		return toDate(getMaxNum());
	};

	protected abstract int getMinNum();

	protected abstract int getMaxNum();

	@Override
	public String toString() {
		TLDateFormat f = DateFormats.DATE_TIME_CS;
		return "Precision:" + (scale / 1000) + "s from " + f.format(getMinValue()) + " to "
				+ f.format(getMaxValue());
	}

}
