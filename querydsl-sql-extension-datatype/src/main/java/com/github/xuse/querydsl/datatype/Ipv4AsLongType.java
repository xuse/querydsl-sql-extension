package com.github.xuse.querydsl.datatype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.jetbrains.annotations.Nullable;

import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

/**
 * 允许使用一个USIGNED INT或 BIGINT数据来存储IPv4地址.
 * Note: In MySQL，it is able to use 'INET_ATON' / 'INET_NTOA' to convert between integer and ip string.   
 * @author Joey
 *
 */
public class Ipv4AsLongType extends AbstractType<String> {
	public Ipv4AsLongType() {
		super(Types.INTEGER);
	}

	public static long toLong(String ipStr) {
		String[] ip = StringUtils.split(ipStr,'.');
		if(ip.length<4) {
			throw Exceptions.illegalArgument("{} is not a valid ipv4 address.", ipStr);
		}
		return (Long.valueOf(ip[0]) << 24) + (Long.valueOf(ip[1]) << 16) + (Long.valueOf(ip[2]) << 8)
				+ Long.valueOf(ip[3]);
	}
	
	public static String toIpv4(long ipLong) {
		StringBuilder ip = new StringBuilder();
		ip.append(ipLong >>> 24).append('.');
		ip.append((ipLong >>> 16) & 0xFF).append('.');
		ip.append((ipLong >>> 8) & 0xFF).append('.');
		ip.append(ipLong & 0xFF);
		return ip.toString();
	}

	@Override
	public Class<String> getReturnedClass() {
		return String.class;
	}

	@Override
	public @Nullable String getValue(ResultSet rs, int startIndex) throws SQLException {
		long value=rs.getLong(startIndex);
		if(rs.wasNull()) {
			return null;
		}
		return toIpv4(value);
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, String value) throws SQLException {
		st.setLong(startIndex, toLong(value));
	}
}
