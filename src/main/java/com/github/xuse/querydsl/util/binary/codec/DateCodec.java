package com.github.xuse.querydsl.util.binary.codec;

import java.nio.ByteBuffer;
import java.util.Date;

public class DateCodec implements Codec<Date>{
	public static final DateCodec INSTANCE = new DateCodec();
	
	@Override
	public Date decode(ByteBuffer buffer, CodecContext context) {
		long l=buffer.getLong();
		return new Date(l);
	}

	@Override
	public void encode(ByteBuffer buffer, Object obj, CodecContext context) {
		Date d=(Date)obj;
		buffer.put(CodecContext.TYPE_DATE);
		buffer.putLong(d.getTime());
	}

}
