package com.github.xuse.querydsl.util;

import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.github.xuse.querydsl.datatype.util.ToStringUtils;
import com.github.xuse.querydsl.datatype.util.binary.BeanCodec;
import com.github.xuse.querydsl.datatype.util.binary.BufferedDecoder;
import com.github.xuse.querydsl.datatype.util.binary.BufferedEncoder;
import com.github.xuse.querydsl.datatype.util.binary.CodecContext;

public class BinaryUtilsTest {
	@Test
	public void testBinary() {
		Aaa a = new Aaa();
		a.setName(StringUtils.randomString());
		a.setVersion(1);
		
		CodecContext.INSTANCE.register(new BeanCodec<Aaa>() {
			@Override
			public Class<Aaa> getType() {
				return Aaa.class;
			}

			@Override
			public Aaa decode(ByteBuffer buffer, CodecContext context) {
				Aaa aaa=new Aaa();
				aaa.setName(context.readObject(buffer, String.class));
				aaa.setVersion(buffer.getInt());
				boolean isEnd=context.isBeanEnd(buffer);
				assertTrue(isEnd);
				return aaa;
			}

			@Override
			public void encode(ByteBuffer buffer, Aaa obj, CodecContext context) {
				context.writeObject(buffer, obj.getName());
				buffer.putInt(obj.getVersion());
			}
		});
		
		BufferedEncoder encode=new BufferedEncoder(1024);
		encode.putObject(a);
		
		byte[] buffer= encode.toByteArray();
		System.out.println(ToStringUtils.toString("", buffer));
		
		byte[] data=IOUtils.serialize(a);
		System.out.println(ToStringUtils.toString(data));		
		
		
		
		BufferedDecoder decoder=new BufferedDecoder(buffer);
		Aaa aaa=decoder.getObject(Aaa.class);
		System.out.println(aaa);
		
		
		
		
		
	}
}
