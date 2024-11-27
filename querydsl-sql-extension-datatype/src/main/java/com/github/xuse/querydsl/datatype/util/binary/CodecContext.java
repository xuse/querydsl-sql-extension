package com.github.xuse.querydsl.datatype.util.binary;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.github.xuse.querydsl.util.Exceptions;

/**
 * 二进制数据编解码器
 * 
 * @author Joey
 */
@SuppressWarnings("rawtypes")
public class CodecContext {
	public static final CodecContext INSTANCE = new CodecContext();
	// 数据类型，从-128~127
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
	static final byte TYPE_ONE_INT = 11;

	static final byte TYPE_LONG = 16;
	static final byte TYPE_4BYTE_LONG = 17;
	static final byte TYPE_2BYTE_LONG = 18;
	static final byte TYPE_1BYTE_LONG = 19;
	static final byte TYPE_ZERO_LONG = 20;

	static final byte TYPE_DATE = 24;

	static final byte TYPE_FLOAT = 26;
	static final byte TYPE_ZERO_FLOAT = 27;
	static final byte TYPE_DOUBLE = 28;
	static final byte TYPE_ZERO_DOUBLE = 29;
	// 长度255以内的byte[]
	static final byte TYPE_SHORT_BYTEARRAY = 30;
	// 长度65535以内的byte[]
	static final byte TYPE_BYTEARRAY = 31;
	// 长度65535以内的String
	static final byte TYPE_STRING = 32;
	// 长度255以内的String
	static final byte TYPE_SHORT_STRING = 33;
	// 长度255以内的List
	static final byte TYPE_SHORT_LIST = 34;
	// 长度65535以内的List
	static final byte TYPE_LIST = 35;
	// 长度255以内的Map
	static final byte TYPE_SHORT_MAP = 36;
	// 长度65535以内的Map
	static final byte TYPE_MAP = 37;

	// 空数据类型
	static final byte TYPE_EMPTY_BYTEARRAY = 49;
	/**
	 * @deprecated
	 */
	static final byte TYPE_NULL_STRING = 50;
	static final byte TYPE_EMPTY_STRING = 51;
	static final byte TYPE_EMPTY_LIST = 54;
	static final byte TYPE_EMPTY_MAP = 57;
	static final byte TYPE_BEAN = -2;
	static final byte TYPE_BEAN_NULL = -3;
	static final byte TYPE_VOID_BEAN_END = -4;

	final Map<Byte, Codec> decodeMap = new HashMap<>();

	final Map<Class, Codec> encodeMap = new HashMap<>();

	final Map<String, BeanCodec> beanCodecs = new HashMap<>();

	public final Codec<?> get(Class<?> clz) {
		return adjustEncoder(encodeMap.get(clz), clz);
	}

	@SuppressWarnings("unchecked")
	private Codec<?> adjustEncoder(Codec codec, Class<?> clz) {
		if (codec == null) {
			if (List.class.isAssignableFrom(clz)) {
				codec = ListCodec.INSTANCE;
			} else if (Map.class.isAssignableFrom(clz)) {
				codec = MapCodec.INSTANCE;
			}
		}
		if(codec==null) {
			BeanCodec bc=beanCodecs.get(clz.getName());
			if (bc != null) {
				return new BeanCodecAdapter(bc);
			}
		}
		return notNull(codec, clz);
	}

	public final Codec<?> get(byte b) {
		return notNull(decodeMap.get(b), b);
	}

	private Codec<?> notNull(Codec codec, Object key) {
		if (codec == null) {
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
		decodeMap.put(TYPE_ZERO_INT, IntCodec.FOR_ZERO);
		decodeMap.put(TYPE_ONE_INT, IntCodec.FOR_ONE);
		decodeMap.put(TYPE_1BYTE_INT, IntCodec.BYTE1);
		decodeMap.put(TYPE_2BYTE_INT, IntCodec.BYTE2);
		decodeMap.put(TYPE_LONG, LongCodec.INSTANCE);
		decodeMap.put(TYPE_ZERO_LONG, LongCodec.BYTE0);
		decodeMap.put(TYPE_1BYTE_LONG, LongCodec.BYTE1);
		decodeMap.put(TYPE_2BYTE_LONG, LongCodec.BYTE2);
		decodeMap.put(TYPE_4BYTE_LONG, LongCodec.BYTE4);

		decodeMap.put(TYPE_FLOAT, FloatCodec.INSTANCE);
		decodeMap.put(TYPE_ZERO_FLOAT, FloatCodec.FOR_ZERO);
		decodeMap.put(TYPE_DOUBLE, DoubleCodec.INSTANCE);
		decodeMap.put(TYPE_ZERO_DOUBLE, DoubleCodec.FOR_ZERO);
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

		decodeMap.put(TYPE_SHORT_BYTEARRAY, ByteArrayCodec.INSTANCE_SHORT);
		decodeMap.put(TYPE_BYTEARRAY, ByteArrayCodec.INSTANCE);
		decodeMap.put(TYPE_EMPTY_BYTEARRAY, ByteArrayCodec.INSTANCE_EMPTY);
		
		decodeMap.put(TYPE_BEAN, new BeanDecoderAdapter());
		decodeMap.put(TYPE_BEAN_NULL, new BeanCodecAdapter<Void>(null));

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
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		CodecContext.INSTANCE.writeObject(buffer, e);
//		buffer.clear();
		buffer.flip();
		byte[] result = new byte[buffer.remaining()];
		buffer.get(result);
		return result;
	}

	public void writeObject(ByteBuffer buffer, Object e) {
		if (e == null) {
			NullCodec.INSTANCE.encode(buffer, null, this);
		} else {
			get(e.getClass()).encode(buffer, e, this);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T readObject(ByteBuffer buffer, Class<T> clz) {
		Object o = get(buffer.get()).decode(buffer, this);
		return (T) o;
	}

	public void putString(ByteBuffer buffer, String str) {
		StringCodec.INSTANCE.encode(buffer, str, CodecContext.INSTANCE);
	}

	public String getString(byte b, ByteBuffer buffer) {
		StringCodec c;
		switch (b) {
		case CodecContext.TYPE_STRING:
			c = StringCodec.INSTANCE;
			break;
		case CodecContext.TYPE_SHORT_STRING:
			c = StringCodec.SHORT;
			break;
		case CodecContext.TYPE_EMPTY_STRING:
			c = StringCodec.EMPTY;
			break;
		case CodecContext.TYPE_NULL_STRING:
			c = StringCodec.NULL;
			break;
		case CodecContext.TYPE_NULL:
			return null;
		default:
			throw Exceptions.illegalArgument("buffer data is not a string type. type={}", b);
		}
		return c.decode(buffer, this);
	}

	public <T> void register(BeanCodec<T> codec) {
		this.beanCodecs.put(codec.getType().getName(), codec);
	}
	
	public boolean isBeanEnd(ByteBuffer buffer) {
		return buffer.get(buffer.position())==TYPE_VOID_BEAN_END;
	}

	class BeanDecoderAdapter implements Codec{
		@Override
		public Object decode(ByteBuffer buffer, CodecContext context) {
			String clzName=context.getString(TYPE_SHORT_STRING, buffer);
			BeanCodec bc=beanCodecs.get(clzName);
			if(bc!=null) {
				Object result=bc.decode(buffer, context);
				byte b = buffer.get();
				assert b == TYPE_VOID_BEAN_END;
				return result;
			}
			throw new IllegalArgumentException("There is no BeanDecoder for class: "+ clzName);
		}

		@Override
		public void encode(ByteBuffer buffer, Object obj, CodecContext context) {
			throw new UnsupportedOperationException();
		}
	}
	
	static final class BeanCodecAdapter<T> implements Codec<T> {
		@Override
		public T decode(ByteBuffer buffer, CodecContext context) {
			context.getString(TYPE_SHORT_STRING, buffer);
			byte b=buffer.get();
			assert b == TYPE_VOID_BEAN_END;
			return null;
		}
		
		private final BeanCodec<T> bc;
		
		BeanCodecAdapter(BeanCodec<T> bc) {
			this.bc = bc;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void encode(ByteBuffer buffer, Object obj, CodecContext context) {
			if (obj == null) {
				buffer.put(CodecContext.TYPE_BEAN_NULL);
			} else {
				buffer.put(CodecContext.TYPE_BEAN);
			}
			String clzName=bc.getType().getName();
			buffer.put((byte) clzName.length());
			byte[] bytes = clzName.getBytes(StandardCharsets.UTF_8);
			buffer.put(bytes, 0, bytes.length);
			bc.encode(buffer, (T)obj, context);
			buffer.put(TYPE_VOID_BEAN_END);
		}

	}

}
