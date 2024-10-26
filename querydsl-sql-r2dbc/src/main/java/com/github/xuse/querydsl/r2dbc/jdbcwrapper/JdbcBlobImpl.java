package com.github.xuse.querydsl.r2dbc.jdbcwrapper;

import com.github.xuse.querydsl.util.ArrayUtils;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.sql.Blob;

public class JdbcBlobImpl implements Blob{
	private static final int MAX_SIZE = 1024 * 1024 * 32;
	
	@SuppressWarnings("unused")
	private final io.r2dbc.spi.Blob blob;
	private byte[] data;
	
	public JdbcBlobImpl(io.r2dbc.spi.Blob blob) {
		this.blob = blob;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		for (ByteBuffer bf : Flux.from(blob.stream()).toIterable()) {
			while (bf.hasRemaining()) {
				int len = Math.min(2048, bf.remaining());
				bf.get(buffer, 0, len);
				out.write(buffer, 0, len);
				if (out.size() >= MAX_SIZE) {
					throw new RuntimeException("blob size too big. " + out.size());
				}
			}
		}
		data=out.toByteArray();
	}
	@Override
	public long length() {
		return data.length;
	}
	
	@Override
	public InputStream getBinaryStream(long pos, long length) {
		return new ByteArrayInputStream(getBytes(pos,(int)length));
	}
	
	@Override
	public byte[] getBytes(long pos, int length){
		return ArrayUtils.subArray(data, (int)pos, length);
	}
	@Override
	public InputStream getBinaryStream(){
		return new ByteArrayInputStream(data);
	}
	@Override
	public long position(byte[] pattern, long start){
		throw new UnsupportedOperationException();
	}
	@Override
	public long position(Blob pattern, long start){
		throw new UnsupportedOperationException();
	}
	@Override
	public int setBytes(long pos, byte[] bytes) {
		throw new UnsupportedOperationException();
	}
	@Override
	public int setBytes(long pos, byte[] bytes, int offset, int len) {
		throw new UnsupportedOperationException();
	}
	@Override
	public OutputStream setBinaryStream(long pos) {
		throw new UnsupportedOperationException();
	}
	@Override
	public void truncate(long len) {
		throw new UnsupportedOperationException();
	}
	@Override
	public void free() {
		this.data = null;
	}
}
