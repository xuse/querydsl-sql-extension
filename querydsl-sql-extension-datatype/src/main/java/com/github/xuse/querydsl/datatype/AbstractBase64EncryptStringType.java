package com.github.xuse.querydsl.datatype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.github.xuse.querydsl.util.JefBase64;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.types.AbstractType;

/**
 * 支持加密解密保存的消息
 * @author Joey
 */
public abstract class AbstractBase64EncryptStringType extends AbstractType<String> {

	public AbstractBase64EncryptStringType() {
		super(Types.VARCHAR);
	}

	@Override
	public Class<String> getReturnedClass() {
		return String.class;
	}

	@Override
	public String getValue(ResultSet rs, int startIndex) throws SQLException {
		String value = rs.getString(startIndex);
		return StringUtils.isEmpty(value) ? value : decrypt(JefBase64.decode(value));
	}

	@Override
	public void setValue(PreparedStatement st, int startIndex, String value) throws SQLException {
		if (StringUtils.isEmpty(value)) {
			st.setString(startIndex, value);
		} else {
			st.setString(startIndex, JefBase64.encode(encrypt(value)));
		}
	}

	/**
	 * 文本加密
	 * @param value value
	 * @return byte[]
	 */
	protected abstract byte[] encrypt(String value);

	/**
	 * 文本解密
	 * @param value value
	 * @return String
	 */
	protected abstract String decrypt(byte[] value);
}
