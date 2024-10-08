package com.github.xuse.querydsl.datatype.util;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import com.github.xuse.querydsl.util.StringUtils;

import lombok.SneakyThrows;


public class ToStringUtils {
	public static CharSequence toString(byte[] data) {
		return toString("",data);
	}
	
	public static CharSequence toString(String head, byte[] b) {
		StringBuilder sb = new StringBuilder(head.length() + b.length * 4 + 16);
		sb.append(head);
		appendBytesString(sb, b);
		return sb;
	}
	
	public static CharSequence toString(ResultSet rs){
		return toString(rs, 200);
	}

	@SneakyThrows
	public static CharSequence toString(ResultSet rs, int limit){
		if (limit > 1000) {
			limit = 1000;
		}
		StringBuilder sb = new StringBuilder(128);
		ResultSetMetaData meta = rs.getMetaData();
		int count = meta.getColumnCount();
		sb.append(meta.getColumnLabel(1));
		for (int i = 2; i <= count; i++) {
			sb.append(", ");
			sb.append(meta.getColumnLabel(i));
		}
		sb.append('\n');
		int size = 0;
		while (rs.next()) {
			size++;
			sb.append('[');
			sb.append(rs.getObject(1));
			for (int i = 2; i <= count; i++) {
				sb.append(", ");
				sb.append(rs.getObject(i));
			}
			sb.append("]\n");
			if (limit == size) {// No need to print...
				while (rs.next()) {
					size++;
				}
				break;
			}
		}
		sb.append("Total:").append(size).append(" record(s).");
		return sb;

	}
	
	public static void appendBytesString(StringBuilder sb, byte[] value) {
		sb.append("          -0 -1 -2 -3 -4 -5 -6 -7 -8 -9 -A -B -C -D -E -F\r\n");
		int left = value.length;
		int i = 0;
		while (left > 16) {
			String name = Integer.toHexString(i);
			int offset = i * 16;
			name = StringUtils.leftPad(name, 7, '0').concat("0: ");
			sb.append(name).append(StringUtils.join(value, ' ', offset, 16)).append(" ; ");
			String text = new String(value, offset, 16, StandardCharsets.ISO_8859_1);
			text = StringUtils.replaceChars(text, "\r\n\t", "...");
			sb.append(text);
			sb.append("\r\n");
			left -= 16;
			i++;
		}
		if (left > 0) {
			int offset = i * 16;
			String name = Integer.toHexString(i);
			name = StringUtils.leftPad(name, 7, '0').concat("0: ");
			sb.append(name).append(StringUtils.join(value, ' ', offset, 16));

			for (int x = left; x < 16; x++) {
				sb.append("   ");
			}
			String text = new String(value, offset, left, StandardCharsets.ISO_8859_1);
			text = StringUtils.replaceChars(text, "\r\n\t", "...");
			sb.append(" ; ").append(text);
		}
	}
}
