package com.github.xuse.querydsl.r2dbc.jdbcwrapper;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;

import java.io.Reader;

public class CharSequenceFluxReader extends Reader {
	private final Flux<CharSequence> flux;
	private final long begin;
	private final long end;
	private long position;

	
	public CharSequenceFluxReader(Flux<CharSequence> flux, long start, long length) {
		this.flux = flux;
		this.begin = start;
		this.end = start + length;
		this.position = begin;
	}

	@Override
	public int read(char @NotNull [] cbuf, int off, int len){
		if(position>=end) {
			return -1;
		}
		int readLeft=len;
		long seekOffset=0;
		for (CharSequence seq : flux.toIterable()) {
			if (seekOffset + seq.length() < position) {
				//检索下一个字符串
				seekOffset += seq.length();
				continue;
			}
			//相对起始偏移
			int pos = (int)(position - seekOffset);
			//最大读取字数
			int maxReadLen = seq.length() -  pos;
			//本次实际读取字数
			int read = Math.min(maxReadLen, readLeft);
			for(int i=0;i<read;i++) {
				cbuf[i + off] = seq.charAt(pos + i);
			}
			position += read;
			if (read >= readLeft) {
				//读完了
				return len;
			}
			off += read;
			readLeft -= read;
			seekOffset += seq.length();
		}
		if (readLeft > 0) {
			return readLeft == len? -1: len - readLeft;
		}
		return len;
	}

	@Override
	public void close() {
	}
}
