package com.github.xuse.querydsl.util.binary;

import java.awt.List;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.github.xuse.querydsl.util.Exceptions;

/**
 * 二进制数据编解码器
 * @author Joey
 */
@SuppressWarnings("rawtypes")
public class CodecContext {
	public static final CodecContext INSTANCE=new CodecContext();
	
	static final byte TYPE_NULL = -1;
	static final byte TYPE_BYTE = 0;
	static final byte TYPE_BOOLEAN_T = 1;
	static final byte TYPE_BOOLEAN_F = 2;
	
	
	static final byte TYPE_SHORT = 4;
	static final byte TYPE_CHAR = 5;
	
	static final byte TYPE_INT = 7;
	static final byte TYPE_2BYTE_INT = 8;
	static final byte TYPE_1BYTE_INT = 9;
	static final byte TYPE_ZERO_INT = 10;
	
	static final byte TYPE_LONG = 16;
	static final byte TYPE_4BYTE_LONG = 17;
	static final byte TYPE_2BYTE_LONG = 18;
	static final byte TYPE_1BYTE_LONG = 19;
	static final byte TYPE_ZERO_LONG = 20;
	
	static final byte TYPE_DATE = 24;
	static final byte TYPE_SHORT_BYTEARRAY = 30;
	static final byte TYPE_BYTEARRAY = 31;
	
	static final byte TYPE_STRING = 32;
	static final byte TYPE_SHORT_STRING = 33;
	
	
	static final byte TYPE_SHORT_LIST = 34;
	static final byte TYPE_LIST = 35;
	
	
	static final byte TYPE_SHORT_MAP = 36;
	static final byte TYPE_MAP = 37;
	
	//优化类型
	static final byte TYPE_EMPTY_BYTEARRAY = 49;
	static final byte TYPE_NULL_STRING = 50;
	static final byte TYPE_EMPTY_STRING = 51;
	static final byte TYPE_EMPTY_LIST = 54;
	static final byte TYPE_EMPTY_MAP = 57;
	
	final Map<Byte, Codec> decodeMap = new HashMap<>();

	final Map<Class, Codec> encodeMap = new HashMap<>();

	public final Codec<?> get(Class<?> clz) {
		return adjustEncoder(encodeMap.get(clz),clz);
	}

	private Codec<?> adjustEncoder(Codec codec, Class<?> clz) {
		if(codec==null) {
			if (List.class.isAssignableFrom(clz)) {
				codec = ListCodec.INSTANCE;
			} else if (Map.class.isAssignableFrom(clz)) {
				codec = MapCodec.INSTANCE;
			}
		}
		return notNull(codec,clz);
	}

	public final Codec<?> get(byte b) {
		return notNull(decodeMap.get(b),b);
	}
	
	private Codec<?> notNull(Codec codec, Object key) {
		if(codec==null) {
			throw new IllegalArgumentException("Not supported type." + key);
		}
		return codec;
	}

	public CodecContext() {
		init();
	}

	private void init() {
		decodeMap.put(TYPE_NULL, NullCodec.INSTANCE);
		decodeMap.put(TYPE_BYTE, ByteCodec.INSTANCE);
		decodeMap.put(TYPE_BOOLEAN_T, new BooleanT());
		decodeMap.put(TYPE_BOOLEAN_F, new BooleanF());
		decodeMap.put(TYPE_SHORT, ShortCodec.INSTANCE);
		decodeMap.put(TYPE_CHAR, CharCodec.INSTANCE);
		decodeMap.put(TYPE_INT, IntCodec.INSTANCE);
		decodeMap.put(TYPE_ZERO_INT, IntCodec.BYTE0);
		decodeMap.put(TYPE_1BYTE_INT, IntCodec.BYTE1);
		decodeMap.put(TYPE_2BYTE_INT, IntCodec.BYTE2);
		decodeMap.put(TYPE_LONG, LongCodec.INSTANCE);
		decodeMap.put(TYPE_ZERO_LONG, LongCodec.BYTE0);
		decodeMap.put(TYPE_1BYTE_LONG, LongCodec.BYTE1);
		decodeMap.put(TYPE_2BYTE_LONG, LongCodec.BYTE2);
		decodeMap.put(TYPE_4BYTE_LONG, LongCodec.BYTE4);
		decodeMap.put(TYPE_DATE, DateCodec.INSTANCE);
		
		decodeMap.put(TYPE_STRING, StringCodec.INSTANCE);
		decodeMap.put(TYPE_SHORT_STRING, StringCodec.SHORT);
		decodeMap.put(TYPE_EMPTY_STRING, StringCodec.EMPTY);
		decodeMap.put(TYPE_NULL_STRING, StringCodec.NULL);
		
		decodeMap.put(TYPE_EMPTY_LIST, ListCodec.EMPTY_LIST);
		decodeMap.put(TYPE_SHORT_LIST, ListCodec.SHORT_LIST);
		decodeMap.put(TYPE_LIST, ListCodec.INSTANCE);
		
		decodeMap.put(TYPE_EMPTY_MAP, MapCodec.EMPTY_MAP);
		decodeMap.put(TYPE_SHORT_MAP, MapCodec.SHORT_MAP);
		decodeMap.put(TYPE_MAP, MapCodec.INSTANCE);
		
		decodeMap.put(TYPE_EMPTY_BYTEARRAY, ByteArrayCodec.INSTANCE_EMPTY);
		decodeMap.put(TYPE_SHORT_BYTEARRAY, ByteArrayCodec.INSTANCE_SHORT);
		decodeMap.put(TYPE_BYTEARRAY, ByteArrayCodec.INSTANCE);
		
		encodeMap.put(Byte.class, ByteCodec.INSTANCE);
		encodeMap.put(Boolean.class, new BooleanCodec());
		encodeMap.put(Short.class, ShortCodec.INSTANCE);
		encodeMap.put(Character.class, CharCodec.INSTANCE);
		encodeMap.put(Integer.class, IntCodec.INSTANCE);
		encodeMap.put(Long.class, LongCodec.INSTANCE);
		encodeMap.put(Date.class, DateCodec.INSTANCE);
		encodeMap.put(String.class, StringCodec.INSTANCE);
		encodeMap.put(byte[].class, ByteArrayCodec.INSTANCE);
		encodeMap.put(ArrayList.class, ListCodec.INSTANCE);
		encodeMap.put(LinkedList.class, ListCodec.INSTANCE);
		encodeMap.put(HashMap.class, MapCodec.INSTANCE);
		encodeMap.put(LinkedHashMap.class, MapCodec.INSTANCE);
		encodeMap.put(TreeMap.class, MapCodec.INSTANCE);
		encodeMap.put(ConcurrentHashMap.class, MapCodec.INSTANCE);
	}
	
	public static final byte[] encode(Object e) {
		ByteBuffer buffer=ByteBuffer.allocate(1024);
		CodecContext.INSTANCE.writeObject(buffer, e);
//		buffer.clear();
		buffer.flip();
		byte[] result=new byte[buffer.remaining()];
		 buffer.get(result);
		 return result;
	}

	public void writeObject(ByteBuffer buffer, Object e) {
		if(e==null) {
			NullCodec.INSTANCE.encode(buffer, null, this);
		}else {
			get(e.getClass()).encode(buffer, e, this);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T readObject(ByteBuffer buffer, Class<T> clz) {
		Object o=get(buffer.get()).decode(buffer, this);
		return (T)o;
	}

	public String getString(byte b, ByteBuffer buffer) {
		StringCodec c;
		switch(b) {
		case CodecContext.TYPE_STRING:
			c=StringCodec.INSTANCE;
			break;
		case CodecContext.TYPE_SHORT_STRING:
			c=StringCodec.SHORT;
		case CodecContext.TYPE_EMPTY_STRING:
			c=StringCodec.EMPTY;
		case CodecContext.TYPE_NULL_STRING:
			c=StringCodec.NULL;
		default:
			throw Exceptions.illegalArgument("buffer data is not a string type. type={}", b);
		}
		return c.decode(buffer, this);
	}

}
