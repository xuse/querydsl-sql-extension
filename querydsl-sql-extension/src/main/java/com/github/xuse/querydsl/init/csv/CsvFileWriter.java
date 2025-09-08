package com.github.xuse.querydsl.init.csv;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.annotation.dbdef.ColumnSpec;
import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.github.xuse.querydsl.sql.expression.BeanCodecManager;
import com.github.xuse.querydsl.sql.expression.Property;
import com.github.xuse.querydsl.util.ArrayUtils;
import com.github.xuse.querydsl.util.IOUtils;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.Column;

import lombok.SneakyThrows;

public class CsvFileWriter<T> implements Closeable {

	private Writer writer = null;

	private boolean firstColumn = true;

	private final WriteSettings userSettings = new WriteSettings();
	
	private static final String RECORDDELIMITER = System.getProperty("line.separator");
	
	private final Function<T,String[]> func;
	
	private static final Function<String[],String[]> DEFAULT_FUNC=(s)->s;
	
	public static CsvFileWriter<String[]> ofUtf8Writer(File file) {
		return of(file, StandardCharsets.UTF_8);
	}
	
	public static CsvFileWriter<String[]> of(File file,Charset charset){
		return new CsvFileWriter<>(file,charset,DEFAULT_FUNC);
	}
	
	public static CsvFileWriter<String[]> of(Writer writer){
		return new CsvFileWriter<>(writer, DEFAULT_FUNC);
	}
	
	public static <T> CsvFileWriterBuilder<T> ofBean(Class<T> bean) {
		return new CsvFileWriterBuilder<T>(bean);
	}
	
	public static class CsvFileWriterBuilder<T>{
		private final Class<T> clz;
		private String[] headers;
		private Charset cs=StandardCharsets.UTF_8;
		public CsvFileWriterBuilder(Class<T> clz) {
			this.clz=clz;
		}
		public CsvFileWriterBuilder<T> headers(String[] headers){
			this.headers=headers;
			return this;
		}
		public CsvFileWriterBuilder<T> charset(String charset){
			cs=Charset.forName(charset);
			return this;
		}
		public CsvFileWriterBuilder<T> charset(Charset charset){
			cs = charset;
			return this;
		}
		public CsvFileWriter<T> build(File file){
			BeanFunction<T> func=new BeanFunction<>(clz,headers);
			CsvFileWriter<T> cw= new CsvFileWriter<>(file,cs,func);
			cw.writeHeaders(func.getHeaders());
			return cw;
		}
		public CsvFileWriter<T> build(Writer writer){
			BeanFunction<T> func=new BeanFunction<>(clz,headers);
			CsvFileWriter<T> cw= new CsvFileWriter<>(writer,func);
			cw.writeHeaders(func.getHeaders());
			return cw;
		}
	}
	
	private static class BeanFunction<T> implements Function<T,String[]>{
		final BeanCodec codec;
		int[] mapping;
		final String[] headers;
		
		BeanFunction(Class<T> clz, String[] headers){
			this.codec=BeanCodecManager.getInstance().getCodec(clz);
			Map<String,Integer> map = generateHeadersIndex(codec);
			if(headers==null || headers.length==0){
				headers=Arrays.stream(codec.getFields()).map(Property::properName).collect(Collectors.toList()).toArray(ArrayUtils.EMPTY_STRING_ARRAY);
			}
			this.headers=headers;
			int[] mapping = new int[headers.length];
			for(int i=0;i<headers.length;i++) {
				Integer index=map.get(headers[i]);
				if(index!=null){
					mapping[i]=index;
				}else {
					mapping[i]=-1;
				}
			}
			this.mapping=mapping;
		}
		private Map<String,Integer> generateHeadersIndex(BeanCodec codec) {
			Property[] pps=codec.getFields();
			int size=pps.length;
			Map<String,Integer> index=new HashMap<>();
			for(int i=0;i<size;i++){
				Property pp=pps[i];
				String name=pp.getName();
				index.put(name, i);
				
				Column c=pp.getAnnotation(Column.class);
				if(c!=null && StringUtils.isNotEmpty(c.value())){
					name=c.value();
					index.put(name, i);
				}
				ColumnSpec cs=pp.getAnnotation(ColumnSpec.class);
				if(cs!=null && StringUtils.isNotEmpty(cs.name())) {
					name=cs.name();
					index.put(name, i);
				}
			}
			return index;
		}
		public String[] getHeaders() {
			return headers;
		}
		@Override
		public String[] apply(T t) {
			Object[] objs=codec.values(t);
			int len=headers.length;
			String[] values=new String[len];
			Property[] pps=codec.getFields();
			for(int i=0;i<len;i++) {
				int index=mapping[i];
				if(index>=0) {
					Object o=objs[index];
					values[i]=Codecs.toString(o, pps[index].getGenericType());
				}else {
					values[i]=""; //?
				}
			}
			return values;
		}
	}

	public CsvFileWriter(File fileName, Charset charset,Function<T,String[]> func) {
		if (fileName == null) {
			throw new IllegalArgumentException("Parameter fileName can not be null.");
		}
		if (charset == null) {
			throw new IllegalArgumentException("Parameter charset can not be null.");
		}
		IOUtils.ensureParentFolder(fileName);
		try {
			this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), charset));
		}catch(IOException e) {
			throw new IllegalStateException(e);
		}
		this.func=func;
	}

	public CsvFileWriter(Writer writer,Function<T,String[]> func) {
		if (writer == null) {
			throw new IllegalArgumentException("Parameter outputStream can not be null.");
		}
		this.writer = writer;
		this.func=func;
	}
	
	public WriteSettings getSettings() {
		return userSettings;
	}

	@SneakyThrows
	public CsvFileWriter<T> write(String content, boolean preserveSpaces){
		checkClosed();
		if (content == null) {
			content = "";
		}
		if (!firstColumn) {
			writer.write(userSettings.delimiter);
		}
		boolean textQualify = userSettings.forceQualifier;
		if (!preserveSpaces && content.length() > 0) {
			content = content.trim();
		}
		if (!textQualify && (
				// be qualified or the line will be skipped
				content.indexOf(userSettings.textQualifier) > -1 
				|| content.indexOf(userSettings.delimiter) > -1
				|| content.indexOf(Characters.LF) > -1 
				|| content.indexOf(Characters.CR) > -1 
				|| (firstColumn && content.length() > 0 && content.charAt(0) == userSettings.comment) 
				|| (firstColumn && content.length() == 0))) {
			textQualify = true;
		}
		if (!textQualify && content.length() > 0 && preserveSpaces) {
			char firstLetter = content.charAt(0);
			if (firstLetter == Characters.SPACE || firstLetter == Characters.TAB) {
				textQualify = true;
			}
			if (!textQualify && content.length() > 1) {
				char lastLetter = content.charAt(content.length() - 1);
				if (lastLetter == Characters.SPACE || lastLetter == Characters.TAB) {
					textQualify = true;
				}
			}
		}
		if (textQualify) {
			writer.write(userSettings.textQualifier);
			if (userSettings.escapeMode == EscapeMode.BACKSLASH) {
				content = replace(content, "" + Characters.BACKSLASH, "" + Characters.BACKSLASH + Characters.BACKSLASH);
				content = replace(content, String.valueOf(userSettings.textQualifier), String.valueOf(Characters.BACKSLASH) + userSettings.textQualifier);
			} else {
				content = replace(content, String.valueOf(userSettings.textQualifier), String.valueOf(userSettings.textQualifier) + userSettings.textQualifier);
			}
		} else if (userSettings.escapeMode == EscapeMode.BACKSLASH) {
			content = replace(content, "" + Characters.BACKSLASH, "" + Characters.BACKSLASH + Characters.BACKSLASH);
			content = replace(content, String.valueOf(userSettings.delimiter), String.valueOf(Characters.BACKSLASH) + userSettings.delimiter);
			content = replace(content, "" + Characters.CR, "" + Characters.BACKSLASH + Characters.CR);
			content = replace(content, "" + Characters.LF, "" + Characters.BACKSLASH + Characters.LF);
		}
		writer.write(content);
		if (textQualify) {
			writer.write(userSettings.textQualifier);
		}
		firstColumn = false;
		return this;
	}

	public CsvFileWriter<T> write(String content) throws IOException {
		return write(content, false);
	}

	public void writeComment(String commentText) throws IOException {
		checkClosed();
		writer.write(userSettings.comment);
		writer.write(commentText);
		writer.write(RECORDDELIMITER);
		firstColumn = true;
	}

	public void writeRecord(T record, boolean preserveSpaces){
		String[] values=func.apply(record);
		if (values != null && values.length > 0) {
			for (int i = 0; i < values.length; i++) {
				write(values[i], preserveSpaces);
			}
			endRecord();
		}
	}
	
	public void writeHeaders(String[] values) {
		if (values != null && values.length > 0) {
			for (int i = 0; i < values.length; i++) {
				write(values[i], true);
			}
			endRecord();
		}
	}

	public void writeRecord(T values) throws IOException {
		writeRecord(values, false);
	}

	public void endRecord(){
		checkClosed();
		try {
			writer.write(RECORDDELIMITER);
			firstColumn = true;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void flush() throws IOException {
		writer.flush();
	}

	public void close() {
		if (writer!=null) {
			IOUtils.closeQuietly(writer);
			writer = null;
		}
	}

	private void checkClosed() {
		if (writer == null) {
			throw new IllegalStateException("This instance of the CsvWriter class has already been closed.");
		}
	}

	public static String replace(String original, String pattern, String replace) {
		final int len = pattern.length();
		int found = original.indexOf(pattern);
		if (found > -1) {
			StringBuilder sb = new StringBuilder();
			int start = 0;
			while (found != -1) {
				sb.append(original, start, found);
				sb.append(replace);
				start = found + len;
				found = original.indexOf(pattern, start);
			}
			sb.append(original.substring(start));
			return sb.toString();
		} else {
			return original;
		}
	}
}
