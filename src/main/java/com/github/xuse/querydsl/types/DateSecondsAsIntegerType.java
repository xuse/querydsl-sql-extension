package com.github.xuse.querydsl.types;

import java.sql.Types;
import java.util.concurrent.TimeUnit;

/**
  低精度时间表示法。使用INT(4 bytes)数据存储。
 * <p>
 * 默认精度为1秒。可以表达1970-01-01 00:00:00 to 2106-02-07 6:28:14的时间范围。
 * 一般来说这个范围对大多数业务场景已经足够。
 * 同样是4个字节。MySQL的timestamp只能表示到2038年1月19日。这就是这种方法的意义所在。
 * <p>
 * 但实际上，MySQL DateTime只要5个字节。就能表达已知的几乎所有时间范围，所以这个方法的意义也没那么大。
 * 极致抠门的情况下可以考虑。
 */
public class DateSecondsAsIntegerType extends AbstractLowPrecisionTime{
	public DateSecondsAsIntegerType(long scale) {
		super(Types.INTEGER, scale, scale * Integer.MAX_VALUE);
	}
	
	public DateSecondsAsIntegerType() {
		this(TimeUnit.SECONDS.toMillis(1));
	}

	@Override
	public int getMinNum() {
		return Integer.MIN_VALUE;
	}

	@Override
	public int getMaxNum() {
		return Integer.MAX_VALUE;
	}
}
