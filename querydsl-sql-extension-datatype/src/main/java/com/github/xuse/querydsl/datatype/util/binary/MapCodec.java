package com.github.xuse.querydsl.datatype.util.binary;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
class MapCodec implements Codec<Map>{
	public static final MapCodec INSTANCE = new MapCodec();

	public static final MapCodec SHORT_MAP = new MapCodec() {
		@SuppressWarnings("unchecked")
		@Override
		public Map decode(ByteBuffer buffer, CodecContext context) {
			int length=Unsigned.of(buffer.get());
			Map list=new LinkedHashMap<>();
			for(int i=0;i<length;i++) {
				Object key=context.get(buffer.get()).decode(buffer, context);
				Object value=context.get(buffer.get()).decode(buffer, context);
				list.put(key, value);
			}
			return list;
		}
	};
	
	public static final MapCodec EMPTY_MAP = new MapCodec() {
		@Override
		public Map decode(ByteBuffer buffer, CodecContext context) {
			return new HashMap<>();
		}
	};
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Map decode(ByteBuffer buffer, CodecContext context) {
		int length=Unsigned.of(buffer.getShort());
		Map list=new LinkedHashMap<>();
		for(int i=0;i<length;i++) {
			Object key=context.get(buffer.get()).decode(buffer, context);
			Object value=context.get(buffer.get()).decode(buffer, context);
			list.put(key, value);
		}
		return list;
	}

	@Override
	public void encode(ByteBuffer buffer, Object obj1, CodecContext context) {
		Map<?,?> obj=(Map<?,?>)obj1;
		int size=obj.size();
		if(size>255) {
			buffer.put(CodecContext.TYPE_MAP);
			buffer.putShort((short)obj.size());
		}else if(size>0){
			buffer.put(CodecContext.TYPE_SHORT_MAP);
			buffer.put((byte)obj.size());
		}else {
			buffer.put(CodecContext.TYPE_EMPTY_MAP);
			return;
		}
		for(Map.Entry<?,?> e:obj.entrySet()) {
			Object key=e.getKey();
			Object value=e.getValue();
			context.writeObject(buffer,key);
			context.writeObject(buffer,value);
		}
	}
	
	

}
