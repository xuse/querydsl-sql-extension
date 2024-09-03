package com.github.xuse.querydsl.init.csv;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.github.xuse.querydsl.util.IOUtils;

public class CsvFileWriter implements Closeable {

	private Writer writer = null;

	private File fileName = null;

	private boolean firstColumn = true;

	private boolean useCustomRecordDelimiter = false;

	private Charset charset = null;

	private final Constants userSettings = new Constants();

	private boolean initialized = false;

	private boolean closed = false;

	private final String systemRecordDelimiter = System.getProperty("line.separator");


	public CsvFileWriter(File fileName, Charset charset) {
		if (fileName == null) {
			throw new IllegalArgumentException("Parameter fileName can not be null.");
		}
		if (charset == null) {
			throw new IllegalArgumentException("Parameter charset can not be null.");
		}
		this.fileName = fileName;
		userSettings.delimiter = Characters.COMMA;
		this.charset = charset;
	}

	public CsvFileWriter(String fileName) {
		this(new File(fileName), StandardCharsets.UTF_8);
	}

	public CsvFileWriter(Writer outputStream) {
		if (outputStream == null) {
			throw new IllegalArgumentException("Parameter outputStream can not be null.");
		}
		this.writer = outputStream;
		userSettings.delimiter = Characters.COMMA;
		initialized = true;
	}

	public CsvFileWriter(OutputStream outputStream, Charset charset) {
		this(new OutputStreamWriter(outputStream, charset));
	}

	public char getDelimiter() {
		return userSettings.delimiter;
	}

	public void setDelimiter(char delimiter) {
		userSettings.delimiter = delimiter;
	}

	public char getRecordDelimiter() {
		return userSettings.recordDelimiter;
	}

	public void setRecordDelimiter(char recordDelimiter) {
		useCustomRecordDelimiter = true;
		userSettings.recordDelimiter = recordDelimiter;
	}

	public char getTextQualifier() {
		return userSettings.textQualifier;
	}

	public void setTextQualifier(char textQualifier) {
		userSettings.textQualifier = textQualifier;
	}

	public boolean getUseTextQualifier() {
		return userSettings.useTextQualifier;
	}

	public void setUseTextQualifier(boolean useTextQualifier) {
		userSettings.useTextQualifier = useTextQualifier;
	}

	public int getEscapeMode() {
		return userSettings.escapeMode;
	}

	public void setEscapeMode(int escapeMode) {
		userSettings.escapeMode = escapeMode;
	}

	public void setComment(char comment) {
		userSettings.comment = comment;
	}

	public char getComment() {
		return userSettings.comment;
	}

	public boolean getForceQualifier() {
		return userSettings.forceQualifier;
	}

	public void setForceQualifier(boolean forceQualifier) {
		userSettings.forceQualifier = forceQualifier;
	}

	public void write(String content, boolean preserveSpaces) throws IOException {
		checkClosed();
		checkInit();
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
		if (!textQualify && userSettings.useTextQualifier && (// be qualified or the line will be skipped
		content.indexOf(userSettings.textQualifier) > -1 || content.indexOf(userSettings.delimiter) > -1 || (!useCustomRecordDelimiter && (content.indexOf(Characters.LF) > -1 || content.indexOf(Characters.CR) > -1)) || (useCustomRecordDelimiter && content.indexOf(userSettings.recordDelimiter) > -1) || (firstColumn && content.length() > 0 && content.charAt(0) == userSettings.comment) || (firstColumn && content.length() == 0))) {
			textQualify = true;
		}
		if (userSettings.useTextQualifier && !textQualify && content.length() > 0 && preserveSpaces) {
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
			if (userSettings.escapeMode == Constants.ESCAPE_MODE_BACKSLASH) {
				content = replace(content, "" + Characters.BACKSLASH, "" + Characters.BACKSLASH + Characters.BACKSLASH);
				content = replace(content, String.valueOf(userSettings.textQualifier), String.valueOf(Characters.BACKSLASH) + userSettings.textQualifier);
			} else {
				content = replace(content, String.valueOf(userSettings.textQualifier), String.valueOf(userSettings.textQualifier) + userSettings.textQualifier);
			}
		} else if (userSettings.escapeMode == Constants.ESCAPE_MODE_BACKSLASH) {
			content = replace(content, "" + Characters.BACKSLASH, "" + Characters.BACKSLASH + Characters.BACKSLASH);
			content = replace(content, String.valueOf(userSettings.delimiter), String.valueOf(Characters.BACKSLASH) + userSettings.delimiter);
			if (useCustomRecordDelimiter) {
				content = replace(content, String.valueOf(userSettings.recordDelimiter), String.valueOf(Characters.BACKSLASH) + userSettings.recordDelimiter);
			} else {
				content = replace(content, "" + Characters.CR, "" + Characters.BACKSLASH + Characters.CR);
				content = replace(content, "" + Characters.LF, "" + Characters.BACKSLASH + Characters.LF);
			}
			if (firstColumn && content.length() > 0 && content.charAt(0) == userSettings.comment) {
				if (content.length() > 1) {
					content = String.valueOf(Characters.BACKSLASH) + userSettings.comment + content.substring(1);
				} else {
					content = String.valueOf(Characters.BACKSLASH) + userSettings.comment;
				}
			}
		}
		writer.write(content);
		if (textQualify) {
			writer.write(userSettings.textQualifier);
		}
		firstColumn = false;
	}

	public void write(String content) throws IOException {
		write(content, false);
	}

	public void writeComment(String commentText) throws IOException {
		checkClosed();
		checkInit();
		writer.write(userSettings.comment);
		writer.write(commentText);
		if (useCustomRecordDelimiter) {
			writer.write(userSettings.recordDelimiter);
		} else {
			writer.write(systemRecordDelimiter);
		}
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
		checkInit();
		if (useCustomRecordDelimiter) {
			writer.write(userSettings.recordDelimiter);
		} else {
			writer.write(systemRecordDelimiter);
		}
		firstColumn = true;
	}

	private void checkInit() throws IOException {
		if (!initialized) {
			if (fileName != null) {
				IOUtils.ensureParentFolder(fileName);
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), charset));
			}
			initialized = true;
		}
	}

	public void flush() throws IOException {
		writer.flush();
	}

	public void close() {
		if (!closed) {
			close(true);
			closed = true;
		}
	}

	private void close(boolean closing) {
		if (!closed) {
			if (closing) {
				charset = null;
			}
			try {
				if (initialized) {
					writer.close();
				}
			} catch (Exception e) {
			// just eat the exception
			}
			writer = null;
			closed = true;
		}
	}

	private void checkClosed() throws IOException {
		if (closed) {
			throw new IOException("This instance of the CsvWriter class has already been closed.");
		}
	}

	protected void finalize() {
		close(false);
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
