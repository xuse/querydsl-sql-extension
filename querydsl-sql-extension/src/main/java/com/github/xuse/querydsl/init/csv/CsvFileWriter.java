package com.github.xuse.querydsl.init.csv;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import com.github.xuse.querydsl.util.IOUtils;

import lombok.SneakyThrows;

public class CsvFileWriter implements Closeable {

	private Writer writer = null;

	private boolean firstColumn = true;

	private final WriteSettings userSettings = new WriteSettings();
	
	private static final String RECORDDELIMITER = System.getProperty("line.separator");

	@SneakyThrows
	public CsvFileWriter(File fileName, Charset charset) {
		if (fileName == null) {
			throw new IllegalArgumentException("Parameter fileName can not be null.");
		}
		if (charset == null) {
			throw new IllegalArgumentException("Parameter charset can not be null.");
		}
		IOUtils.ensureParentFolder(fileName);
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), charset));
	}

	public CsvFileWriter(Writer outputStream) {
		if (outputStream == null) {
			throw new IllegalArgumentException("Parameter outputStream can not be null.");
		}
		this.writer = outputStream;
	}
	
	public WriteSettings getSettings() {
		return userSettings;
	}

	public CsvFileWriter write(String content, boolean preserveSpaces) throws IOException {
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

	public CsvFileWriter write(String content) throws IOException {
		return write(content, false);
	}

	public void writeComment(String commentText) throws IOException {
		checkClosed();
		writer.write(userSettings.comment);
		writer.write(commentText);
		writer.write(RECORDDELIMITER);
		firstColumn = true;
	}

	public void writeRecord(String[] values, boolean preserveSpaces) throws IOException {
		if (values != null && values.length > 0) {
			for (int i = 0; i < values.length; i++) {
				write(values[i], preserveSpaces);
			}
			endRecord();
		}
	}

	public void writeRecord(String[] values) throws IOException {
		writeRecord(values, false);
	}

	public void endRecord() throws IOException {
		checkClosed();
		writer.write(RECORDDELIMITER);
		firstColumn = true;
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

	private void checkClosed() throws IOException {
		if (writer == null) {
			throw new IOException("This instance of the CsvWriter class has already been closed.");
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
