package com.github.xuse.querydsl.datatype.util.binary;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("rawtypes")
class ListCodec implements Codec<List>{
	public static final ListCodec INSTANCE = new ListCodec();

	public static final ListCodec SHORT_LIST = new ListCodec() {
		@SuppressWarnings("unchecked")
		@Override
		public List decode(ByteBuffer buffer, CodecContext context) {
			int length=Unsigned.of(buffer.get());
			List list=new ArrayList<>();
			for(int i=0;i<length;i++) {
				Object value=context.get(buffer.get()).decode(buffer, context);
				list.add(value);
			}
			return list;
		}
	};
	
	public static final ListCodec EMPTY_LIST = new ListCodec() {
		@Override
		public List decode(ByteBuffer buffer, CodecContext context) {
			return new ArrayList<>();
		}
	};
	
	@SuppressWarnings("unchecked")
	@Override
	public List decode(ByteBuffer buffer, CodecContext context) {
		int length=Unsigned.of(buffer.getShort());
		if(length<=0) {
			return Collections.EMPTY_LIST;
		}
		List list=new ArrayList<>();
		for(int i=0;i<length;i++) {
			Object value=context.get(buffer.get()).decode(buffer, context);
			list.add(value);
		}
		return list;
	}

	@Override
	public void encode(ByteBuffer buffer, Object obj1, CodecContext context) {
		List obj=(List)obj1;
		int size=obj.size();
		if(size>255) {
			buffer.put(CodecContext.TYPE_LIST);
			buffer.putShort((short)obj.size());	
		}else if(size>0){
			buffer.put(CodecContext.TYPE_SHORT_LIST);
			buffer.put((byte)obj.size());
		}else {
			buffer.put(CodecContext.TYPE_EMPTY_LIST);
		}
		for(Object e:obj) {
			context.writeObject(buffer, e);
		}
	}
}
