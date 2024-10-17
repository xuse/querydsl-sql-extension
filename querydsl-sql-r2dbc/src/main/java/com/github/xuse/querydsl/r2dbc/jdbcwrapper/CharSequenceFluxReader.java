package com.github.xuse.querydsl.r2dbc.jdbcwrapper;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import reactor.core.publisher.Flux;

public class CharSequenceFluxReader extends Reader {
	private final Flux<CharSequence> flux;
	private long begin;
	private long end;
	private long position;

	
	public CharSequenceFluxReader(Flux<CharSequence> flux, long start, long length) {
		this.flux = flux;
		this.begin = start;
		this.end = start + length;
		this.position = begin;
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
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
	public void close() throws IOException {
	}

	//todo move to test cases.
	public static void main(String[] args) throws IOException {
		List<String> strs = Arrays.asList("12345", "67890", "abcdef");
		CharSequenceFluxReader reader = new CharSequenceFluxReader(Flux.fromIterable(strs), 4, 15);
		int c;
		char[] buf=new char[4];
		System.out.println(reader.read(buf));
		System.out.println(Arrays.toString(buf));
		
		System.out.println(reader.read(buf));
		System.out.println(Arrays.toString(buf));
		
		System.out.println(reader.read(buf));
		System.out.println(Arrays.toString(buf));
		
		System.out.println(reader.read(buf));
		System.out.println(Arrays.toString(buf));
	}
}
