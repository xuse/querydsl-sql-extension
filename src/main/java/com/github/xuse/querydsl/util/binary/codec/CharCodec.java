package com.github.xuse.querydsl.util.binary.codec;

import java.nio.ByteBuffer;

public class CharCodec implements Codec<Character>{

	public static final CharCodec INSTANCE = new CharCodec();
	
	@Override
	public Character decode(ByteBuffer buffer, CodecContext context) {
		return (char)buffer.getShort();
	}

	@Override
	public void encode(ByteBuffer buffer, Object obj, CodecContext context) {
		buffer.put(CodecContext.TYPE_CHAR);
		Character c=(Character)obj;
		buffer.putShort((short)c.charValue());
	}
	

}
