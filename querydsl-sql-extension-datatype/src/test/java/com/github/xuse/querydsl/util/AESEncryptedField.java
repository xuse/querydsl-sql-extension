package com.github.xuse.querydsl.util;

import java.nio.charset.StandardCharsets;

import com.github.xuse.querydsl.datatype.AbstractBase64EncryptStringType;

public class AESEncryptedField extends AbstractBase64EncryptStringType {

	@Override
	protected byte[] encrypt(String value) {
		return value.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	protected String decrypt(byte[] value) {
		return new String(value,StandardCharsets.UTF_8);
	}

}
